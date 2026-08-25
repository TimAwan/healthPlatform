-- system_db V1：后台用户表（M2 认证闭环所需）
-- 角色/权限/菜单/操作日志等表在 M3 增量迁移中补充
CREATE TABLE sys_user (
    id            BIGINT       NOT NULL COMMENT '主键（雪花 ID）',
    username      VARCHAR(50)  NOT NULL COMMENT '登录名',
    password      VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
    real_name     VARCHAR(50)  NULL COMMENT '姓名',
    phone         VARCHAR(20)  NULL COMMENT '手机号',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    last_login_at DATETIME     NULL COMMENT '最后登录时间',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '后台用户';
