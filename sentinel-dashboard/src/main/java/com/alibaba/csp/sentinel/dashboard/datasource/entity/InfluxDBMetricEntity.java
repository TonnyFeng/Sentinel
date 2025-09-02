package com.alibaba.csp.sentinel.dashboard.datasource.entity;


import com.influxdb.annotations.Column;
import com.influxdb.annotations.Measurement;
import lombok.Data;

import java.time.Instant;

/**
 * <p>Description: </p>
 * @author Mirson
 */
@Measurement(name = "sentinelInfo")
public class InfluxDBMetricEntity {

    @Column(name = "time")
    private Instant time;
    @Column(name = "gmtCreate", tag = true)
    private long gmtCreate;
    @Column(name = "gmtModified")
    private long gmtModified;

    /**
     * 监控信息的时间戳
     */
    @Column(name = "app", tag = true)
    private String app;
    @Column(name = "resource", tag = true)
    private String resource;
    @Column(name = "timestamp", tag = true)
    private long timestamp;
    @Column(name = "passQps", tag = true)
    private long passQps;//通过qps
    @Column(name = "successQps", tag = true)
    private long successQps;//成功qps
    @Column(name = "blockQps", tag = true)
    private long blockQps;//限流qps
    @Column(name = "_exceptionQps")
    private long exceptionQps;//异常qps

    /**
     * 所有successQps的rt的和
     */
    @Column(name = "rt", tag = true)
    private double rt;

    /**
     * 本次聚合的总条数
     */
    @Column(name = "count", tag = true)
    private int count;
    @Column(name = "resourceCode", tag = true)
    private int resourceCode;

    public Instant getTime() {
        return time;
    }

    public void setTime(Instant time) {
        this.time = time;
    }

    public long getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(long gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public long getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(long gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getPassQps() {
        return passQps;
    }

    public void setPassQps(long passQps) {
        this.passQps = passQps;
    }

    public long getSuccessQps() {
        return successQps;
    }

    public void setSuccessQps(long successQps) {
        this.successQps = successQps;
    }

    public long getBlockQps() {
        return blockQps;
    }

    public void setBlockQps(long blockQps) {
        this.blockQps = blockQps;
    }

    public long getExceptionQps() {
        return exceptionQps;
    }

    public void setExceptionQps(long exceptionQps) {
        this.exceptionQps = exceptionQps;
    }

    public double getRt() {
        return rt;
    }

    public void setRt(double rt) {
        this.rt = rt;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(int resourceCode) {
        this.resourceCode = resourceCode;
    }
}
