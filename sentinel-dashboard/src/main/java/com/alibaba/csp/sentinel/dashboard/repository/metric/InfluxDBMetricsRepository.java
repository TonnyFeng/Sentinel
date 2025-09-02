package com.alibaba.csp.sentinel.dashboard.repository.metric;

import com.alibaba.csp.sentinel.dashboard.config.InfluxDBConfig;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.InfluxDBMetricEntity;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.MetricEntity;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.fastjson.JSONObject;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component("influxDBMetricsRepository")
@Primary
public class InfluxDBMetricsRepository implements MetricsRepository<MetricEntity> {

    @Autowired
    private InfluxDBConfig influxDBConfig;

    @Autowired
    public WriteApiBlocking writeApiBlocking;

    @Autowired
    public InfluxDBClient influxDBClient;

    @Autowired
    public QueryApi queryApi;


    /**
     * 保存数据
     * @param metric metric data to save
     */
    @Override
    public synchronized void save(MetricEntity metric) {

        try {
            // 记录数据
            InfluxDBMetricEntity entity = new InfluxDBMetricEntity();
            BeanUtils.copyProperties(metric, entity, new String[]{"gmtCreate", "gmtModified", "timestamp"});
            entity.setResource(metric.getResource());
            entity.setGmtCreate(metric.getGmtCreate().getTime());
            entity.setGmtModified(metric.getGmtModified().getTime());
            entity.setTimestamp(metric.getTimestamp().getTime());
            entity.setPassQps(metric.getPassQps());
            entity.setSuccessQps(metric.getSuccessQps());
            entity.setBlockQps(metric.getBlockQps());
            entity.setExceptionQps(metric.getExceptionQps());
            entity.setRt(metric.getRt());
            entity.setCount(metric.getCount());
            entity.setResourceCode(metric.getResourceCode());
            entity.setTime(Instant.now());
            writeApiBlocking.writeMeasurement(WritePrecision.MS, entity);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /**
     * 批量保存
     * @param metrics metrics to save
     */
    @Override
    public synchronized void saveAll(Iterable<MetricEntity> metrics) {

        if (metrics == null) {
            return;
        }
        metrics.forEach(metric -> {

            save(metric);

        });
    }

    /**
     * 根据时间范围查询数据
     * @param app       application name for Sentinel
     * @param resource  resource name
     * @param startTime start timestamp
     * @param endTime   end timestamp
     * @return
     */
    @Override
    public synchronized List<MetricEntity> queryByAppAndResourceBetween(String app, String resource, long startTime, long endTime) {

        log.info("queryByAppAndResourceBetween app:{}, resource:{}, startTime:{}, endTime:{}", app, resource, startTime, endTime);
        List<MetricEntity> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        Instant start = Instant.ofEpochMilli(startTime);
        Instant end   = Instant.ofEpochMilli(endTime);
        // 根据APP和RESOURCE查询时间范围内的数据
        String flux = String.format("from(bucket:\"%s\") |> range(start: %s, stop: %s)"
                        + " |> filter(fn: (r) => (r[\"_measurement\"] == \"sentinelInfo\" and r[\"app\"] == \"%s\") and r[\"resource\"] == \"%s\")",
                influxDBConfig.getInfluxBucket(), DateTimeFormatter.ISO_INSTANT.format(start),
                DateTimeFormatter.ISO_INSTANT.format(end), app, resource);

        List<FluxTable> tables = queryApi.query(flux);
        log.info(" queryByAppAndResourceBetween tables : {}", JSONObject.toJSONString( tables ));
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                MetricEntity metricEntity = MetricEntity.copyOf(fluxRecord);
                results.add(metricEntity);
            }
        }
        log.info(" queryByAppAndResourceBetween results : {}", JSONObject.toJSONString( results ));
        return results;
    }


    @Override
    public synchronized List<String> listResourcesOfApp(String app) {

        List<String> results = new ArrayList<>();
        if (StringUtil.isBlank(app)) {
            return results;
        }
        //查询最近5分钟的指标(实时数据)
        String command = String.format("from(bucket:\"%s\") |> range(start: -5m)"
                        + " |> filter(fn: (r) => (r[\"_measurement\"] == \"sentinelInfo\" and r[\"app\"] == \"%s\") )",
                influxDBConfig.getInfluxBucket(), app);

        List<MetricEntity> influxResults = new ArrayList<>();
        log.info("command:{}", command);
        // 查询
        List<FluxTable> tables = queryApi.query(command);
        log.info(" tables : {}", JSONObject.toJSONString( tables ));
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                MetricEntity metricEntity = MetricEntity.copyOf(fluxRecord);
                influxResults.add(metricEntity);
            }
        }
        log.info(" influxResults : {}", JSONObject.toJSONString( influxResults ));
        try {

            if (CollectionUtils.isEmpty(influxResults)) {
                return results;
            }
            Map<String, MetricEntity> resourceCount = new HashMap<>(32);
            for (MetricEntity metricEntity : influxResults) {
                String resource = metricEntity.getResource();
                if (resourceCount.containsKey(resource)) {
                    // 累加统计
                    MetricEntity oldEntity = resourceCount.get(resource);
                    oldEntity.addPassQps(metricEntity.getPassQps());
                    oldEntity.addRtAndSuccessQps(metricEntity.getRt(), metricEntity.getSuccessQps());
                    oldEntity.addBlockQps(metricEntity.getBlockQps());
                    oldEntity.addExceptionQps(metricEntity.getExceptionQps());
                    oldEntity.addCount(1);
                } else {

                    resourceCount.put(resource, metricEntity);
                }
            }
            log.info(" resourceCount : {}", JSONObject.toJSONString( resourceCount ));
            //排序
            results = resourceCount.entrySet()
                    .stream()
                    .sorted((o1, o2) -> {
                        MetricEntity e1 = o1.getValue();
                        MetricEntity e2 = o2.getValue();
                        int t = e2.getBlockQps().compareTo(e1.getBlockQps());
                        if (t != 0) {

                            return t;
                        }
                        return e2.getPassQps().compareTo(e1.getPassQps());
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            log.info(" results : {}", JSONObject.toJSONString( results ));
        } catch (Exception e) {

            e.printStackTrace();
        }
        return results;
    }
}
