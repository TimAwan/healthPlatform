package com.health.platform.doctor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.core.exception.BizException;
import com.health.platform.doctor.entity.Doctor;
import com.health.platform.doctor.entity.DoctorServiceItem;
import com.health.platform.doctor.enums.DoctorAuditStatus;
import com.health.platform.doctor.enums.ShelfStatus;
import com.health.platform.doctor.dto.DoctorItemDTO;
import com.health.platform.doctor.mapper.DoctorMapper;
import com.health.platform.doctor.mapper.DoctorServiceItemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 医生服务项目管理。服务上下架与医生上下架相互区分（18 章）：
 * 服务上架前提：所属医生审核通过且本人处于上架状态。
 */
@Service
@RequiredArgsConstructor
public class DoctorItemService {

    private final DoctorServiceItemMapper doctorServiceItemMapper;
    private final DoctorMapper doctorMapper;

    public List<DoctorServiceItem> listByDoctor(Long doctorId) {
        return doctorServiceItemMapper.selectList(new LambdaQueryWrapper<DoctorServiceItem>()
                .eq(DoctorServiceItem::getDoctorId, doctorId)
                .orderByDesc(DoctorServiceItem::getId));
    }

    public Long create(Long doctorId, DoctorItemDTO dto) {
        Doctor doctor = requireDoctor(doctorId);
        if (DoctorAuditStatus.TERMINATED.name().equals(doctor.getAuditStatus())) {
            throw new BizException("已解约医生不允许添加服务");
        }
        DoctorServiceItem item = new DoctorServiceItem();
        applyFields(item, doctorId, dto);
        item.setStatus(ShelfStatus.OFF_SHELF.name());
        doctorServiceItemMapper.insert(item);
        return item.getId();
    }

    public void update(Long doctorId, Long itemId, DoctorItemDTO dto) {
        requireItem(doctorId, itemId);
        DoctorServiceItem update = new DoctorServiceItem();
        applyFields(update, doctorId, dto);
        update.setId(itemId);
        doctorServiceItemMapper.updateById(update);
    }

    public void onShelf(Long doctorId, Long itemId) {
        Doctor doctor = requireDoctor(doctorId);
        if (!DoctorAuditStatus.APPROVED.name().equals(doctor.getAuditStatus())
                || !ShelfStatus.ON_SHELF.name().equals(doctor.getServiceStatus())) {
            throw new BizException("医生须审核通过且上架后，服务才可上架");
        }
        updateStatus(doctorId, itemId, ShelfStatus.ON_SHELF);
    }

    public void offShelf(Long doctorId, Long itemId) {
        requireItem(doctorId, itemId);
        updateStatus(doctorId, itemId, ShelfStatus.OFF_SHELF);
    }

    private void updateStatus(Long doctorId, Long itemId, ShelfStatus status) {
        DoctorServiceItem update = new DoctorServiceItem();
        update.setId(itemId);
        update.setStatus(status.name());
        doctorServiceItemMapper.updateById(update);
    }

    private void applyFields(DoctorServiceItem item, Long doctorId, DoctorItemDTO dto) {
        item.setDoctorId(doctorId);
        item.setName(dto.getName());
        item.setType(dto.getType());
        item.setPrice(dto.getPrice());
        item.setDescription(dto.getDescription());
    }

    private Doctor requireDoctor(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            throw new BizException("医生不存在");
        }
        return doctor;
    }

    private DoctorServiceItem requireItem(Long doctorId, Long itemId) {
        DoctorServiceItem item = doctorServiceItemMapper.selectById(itemId);
        if (item == null || !item.getDoctorId().equals(doctorId)) {
            throw new BizException("医生服务不存在");
        }
        return item;
    }
}
