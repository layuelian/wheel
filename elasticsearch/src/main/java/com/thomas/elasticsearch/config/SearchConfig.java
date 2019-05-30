package com.thomas.elasticsearch.config;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @Author: lf
 * @Date: 2019/5/23 16:43
 */
@Configuration
public class SearchConfig {
    @Autowired
    ESConfig esConfig;
    @Bean
    public TransportClient client() throws UnknownHostException {
        TransportAddress node = new TransportAddress(InetAddress.getByName(esConfig.getIp()), esConfig.getPort());

        Settings settings = Settings.builder().put("cluster.name", esConfig.getClusterName()).build();

        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(node);
        return client;
    }
}
