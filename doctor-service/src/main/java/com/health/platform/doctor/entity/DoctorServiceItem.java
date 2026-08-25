package com.health.platform.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("doctor_service")
public class DoctorServiceItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long doctorId;

    private String name;

    private String type;

    private BigDecimal price;

    private String description;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
