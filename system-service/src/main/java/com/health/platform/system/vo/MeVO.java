package com.health.platform.system.vo;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class MeVO {

    private Long userId;

    private String username;

    private String realName;

    private Set<String> permissions;

    private List<SysMenuVO> menus;
}
