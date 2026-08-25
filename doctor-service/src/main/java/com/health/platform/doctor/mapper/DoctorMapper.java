package com.health.platform.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.health.platform.doctor.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
}
