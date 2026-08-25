package com.health.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.system.dto.SysRoleDTO;
import com.health.platform.system.entity.SysRole;
import com.health.platform.system.entity.SysRoleMenu;
import com.health.platform.system.entity.SysRolePermission;
import com.health.platform.system.mapper.SysRoleMapper;
import com.health.platform.system.mapper.SysRoleMenuMapper;
import com.health.platform.system.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SysRoleService {

    private final SysRoleMapper sysRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final RbacService rbacService;

    public PageResult<SysRole> page(PageQuery query, String keyword) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(StringUtils.hasText(keyword), SysRole::getName, keyword)
                .orderByAsc(SysRole::getId);
        Page<SysRole> page = sysRoleMapper.selectPage(Page.of(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), page.getRecords());
    }

    public List<SysRole> listAll() {
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getId));
    }

    public SysRole getById(Long id) {
        return sysRoleMapper.selectById(id);
    }

    public SysRole create(SysRoleDTO dto) {
        assertCodeUnique(dto.getCode(), null);
        SysRole role = new SysRole();
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setRemark(dto.getRemark());
        role.setStatus("ENABLED");
        sysRoleMapper.insert(role);
        return role;
    }

    public void update(Long id, SysRoleDTO dto) {
        SysRole existing = sysRoleMapper.selectById(id);
        if (existing == null) {
            throw new BizException("角色不存在");
        }
        assertCodeUnique(dto.getCode(), id);
        SysRole update = new SysRole();
        update.setId(id);
        update.setCode(dto.getCode());
        update.setName(dto.getName());
        update.setRemark(dto.getRemark());
        sysRoleMapper.updateById(update);
    }

    public List<Long> getPermissionIds(Long roleId) {
        return sysRolePermissionMapper.selectList(new LambdaQueryWrapper<SysRolePermission>()
                        .eq(SysRolePermission::getRoleId, roleId))
                .stream().map(SysRolePermission::getPermissionId).toList();
    }

    public List<Long> getMenuIds(Long roleId) {
        return sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId))
                .stream().map(SysRoleMenu::getMenuId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        assertRoleExists(roleId);
        sysRolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getRoleId, roleId));
        for (Long permissionId : permissionIds) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            sysRolePermissionMapper.insert(relation);
        }
        rbacService.evictRolePermissionCache(roleId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        assertRoleExists(roleId);
        sysRoleMenuMapper.delete(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId));
        for (Long menuId : menuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            sysRoleMenuMapper.insert(relation);
        }
    }

    private void assertRoleExists(Long roleId) {
        if (sysRoleMapper.selectById(roleId) == null) {
            throw new BizException("角色不存在");
        }
    }

    private void assertCodeUnique(String code, Long excludeId) {
        SysRole existing = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getCode, code));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BizException("角色编码已存在: " + code);
        }
    }
}
