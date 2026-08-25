package com.health.platform.system.controller;

import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.core.result.Result;
import com.health.platform.security.permission.RequirePermission;
import com.health.platform.system.controller.internal.OperationLogInternalController;
import com.health.platform.system.entity.OperationLog;
import com.health.platform.system.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/system/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @GetMapping
    @RequirePermission("LOG_VIEW")
    public Result<PageResult<OperationLog>> page(PageQuery query,
                                                 @RequestParam(required = false) Long operatorId,
                                                 @RequestParam(required = false) String targetType,
                                                 @RequestParam(required = false) String targetId,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime beginTime,
                                                 @RequestParam(required = false)
                                                 @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        return Result.ok(operationLogService.page(query, operatorId, targetType, targetId, beginTime, endTime));
    }
}
