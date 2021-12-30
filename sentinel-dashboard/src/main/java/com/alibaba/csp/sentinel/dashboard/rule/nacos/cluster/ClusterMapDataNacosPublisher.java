package com.alibaba.csp.sentinel.dashboard.rule.nacos.cluster;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.ClusterGroupEntity;
import com.alibaba.csp.sentinel.dashboard.domain.cluster.request.ClusterAppAssignMap;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("clusterMapDataNacosPublisher")
public class ClusterMapDataNacosPublisher implements DynamicRulePublisher<List<ClusterAppAssignMap>> {

    @Autowired
    private ConfigService configService;

    @Override
    public void publish(String app, List<ClusterAppAssignMap> rules) throws Exception {
        if (rules == null) {
            return;
        }
        List<ClusterAppAssignMap> clusterAppAssignMapList = (List<ClusterAppAssignMap>) rules;

        List<ClusterGroupEntity> clusterGroupEntityList = new ArrayList<>();
        for (ClusterAppAssignMap clusterAppAssignMap : clusterAppAssignMapList) {
            ClusterGroupEntity clusterGroupEntity = new ClusterGroupEntity();
            clusterGroupEntity.setMachineId(clusterAppAssignMap.getMachineId());
            clusterGroupEntity.setIp(clusterAppAssignMap.getIp());
            clusterGroupEntity.setPort(clusterAppAssignMap.getPort());
            clusterGroupEntity.setClientSet(clusterAppAssignMap.getClientSet());
            clusterGroupEntity.setMaxAllowedQps(clusterAppAssignMap.getMaxAllowedQps());

            clusterGroupEntityList.add(clusterGroupEntity);
        }

        NacosConfigUtil.setClusterGroupEntity2Nacos(
                this.configService,
                app,
                NacosConfigUtil.CLUSTER_MAP_DATA_ID_POSTFIX,
                clusterGroupEntityList
        );
    }
}
