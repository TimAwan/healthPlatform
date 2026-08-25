package com.health.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.system.entity.OperationLog;
import com.health.platform.system.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;

    public void save(OperationLog log) {
        operationLogMapper.insert(log);
    }

    public PageResult<OperationLog> page(PageQuery query, Long operatorId, String targetType,
                                         String targetId, LocalDateTime beginTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<OperationLog>()
                .eq(operatorId != null, OperationLog::getOperatorId, operatorId)
                .eq(StringUtils.hasText(targetType), OperationLog::getTargetType, targetType)
                .eq(StringUtils.hasText(targetId), OperationLog::getTargetId, targetId)
                .ge(beginTime != null, OperationLog::getCreatedAt, beginTime)
                .le(endTime != null, OperationLog::getCreatedAt, endTime)
                .orderByDesc(OperationLog::getId);
        Page<OperationLog> page = operationLogMapper.selectPage(Page.of(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), page.getRecords());
    }
}
