package com.health.platform.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor")
public class Doctor {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String name;

    private String gender;

    private String phone;

    private Long avatarFileId;

    private Long hospitalId;

    private Long departmentId;

    private String title;

    private String specialty;

    private String intro;

    private String auditStatus;

    private String cooperationStatus;

    private String serviceStatus;

    /** 预留：医生登录账号关联（决策 4，二期启用） */
    private Long userId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
