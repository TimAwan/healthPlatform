package com.health.platform.doctor.controller;

import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.core.result.Result;
import com.health.platform.doctor.entity.Department;
import com.health.platform.doctor.service.DepartmentService;
import com.health.platform.log.OperationLog;
import com.health.platform.security.permission.RequirePermission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    @RequirePermission("DEPARTMENT_VIEW")
    public Result<PageResult<Department>> page(PageQuery query,
                                               @RequestParam(required = false) Long hospitalId,
                                               @RequestParam(required = false) String keyword) {
        return Result.ok(departmentService.page(query, hospitalId, keyword));
    }

    @GetMapping("/by-hospital/{hospitalId}")
    @RequirePermission("DEPARTMENT_VIEW")
    public Result<List<Department>> listByHospital(@PathVariable Long hospitalId) {
        return Result.ok(departmentService.listByHospital(hospitalId));
    }

    @PostMapping
    @RequirePermission("DEPARTMENT_CREATE")
    @OperationLog(operation = "新增科室", targetType = "DEPARTMENT")
    public Result<Long> create(@Validated @RequestBody DepartmentRequest request) {
        Department department = new Department();
        department.setHospitalId(request.getHospitalId());
        department.setName(request.getName());
        department.setCode(request.getCode());
        departmentService.create(department);
        return Result.ok(department.getId());
    }

    @PutMapping("/{id}")
    @RequirePermission("DEPARTMENT_UPDATE")
    @OperationLog(operation = "修改科室", targetType = "DEPARTMENT", targetId = "#id")
    public Result<Void> update(@PathVariable Long id, @Validated @RequestBody DepartmentRequest request) {
        Department department = new Department();
        department.setHospitalId(request.getHospitalId());
        department.setName(request.getName());
        department.setCode(request.getCode());
        departmentService.update(id, department);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("DEPARTMENT_DELETE")
    @OperationLog(operation = "删除科室", targetType = "DEPARTMENT", targetId = "#id")
    public Result<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/disable")
    @RequirePermission("DEPARTMENT_DELETE")
    @OperationLog(operation = "禁用科室", targetType = "DEPARTMENT", targetId = "#id")
    public Result<Void> disable(@PathVariable Long id) {
        departmentService.changeStatus(id, false);
        return Result.ok();
    }

    @PostMapping("/{id}/enable")
    @RequirePermission("DEPARTMENT_DELETE")
    @OperationLog(operation = "启用科室", targetType = "DEPARTMENT", targetId = "#id")
    public Result<Void> enable(@PathVariable Long id) {
        departmentService.changeStatus(id, true);
        return Result.ok();
    }

    @Data
    public static class DepartmentRequest {

        @NotNull(message = "所属医院不能为空")
        private Long hospitalId;

        @NotBlank(message = "科室名称不能为空")
        @Size(max = 100)
        private String name;

        @NotBlank(message = "科室编码不能为空")
        @Size(max = 50)
        private String code;
    }
}
