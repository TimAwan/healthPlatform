package com.health.platform.doctor.controller;

import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.core.result.Result;
import com.health.platform.doctor.entity.Hospital;
import com.health.platform.doctor.service.HospitalService;
import com.health.platform.log.OperationLog;
import com.health.platform.security.permission.RequirePermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
@Validated
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    @RequirePermission("HOSPITAL_VIEW")
    public Result<PageResult<Hospital>> page(PageQuery query, @RequestParam(required = false) String keyword) {
        return Result.ok(hospitalService.page(query, keyword));
    }

    @GetMapping("/all")
    @RequirePermission("HOSPITAL_VIEW")
    public Result<List<Hospital>> listEnabled() {
        return Result.ok(hospitalService.listEnabled());
    }

    @PostMapping
    @RequirePermission("HOSPITAL_CREATE")
    @OperationLog(operation = "新增医院", targetType = "HOSPITAL")
    public Result<Long> create(@Validated @RequestBody HospitalRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setCode(request.getCode());
        hospitalService.create(hospital);
        return Result.ok(hospital.getId());
    }

    @PutMapping("/{id}")
    @RequirePermission("HOSPITAL_UPDATE")
    @OperationLog(operation = "修改医院", targetType = "HOSPITAL", targetId = "#id")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody HospitalRequest request) {
        Hospital hospital = new Hospital();
        hospital.setName(request.getName());
        hospital.setCode(request.getCode());
        hospitalService.update(id, hospital);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("HOSPITAL_DELETE")
    @OperationLog(operation = "删除医院", targetType = "HOSPITAL", targetId = "#id")
    public Result<Void> delete(@PathVariable Long id) {
        hospitalService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/disable")
    @RequirePermission("HOSPITAL_DELETE")
    @OperationLog(operation = "禁用医院", targetType = "HOSPITAL", targetId = "#id")
    public Result<Void> disable(@PathVariable Long id) {
        hospitalService.changeStatus(id, false);
        return Result.ok();
    }

    @PostMapping("/{id}/enable")
    @RequirePermission("HOSPITAL_DELETE")
    @OperationLog(operation = "启用医院", targetType = "HOSPITAL", targetId = "#id")
    public Result<Void> enable(@PathVariable Long id) {
        hospitalService.changeStatus(id, true);
        return Result.ok();
    }

    @Data
    public static class HospitalRequest {

        @NotBlank(message = "医院名称不能为空")
        @Size(max = 100)
        private String name;

        @NotBlank(message = "医院编码不能为空")
        @Size(max = 50)
        private String code;
    }
}
