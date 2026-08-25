package com.health.platform.doctor.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditRequest {

    @Size(max = 500, message = "原因长度不能超过 500")
    private String reason;
}
