package com.health.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.system.dto.SysUserDTO;
import com.health.platform.system.dto.SysUserUpdateDTO;
import com.health.platform.system.entity.SysUser;
import com.health.platform.system.entity.SysUserRole;
import com.health.platform.system.mapper.SysUserMapper;
import com.health.platform.system.mapper.SysUserRoleMapper;
import com.health.platform.system.vo.SysUserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SysUserService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final RbacService rbacService;
    private final PasswordEncoder passwordEncoder;

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

    public PageResult<SysUserVO> page(PageQuery query, String keyword, String status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .and(StringUtils.hasText(keyword), w -> w
                        .like(SysUser::getUsername, keyword)
                        .or().like(SysUser::getRealName, keyword))
                .eq(StringUtils.hasText(status), SysUser::getStatus, status)
                .orderByDesc(SysUser::getId);
        Page<SysUser> page = sysUserMapper.selectPage(Page.of(query.getPage(), query.getSize()), wrapper);
        List<SysUserVO> voList = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), voList);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(SysUserDTO dto) {
        if (getByUsername(dto.getUsername()) != null) {
            throw new BizException("用户名已存在: " + dto.getUsername());
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setStatus("ENABLED");
        sysUserMapper.insert(user);
        assignRoles(user.getId(), dto.getRoleIds() == null ? List.of() : dto.getRoleIds());
        return user.getId();
    }

    public void update(Long id, SysUserUpdateDTO dto) {
        assertExists(id);
        SysUser update = new SysUser();
        update.setId(id);
        update.setRealName(dto.getRealName());
        update.setPhone(dto.getPhone());
        sysUserMapper.updateById(update);
    }

    public void changeStatus(Long id, boolean enabled) {
        assertExists(id);
        if (id == 1L && !enabled) {
            throw new BizException("初始管理员不允许停用");
        }
        SysUser update = new SysUser();
        update.setId(id);
        update.setStatus(enabled ? "ENABLED" : "DISABLED");
        sysUserMapper.updateById(update);
        rbacService.evictUserPermissionCache(id);
    }

    public void resetPassword(Long id, String newPassword) {
        assertExists(id);
        SysUser update = new SysUser();
        update.setId(id);
        update.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(update);
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        assertExists(userId);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            sysUserRoleMapper.insert(relation);
        }
        rbacService.evictUserPermissionCache(userId);
    }

    private void assertExists(Long id) {
        if (sysUserMapper.selectById(id) == null) {
            throw new BizException("用户不存在");
        }
    }

    private SysUserVO toVO(SysUser user) {
        SysUserVO vo = new SysUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setPhone(user.getPhone());
        vo.setStatus(user.getStatus());
        vo.setLastLoginAt(user.getLastLoginAt());
        vo.setCreatedAt(user.getCreatedAt());
        List<Long> roleIds = rbacService.getUserRoleIds(user.getId());
        vo.setRoleIds(roleIds);
        if (!roleIds.isEmpty()) {
            Map<Long, String> roleNames = rbacService.getRoleNames(roleIds);
            vo.setRoleNames(roleIds.stream().map(roleNames::get).toList());
        }
        return vo;
    }
}
