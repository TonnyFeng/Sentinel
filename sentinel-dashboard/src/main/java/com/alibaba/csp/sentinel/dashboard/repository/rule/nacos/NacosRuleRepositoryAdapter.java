package com.alibaba.csp.sentinel.dashboard.repository.rule.nacos;


import com.alibaba.csp.sentinel.dashboard.discovery.MachineInfo;
import com.alibaba.csp.sentinel.dashboard.repository.rule.RuleRepository;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用 Nacos Rule Repository Adapter
 * T: 规则实体类型
 */
public abstract class NacosRuleRepositoryAdapter<T> implements RuleRepository<T, Long> {

    @Autowired
    private ConfigService configService;

    private final Map<Long, T> localCache = new ConcurrentHashMap<>();

    private static final String GROUP_ID = "SENTINEL_GROUP";

    /** 子类需要实现：获取规则对应的 appName */
    protected abstract String getApp(T rule);

    /** 子类需要实现：生成 dataId (不同规则类型区分开) */
    protected abstract String getDataId(String app);

    /** 子类实现：根据规则获取 MachineInfo */
    protected abstract MachineInfo getMachine(T rule); // Object 可换为 MachineInfo
    @Override
    public T save(T entity) {
        Long id = extractId(entity);
        if (id == null) {
            id = System.currentTimeMillis();
            assignId(entity, id);
        }
        localCache.put(id, entity);
        persistToNacos(getApp(entity));
        return entity;
    }

    @Override
    public List<T> saveAll(List<T> rules) {
        rules.forEach(rule -> {
            Long id = extractId(rule);
            if (id == null) {
                id = System.currentTimeMillis();
                assignId(rule, id);
            }
            localCache.put(id, rule);
        });
        if (!rules.isEmpty()) {
            persistToNacos(getApp(rules.iterator().next()));
        }
        return new ArrayList<>(rules);
    }

    @Override
    public T delete(Long id) {
        T entity = localCache.remove(id);
        if (entity != null) {
            persistToNacos(getApp(entity));
        }
        return entity;
    }

    @Override
    public List<T> findAllByApp(String appName) {
        try {
            String rules = configService.getConfig(getDataId(appName), GROUP_ID, 3000);
            if (rules != null && !rules.isEmpty()) {
                List<T> list = JSON.parseObject(rules, new TypeReference<List<T>>() {});
                list.forEach(rule -> localCache.put(extractId(rule), rule));
                return list;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>(localCache.values());
    }

    @Override
    public T findById(Long id) {
        return localCache.get(id);
    }

    private void persistToNacos(String app) {
        try {
            List<T> rules = new ArrayList<>(localCache.values());
            configService.publishConfig(
                    getDataId(app),
                    GROUP_ID,
                    toPrettyFormat(JSON.toJSONString(rules)),ConfigType.JSON.getType()
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 从实体提取 ID (反射或接口约束，这里假设是 getId 方法) */
    private Long extractId(T entity) {
        try {
            return (Long) entity.getClass().getMethod("getId").invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<T> findAllByMachine(MachineInfo machineInfo) {
        // 遍历缓存，匹配 MachineInfo
        List<T> result = new ArrayList<>();
        for (T rule : localCache.values()) {
            if (Objects.equals(getMachine(rule), machineInfo)) {
                result.add(rule);
            }
        }
        return result;
    }

    /** 给实体设置 ID (假设有 setId 方法) */
    private void assignId(T entity, Long id) {
        try {
            entity.getClass().getMethod("setId", Long.class).invoke(entity, id);
        } catch (Exception ignored) {
        }
    }

    protected T preProcess(T entity) {
        return entity;
    }

    abstract protected long nextId(T entity);

    /**
     * 格式化输出JSON字符串
     *
     * @return 格式化后的JSON字符串
     */
    private String toPrettyFormat(String json) {
        JsonArray asJsonArray = JsonParser.parseString(json).getAsJsonArray();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(asJsonArray);

    }
}
