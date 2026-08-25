package com.health.platform.doctor.audit;

import com.health.platform.doctor.entity.DoctorAudit;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AuditHistoryVO {

    private List<DoctorAudit> records;
}
