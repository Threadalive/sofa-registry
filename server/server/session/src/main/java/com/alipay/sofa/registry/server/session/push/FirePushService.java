/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alipay.sofa.registry.server.session.push;

import com.alipay.sofa.registry.common.model.SubscriberUtils;
import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import com.alipay.sofa.registry.common.model.dataserver.Datum;
import com.alipay.sofa.registry.common.model.store.DataInfo;
import com.alipay.sofa.registry.common.model.store.Subscriber;
import com.alipay.sofa.registry.core.model.AssembleType;
import com.alipay.sofa.registry.core.model.ScopeEnum;
import com.alipay.sofa.registry.log.Logger;
import com.alipay.sofa.registry.log.LoggerFactory;
import com.alipay.sofa.registry.server.session.bootstrap.SessionServerConfig;
import com.alipay.sofa.registry.server.session.cache.*;
import com.alipay.sofa.registry.server.session.store.Interests;
import com.alipay.sofa.registry.server.shared.util.DatumUtils;
import com.alipay.sofa.registry.task.KeyedThreadPoolExecutor;
import com.alipay.sofa.registry.util.DatumVersionUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class FirePushService {

    private static final Logger      LOGGER   = LoggerFactory.getLogger(FirePushService.class);

    @Autowired
    private SessionServerConfig      sessionServerConfig;

    @Autowired
    private CacheService             sessionCacheService;

    @Autowired
    private Interests                sessionInterests;

    @Autowired
    private AppRevisionCacheRegistry appRevisionCacheRegistry;

    private KeyedThreadPoolExecutor  fetchExecutor;

    @Autowired
    private PushProcessor            pushProcessor;

    private final AtomicLong         fetchSeq = new AtomicLong();

    @PostConstruct
    public void init() {
        fetchExecutor = new KeyedThreadPoolExecutor("FetchExecutor",
            sessionServerConfig.getDataChangeFetchTaskWorkerSize(),
            sessionServerConfig.getDataChangeFetchTaskMaxBufferSize());
    }

    public boolean fireOnChange(String dataCenter, String dataInfoId, long expectVersion) {
        try {
            fetchExecutor
                .execute(dataInfoId, new ChangeTask(dataCenter, dataInfoId, expectVersion));
            return true;
        } catch (Throwable e) {
            LOGGER.error("failed to exec ChangeTask {}, dataCenter={}, expectVer={}", dataInfoId,
                dataCenter, expectVersion, e);
            return false;
        }
    }

    public boolean fireOnPushEmpty(Subscriber subscriber) {
        processPush(true, DatumVersionUtil.nextId(), getDataCenterWhenPushEmpty(),
            Collections.emptyMap(), Collections.singletonList(subscriber), Long.MAX_VALUE,
            Long.MAX_VALUE);
        LOGGER.info("firePushEmpty, {}", subscriber);
        return true;
    }

    public boolean fireOnRegister(Subscriber subscriber) {
        try {
            fetchExecutor.execute(subscriber.getDataInfoId(), new RegisterTask(subscriber));
            return true;
        } catch (Throwable e) {
            LOGGER.error("failed to exec SubscriberTask {}, {}", subscriber.getDataInfoId(),
                subscriber, e);
            return false;
        }
    }

    public boolean fireOnDatum(Datum datum) {
        DataInfo dataInfo = DataInfo.valueOf(datum.getDataInfoId());
        if (ValueConstants.SOFA_APP.equals(dataInfo.getDataType())) {
            LOGGER.error("unsupported DataType when fireOnDatum {}", dataInfo);
            return false;
        }
        Collection<Subscriber> subscribers = sessionInterests.getInterestOfDatum(dataInfo
            .getDataInfoId());
        processPush(true, datum.getVersion(), datum.getDataCenter(),
            Collections.singletonMap(datum.getDataInfoId(), datum), subscribers,
            fetchSeq.incrementAndGet(), fetchSeq.incrementAndGet());
        return true;
    }

    protected String getDataCenterWhenPushEmpty() {
        // TODO cloud mode use default.datacenter?
        return sessionServerConfig.getSessionServerDataCenter();
    }

    private void doExecuteOnChange(String dataCenter, String changeDataInfoId, long expectVersion) {
        final long fetchSeqStart = fetchSeq.incrementAndGet();
        final Datum datum = getDatum(dataCenter, changeDataInfoId, expectVersion);
        Set<String> revisions = Collections.emptySet();
        if (datum != null) {
            revisions = datum.revisions();
            if (datum.getVersion() < expectVersion) {
                LOGGER.warn("[lessVer] {},{},{}<{}", dataCenter, changeDataInfoId,
                    datum.getVersion(), expectVersion);
            }
        } else {
            LOGGER.warn("[NilDatum] {},{},{}", dataCenter, changeDataInfoId, expectVersion);
        }
        DataInfo dataInfo = DataInfo.valueOf(changeDataInfoId);
        if (ValueConstants.SOFA_APP.equals(dataInfo.getDataType())) {
            appRevisionCacheRegistry.refreshMeta(revisions);
            onAppDatumChange(dataInfo, datum, fetchSeqStart, dataCenter);
        } else {
            onInterfaceDatumChange(dataInfo, datum, fetchSeqStart, dataCenter);
        }
    }

    private void onAppDatumChange(DataInfo appDataInfo, Datum appDatum, long fetchSeqStart, String dataCenter) {
        //dataInfoId is app, get relate interfaces dataInfoId from cache
        Set<String> interfaceInfoIds = appRevisionCacheRegistry.getInterfaces(appDataInfo.getDataId());

        if (CollectionUtils.isEmpty(interfaceInfoIds)) {
            LOGGER.warn("App no interfaces {}", appDataInfo.getDataInfoId());
            return;
        }
        for (String interfaceDataInfoId : interfaceInfoIds) {
            Map<AssembleType, Map<ScopeEnum, List<Subscriber>>> groups = SubscriberUtils
                    .groupByAssembleAndScope(sessionInterests.getDatas(interfaceDataInfoId));
            if (groups.isEmpty()) {
                continue;
            }
            for (Map.Entry<AssembleType, Map<ScopeEnum, List<Subscriber>>> group : groups
                    .entrySet()) {
                final AssembleType assembleType = group.getKey();
                final Map<String, Datum> datumMap = Maps.newHashMap();
                collect(datumMap, appDatum);

                switch (assembleType) {
                    // not care app change
                    case sub_interface:
                        continue;
                    case sub_app_and_interface: {
                        // not collect self, self has collected
                        datumMap.putAll(getAppDatumsOfInterface(interfaceDataInfoId, dataCenter,
                                appDataInfo.getInstanceId(), t -> !t.equals(appDataInfo.getDataId())));
                        // add interface datum
                        Datum interfaceDatum = getDatum(dataCenter, interfaceDataInfoId, Long.MIN_VALUE);
                        collect(datumMap, interfaceDatum);
                        break;
                    }
                    case sub_app: {
                        // not collect self, self has collected
                        datumMap.putAll(getAppDatumsOfInterface(interfaceDataInfoId, dataCenter,
                                appDataInfo.getInstanceId(), t -> !t.equals(appDataInfo.getDataId())));
                        break;
                    }
                    default: {
                        LOGGER.error("unsupported AssembleType:" + assembleType);
                        continue;
                    }
                }
                final long pushVersion = DatumVersionUtil.nextId();
                final long fetchEndSeq = fetchSeq.incrementAndGet();
                // push1.fetchSeq.start > push2.fetchSeq.end, means
                // 1. push1.datum > push2.datum
                // 2. push1.pushVersion > push2.pushVersion
                if (CollectionUtils.isEmpty(datumMap)) {
                    LOGGER.warn("empty push {}, dataCenter={}", interfaceDataInfoId, dataCenter);
                }
                for (Map.Entry<ScopeEnum, List<Subscriber>> scopes : group.getValue().entrySet()) {
                    processPush(false, pushVersion, dataCenter, datumMap, scopes.getValue(),
                            fetchSeqStart, fetchEndSeq);
                }
            }
        }
    }

    private Map<String, Datum> getAppDatumsOfInterface(String interfaceDataInfoId,
                                                       String dataCenter, String instanceId,
                                                       Predicate<String> predicate) {
        Set<String> appDataIds = appRevisionCacheRegistry.getAppRevisions(interfaceDataInfoId)
            .keySet();
        if (CollectionUtils.isEmpty(appDataIds)) {
            return Collections.emptyMap();
        }
        Map<String, Datum> datumMap = Maps.newHashMap();
        for (String appDataId : appDataIds) {
            if (predicate == null || predicate.test(appDataId)) {
                String appDataInfoId = DataInfo.toDataInfoId(appDataId, instanceId,
                    ValueConstants.SOFA_APP);
                Datum appDatum = getDatum(dataCenter, appDataInfoId, Long.MIN_VALUE);
                collect(datumMap, appDatum);
            }
        }
        return datumMap;
    }

    private void onInterfaceDatumChange(DataInfo interfaceDataInfo, Datum interfaceDatum,
                                        long fetchSeqStart, String dataCenter) {
        Map<AssembleType, Map<ScopeEnum, List<Subscriber>>> grous = SubscriberUtils
            .groupByAssembleAndScope(sessionInterests.getDatas(interfaceDataInfo.getDataInfoId()));

        for (Map.Entry<AssembleType, Map<ScopeEnum, List<Subscriber>>> group : grous.entrySet()) {
            final AssembleType assembleType = group.getKey();
            final Map<String, Datum> datumMap = Maps.newHashMap();
            collect(datumMap, interfaceDatum);

            switch (assembleType) {
                case sub_app:
                    // not care interface change
                    continue;
                case sub_app_and_interface: {
                    datumMap.putAll(getAppDatumsOfInterface(interfaceDataInfo.getDataInfoId(),
                        dataCenter, interfaceDataInfo.getInstanceId(), null));
                    break;
                }
                case sub_interface: {
                    // only care the interface
                    break;
                }
                default: {
                    LOGGER.error("unsupported AssembleType:" + assembleType);
                    continue;
                }
            }
            final long pushVersion = DatumVersionUtil.nextId();
            final long fetchSeqEnd = fetchSeq.incrementAndGet();
            if (CollectionUtils.isEmpty(datumMap)) {
                LOGGER.warn("empty push {}, dataCenter={}", interfaceDataInfo.getDataInfoId(),
                    dataCenter);
            }
            for (Map.Entry<ScopeEnum, List<Subscriber>> scopes : group.getValue().entrySet()) {
                processPush(false, pushVersion, dataCenter, datumMap, scopes.getValue(),
                    fetchSeqStart, fetchSeqEnd);
            }
        }
    }

    private void processPush(boolean noDelay, long pushVersion, String dataCenter,
                             Map<String, Datum> datumMap, Collection<Subscriber> subscriberList,
                             long fetchSeqStart, long fetchSeqEnd) {
        if (subscriberList.isEmpty()) {
            return;
        }
        subscriberList = subscribersPushCheck(dataCenter, DatumUtils.getVesions(datumMap),
            subscriberList);
        if (CollectionUtils.isEmpty(subscriberList)) {
            return;
        }
        Map<InetSocketAddress, Map<String, Subscriber>> group = SubscriberUtils
            .groupBySourceAddress(subscriberList);
        for (Map.Entry<InetSocketAddress, Map<String, Subscriber>> e : group.entrySet()) {
            final InetSocketAddress addr = e.getKey();
            final Map<String, Subscriber> subscriberMap = e.getValue();
            pushProcessor.firePush(noDelay, pushVersion, dataCenter, addr, subscriberMap, datumMap,
                fetchSeqStart, fetchSeqEnd);
        }
    }

    private Datum getDatum(String dataCenter, String dataInfoId, long expectVersion) {
        Key key = new Key(Key.KeyType.OBJ, DatumKey.class.getName(), new DatumKey(dataInfoId,
            dataCenter));
        Value value = sessionCacheService.getValueIfPresent(key);
        if (value != null) {
            Datum datum = (Datum) value.getPayload();
            if (datum != null && datum.getVersion() >= expectVersion) {
                // the expect version got
                return datum;
            }
        }
        // the cache is too old
        sessionCacheService.invalidate(key);
        value = sessionCacheService.getValue(key);
        return value == null ? null : (Datum) value.getPayload();
    }

    private List<Subscriber> subscribersPushCheck(String dataCenter, Map<String, Long> versions,
                                                  Collection<Subscriber> subscribers) {
        List<Subscriber> subscribersSend = Lists.newArrayList();
        for (Subscriber subscriber : subscribers) {
            if (subscriber.checkVersion(dataCenter, versions)) {
                subscribersSend.add(subscriber);
            }
        }
        return subscribersSend;
    }

    private final class ChangeTask implements Runnable {
        final String dataCenter;
        final String dataInfoId;
        final long   expectVersion;

        ChangeTask(String dataCenter, String dataInfoId, long expectVersion) {
            this.dataCenter = dataCenter;
            this.dataInfoId = dataInfoId;
            this.expectVersion = expectVersion;
        }

        @Override
        public void run() {
            try {
                doExecuteOnChange(dataCenter, dataInfoId, expectVersion);
            } catch (Throwable e) {
                LOGGER.error("failed to do change Task, {}, dataCenter={}, expectVersion={}",
                    dataInfoId, dataCenter, expectVersion, e);
            }
        }
    }

    private void doExecuteOnSubscriber(String dataCenter, Subscriber subscriber) {
        final AssembleType assembleType = subscriber.getAssembleType();
        final String subDataInfoId = subscriber.getDataInfoId();

        final long fetchSeqStart = fetchSeq.incrementAndGet();

        final Map<String, Datum> datumMap = Maps.newHashMap();
        switch (assembleType) {
            case sub_interface: {
                // only care the interface
                Datum datum = getDatum(dataCenter, subDataInfoId, Long.MIN_VALUE);
                collect(datumMap, datum);
                break;
            }
            case sub_app_and_interface: {
                // try get app
                datumMap.putAll(getAppDatumsOfInterface(subDataInfoId, dataCenter,
                    subscriber.getInstanceId(), null));
                // try get interface
                Datum datum = getDatum(dataCenter, subDataInfoId, Long.MIN_VALUE);
                collect(datumMap, datum);
                break;
            }

            case sub_app: {
                datumMap.putAll(getAppDatumsOfInterface(subDataInfoId, dataCenter,
                    subscriber.getInstanceId(), null));
                break;
            }

            default:
                LOGGER.error("unsupported assembleType {}, {}", assembleType, subscriber);
                return;
        }
        final long pushVersion = DatumVersionUtil.nextId();
        final long fetchSeqEnd = fetchSeq.incrementAndGet();
        if (CollectionUtils.isEmpty(datumMap)) {
            LOGGER.warn("empty push, dataCenter={}, {}", dataCenter, subscriber);
        }
        processPush(true, pushVersion, sessionServerConfig.getSessionServerDataCenter(), datumMap,
            Collections.singletonList(subscriber), fetchSeqStart, fetchSeqEnd);
    }

    private void collect(Map<String, Datum> datumMap, Datum datum) {
        if (datum != null) {
            datumMap.put(datum.getDataInfoId(), datum);
        }
    }

    private final class RegisterTask implements Runnable {
        final Subscriber subscriber;

        RegisterTask(Subscriber subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void run() {
            final String dataCenter = sessionServerConfig.getSessionServerDataCenter();
            try {
                doExecuteOnSubscriber(dataCenter, subscriber);
            } catch (Throwable e) {
                LOGGER.error("failed to do register Task, dataCenter={}, {}", dataCenter,
                    subscriber, e);
            }
        }
    }
}