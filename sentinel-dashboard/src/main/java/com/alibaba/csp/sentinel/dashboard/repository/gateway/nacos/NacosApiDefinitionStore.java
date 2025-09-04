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
package com.alibaba.csp.sentinel.dashboard.repository.gateway.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.InMemoryRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.repository.rule.nacos.NacosRuleRepositoryAdapter;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Store {@link ApiDefinitionEntity} in memory.
 *
 * @author cdfive
 * @since 1.7.0
 */
@Primary
@Component
public class NacosApiDefinitionStore extends NacosRuleRepositoryAdapter<ApiDefinitionEntity> {

    private static AtomicLong ids = new AtomicLong(0);

    @Override
    protected String getApp(ApiDefinitionEntity rule) {
        return rule.getApp();
    }

    @Override
    protected String getDataId(String app) {
        return app + NacosConfigUtil.CLIENT_CONFIG_DATA_ID_POSTFIX;
    }

    @Override
    protected MachineInfo getMachine(ApiDefinitionEntity rule) {
        // 构造一个 MachineInfo 对象，用于 findAllByMachine 方法匹配
        MachineInfo machine = new MachineInfo();
        machine.setApp(rule.getApp());
        machine.setIp(rule.getIp());
        machine.setPort(rule.getPort());
        return machine;
    }

    @Override
    protected long nextId(ApiDefinitionEntity entity) {
        if (ids.intValue() == 0) {//如果是重启后 且存在已有规则则赋值为最大id+1
            if (!CollectionUtils.isEmpty(this.findAllByApp(entity.getApp()))) {
                long maxId = this.findAllByApp(entity.getApp()).stream().max(Comparator.comparingLong(ApiDefinitionEntity::getId)).get().getId();
                ids.set(maxId);
            }
        }
        return ids.incrementAndGet();
    }
}
