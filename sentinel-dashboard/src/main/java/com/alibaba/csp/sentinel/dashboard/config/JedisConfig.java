package com.alibaba.csp.sentinel.dashboard.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;
import java.util.UUID;

@Configuration
public class JedisConfig {

    /**
     * influxdb连接URL
     */
    @Value("${spring.redis.host:''}")
    private String redisHost;

    /**
     * influxdb的访问token权限
     */
    @Value("${spring.redis.port:''}")
    private Integer redisPort;
    @Bean
    @Primary
    public JedisPool jedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setJmxNamePrefix("jedisPool-" + UUID.randomUUID());
        poolConfig.setJmxEnabled(false); // 显式开启
        poolConfig.setMaxWait(Duration.ofMillis(2000));
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(2);
        poolConfig.setTestOnBorrow(true);  // 借用时检查连接有效性
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        JedisPool jedisPool = new JedisPool(poolConfig, redisHost, redisPort);
        return jedisPool;
    }
}
