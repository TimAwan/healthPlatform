package com.health.platform.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.platform.system.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
