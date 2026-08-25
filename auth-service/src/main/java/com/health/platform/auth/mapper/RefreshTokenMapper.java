package com.health.platform.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.platform.auth.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RefreshTokenMapper extends BaseMapper<RefreshToken> {
}
