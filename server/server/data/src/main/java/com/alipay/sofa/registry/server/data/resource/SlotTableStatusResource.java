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
package com.alipay.sofa.registry.server.data.resource;

import com.alipay.sofa.registry.common.model.GenericResponse;
import com.alipay.sofa.registry.common.model.slot.BaseSlotStatus;
import com.alipay.sofa.registry.common.model.slot.FollowerSlotStatus;
import com.alipay.sofa.registry.common.model.slot.LeaderSlotStatus;
import com.alipay.sofa.registry.common.model.slot.Slot;
import com.alipay.sofa.registry.server.data.slot.SlotManager;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author chen.zhu
 *     <p>Mar 22, 2021
 */
@Path("openapi/v1/slot/table")
public class SlotTableStatusResource {

  public static final long MAX_SYNC_GAP = Long.getLong("data.replicate.max.gap", 3 * 60 * 1000);

  @Autowired private SlotManager slotManager;

  @GET
  @Path("/sync/task/status")
  @Produces(MediaType.APPLICATION_JSON)
  public GenericResponse<Object> getSlotTableSyncTaskStatus() {
    long epoch;
    List<BaseSlotStatus> slotStatuses;
    slotManager.readLock().lock();
    try {
      epoch = slotManager.getSlotTableEpoch();
      slotStatuses = slotManager.getSlotStatuses();
    } finally {
      slotManager.readLock().unlock();
    }
    boolean isCurrentSlotStable = true;
    for (BaseSlotStatus slotStatus : slotStatuses) {
      if (slotStatus.getRole() == Slot.Role.Leader) {
        if (!((LeaderSlotStatus) slotStatus).getLeaderStatus().isHealthy()) {
          isCurrentSlotStable = false;
          break;
        }
      } else {
        if (System.currentTimeMillis() - ((FollowerSlotStatus) slotStatus).getLastLeaderSyncTime()
            > MAX_SYNC_GAP) {
          isCurrentSlotStable = false;
          break;
        }
      }
    }
    return new GenericResponse<>()
        .fillSucceed(new SlotTableSyncTaskStatus(epoch, isCurrentSlotStable, slotStatuses));
  }

  public static class SlotTableSyncTaskStatus {
    private final long epoch;
    private final boolean isCurrentSlotTableStable;
    private final List<BaseSlotStatus> slotStatuses;

    public SlotTableSyncTaskStatus(
        long epoch, boolean isCurrentSlotStable, List<BaseSlotStatus> slotStatuses) {
      this.epoch = epoch;
      this.isCurrentSlotTableStable = isCurrentSlotStable;
      this.slotStatuses = slotStatuses;
    }

    public long getEpoch() {
      return epoch;
    }

    public boolean isCurrentSlotTableStable() {
      return isCurrentSlotTableStable;
    }

    public List<BaseSlotStatus> getSlotStatuses() {
      return slotStatuses;
    }
  }
}