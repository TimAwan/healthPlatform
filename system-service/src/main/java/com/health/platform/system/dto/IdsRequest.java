package com.health.platform.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class IdsRequest {

    @NotNull(message = "ID 列表不能为 null")
    private List<Long> ids;
}
