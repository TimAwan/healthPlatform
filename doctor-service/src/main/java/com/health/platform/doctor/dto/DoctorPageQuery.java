package com.health.platform.doctor.dto;

import com.health.platform.core.result.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoctorPageQuery extends PageQuery {

    private String keyword;

    private String auditStatus;

    private Long hospitalId;

    private Long departmentId;
}
