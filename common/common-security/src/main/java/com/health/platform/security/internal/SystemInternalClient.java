package com.health.platform.security.internal;

import com.health.platform.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Set;

/**
 * system-service 内部接口（/internal/**，需携带 X-Internal-Token）。
 * 使用方需开启：@EnableFeignClients(basePackageClasses = SystemInternalClient.class)
 */
@FeignClient(name = "system-service", contextId = "systemInternalClient", path = "/internal")
public interface SystemInternalClient {

    @GetMapping("/users/{username}/credentials")
    Result<UserCredentialsDTO> getUserCredentials(@PathVariable("username") String username);

    @GetMapping("/users/id/{id}/credentials")
    Result<UserCredentialsDTO> getUserCredentialsById(@PathVariable("id") Long id);

    @GetMapping("/users/{id}/permissions")
    Result<Set<String>> getUserPermissions(@PathVariable("id") Long id);

    @PostMapping("/users/{id}/login-success")
    Result<Void> markLoginSuccess(@PathVariable("id") Long id);
}
