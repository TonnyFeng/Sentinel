package com.alibaba.csp.sentinel.dashboard.rule.nacos;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author fw
 * @version 1.0
 * @title NacosPropertiesConfiguration
 * @Description
 * @create 2025/8/29 16:32
 */
@ConfigurationProperties(prefix = "sentinel.nacos")
@Configuration
public class NacosPropertiesConfiguration {
    private String serverAddr;
    private String namespace;
    private String groupId;
    private String username;
    private String password;

    public String getServerAddr() {
        return serverAddr;
    }

    public void setServerAddr(String serverAddr) {
        this.serverAddr = serverAddr;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
