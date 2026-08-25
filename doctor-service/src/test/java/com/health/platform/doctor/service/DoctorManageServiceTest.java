package com.health.platform.doctor.service;

import com.health.platform.core.exception.BizException;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.health.platform.doctor.entity.Department;
import com.health.platform.doctor.entity.Doctor;
import com.health.platform.doctor.entity.DoctorServiceItem;
import com.health.platform.doctor.entity.Hospital;
import com.health.platform.doctor.enums.CooperationStatus;
import com.health.platform.doctor.enums.DoctorAuditStatus;
import com.health.platform.doctor.enums.DoctorAction;
import com.health.platform.doctor.enums.ShelfStatus;
import com.health.platform.doctor.mapper.DepartmentMapper;
import com.health.platform.doctor.mapper.DoctorAttachmentMapper;
import com.health.platform.doctor.mapper.DoctorAuditMapper;
import com.health.platform.doctor.mapper.DoctorMapper;
import com.health.platform.doctor.mapper.DoctorServiceItemMapper;
import com.health.platform.doctor.mapper.HospitalMapper;
import com.health.platform.security.context.CurrentUser;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorManageServiceTest {

    @Mock
    private DoctorMapper doctorMapper;
    @Mock
    private DoctorAttachmentMapper doctorAttachmentMapper;
    @Mock
    private DoctorServiceItemMapper doctorServiceItemMapper;
    @Mock
    private HospitalMapper hospitalMapper;
    @Mock
    private DepartmentMapper departmentMapper;
    @Mock
    private DoctorAuditMapper doctorAuditMapper;

    private CurrentUser operator = new CurrentUser(9L, "ops");

    @BeforeAll
    static void initMybatisPlusLambdaCache() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "");
        TableInfoHelper.initTableInfo(assistant, Doctor.class);
        TableInfoHelper.initTableInfo(assistant, DoctorServiceItem.class);
    }

    private Doctor doctor(DoctorAuditStatus auditStatus, ShelfStatus shelfStatus) {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("张三");
        doctor.setAuditStatus(auditStatus.name());
        doctor.setCooperationStatus(CooperationStatus.ACTIVE.name());
        doctor.setServiceStatus(shelfStatus.name());
        return doctor;
    }

    private DoctorManageService manageService() {
        return new DoctorManageService(doctorMapper, doctorAttachmentMapper, doctorServiceItemMapper,
                hospitalMapper, departmentMapper,
                new DoctorAuditFlowService(doctorMapper, doctorAuditMapper, doctorServiceItemMapper));
    }

    private DoctorAuditFlowService flowService() {
        return new DoctorAuditFlowService(doctorMapper, doctorAuditMapper, doctorServiceItemMapper);
    }

    @Test
    void onShelfShouldRejectNonApprovedDoctor() {
        when(doctorMapper.selectById(1L)).thenReturn(doctor(DoctorAuditStatus.PENDING_REVIEW, ShelfStatus.OFF_SHELF));
        BizException e = assertThrows(BizException.class, () -> manageService().onShelf(1L));
        assertTrue(e.getMessage().contains("审核通过"));
        verify(doctorMapper, never()).updateById(any(Doctor.class));
    }

    @Test
    void onShelfShouldAllowApprovedDoctor() {
        when(doctorMapper.selectById(1L)).thenReturn(doctor(DoctorAuditStatus.APPROVED, ShelfStatus.OFF_SHELF));
        manageService().onShelf(1L);
        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorMapper).updateById(captor.capture());
        assertEquals(ShelfStatus.ON_SHELF.name(), captor.getValue().getServiceStatus());
    }

    @Test
    void suspendShouldForceDoctorAndAllServicesOffShelf() {
        when(doctorMapper.selectById(1L))
                .thenReturn(doctor(DoctorAuditStatus.APPROVED, ShelfStatus.ON_SHELF));
        DoctorServiceItem item = new DoctorServiceItem();
        item.setId(77L);
        item.setDoctorId(1L);
        item.setStatus(ShelfStatus.ON_SHELF.name());

        flowService().execute(1L, DoctorAction.SUSPEND, "违规", operator);

        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorMapper).updateById(doctorCaptor.capture());
        assertEquals(DoctorAuditStatus.SUSPENDED.name(), doctorCaptor.getValue().getAuditStatus());
        assertEquals(ShelfStatus.OFF_SHELF.name(), doctorCaptor.getValue().getServiceStatus());
        verify(doctorServiceItemMapper).update(isNull(), any());
    }

    @Test
    void terminateShouldSetCooperationTerminatedAndOffShelfEverything() {
        when(doctorMapper.selectById(1L))
                .thenReturn(doctor(DoctorAuditStatus.APPROVED, ShelfStatus.ON_SHELF));

        flowService().execute(1L, DoctorAction.TERMINATE, "合同到期", operator);

        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorMapper).updateById(captor.capture());
        assertEquals(DoctorAuditStatus.TERMINATED.name(), captor.getValue().getAuditStatus());
        assertEquals(CooperationStatus.TERMINATED.name(), captor.getValue().getCooperationStatus());
        assertEquals(ShelfStatus.OFF_SHELF.name(), captor.getValue().getServiceStatus());
    }

    @Test
    void rejectWithoutReasonShouldThrow() {
        when(doctorMapper.selectById(1L))
                .thenReturn(doctor(DoctorAuditStatus.PENDING_REVIEW, ShelfStatus.OFF_SHELF));
        BizException e = assertThrows(BizException.class,
                () -> flowService().execute(1L, DoctorAction.REJECT, "  ", operator));
        assertTrue(e.getMessage().contains("原因"));
    }

    @Test
    void illegalTransitionShouldThrowAndNotPersist() {
        when(doctorMapper.selectById(1L))
                .thenReturn(doctor(DoctorAuditStatus.REJECTED, ShelfStatus.OFF_SHELF));
        assertThrows(com.health.platform.core.exception.BizException.class,
                () -> flowService().execute(1L, DoctorAction.SUSPEND, "x", operator));
        verify(doctorMapper, never()).updateById(any(Doctor.class));
        verify(doctorAuditMapper, never()).insert(any(com.health.platform.doctor.entity.DoctorAudit.class));
    }

    @Test
    void auditRecordShouldCaptureOperatorAndTransition() {
        when(doctorMapper.selectById(1L))
                .thenReturn(doctor(DoctorAuditStatus.PENDING_REVIEW, ShelfStatus.OFF_SHELF));

        flowService().execute(1L, DoctorAction.APPROVE, null, operator);

        ArgumentCaptor<com.health.platform.doctor.entity.DoctorAudit> captor =
                ArgumentCaptor.forClass(com.health.platform.doctor.entity.DoctorAudit.class);
        verify(doctorAuditMapper).insert(captor.capture());
        com.health.platform.doctor.entity.DoctorAudit audit = captor.getValue();
        assertEquals("PENDING_REVIEW", audit.getFromStatus());
        assertEquals("APPROVED", audit.getToStatus());
        assertEquals("APPROVE", audit.getAction());
        assertEquals(9L, audit.getOperatorId());
        assertEquals("ops", audit.getOperatorName());
    }
}
