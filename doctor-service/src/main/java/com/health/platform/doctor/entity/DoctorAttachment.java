package com.health.platform.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("doctor_attachment")
public class DoctorAttachment {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long doctorId;

    private Long fileId;

    private String fileType;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
