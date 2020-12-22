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
package com.alipay.sofa.registry.server.meta.bootstrap.config;

import com.alipay.sofa.registry.common.model.constants.ValueConstants;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

/**
 * @author shangyu.wh
 * @version $Id: MetaServerConfigBean.java, v 0.1 2018-01-16 11:01 shangyu.wh Exp $
 */
@ConfigurationProperties(prefix = MetaServerConfigBean.PREFIX)
public class MetaServerConfigBean implements MetaServerConfig {

    public static final String PREFIX                                          = "meta.server";

    private int                sessionServerPort                               = 9610;

    private int                dataServerPort                                  = 9611;

    private int                metaServerPort                                  = 9612;

    private int                httpServerPort                                  = 9615;

    private int                schedulerHeartbeatTimeout                       = 3;

    private int                schedulerHeartbeatExpBackOffBound               = 10;

    private int                schedulerCheckNodeListChangePushTimeout         = 3;

    private int                schedulerCheckNodeListChangePushFirstDelay      = 1;

    private int                schedulerCheckNodeListChangePushExpBackOffBound = 10;

    private int                dataNodeExchangeTimeout                         = 3000;

    private int                sessionNodeExchangeTimeout                      = 3000;

    private int                metaNodeExchangeTimeout                         = 3000;

    private int                raftElectionTimeout                             = 1000;

    private int                initialSlotTableLockTimeMilli                   = 1000;

    private int                waitForDataServerRestartTimeMilli               = 15 * 1000;

    /**
     * Whether to enable metrics for node.
     */
    private boolean            enableMetrics                                   = true;

    private String             raftDataPath                                    = System
                                                                                   .getProperty("user.home")
                                                                                 + File.separator
                                                                                 + "raftData";

    private int                rockDBCacheSize                                 = 64;           //64M

    private int                connectMetaServerExecutorMinSize                = 3;
    private int                connectMetaServerExecutorMaxSize                = 10;
    private int                connectMetaServerExecutorQueueSize              = 1024;

    private int                raftClientRefreshExecutorMinSize                = 3;
    private int                raftClientRefreshExecutorMaxSize                = 10;
    private int                raftClientRefreshExecutorQueueSize              = 1024;

    private int                defaultRequestExecutorMinSize                   = 20;
    private int                defaultRequestExecutorMaxSize                   = 600;
    private int                defaultRequestExecutorQueueSize                 = 500;

    private int                raftExecutorMinSize                             = 20;
    private int                raftExecutorMaxSize                             = 400;
    private int                raftExecutorQueueSize                           = 100;

    private int                raftServerExecutorMinSize                       = 20;
    private int                raftServerExecutorMaxSize                       = 100;
    private int                raftServerExecutorQueueSize                     = 100;

    private int                raftFsmExecutorMinSize                          = 3;
    private int                raftFsmExecutorMaxSize                          = 10;
    private int                raftFsmExecutorQueueSize                        = 100;

    private int                metaSchedulerPoolSize                           = 6;
    private double             sessionLoadbalanceThresholdRatio                = 1.1;

    @Override
    public int getSessionServerPort() {
        return sessionServerPort;
    }

    /**
     * Setter method for property <tt>sessionServerPort</tt>.
     *
     * @param sessionServerPort value to be assigned to property sessionServerPort
     */
    public void setSessionServerPort(int sessionServerPort) {
        this.sessionServerPort = sessionServerPort;
    }

    @Override
    public int getDataServerPort() {
        return dataServerPort;
    }

    /**
     * Setter method for property <tt>dataServerPort</tt>.
     *
     * @param dataServerPort value to be assigned to property dataServerPort
     */
    public void setDataServerPort(int dataServerPort) {
        this.dataServerPort = dataServerPort;
    }

    @Override
    public int getHttpServerPort() {
        return httpServerPort;
    }

