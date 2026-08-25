-- file_db V1：文件元数据（说明书第 24 章：业务表存 file_id，不存 URL）
CREATE TABLE file_record (
    id            BIGINT       NOT NULL,
    file_id       VARCHAR(64)  NOT NULL COMMENT '对外文件标识',
    original_name VARCHAR(255) NOT NULL,
    storage_type  VARCHAR(20)  NOT NULL COMMENT 'LOCAL/OSS',
    storage_path  VARCHAR(500) NOT NULL COMMENT '本地路径或 OSS object key',
    bucket        VARCHAR(100) NULL,
    size          BIGINT       NOT NULL COMMENT '字节数',
    content_type  VARCHAR(100) NULL,
    biz_type      VARCHAR(50)  NULL COMMENT '业务类型，如 DOCTOR_ATTACHMENT',
    uploader_id   BIGINT       NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_file_record_file_id (file_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '文件元数据';
