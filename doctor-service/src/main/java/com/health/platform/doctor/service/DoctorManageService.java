package com.health.platform.doctor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.doctor.entity.Department;
import com.health.platform.doctor.entity.Doctor;
import com.health.platform.doctor.entity.DoctorAttachment;
import com.health.platform.doctor.entity.DoctorServiceItem;
import com.health.platform.doctor.entity.Hospital;
import com.health.platform.doctor.enums.CooperationStatus;
import com.health.platform.doctor.enums.DoctorAuditStatus;
import com.health.platform.doctor.enums.ShelfStatus;
import com.health.platform.doctor.mapper.DepartmentMapper;
import com.health.platform.doctor.mapper.DoctorAttachmentMapper;
import com.health.platform.doctor.mapper.DoctorMapper;
import com.health.platform.doctor.mapper.DoctorServiceItemMapper;
import com.health.platform.doctor.mapper.HospitalMapper;
import com.health.platform.doctor.dto.DoctorCreateDTO;
import com.health.platform.doctor.dto.DoctorPageQuery;
import com.health.platform.doctor.vo.DoctorVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorManageService {

    private final DoctorMapper doctorMapper;
    private final DoctorAttachmentMapper doctorAttachmentMapper;
    private final DoctorServiceItemMapper doctorServiceItemMapper;
    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper departmentMapper;
    private final DoctorAuditFlowService doctorAuditFlowService;

    public PageResult<DoctorVO> page(DoctorPageQuery query) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<Doctor>()
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(Doctor::getName, query.getKeyword())
                        .or().like(Doctor::getPhone, query.getKeyword()))
                .eq(StringUtils.hasText(query.getAuditStatus()), Doctor::getAuditStatus, query.getAuditStatus())
                .eq(query.getHospitalId() != null, Doctor::getHospitalId, query.getHospitalId())
                .eq(query.getDepartmentId() != null, Doctor::getDepartmentId, query.getDepartmentId())
                .orderByDesc(Doctor::getId);
        Page<Doctor> page = doctorMapper.selectPage(Page.of(query.getPage(), query.getSize()), wrapper);
        List<DoctorVO> voList = page.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), voList);
    }

    public DoctorVO detail(Long id) {
        Doctor doctor = requireDoctor(id);
        return toVO(doctor);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(DoctorCreateDTO dto) {
        validateHospitalAndDepartment(dto.getHospitalId(), dto.getDepartmentId());
        Doctor doctor = new Doctor();
        applyFields(doctor, dto);
        doctor.setAuditStatus(DoctorAuditStatus.PENDING_REVIEW.name());
        doctor.setCooperationStatus(CooperationStatus.ACTIVE.name());
        doctor.setServiceStatus(ShelfStatus.OFF_SHELF.name());
        doctorMapper.insert(doctor);
        replaceAttachments(doctor.getId(), dto);
        return doctor.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, DoctorCreateDTO dto) {
        Doctor existing = requireDoctor(id);
        if (DoctorAuditStatus.TERMINATED.name().equals(existing.getAuditStatus())) {
            throw new BizException("已解约医生不允许编辑");
        }
        validateHospitalAndDepartment(dto.getHospitalId(), dto.getDepartmentId());
        Doctor update = new Doctor();
        update.setId(id);
        applyFields(update, dto);
        doctorMapper.updateById(update);
        replaceAttachments(id, dto);
    }

    /** 决策 5：仅审核通过的医生可上架 */
    public void onShelf(Long id) {
        Doctor doctor = requireDoctor(id);
        if (!DoctorAuditStatus.APPROVED.name().equals(doctor.getAuditStatus())) {
            throw new BizException("仅审核通过的医生可上架");
        }
        updateShelfStatus(id, ShelfStatus.ON_SHELF);
    }

    public void offShelf(Long id) {
        requireDoctor(id);
        updateShelfStatus(id, ShelfStatus.OFF_SHELF);
        doctorServiceItemsOffShelf(id);
    }

    private void updateShelfStatus(Long id, ShelfStatus status) {
        Doctor update = new Doctor();
        update.setId(id);
        update.setServiceStatus(status.name());
        doctorMapper.updateById(update);
    }

    private void doctorServiceItemsOffShelf(Long doctorId) {
        List<DoctorServiceItem> items = doctorServiceItemMapper.selectList(
                new LambdaQueryWrapper<DoctorServiceItem>().eq(DoctorServiceItem::getDoctorId, doctorId));
        for (DoctorServiceItem item : items) {
            DoctorServiceItem update = new DoctorServiceItem();
            update.setId(item.getId());
            update.setStatus(ShelfStatus.OFF_SHELF.name());
            doctorServiceItemMapper.updateById(update);
        }
    }

    private Doctor requireDoctor(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor == null) {
            throw new BizException("医生不存在");
        }
        return doctor;
    }

    private void applyFields(Doctor doctor, DoctorCreateDTO dto) {
        doctor.setName(dto.getName());
        doctor.setGender(dto.getGender());
        doctor.setPhone(dto.getPhone());
        doctor.setAvatarFileId(dto.getAvatarFileId());
        doctor.setHospitalId(dto.getHospitalId());
        doctor.setDepartmentId(dto.getDepartmentId());
        doctor.setTitle(dto.getTitle());
        doctor.setSpecialty(dto.getSpecialty());
        doctor.setIntro(dto.getIntro());
    }

    private void replaceAttachments(Long doctorId, DoctorCreateDTO dto) {
        doctorAttachmentMapper.delete(new LambdaQueryWrapper<DoctorAttachment>()
                .eq(DoctorAttachment::getDoctorId, doctorId));
        if (CollectionUtils.isEmpty(dto.getAttachments())) {
            return;
        }
        for (DoctorCreateDTO.AttachmentDTO attachmentDTO : dto.getAttachments()) {
            DoctorAttachment attachment = new DoctorAttachment();
            attachment.setDoctorId(doctorId);
            attachment.setFileId(attachmentDTO.getFileId());
            attachment.setFileType(attachmentDTO.getFileType());
            doctorAttachmentMapper.insert(attachment);
        }
    }

    private void validateHospitalAndDepartment(Long hospitalId, Long departmentId) {
        Hospital hospital = hospitalMapper.selectById(hospitalId);
        if (hospital == null || !"ENABLED".equals(hospital.getStatus())) {
            throw new BizException("医院不存在或已禁用");
        }
        Department department = departmentMapper.selectById(departmentId);
        if (department == null || !"ENABLED".equals(department.getStatus())) {
            throw new BizException("科室不存在或已禁用");
        }
        if (!department.getHospitalId().equals(hospitalId)) {
            throw new BizException("科室不属于所选医院");
        }
    }

    private DoctorVO toVO(Doctor doctor) {
        DoctorVO vo = new DoctorVO();
        vo.setId(doctor.getId());
        vo.setName(doctor.getName());
        vo.setGender(doctor.getGender());
        vo.setPhone(doctor.getPhone());
        vo.setAvatarFileId(doctor.getAvatarFileId());
        vo.setHospitalId(doctor.getHospitalId());
        vo.setDepartmentId(doctor.getDepartmentId());
        vo.setTitle(doctor.getTitle());
        vo.setSpecialty(doctor.getSpecialty());
        vo.setIntro(doctor.getIntro());
        vo.setAuditStatus(doctor.getAuditStatus());
        vo.setCooperationStatus(doctor.getCooperationStatus());
        vo.setServiceStatus(doctor.getServiceStatus());
        vo.setCreatedAt(doctor.getCreatedAt());
        Map<Long, String> hospitalNames = hospitalMapper.selectBatchIds(List.of(doctor.getHospitalId()))
                .stream().collect(Collectors.toMap(Hospital::getId, Hospital::getName));
        Map<Long, String> departmentNames = departmentMapper.selectBatchIds(List.of(doctor.getDepartmentId()))
                .stream().collect(Collectors.toMap(Department::getId, Department::getName));
        vo.setHospitalName(hospitalNames.get(doctor.getHospitalId()));
        vo.setDepartmentName(departmentNames.get(doctor.getDepartmentId()));
        vo.setAttachments(doctorAttachmentMapper.selectList(
                        new LambdaQueryWrapper<DoctorAttachment>().eq(DoctorAttachment::getDoctorId, doctor.getId()))
                .stream().map(a -> {
                    DoctorVO.AttachmentVO attachmentVO = new DoctorVO.AttachmentVO();
                    attachmentVO.setFileId(a.getFileId());
                    attachmentVO.setFileType(a.getFileType());
                    return attachmentVO;
                }).toList());
        return vo;
    }
}