    /**
     * Setter method for property <tt>httpServerPort</tt>.
     *
     * @param httpServerPort value to be assigned to property httpServerPort
     */
    public void setHttpServerPort(int httpServerPort) {
        this.httpServerPort = httpServerPort;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatTimeout</tt>.
     *
     * @return property value of schedulerHeartbeatTimeout
     */
    @Override
    public int getSchedulerHeartbeatTimeout() {
        return schedulerHeartbeatTimeout;
    }

    /**
     * Setter method for property <tt>schedulerHeartbeatTimeout</tt>.
     *
     * @param schedulerHeartbeatTimeout value to be assigned to property schedulerHeartbeatTimeout
     */
    public void setSchedulerHeartbeatTimeout(int schedulerHeartbeatTimeout) {
        this.schedulerHeartbeatTimeout = schedulerHeartbeatTimeout;
    }

    /**
     * Getter method for property <tt>schedulerHeartbeatExpBackOffBound</tt>.
     *
     * @return property value of schedulerHeartbeatExpBackOffBound
     */
    @Override
    public int getSchedulerHeartbeatExpBackOffBound() {
        return schedulerHeartbeatExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerHeartbeatExpBackOffBound</tt>.
     *
     * @param schedulerHeartbeatExpBackOffBound value to be assigned to property schedulerHeartbeatExpBackOffBound
     */
    public void setSchedulerHeartbeatExpBackOffBound(int schedulerHeartbeatExpBackOffBound) {
        this.schedulerHeartbeatExpBackOffBound = schedulerHeartbeatExpBackOffBound;
    }

    /**
     * Getter method for property <tt>dataNodeExchangeTimeout</tt>.
     *
     * @return property value of dataNodeExchangeTimeout
     */
    @Override
    public int getDataNodeExchangeTimeout() {
        return dataNodeExchangeTimeout;
    }

    /**
     * Setter method for property <tt>dataNodeExchangeTimeout</tt>.
     *
     * @param dataNodeExchangeTimeout value to be assigned to property dataNodeExchangeTimeout
     */
    public void setDataNodeExchangeTimeout(int dataNodeExchangeTimeout) {
        this.dataNodeExchangeTimeout = dataNodeExchangeTimeout;
    }

    /**
     * Getter method for property <tt>sessionNodeExchangeTimeout</tt>.
     *
     * @return property value of sessionNodeExchangeTimeout
     */
    @Override
    public int getSessionNodeExchangeTimeout() {
        return sessionNodeExchangeTimeout;
    }

    /**
     * Setter method for property <tt>sessionNodeExchangeTimeout</tt>.
     *
     * @param sessionNodeExchangeTimeout value to be assigned to property sessionNodeExchangeTimeout
     */
    public void setSessionNodeExchangeTimeout(int sessionNodeExchangeTimeout) {
        this.sessionNodeExchangeTimeout = sessionNodeExchangeTimeout;
    }

    /**
     * Getter method for property <tt>metaServerPort</tt>.
     *
     * @return property value of metaServerPort
     */
    @Override
    public int getMetaServerPort() {
        return metaServerPort;
    }

    /**
     * Setter method for property <tt>metaServerPort</tt>.
     *
     * @param metaServerPort value to be assigned to property metaServerPort
     */
    public void setMetaServerPort(int metaServerPort) {
        this.metaServerPort = metaServerPort;
    }

    /**
     * Getter method for property <tt>metaNodeExchangeTimeout</tt>.
     *
     * @return property value of metaNodeExchangeTimeout
     */
    @Override
    public int getMetaNodeExchangeTimeout() {
        return metaNodeExchangeTimeout;
    }

    /**
     * Setter method for property <tt>metaNodeExchangeTimeout</tt>.
     *
     * @param metaNodeExchangeTimeout value to be assigned to property metaNodeExchangeTimeout
     */
    public void setMetaNodeExchangeTimeout(int metaNodeExchangeTimeout) {
        this.metaNodeExchangeTimeout = metaNodeExchangeTimeout;
    }

    /**
     * Getter method for property <tt>schedulerCheckNodeListChangePushTimeout</tt>.
     *
     * @return property value of schedulerCheckNodeListChangePushTimeout
     */
    @Override
    public int getSchedulerCheckNodeListChangePushTimeout() {
        return schedulerCheckNodeListChangePushTimeout;
    }

    /**
     * Setter method for property <tt>schedulerCheckNodeListChangePushTimeout</tt>.
     *
     * @param schedulerCheckNodeListChangePushTimeout value to be assigned to property schedulerCheckNodeListChangePushTimeout
     */
    public void setSchedulerCheckNodeListChangePushTimeout(int schedulerCheckNodeListChangePushTimeout) {
        this.schedulerCheckNodeListChangePushTimeout = schedulerCheckNodeListChangePushTimeout;
    }

    /**
     * Getter method for property <tt>schedulerCheckNodeListChangePushFirstDelay</tt>.
     *
     * @return property value of schedulerCheckNodeListChangePushFirstDelay
     */
    @Override
    public int getSchedulerCheckNodeListChangePushFirstDelay() {
        return schedulerCheckNodeListChangePushFirstDelay;
    }

    /**
     * Setter method for property <tt>schedulerCheckNodeListChangePushFirstDelay</tt>.
     *
     * @param schedulerCheckNodeListChangePushFirstDelay value to be assigned to property schedulerCheckNodeListChangePushFirstDelay
     */
    public void setSchedulerCheckNodeListChangePushFirstDelay(int schedulerCheckNodeListChangePushFirstDelay) {
        this.schedulerCheckNodeListChangePushFirstDelay = schedulerCheckNodeListChangePushFirstDelay;
    }

    /**
     * Getter method for property <tt>schedulerCheckNodeListChangePushExpBackOffBound</tt>.
     *
     * @return property value of schedulerCheckNodeListChangePushExpBackOffBound
     */
    @Override
    public int getSchedulerCheckNodeListChangePushExpBackOffBound() {
        return schedulerCheckNodeListChangePushExpBackOffBound;
    }

    /**
     * Setter method for property <tt>schedulerCheckNodeListChangePushExpBackOffBound</tt>.
     *
     * @param schedulerCheckNodeListChangePushExpBackOffBound value to be assigned to property
     *                                                        schedulerCheckNodeListChangePushExpBackOffBound
     */
    public void setSchedulerCheckNodeListChangePushExpBackOffBound(int schedulerCheckNodeListChangePushExpBackOffBound) {
        this.schedulerCheckNodeListChangePushExpBackOffBound = schedulerCheckNodeListChangePushExpBackOffBound;
    }

    /**
     * Getter method for property <tt>raftGroup</tt>.
     *
     * @return property value of raftGroup
     */
    @Override
    public String getRaftGroup() {
        return ValueConstants.RAFT_SERVER_GROUP;
    }

    /**
     * Getter method for property <tt>raftServerPort</tt>.
     *
     * @return property value of raftServerPort
     */
    @Override
    public int getRaftServerPort() {
        return ValueConstants.RAFT_SERVER_PORT;
    }

    @Override
    public int getCrossDcMetaSyncIntervalMilli() {
        return ValueConstants.CROSS_DC_META_SYNC_INTERVAL_MILLI;
    }

    /**
     * Getter method for property <tt>raftDataPath</tt>.
     *
     * @return property value of raftDataPath
     */
    @Override
    public String getRaftDataPath() {
        return raftDataPath;
    }

    /**
     * Setter method for property <tt>raftDataPath</tt>.
     *
     * @param raftDataPath value to be assigned to property raftDataPath
     */
    public void setRaftDataPath(String raftDataPath) {
        this.raftDataPath = raftDataPath;
    }

    @Override
    public boolean isEnableMetrics() {
        return enableMetrics;
    }

    /**
     * Setter method for property <tt>enableMetrics</tt>.
     *
     * @param enableMetrics value to be assigned to property enableMetrics
     */
    public void setEnableMetrics(boolean enableMetrics) {
        this.enableMetrics = enableMetrics;
    }

    /**
     * Getter method for property <tt>RockDBCacheSize</tt>.
     *
     * @return property value of RockDBCacheSize
     */
    public int getRockDBCacheSize() {
        return rockDBCacheSize;
    }

    /**
     * Setter method for property <tt>RockDBCacheSize</tt>.
     *
     * @param rockDBCacheSize value to be assigned to property RockDBCacheSize
     */
    public void setRockDBCacheSize(int rockDBCacheSize) {
        this.rockDBCacheSize = rockDBCacheSize;
    }

    /**
     * Getter method for property <tt>connectMetaServerExecutorMinSize</tt>.
     *
     * @return property value of connectMetaServerExecutorMinSize
     */
    @Override
    public int getConnectMetaServerExecutorMinSize() {
        return connectMetaServerExecutorMinSize;
    }

    /**
     * Getter method for property <tt>connectMetaServerExecutorMaxSize</tt>.
     *
     * @return property value of connectMetaServerExecutorMaxSize
     */
    @Override
    public int getConnectMetaServerExecutorMaxSize() {
        return connectMetaServerExecutorMaxSize;
    }

    /**
     * Getter method for property <tt>connectMetaServerExecutorQueueSize</tt>.
     *
     * @return property value of connectMetaServerExecutorQueueSize
     */
    @Override
    public int getConnectMetaServerExecutorQueueSize() {
        return connectMetaServerExecutorQueueSize;
    }

    /**
     * Getter method for property <tt>raftClientRefreshExecutorMinSize</tt>.
     *
     * @return property value of raftClientRefreshExecutorMinSize
     */
    @Override
    public int getRaftClientRefreshExecutorMinSize() {
        return raftClientRefreshExecutorMinSize;
    }

    /**
     * Getter method for property <tt>raftClientRefreshExecutorMaxSize</tt>.
     *
     * @return property value of raftClientRefreshExecutorMaxSize
     */
    @Override
    public int getRaftClientRefreshExecutorMaxSize() {
        return raftClientRefreshExecutorMaxSize;
    }

    /**
     * Getter method for property <tt>raftClientRefreshExecutorQueueSize</tt>.
     *
     * @return property value of raftClientRefreshExecutorQueueSize
     */
    @Override
    public int getRaftClientRefreshExecutorQueueSize() {
        return raftClientRefreshExecutorQueueSize;
    }

    /**
     * Setter method for property <tt>metaSchedulerPoolSize </tt>.
     *
     * @param metaSchedulerPoolSize value to be assigned to property metaSchedulerPoolSize
     */
    public void setMetaSchedulerPoolSize(int metaSchedulerPoolSize) {
        this.metaSchedulerPoolSize = metaSchedulerPoolSize;
    }

    @Override
    public int getMetaSchedulerPoolSize() {
        return metaSchedulerPoolSize;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }

    public int getDefaultRequestExecutorMinSize() {
        return defaultRequestExecutorMinSize;
    }

    public void setDefaultRequestExecutorMinSize(int defaultRequestExecutorMinSize) {
        this.defaultRequestExecutorMinSize = defaultRequestExecutorMinSize;
    }

    public int getDefaultRequestExecutorMaxSize() {
        return defaultRequestExecutorMaxSize;
    }

    public void setDefaultRequestExecutorMaxSize(int defaultRequestExecutorMaxSize) {
        this.defaultRequestExecutorMaxSize = defaultRequestExecutorMaxSize;
    }

    public int getDefaultRequestExecutorQueueSize() {
        return defaultRequestExecutorQueueSize;
    }

    public void setDefaultRequestExecutorQueueSize(int defaultRequestExecutorQueueSize) {
        this.defaultRequestExecutorQueueSize = defaultRequestExecutorQueueSize;
    }

    public int getRaftExecutorMinSize() {
        return raftExecutorMinSize;
    }

    public void setRaftExecutorMinSize(int raftExecutorMinSize) {
        this.raftExecutorMinSize = raftExecutorMinSize;
    }

    public int getRaftExecutorMaxSize() {
        return raftExecutorMaxSize;
    }

    public void setRaftExecutorMaxSize(int raftExecutorMaxSize) {
        this.raftExecutorMaxSize = raftExecutorMaxSize;
    }

    public int getRaftExecutorQueueSize() {
        return raftExecutorQueueSize;
    }

    public void setRaftExecutorQueueSize(int raftExecutorQueueSize) {
        this.raftExecutorQueueSize = raftExecutorQueueSize;
    }

    public int getRaftServerExecutorMinSize() {
        return raftServerExecutorMinSize;
    }

    public void setRaftServerExecutorMinSize(int raftServerExecutorMinSize) {
        this.raftServerExecutorMinSize = raftServerExecutorMinSize;
    }

    public int getRaftServerExecutorMaxSize() {
        return raftServerExecutorMaxSize;
    }

    public void setRaftServerExecutorMaxSize(int raftServerExecutorMaxSize) {
        this.raftServerExecutorMaxSize = raftServerExecutorMaxSize;
    }

    public int getRaftServerExecutorQueueSize() {
        return raftServerExecutorQueueSize;
    }

    public void setRaftServerExecutorQueueSize(int raftServerExecutorQueueSize) {
        this.raftServerExecutorQueueSize = raftServerExecutorQueueSize;
    }

    @Override
    public int getRaftFsmExecutorMinSize() {
        return raftFsmExecutorMinSize;
    }

    public void setRaftFsmExecutorMinSize(int raftFsmExecutorMinSize) {
        this.raftFsmExecutorMinSize = raftFsmExecutorMinSize;
    }

    @Override
    public int getRaftFsmExecutorMaxSize() {
        return raftFsmExecutorMaxSize;
    }

    public void setRaftFsmExecutorMaxSize(int raftFsmExecutorMaxSize) {
        this.raftFsmExecutorMaxSize = raftFsmExecutorMaxSize;
    }

    @Override
    public int getRaftFsmExecutorQueueSize() {
        return raftFsmExecutorQueueSize;
    }

    public void setRaftFsmExecutorQueueSize(int raftFsmExecutorQueueSize) {
        this.raftFsmExecutorQueueSize = raftFsmExecutorQueueSize;
    }

    public int getRaftElectionTimeout() {
        return raftElectionTimeout;
    }

    public void setRaftElectionTimeout(int raftElectionTimeout) {
        this.raftElectionTimeout = raftElectionTimeout;
    }

    @Override
    public double getSessionLoadbalanceThresholdRatio() {
        return sessionLoadbalanceThresholdRatio;
    }

    @Override
    public int getInitialSlotTableNonChangeLockTimeMilli() {
        return initialSlotTableLockTimeMilli;
    }

    @Override
    public int getWaitForDataServerRestartTime() {
        return waitForDataServerRestartTimeMilli;
    }

    public void setSessionLoadbalanceThresholdRatio(double sessionLoadbalanceThresholdRatio) {
        this.sessionLoadbalanceThresholdRatio = sessionLoadbalanceThresholdRatio;
    }
}