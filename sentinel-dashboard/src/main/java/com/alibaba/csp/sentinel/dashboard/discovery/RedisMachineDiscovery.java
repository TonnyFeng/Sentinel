package com.alibaba.csp.sentinel.dashboard.discovery;


import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

@Component
@Primary
public class RedisMachineDiscovery implements MachineDiscovery{
    private static Logger logger = LoggerFactory.getLogger(RedisMachineDiscovery.class);

    private static final String KEY = "machineInfo";

    @Autowired
    private JedisPool jedisPool;

    @Override
    public long addMachine(MachineInfo machineInfo) {
        AssertUtil.notNull(machineInfo, "machineInfo cannot be null");
        AppInfo appInfo = getDetailApp(machineInfo.getApp());
        if (appInfo == null) {
            appInfo = new AppInfo(machineInfo.getApp(), machineInfo.getAppType());
        }
        /*
         * else { Optional<MachineInfo> machine =
         * appInfo.getMachine(machineInfo.getIp(), machineInfo.getPort()); //
         * 判断当前服务器节点是否已经存在了，存在则不增加了 if (machine.isPresent()) { return 0; } }
         */
        appInfo.addMachine(machineInfo);

        boolean saveResult = saveData(machineInfo.getApp(), appInfo);
        if (saveResult) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean removeMachine(String app, String ip, int port) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        AppInfo appInfo = getDetailApp(app);
        if (appInfo != null) {
            Optional<MachineInfo> machineInfo = appInfo.getMachine(ip, port);
            if (machineInfo.isPresent()) {
                boolean removeResult = appInfo.removeMachine(ip, port);
                if (removeResult) {
                    // 将删除后的结果回写的ZK
                    boolean saveResult = saveData(app, appInfo);
                    if (!saveResult) {
                        // 如果保存失败，再加回去
                        appInfo.addMachine(machineInfo.get());
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    @Override
    public List<String> getAppNames() {
        List<String> childRenList = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource()){
            Map<String, String> machinesMap = jedis.hgetAll(KEY);

            for (Map.Entry<String, String> entry : machinesMap.entrySet()) {
                    childRenList.add(entry.getKey());
            }
            return childRenList;
        } catch (Exception e) {
            logger.error("从Redis中获取所有的应用列表发生异常：" + e.getMessage(), e);
        }
        return childRenList;
    }

    @Override
    public AppInfo getDetailApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        try (Jedis jedis = jedisPool.getResource()){
            String appInfoStr = jedis.hget(KEY , app);
            AppInfo appInfo = JSONObject.parseObject(appInfoStr, AppInfo.class);
            return appInfo;
        }catch (Exception e){
            logger.info("从Redis中获取应用列表发生异常：" + e.getMessage(), e);
        }
        return null;
    }
    /**
     * 获取所有应用程序的简要信息集合
     * 该方法返回一个包含所有应用程序信息的Set集合
     *
     * @return 包含所有AppInfo的Set集合，通过HashSet实现
     */
    @Override
    public Set<AppInfo> getBriefApps() {
        final Set<AppInfo> appSet = new HashSet<AppInfo>();
        try (Jedis jedis = jedisPool.getResource()){
            // 根据APP和RESOURCE查询时间范围内的数据
            Map<String, String> machinesMap = jedis.hgetAll(KEY);
            for (Map.Entry<String, String> entry : machinesMap.entrySet()) {
                AppInfo appInfo = JSONObject.parseObject(entry.getValue(), AppInfo.class);
                appSet.add(appInfo);
            }
            return appSet;
        } catch (Exception e) {
            logger.error("从Redis中获取所有的应用信息发生异常：" + e.getMessage(), e);
        }
        return appSet;
    }



    @Override
    public void removeApp(String app) {
        AssertUtil.assertNotBlank(app, "app name cannot be blank");
        try (Jedis jedis = jedisPool.getResource()){
            jedis.hdel(KEY , app );
        } catch (Exception e) {
            logger.error("从Redis中删除app：" + app + "的信息发生异常：" + e.getMessage(), e);
        }
    }


    /**
     * 将app机器列表保存到ZK中
     *
     * @param app
     * @param appInfo
     */
    private boolean saveData(String app, AppInfo appInfo) {
        try (Jedis jedis = jedisPool.getResource()){
            long result = jedis.hset(KEY, app, JSONObject.toJSONString(appInfo));
            logger.info("保存app:" + app + "的机器列表结果：" + result);
            return true;
        }catch (Exception e){
            logger.error("保存app:" + app + "的机器列表发生异常：" + e.getMessage(), e);
        }
        return false;
    }
}
