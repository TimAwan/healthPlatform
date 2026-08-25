package com.health.platform.security.config;

import com.health.platform.security.context.UserContextFilter;
import com.health.platform.security.internal.InternalFeignTokenInterceptor;
import com.health.platform.security.internal.InternalTokenFilter;
import com.health.platform.core.jwt.JwtProperties;
import com.health.platform.core.jwt.JwtUtils;
import com.health.platform.security.permission.PermissionInterceptor;
import com.health.platform.security.permission.PermissionProvider;
import com.health.platform.security.permission.RedisPermissionProvider;
import com.health.platform.security.internal.SystemInternalClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(JwtProperties.class)
public class HealthSecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public JwtUtils jwtUtils(JwtProperties properties) {
        return new JwtUtils(properties);
    }

    @Bean
    @ConditionalOnMissingBean(PasswordEncoder.class)
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public FilterRegistrationBean<UserContextFilter> userContextFilterRegistration() {
        FilterRegistrationBean<UserContextFilter> registration = new FilterRegistrationBean<>(new UserContextFilter());
        registration.setOrder(Integer.MIN_VALUE + 20);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    public FilterRegistrationBean<InternalTokenFilter> internalTokenFilterRegistration(
            @Value("${INTERNAL_TOKEN:dev-internal-token}") String internalToken) {
        FilterRegistrationBean<InternalTokenFilter> registration =
                new FilterRegistrationBean<>(new InternalTokenFilter(internalToken));
        registration.setOrder(Integer.MIN_VALUE + 30);
        registration.addUrlPatterns("/internal/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(PermissionProvider.class)
    public PermissionProvider permissionProvider(
            org.springframework.data.redis.core.StringRedisTemplate redisTemplate,
            ObjectProvider<SystemInternalClient> systemInternalClient) {
        return new RedisPermissionProvider(redisTemplate, systemInternalClient.getIfAvailable());
    }

    @Bean
    public WebMvcConfigurer permissionInterceptorConfigurer(PermissionProvider permissionProvider) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new PermissionInterceptor(permissionProvider))
                        .addPathPatterns("/api/**");
            }
        };
    }

    @Bean
    public InternalFeignTokenInterceptor internalFeignTokenInterceptor(
            @Value("${INTERNAL_TOKEN:dev-internal-token}") String internalToken) {
        return new InternalFeignTokenInterceptor(internalToken);
    }
}
