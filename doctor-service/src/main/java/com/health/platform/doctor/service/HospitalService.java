package com.health.platform.doctor.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.PageQuery;
import com.health.platform.core.result.PageResult;
import com.health.platform.doctor.entity.Department;
import com.health.platform.doctor.entity.Doctor;
import com.health.platform.doctor.entity.Hospital;
import com.health.platform.doctor.mapper.DepartmentMapper;
import com.health.platform.doctor.mapper.DoctorMapper;
import com.health.platform.doctor.mapper.HospitalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalMapper hospitalMapper;
    private final DepartmentMapper departmentMapper;
    private final DoctorMapper doctorMapper;

    public PageResult<Hospital> page(PageQuery query, String keyword) {
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<Hospital>()
                .like(StringUtils.hasText(keyword), Hospital::getName, keyword)
                .orderByDesc(Hospital::getId);
        Page<Hospital> page = hospitalMapper.selectPage(Page.of(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), page.getRecords());
    }

    public List<Hospital> listEnabled() {
        return hospitalMapper.selectList(new LambdaQueryWrapper<Hospital>()
                .eq(Hospital::getStatus, "ENABLED")
                .orderByAsc(Hospital::getName));
    }

    public Hospital create(Hospital hospital) {
        assertCodeUnique(hospital.getCode(), null);
        hospital.setStatus("ENABLED");
        hospitalMapper.insert(hospital);
        return hospital;
    }

    public void update(Long id, Hospital input) {
        assertExists(id);
        assertCodeUnique(input.getCode(), id);
        Hospital update = new Hospital();
        update.setId(id);
        update.setName(input.getName());
        update.setCode(input.getCode());
        hospitalMapper.updateById(update);
    }

    /** 决策 8：被医生或科室引用的医院仅允许禁用，删除返回业务错误 */
    public void delete(Long id) {
        assertExists(id);
        Long doctorCount = doctorMapper.selectCount(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getHospitalId, id));
        Long departmentCount = departmentMapper.selectCount(new LambdaQueryWrapper<Department>()
                .eq(Department::getHospitalId, id));
        if (doctorCount > 0 || departmentCount > 0) {
            throw new BizException("该医院已被科室或医生引用，仅允许禁用");
        }
        hospitalMapper.deleteById(id);
    }

    public void changeStatus(Long id, boolean enabled) {
        assertExists(id);
        Hospital update = new Hospital();
        update.setId(id);
        update.setStatus(enabled ? "ENABLED" : "DISABLED");
        hospitalMapper.updateById(update);
    }

    private void assertExists(Long id) {
        if (hospitalMapper.selectById(id) == null) {
            throw new BizException("医院不存在");
        }
    }

    private void assertCodeUnique(String code, Long excludeId) {
        Hospital existing = hospitalMapper.selectOne(new LambdaQueryWrapper<Hospital>()
                .eq(Hospital::getCode, code));
        if (existing != null && !existing.getId().equals(excludeId)) {
            throw new BizException("医院编码已存在: " + code);
        }
    }
}
