package com.health.platform.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysRoleDTO {

    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z][A-Z0-9_]*$", message = "角色编码仅允许大写字母、数字、下划线")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String remark;
}
