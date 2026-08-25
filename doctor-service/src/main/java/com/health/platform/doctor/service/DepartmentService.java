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
public class DepartmentService {

    private final DepartmentMapper departmentMapper;
    private final HospitalMapper hospitalMapper;
    private final DoctorMapper doctorMapper;

    public PageResult<Department> page(PageQuery query, Long hospitalId, String keyword) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<Department>()
                .eq(hospitalId != null, Department::getHospitalId, hospitalId)
                .like(StringUtils.hasText(keyword), Department::getName, keyword)
                .orderByDesc(Department::getId);
        Page<Department> page = departmentMapper.selectPage(Page.of(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page.getTotal(), query.getPage(), query.getSize(), page.getRecords());
    }

    public List<Department> listByHospital(Long hospitalId) {
        return departmentMapper.selectList(new LambdaQueryWrapper<Department>()
                .eq(Department::getHospitalId, hospitalId)
                .eq(Department::getStatus, "ENABLED")
                .orderByAsc(Department::getName));
    }

    public Department create(Department department) {
        Hospital hospital = hospitalMapper.selectById(department.getHospitalId());
        if (hospital == null) {
            throw new BizException("所属医院不存在");
        }
        Long duplicated = departmentMapper.selectCount(new LambdaQueryWrapper<Department>()
                .eq(Department::getHospitalId, department.getHospitalId())
                .eq(Department::getCode, department.getCode()));
        if (duplicated > 0) {
            throw new BizException("该医院下科室编码已存在: " + department.getCode());
        }
        department.setStatus("ENABLED");
        departmentMapper.insert(department);
        return department;
    }

    public void update(Long id, Department input) {
        if (departmentMapper.selectById(id) == null) {
            throw new BizException("科室不存在");
        }
        Long duplicated = departmentMapper.selectCount(new LambdaQueryWrapper<Department>()
                .eq(Department::getHospitalId, input.getHospitalId())
                .eq(Department::getCode, input.getCode())
                .ne(Department::getId, id));
        if (duplicated > 0) {
            throw new BizException("该医院下科室编码已存在: " + input.getCode());
        }
        Department update = new Department();
        update.setId(id);
        update.setHospitalId(input.getHospitalId());
        update.setName(input.getName());
        update.setCode(input.getCode());
        departmentMapper.updateById(update);
    }

    /** 决策 8：被医生引用的科室仅允许禁用 */
    public void delete(Long id) {
        if (departmentMapper.selectById(id) == null) {
            throw new BizException("科室不存在");
        }
        Long doctorCount = doctorMapper.selectCount(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getDepartmentId, id));
        if (doctorCount > 0) {
            throw new BizException("该科室已被医生引用，仅允许禁用");
        }
        departmentMapper.deleteById(id);
    }

    public void changeStatus(Long id, boolean enabled) {
        if (departmentMapper.selectById(id) == null) {
            throw new BizException("科室不存在");
        }
        Department update = new Department();
        update.setId(id);
        update.setStatus(enabled ? "ENABLED" : "DISABLED");
        departmentMapper.updateById(update);
    }
}
