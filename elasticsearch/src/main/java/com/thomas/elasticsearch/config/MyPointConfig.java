package com.thomas.elasticsearch.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @Author: lf
 * @Date: 2019/5/23 16:30
 */
@Component
@PropertySource("classpath:my-point.properties")
@Data
public class MyPointConfig {
    @Value("${my.point.lon}")
    private double lon;
    @Value("${my.point.lat}")
    private double lat;
}
