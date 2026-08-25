package com.health.platform.doctor.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DoctorVO {

    private Long id;

    private String name;

    private String gender;

    private String phone;

    private Long avatarFileId;

    private Long hospitalId;

    private String hospitalName;

    private Long departmentId;

    private String departmentName;

    private String title;

    private String specialty;

    private String intro;

    private String auditStatus;

    private String cooperationStatus;

    private String serviceStatus;

    private LocalDateTime createdAt;

    private List<AttachmentVO> attachments;

    @Data
    public static class AttachmentVO {

        private Long fileId;

        private String fileType;
    }
}
