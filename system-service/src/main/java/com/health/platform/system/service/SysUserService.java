package com.health.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.system.entity.SysUser;
import com.health.platform.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class SysUserService {

    private final SysUserMapper sysUserMapper;

    public SysUserService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public SysUser getByUsername(String username) {
        return sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
    }

    public SysUser getById(Long id) {
        return sysUserMapper.selectById(id);
    }

    public long count() {
        return sysUserMapper.selectCount(null);
    }

    public void create(SysUser user) {
        sysUserMapper.insert(user);
    }

    public void markLoginSuccess(Long userId) {
        SysUser user = new SysUser();
        user.setId(userId);
        user.setLastLoginAt(LocalDateTime.now());
        sysUserMapper.updateById(user);
    }
}
