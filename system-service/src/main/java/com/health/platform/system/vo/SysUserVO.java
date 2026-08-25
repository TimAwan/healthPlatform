package com.health.platform.system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SysUserVO {

    private Long id;

    private String username;

    private String realName;

    private String phone;

    private String status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private List<Long> roleIds;

    private List<String> roleNames;
}
