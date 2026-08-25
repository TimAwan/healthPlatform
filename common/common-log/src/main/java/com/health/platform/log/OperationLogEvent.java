package com.health.platform.log;

import lombok.Data;

@Data
public class OperationLogEvent {

    private Long operatorId;

    private String operatorName;

    private String operation;

    private String targetType;

    private String targetId;

    private String requestId;

    private String ip;

    private String result;

    private String detail;
}
