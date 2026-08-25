package com.health.platform.system.service;

import com.health.platform.system.entity.SysMenu;
import com.health.platform.system.entity.SysPermission;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RbacServiceTest {

    @Mock
    private SysUserRoleMapper sysUserRoleMapper;
    @Mock
    private SysRolePermissionMapper sysRolePermissionMapper;
    @Mock
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Mock
    private SysPermissionMapper sysPermissionMapper;
    @Mock
    private SysMenuMapper sysMenuMapper;
    @Mock
    private SysRoleMapper sysRoleMapper;
    @Mock
    private StringRedisTemplate redisTemplate;

    private RbacService rbacService() {
        return new RbacService(sysUserRoleMapper, sysRolePermissionMapper, sysRoleMenuMapper,
                sysPermissionMapper, sysMenuMapper, sysRoleMapper, redisTemplate);
    }

    private SysUserRole userRole(long userId, long roleId) {
        SysUserRole relation = new SysUserRole();
        relation.setUserId(userId);
        relation.setRoleId(roleId);
        return relation;
    }

    private SysRolePermission rolePermission(long roleId, long permissionId) {
        SysRolePermission relation = new SysRolePermission();
        relation.setRoleId(roleId);
        relation.setPermissionId(permissionId);
        return relation;
    }

    private SysPermission permission(long id, String code, String status) {
        SysPermission p = new SysPermission();
        p.setId(id);
        p.setCode(code);
        p.setStatus(status);
        return p;
    }

    @Test
    void getUserPermissionCodesShouldJoinThroughRolesAndFilterEnabled() {
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of(userRole(1L, 2L)));
        when(sysRolePermissionMapper.selectList(any()))
                .thenReturn(List.of(rolePermission(2L, 100L), rolePermission(2L, 101L)));
        when(sysPermissionMapper.selectBatchIds(anyList())).thenReturn(List.of(
                permission(100L, "DOCTOR_VIEW", "ENABLED"),
                permission(101L, "DOCTOR_CREATE", "DISABLED")));

        Set<String> codes = rbacService().getUserPermissionCodes(1L);

        assertEquals(Set.of("DOCTOR_VIEW"), codes);
    }

    @Test
    void getUserPermissionCodesWithoutRoleShouldReturnEmpty() {
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of());
        assertTrue(rbacService().getUserPermissionCodes(1L).isEmpty());
    }

    @Test
    void evictUserPermissionCacheShouldDeleteRedisKey() {
        rbacService().evictUserPermissionCache(7L);
        verify(redisTemplate).delete("health:perm:7");
    }

    @Test
    void buildTreeShouldNestChildrenSorted() {
        SysMenu root = new SysMenu();
        root.setId(10L);
        root.setParentId(0L);
        root.setName("医生管理");
        root.setSort(1);
        SysMenu childA = new SysMenu();
        childA.setId(12L);
        childA.setParentId(10L);
        childA.setName("医生审核");
        childA.setSort(2);
        SysMenu childB = new SysMenu();
        childB.setId(11L);
        childB.setParentId(10L);
        childB.setName("医生列表");
        childB.setSort(1);

        List<SysMenuVO> tree = RbacService.buildTree(List.of(root, childA, childB));

        assertEquals(1, tree.size());
        assertEquals(10L, tree.get(0).getId());
        assertEquals(2, tree.get(0).getChildren().size());
        assertEquals(11L, tree.get(0).getChildren().get(0).getId());
        assertEquals(12L, tree.get(0).getChildren().get(1).getId());
    }

    @Test
    void getUserMenuTreeShouldReturnEmptyWhenNoMenuAssigned() {
        when(sysUserRoleMapper.selectList(any())).thenReturn(List.of(userRole(1L, 2L)));
        SysRoleMenu relation = new SysRoleMenu();
        relation.setRoleId(2L);
        relation.setMenuId(10L);
        when(sysRoleMenuMapper.selectList(any())).thenReturn(List.of(relation));
        when(sysMenuMapper.selectBatchIds(anyList())).thenReturn(List.of());

        List<SysMenuVO> tree = rbacService().getUserMenuTree(1L);
        assertTrue(tree.isEmpty());
        assertFalse(tree.contains(null));
    }
}
