package com.health.platform.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class DoctorCreateDTO {

    @NotBlank(message = "医生姓名不能为空")
    @Size(max = 50)
    private String name;

    @NotBlank(message = "性别不能为空")
    @Pattern(regexp = "MALE|FEMALE", message = "性别仅允许 MALE/FEMALE")
    private String gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    private Long avatarFileId;

    @NotNull(message = "医院不能为空")
    private Long hospitalId;

    @NotNull(message = "科室不能为空")
    private Long departmentId;

    @Size(max = 50, message = "职称长度不能超过 50")
    private String title;

    @Size(max = 500, message = "擅长领域长度不能超过 500")
    private String specialty;

    @Size(max = 2000, message = "简介长度不能超过 2000")
    private String intro;

    private List<AttachmentDTO> attachments;

    @Data
    public static class AttachmentDTO {

        @NotNull(message = "文件 ID 不能为空")
        private Long fileId;

        @NotBlank(message = "附件类型不能为空")
        @Pattern(regexp = "PRACTICE_LICENSE|QUALIFICATION|OTHER", message = "附件类型非法")
        private String fileType;
    }
}
