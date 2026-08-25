package com.health.platform.system.vo;

import lombok.Data;

import java.util.List;

@Data
public class SysMenuVO {

    private Long id;

    private Long parentId;

    private String name;

    private String path;

    private String component;

    private String icon;

    private Integer sort;

    private String status;

    private List<SysMenuVO> children;
}
