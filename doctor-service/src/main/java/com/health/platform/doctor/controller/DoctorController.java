package com.health.platform.doctor.controller;

import com.health.platform.core.result.PageResult;
import com.health.platform.core.result.Result;
import com.health.platform.doctor.audit.AuditHistoryVO;
import com.health.platform.doctor.dto.AuditRequest;
import com.health.platform.doctor.dto.DoctorCreateDTO;
import com.health.platform.doctor.dto.DoctorPageQuery;
import com.health.platform.doctor.enums.DoctorAction;
import com.health.platform.doctor.service.DoctorAuditFlowService;
import com.health.platform.doctor.service.DoctorManageService;
import com.health.platform.doctor.vo.DoctorVO;
import com.health.platform.log.OperationLog;
import com.health.platform.security.context.UserContext;
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

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorManageService doctorManageService;
    private final DoctorAuditFlowService doctorAuditFlowService;

    @GetMapping
    @RequirePermission("DOCTOR_VIEW")
    public Result<PageResult<DoctorVO>> page(DoctorPageQuery query) {
        return Result.ok(doctorManageService.page(query));
    }

    @GetMapping("/{id}")
    @RequirePermission("DOCTOR_VIEW")
    public Result<DoctorVO> detail(@PathVariable Long id) {
        return Result.ok(doctorManageService.detail(id));
    }

    @PostMapping
    @RequirePermission("DOCTOR_CREATE")
    @OperationLog(operation = "新增医生", targetType = "DOCTOR")
    public Result<Long> create(@Valid @RequestBody DoctorCreateDTO dto) {
        return Result.ok(doctorManageService.create(dto));
    }

    @PutMapping("/{id}")
    @RequirePermission("DOCTOR_UPDATE")
    @OperationLog(operation = "修改医生", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DoctorCreateDTO dto) {
        doctorManageService.update(id, dto);
        return Result.ok();
    }

    @PostMapping("/{id}/approve")
    @RequirePermission("DOCTOR_APPROVE")
    @OperationLog(operation = "审核通过医生", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> approve(@PathVariable Long id, @Valid @RequestBody(required = false) AuditRequest request) {
        doctorAuditFlowService.execute(id, DoctorAction.APPROVE, reason(request), UserContext.require());
        return Result.ok();
    }

    @PostMapping("/{id}/reject")
    @RequirePermission("DOCTOR_REJECT")
    @OperationLog(operation = "驳回医生", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> reject(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        doctorAuditFlowService.execute(id, DoctorAction.REJECT, reason(request), UserContext.require());
        return Result.ok();
    }

    @PostMapping("/{id}/suspend")
    @RequirePermission("DOCTOR_SUSPEND")
    @OperationLog(operation = "暂停医生", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> suspend(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        doctorAuditFlowService.execute(id, DoctorAction.SUSPEND, reason(request), UserContext.require());
        return Result.ok();
    }

    @PostMapping("/{id}/terminate")
    @RequirePermission("DOCTOR_TERMINATE")
    @OperationLog(operation = "解约医生", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> terminate(@PathVariable Long id, @Valid @RequestBody AuditRequest request) {
        doctorAuditFlowService.execute(id, DoctorAction.TERMINATE, reason(request), UserContext.require());
        return Result.ok();
    }

    @PostMapping("/{id}/resubmit")
    @RequirePermission("DOCTOR_RESUBMIT")
    @OperationLog(operation = "重新提交医生审核", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> resubmit(@PathVariable Long id) {
        doctorAuditFlowService.execute(id, DoctorAction.RESUBMIT, null, UserContext.require());
        return Result.ok();
    }

    @PostMapping("/{id}/on-shelf")
    @RequirePermission("DOCTOR_ON_SHELF")
    @OperationLog(operation = "医生上架", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> onShelf(@PathVariable Long id) {
        doctorManageService.onShelf(id);
        return Result.ok();
    }

    @PostMapping("/{id}/off-shelf")
    @RequirePermission("DOCTOR_OFF_SHELF")
    @OperationLog(operation = "医生下架", targetType = "DOCTOR", targetId = "#id")
    public Result<Void> offShelf(@PathVariable Long id) {
        doctorManageService.offShelf(id);
        return Result.ok();
    }

    @GetMapping("/{id}/audit-history")
    @RequirePermission("DOCTOR_VIEW")
    public Result<AuditHistoryVO> auditHistory(@PathVariable Long id) {
        return Result.ok(new AuditHistoryVO(doctorAuditFlowService.auditHistory(id)));
    }

    private String reason(AuditRequest request) {
        return request == null ? null : request.getReason();
    }
}
