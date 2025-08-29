/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.repository.rule;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Store {@link FlowRuleEntity} in memory.
 *
 * @author leyou
 */
@Component
public class InMemFlowRuleStore extends InMemoryRuleRepositoryAdapter<FlowRuleEntity> {

    private static AtomicLong ids = new AtomicLong(0);

    @Override
    protected long nextId(FlowRuleEntity entity) {
        if (ids.intValue() == 0) {//如果是重启后 且存在已有规则则赋值为最大id+1
            if (!CollectionUtils.isEmpty(this.findAllByApp(entity.getApp()))) {
                long maxId = this.findAllByApp(entity.getApp()).stream().max(Comparator.comparingLong(FlowRuleEntity::getId)).get().getId();
                ids.set(maxId);
            }
        }
        return ids.incrementAndGet();
    }

    @Override
    protected FlowRuleEntity preProcess(FlowRuleEntity entity) {
        if (entity != null && entity.isClusterMode()) {
            ClusterFlowConfig config = entity.getClusterConfig();
            if (config == null) {
                config = new ClusterFlowConfig();
                entity.setClusterConfig(config);
            }
            // Set cluster rule id.
            config.setFlowId(entity.getId());
        }
        return entity;
    }
}
