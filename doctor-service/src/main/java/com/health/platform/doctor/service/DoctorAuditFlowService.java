package com.health.platform.doctor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.health.platform.core.exception.BizException;
import com.health.platform.doctor.entity.Doctor;
import com.health.platform.doctor.entity.DoctorAudit;
import com.health.platform.doctor.entity.DoctorServiceItem;
import com.health.platform.doctor.enums.CooperationStatus;
import com.health.platform.doctor.enums.DoctorAction;
import com.health.platform.doctor.enums.DoctorAuditStatus;
import com.health.platform.doctor.enums.ShelfStatus;
import com.health.platform.doctor.mapper.DoctorAuditMapper;
import com.health.platform.doctor.mapper.DoctorMapper;
import com.health.platform.doctor.mapper.DoctorServiceItemMapper;
import com.health.platform.security.context.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 医生审核流转：状态机唯一入口 + 决策 5 联动规则。
 * 联动：SUSPEND/TERMINATE 时医生与其全部服务项目强制下架；TERMINATE 时合作状态置为已终止。
 */
@Service
@RequiredArgsConstructor
public class DoctorAuditFlowService {

    private final DoctorMapper doctorMapper;
    private final DoctorAuditMapper doctorAuditMapper;
    private final DoctorServiceItemMapper doctorServiceItemMapper;

    @Transactional(rollbackFor = Exception.class)
    public void execute(Long doctorId, DoctorAction action, String reason, CurrentUser operator) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new BizException("医生不存在");
        }
        if (requiresReason(action) && !StringUtils.hasText(reason)) {
            throw new BizException(action.getLabel() + "必须填写原因");
        }

        DoctorAuditStatus current = DoctorAuditStatus.valueOf(doctor.getAuditStatus());
        DoctorAuditStatus target = com.health.platform.doctor.statemachine.DoctorAuditStateMachine.next(current, action);

        Doctor update = new Doctor();
        update.setId(doctorId);
        update.setAuditStatus(target.name());
        if (action == DoctorAction.TERMINATE) {
            update.setCooperationStatus(CooperationStatus.TERMINATED.name());
        }
        if (action == DoctorAction.SUSPEND || action == DoctorAction.TERMINATE) {
            update.setServiceStatus(ShelfStatus.OFF_SHELF.name());
            doctorServiceItemMapper.update(null, new LambdaUpdateWrapper<DoctorServiceItem>()
                    .eq(DoctorServiceItem::getDoctorId, doctorId)
                    .set(DoctorServiceItem::getStatus, ShelfStatus.OFF_SHELF.name()));
        }
        doctorMapper.updateById(update);

        DoctorAudit audit = new DoctorAudit();
        audit.setDoctorId(doctorId);
        audit.setFromStatus(current.name());
        audit.setToStatus(target.name());
        audit.setAction(action.name());
        audit.setReason(reason);
        audit.setOperatorId(operator.userId());
        audit.setOperatorName(operator.username());
        doctorAuditMapper.insert(audit);
    }

    public List<DoctorAudit> auditHistory(Long doctorId) {
        return doctorAuditMapper.selectList(new LambdaQueryWrapper<DoctorAudit>()
                .eq(DoctorAudit::getDoctorId, doctorId)
                .orderByDesc(DoctorAudit::getId));
    }

    private boolean requiresReason(DoctorAction action) {
        return action == DoctorAction.REJECT || action == DoctorAction.SUSPEND || action == DoctorAction.TERMINATE;
    }
}
