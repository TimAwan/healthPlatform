package com.health.platform.log;

import com.health.platform.core.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 操作日志接收端（system-service /internal/operation-logs）。
 * 使用方需开启 Feign 扫描：@EnableFeignClients(basePackages = "com.health.platform")
 */
@FeignClient(name = "system-service", contextId = "operationLogClient", path = "/internal/operation-logs")
public interface OperationLogClient {

    @PostMapping
    Result<Void> save(@RequestBody OperationLogEvent event);
}
