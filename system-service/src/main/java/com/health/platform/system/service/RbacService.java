package com.health.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.core.constant.RedisKeyConstants;
import com.health.platform.system.entity.SysMenu;
import com.health.platform.system.entity.SysPermission;
import com.health.platform.system.entity.SysRole;
import com.health.platform.system.entity.SysRoleMenu;
import com.health.platform.system.entity.SysRolePermission;
import com.health.platform.system.entity.SysUserRole;
import com.health.platform.system.mapper.SysMenuMapper;
import com.health.platform.system.mapper.SysPermissionMapper;
import com.health.platform.system.mapper.SysRoleMapper;
import com.health.platform.system.mapper.SysRoleMenuMapper;
import com.health.platform.system.mapper.SysRolePermissionMapper;
import com.health.platform.system.mapper.SysUserRoleMapper;
import com.health.platform.system.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbacService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRolePermissionMapper sysRolePermissionMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final SysMenuMapper sysMenuMapper;
    private final SysRoleMapper sysRoleMapper;
    private final StringRedisTemplate redisTemplate;

    public Set<String> getUserPermissionCodes(Long userId) {
        List<Long> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return Set.of();
        }
        List<Long> permissionIds = sysRolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds))
                .stream().map(SysRolePermission::getPermissionId).distinct().toList();
        if (permissionIds.isEmpty()) {
            return Set.of();
        }
        return sysPermissionMapper.selectBatchIds(permissionIds).stream()
                .filter(p -> "ENABLED".equals(p.getStatus()))
                .map(SysPermission::getCode)
                .collect(Collectors.toSet());
    }

    public List<SysMenuVO> getUserMenuTree(Long userId) {
        List<Long> roleIds = getUserRoleIds(userId);
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = sysRoleMenuMapper.selectList(
                        new LambdaQueryWrapper<SysRoleMenu>().in(SysRoleMenu::getRoleId, roleIds))
                .stream().map(SysRoleMenu::getMenuId).distinct().toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<SysMenu> menus = sysMenuMapper.selectBatchIds(menuIds).stream()
                .filter(m -> "ENABLED".equals(m.getStatus()))
                .sorted(Comparator.comparing(SysMenu::getSort))
                .toList();
        return buildTree(menus);
    }

    public List<Long> getUserRoleIds(Long userId) {
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).distinct().toList();
    }

    public Map<Long, String> getRoleNames(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Map.of();
        }
        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .collect(Collectors.toMap(SysRole::getId, SysRole::getName));
    }

    public List<Long> getUserIdsByRole(Long roleId) {
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getRoleId, roleId))
                .stream().map(SysUserRole::getUserId).distinct().toList();
    }

    /** 角色/用户权限变化后清除 Redis 权限缓存，立即生效 */
    public void evictUserPermissionCache(Long... userIds) {
        for (Long userId : userIds) {
            redisTemplate.delete(RedisKeyConstants.USER_PERMISSIONS + userId);
        }
    }

    public void evictRolePermissionCache(Long roleId) {
        List<Long> userIds = getUserIdsByRole(roleId);
        if (!userIds.isEmpty()) {
            evictUserPermissionCache(userIds.toArray(new Long[0]));
        }
    }

    public static List<SysMenuVO> buildTree(List<SysMenu> menus) {
        Map<Long, List<SysMenu>> byParent = menus.stream()
                .collect(Collectors.groupingBy(SysMenu::getParentId));
        List<SysMenuVO> roots = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == null || menu.getParentId() == 0L
                    || !byParent.containsKey(menu.getParentId())) {
                roots.add(toVO(menu, byParent));
            }
        }
        roots.sort(Comparator.comparing(SysMenuVO::getSort));
        return roots;
    }

    private static SysMenuVO toVO(SysMenu menu, Map<Long, List<SysMenu>> byParent) {
        SysMenuVO vo = new SysMenuVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setName(menu.getName());
        vo.setPath(menu.getPath());
        vo.setComponent(menu.getComponent());
        vo.setIcon(menu.getIcon());
        vo.setSort(menu.getSort());
        vo.setStatus(menu.getStatus());
        List<SysMenu> children = byParent.get(menu.getId());
        if (children != null) {
            List<SysMenuVO> childVOs = children.stream()
                    .sorted(Comparator.comparing(SysMenu::getSort))
                    .map(m -> toVO(m, byParent))
                    .collect(Collectors.toList());
            vo.setChildren(childVOs);
        }
        return vo;
    }
}
