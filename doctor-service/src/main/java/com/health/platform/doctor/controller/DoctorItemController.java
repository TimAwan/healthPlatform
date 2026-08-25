package com.health.platform.doctor.controller;

import com.health.platform.core.result.Result;
import com.health.platform.doctor.dto.DoctorItemDTO;
import com.health.platform.doctor.entity.DoctorServiceItem;
import com.health.platform.doctor.service.DoctorItemService;
import com.health.platform.log.OperationLog;
import com.health.platform.security.permission.RequirePermission;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors/{doctorId}/services")
@RequiredArgsConstructor
public class DoctorItemController {

    private final DoctorItemService doctorItemService;

    @GetMapping
    @RequirePermission("DOCTOR_SERVICE_VIEW")
    public Result<List<DoctorServiceItem>> list(@PathVariable Long doctorId) {
        return Result.ok(doctorItemService.listByDoctor(doctorId));
    }

    @PostMapping
    @RequirePermission("DOCTOR_SERVICE_CREATE")
    @OperationLog(operation = "新增医生服务", targetType = "DOCTOR_SERVICE", targetId = "#doctorId")
    public Result<Long> create(@PathVariable Long doctorId, @Valid @RequestBody DoctorItemDTO dto) {
        return Result.ok(doctorItemService.create(doctorId, dto));
    }

    @PutMapping("/{itemId}")
    @RequirePermission("DOCTOR_SERVICE_UPDATE")
    @OperationLog(operation = "修改医生服务", targetType = "DOCTOR_SERVICE", targetId = "#itemId")
    public Result<Void> update(@PathVariable Long doctorId, @PathVariable Long itemId,
                               @Valid @RequestBody DoctorItemDTO dto) {
        doctorItemService.update(doctorId, itemId, dto);
        return Result.ok();
    }

    @PostMapping("/{itemId}/on-shelf")
    @RequirePermission("DOCTOR_SERVICE_ON_SHELF")
    @OperationLog(operation = "医生服务上架", targetType = "DOCTOR_SERVICE", targetId = "#itemId")
    public Result<Void> onShelf(@PathVariable Long doctorId, @PathVariable Long itemId) {
        doctorItemService.onShelf(doctorId, itemId);
        return Result.ok();
    }

    @PostMapping("/{itemId}/off-shelf")
    @RequirePermission("DOCTOR_SERVICE_OFF_SHELF")
    @OperationLog(operation = "医生服务下架", targetType = "DOCTOR_SERVICE", targetId = "#itemId")
    public Result<Void> offShelf(@PathVariable Long doctorId, @PathVariable Long itemId) {
        doctorItemService.offShelf(doctorId, itemId);
        return Result.ok();
    }
}
