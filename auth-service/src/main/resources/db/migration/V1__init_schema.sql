-- auth_db V1：refresh_token 表（访问令牌黑名单在 Redis，不落库）
CREATE TABLE refresh_token (
    id         BIGINT      NOT NULL COMMENT '主键（雪花 ID）',
    user_id    BIGINT      NOT NULL COMMENT '用户 ID',
    token      VARCHAR(64) NOT NULL COMMENT '随机刷新令牌',
    status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/REVOKED',
    expires_at DATETIME    NOT NULL COMMENT '过期时间',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_token_token (token),
    KEY idx_refresh_token_user (user_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '刷新令牌';
