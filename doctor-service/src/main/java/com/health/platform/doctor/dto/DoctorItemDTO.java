package com.health.platform.doctor.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorItemDTO {

    @NotBlank(message = "服务名称不能为空")
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String type;

    @NotNull(message = "服务价格不能为空")
    @DecimalMin(value = "0", message = "服务价格不能为负")
    private BigDecimal price;

    @Size(max = 1000)
    private String description;
}
