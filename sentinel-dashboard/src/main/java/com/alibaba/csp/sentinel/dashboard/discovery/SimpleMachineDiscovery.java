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
package com.alibaba.csp.sentinel.dashboard.discovery;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author leyou
 */
@Component
public class SimpleMachineDiscovery implements MachineDiscovery {

    private ConcurrentMap<String, AppInfo> apps ;


    @Autowired
    private ConfigService configService;

    @PostConstruct
    public void init(){
        try {
            apps =  NacosConfigUtil.getClusterAppInfo4Nacos(configService , "sentinel",
                    NacosConfigUtil.CLUSTER_MACHINE_APPINFO,
                    AppInfo.class);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }


    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        AppInfo appInfo = apps.computeIfAbsent(machineInfo.getApp(), o -> new AppInfo(machineInfo.getApp(), machineInfo.getAppType()));
        appInfo.addMachine(machineInfo);
        try {
            NacosConfigUtil.setClusterAppInfo4Nacos(configService , "sentinel",
                    NacosConfigUtil.CLUSTER_MACHINE_APPINFO,
                    apps);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        AppInfo appInfo = apps.get(app);
        if (appInfo != null) {
            return appInfo.removeMachine(ip, port);
        }
        try {
            NacosConfigUtil.setClusterAppInfo4Nacos(configService , "sentinel",
                    NacosConfigUtil.CLUSTER_MACHINE_APPINFO,
                    apps);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        return new ArrayList<>(apps.keySet());
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        return apps.get(app);
    }

    @Override
    public Set<AppInfo> getBriefApps() {
        return new HashSet<>(apps.values());
    }

    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        apps.remove(app);
        try {
            NacosConfigUtil.setClusterAppInfo4Nacos(configService , "sentinel",
                    NacosConfigUtil.CLUSTER_MACHINE_APPINFO,
                    apps);
        } catch (NacosException e) {
            e.printStackTrace();
        }
    }

}
