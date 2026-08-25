package com.health.platform.system.controller.internal;

import com.health.platform.core.result.Result;
import com.health.platform.system.entity.OperationLog;
import com.health.platform.system.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/operation-logs")
@RequiredArgsConstructor
public class OperationLogInternalController {

    private final OperationLogService operationLogService;

    @PostMapping
    public Result<Void> save(@RequestBody OperationLog log) {
        operationLogService.save(log);
        return Result.ok();
    }
}
