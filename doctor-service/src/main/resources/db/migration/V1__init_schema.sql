-- doctor_db V1：签约医生核心模型（说明书第 16-19/21 章 + 已确认决策 4/5/8）

CREATE TABLE hospital (
    id         BIGINT      NOT NULL COMMENT '主键',
    name       VARCHAR(100) NOT NULL,
    code       VARCHAR(50)  NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_hospital_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '医院';

CREATE TABLE department (
    id          BIGINT      NOT NULL,
    hospital_id BIGINT      NOT NULL,
    name        VARCHAR(100) NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_hospital_code (hospital_id, code),
    KEY idx_department_hospital (hospital_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '科室';

CREATE TABLE doctor (
    id                 BIGINT       NOT NULL,
    name               VARCHAR(50)  NOT NULL,
    gender             VARCHAR(10)  NOT NULL DEFAULT 'MALE' COMMENT 'MALE/FEMALE',
    phone              VARCHAR(20)  NOT NULL,
    avatar_file_id     BIGINT       NULL COMMENT '头像 file_id',
    hospital_id        BIGINT       NOT NULL COMMENT '单绑定医院（19 章，多绑定需负责人确认）',
    department_id      BIGINT       NOT NULL COMMENT '单绑定科室',
    title              VARCHAR(50)  NULL COMMENT '职称',
    specialty          VARCHAR(500) NULL COMMENT '擅长领域',
    intro              VARCHAR(2000) NULL COMMENT '个人简介',
    audit_status       VARCHAR(20)  NOT NULL DEFAULT 'PENDING_REVIEW' COMMENT '审核状态（16 章状态机）',
    cooperation_status VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT '合作状态：ACTIVE/TERMINATED',
    service_status     VARCHAR(20)  NOT NULL DEFAULT 'OFF_SHELF' COMMENT '医生上下架：ON_SHELF/OFF_SHELF',
    user_id            BIGINT       NULL COMMENT '预留：医生登录账号关联（决策 4，二期启用）',
    created_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted            TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_doctor_phone (phone),
    KEY idx_doctor_audit_status (audit_status),
    KEY idx_doctor_hospital_department (hospital_id, department_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '签约医生';
-- TODO: 医疗敏感数据字段级加密（TODO-001）：手机号等字段未来需评估加密/脱敏/访问审计

CREATE TABLE doctor_audit (
    id            BIGINT      NOT NULL,
    doctor_id     BIGINT      NOT NULL,
    from_status   VARCHAR(20) NOT NULL,
    to_status     VARCHAR(20) NOT NULL,
    action        VARCHAR(20) NOT NULL COMMENT 'APPROVE/REJECT/SUSPEND/TERMINATE/RESUBMIT',
    reason        VARCHAR(500) NULL,
    operator_id   BIGINT      NOT NULL,
    operator_name VARCHAR(50)  NOT NULL,
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_doctor_audit_doctor (doctor_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '医生审核流水（状态机唯一合法入口）';

CREATE TABLE doctor_service (
    id          BIGINT        NOT NULL,
    doctor_id   BIGINT        NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    type        VARCHAR(50)   NULL COMMENT '服务类型',
    price       DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '服务价格',
    description VARCHAR(1000) NULL,
    status      VARCHAR(20)   NOT NULL DEFAULT 'OFF_SHELF' COMMENT '服务上下架，与医生上下架区分（18 章）',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_doctor_service_doctor (doctor_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '医生服务项目';

CREATE TABLE doctor_attachment (
    id         BIGINT     NOT NULL,
    doctor_id  BIGINT     NOT NULL,
    file_id    BIGINT     NOT NULL COMMENT 'file-service 文件标识（24 章：业务表存 file_id）',
    file_type  VARCHAR(50) NOT NULL COMMENT 'PRACTICE_LICENSE执业证/QUALIFICATION资格证/OTHER',
    created_at DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted    TINYINT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_doctor_attachment (doctor_id, file_id),
    KEY idx_doctor_attachment_doctor (doctor_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '医生资质附件';
