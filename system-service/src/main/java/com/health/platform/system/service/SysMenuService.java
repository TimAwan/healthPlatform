package com.health.platform.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.core.exception.BizException;
import com.health.platform.system.entity.SysMenu;
import com.health.platform.system.mapper.SysMenuMapper;
import com.health.platform.system.vo.SysMenuVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMenuService {

    private final SysMenuMapper sysMenuMapper;

    public List<SysMenuVO> tree() {
        List<SysMenu> menus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .orderByAsc(SysMenu::getSort));
        return RbacService.buildTree(menus);
    }

    public void create(SysMenu menu) {
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        menu.setStatus("ENABLED");
        sysMenuMapper.insert(menu);
    }

    public void update(SysMenu menu) {
        if (sysMenuMapper.selectById(menu.getId()) == null) {
            throw new BizException("菜单不存在");
        }
        if (menu.getId().equals(menu.getParentId())) {
            throw new BizException("父菜单不能是自己");
        }
        sysMenuMapper.updateById(menu);
    }

    public List<SysMenu> listByPathPrefix(String keyword) {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .like(StringUtils.hasText(keyword), SysMenu::getName, keyword)
                .orderByAsc(SysMenu::getSort));
    }

    public List<SysMenu> childrenOf(Long menuId) {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getParentId, menuId));
    }

    public Comparator<SysMenu> bySort() {
        return Comparator.comparing(SysMenu::getSort);
    }
}
