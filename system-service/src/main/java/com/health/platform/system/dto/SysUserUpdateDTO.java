package com.health.platform.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SysUserUpdateDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 50, message = "姓名长度不能超过 50")
    private String realName;

    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
}
