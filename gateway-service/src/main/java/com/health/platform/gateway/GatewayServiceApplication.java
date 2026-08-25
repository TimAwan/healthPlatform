package com.health.platform.gateway;

import com.health.platform.core.jwt.JwtProperties;
import com.health.platform.core.jwt.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties({JwtProperties.class, GatewaySecurityProperties.class})
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }

    @Bean
    public JwtUtils jwtUtils(JwtProperties properties) {
        return new JwtUtils(properties);
    }
}
