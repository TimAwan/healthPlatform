package com.health.platform.doctor.statemachine;

import com.health.platform.core.exception.BizException;
import com.health.platform.doctor.enums.DoctorAction;
import com.health.platform.doctor.enums.DoctorAuditStatus;

import java.util.Map;
import java.util.Set;

/**
 * 医生审核状态机（说明书第 16 章），doctor_audit 流水的唯一合法入口。
 * 合法转换：
 *   PENDING_REVIEW --APPROVE-->  APPROVED
 *   PENDING_REVIEW --REJECT-->   REJECTED
 *   REJECTED       --RESUBMIT--> PENDING_REVIEW
 *   APPROVED       --SUSPEND-->  SUSPENDED
 *   APPROVED       --TERMINATE-> TERMINATED
 * SUSPENDED 的流出转换说明书未定义，如需新增（例如"恢复"）必须先向项目负责人确认。
 */
public final class DoctorAuditStateMachine {

    private static final Map<DoctorAuditStatus, Map<DoctorAction, DoctorAuditStatus>> TRANSITIONS = Map.of(
            DoctorAuditStatus.PENDING_REVIEW, Map.of(
                    DoctorAction.APPROVE, DoctorAuditStatus.APPROVED,
                    DoctorAction.REJECT, DoctorAuditStatus.REJECTED),
            DoctorAuditStatus.REJECTED, Map.of(
                    DoctorAction.RESUBMIT, DoctorAuditStatus.PENDING_REVIEW),
            DoctorAuditStatus.APPROVED, Map.of(
                    DoctorAction.SUSPEND, DoctorAuditStatus.SUSPENDED,
                    DoctorAction.TERMINATE, DoctorAuditStatus.TERMINATED),
            DoctorAuditStatus.SUSPENDED, Map.of(),
            DoctorAuditStatus.TERMINATED, Map.of());

    private DoctorAuditStateMachine() {
    }

    public static DoctorAuditStatus next(DoctorAuditStatus current, DoctorAction action) {
        Map<DoctorAction, DoctorAuditStatus> allowed = TRANSITIONS.get(current);
        DoctorAuditStatus target = allowed == null ? null : allowed.get(action);
        if (target == null) {
            throw new BizException(String.format("非法状态转换：[%s] 不允许执行 [%s]", current, action));
        }
        return target;
    }

    public static Set<DoctorAction> allowedActions(DoctorAuditStatus current) {
        Map<DoctorAction, DoctorAuditStatus> allowed = TRANSITIONS.get(current);
        return allowed == null ? Set.of() : allowed.keySet();
    }
}
