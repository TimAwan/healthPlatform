package com.health.platform.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor_audit")
public class DoctorAudit {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long doctorId;

    private String fromStatus;

    private String toStatus;

    private String action;

    private String reason;

    private Long operatorId;

    private String operatorName;

    private LocalDateTime createdAt;
}
