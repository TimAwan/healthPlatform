package com.health.platform.doctor.statemachine;

import com.health.platform.core.exception.BizException;
import com.health.platform.doctor.enums.DoctorAction;
import com.health.platform.doctor.enums.DoctorAuditStatus;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 说明书第 16 章状态机全矩阵测试：合法转换必须精确命中，非法转换必须拒绝。
 */
class DoctorAuditStateMachineTest {

    private static final Map<DoctorAuditStatus, Map<DoctorAction, DoctorAuditStatus>> LEGAL = buildLegalMatrix();

    private static Map<DoctorAuditStatus, Map<DoctorAction, DoctorAuditStatus>> buildLegalMatrix() {
        Map<DoctorAuditStatus, Map<DoctorAction, DoctorAuditStatus>> matrix = new EnumMap<>(DoctorAuditStatus.class);
        matrix.put(DoctorAuditStatus.PENDING_REVIEW, Map.of(
                DoctorAction.APPROVE, DoctorAuditStatus.APPROVED,
                DoctorAction.REJECT, DoctorAuditStatus.REJECTED));
        matrix.put(DoctorAuditStatus.REJECTED, Map.of(
                DoctorAction.RESUBMIT, DoctorAuditStatus.PENDING_REVIEW));
        matrix.put(DoctorAuditStatus.APPROVED, Map.of(
                DoctorAction.SUSPEND, DoctorAuditStatus.SUSPENDED,
                DoctorAction.TERMINATE, DoctorAuditStatus.TERMINATED));
        matrix.put(DoctorAuditStatus.SUSPENDED, Map.of());
        matrix.put(DoctorAuditStatus.TERMINATED, Map.of());
        return matrix;
    }

    @Test
    void allLegalTransitionsShouldMatchSpec() {
        for (DoctorAuditStatus from : DoctorAuditStatus.values()) {
            for (Map.Entry<DoctorAction, DoctorAuditStatus> entry : LEGAL.get(from).entrySet()) {
                assertEquals(entry.getValue(), DoctorAuditStateMachine.next(from, entry.getKey()),
                        () -> from + " --" + entry.getKey() + "--> 应为 " + entry.getValue());
            }
        }
    }

    @Test
    void allIllegalTransitionsShouldThrow() {
        int illegalCount = 0;
        for (DoctorAuditStatus from : DoctorAuditStatus.values()) {
            for (DoctorAction action : DoctorAction.values()) {
                if (!LEGAL.get(from).containsKey(action)) {
                    assertThrows(BizException.class,
                            () -> DoctorAuditStateMachine.next(from, action),
                            () -> from + " --" + action + "--> 应为非法转换");
                    illegalCount++;
                }
            }
        }
        // 5 状态 × 5 动作 = 25，合法 5 条，非法 20 条
        assertEquals(20, illegalCount);
    }

    @Test
    void suspendedAndTerminatedShouldBeTerminalStates() {
        assertTrue(DoctorAuditStateMachine.allowedActions(DoctorAuditStatus.SUSPENDED).isEmpty());
        assertTrue(DoctorAuditStateMachine.allowedActions(DoctorAuditStatus.TERMINATED).isEmpty());
    }

    @Test
    void rejectedDoctorCanResubmitThenApprove() {
        DoctorAuditStatus afterResubmit =
                DoctorAuditStateMachine.next(DoctorAuditStatus.REJECTED, DoctorAction.RESUBMIT);
        assertEquals(DoctorAuditStatus.PENDING_REVIEW, afterResubmit);
        DoctorAuditStatus afterApprove =
                DoctorAuditStateMachine.next(afterResubmit, DoctorAction.APPROVE);
        assertEquals(DoctorAuditStatus.APPROVED, afterApprove);
    }
}
