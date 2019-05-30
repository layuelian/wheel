package com.thomas.elasticsearch.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Author: lf
 * @Date: 2019/5/23 16:24
 */
@Component
@PropertySource("classpath:es-config.properties")
@Data
public class ESConfig {
    @Value("${es-cluster.name}")
    private String clusterName;
    @Value("${es.host.ip}")
    private String ip;
    @Value("${es.host.port}")
    private int port;
}
