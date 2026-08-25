-- system_db V2：RBAC 与操作日志（说明书第 11/21/25 章）
CREATE TABLE sys_role (
    id         BIGINT      NOT NULL COMMENT '主键',
    code       VARCHAR(50) NOT NULL COMMENT '角色编码，如 SYS_ADMIN/OPS_ADMIN',
    name       VARCHAR(50) NOT NULL COMMENT '角色名称',
    status     VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT 'ENABLED/DISABLED',
    remark     VARCHAR(200) NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色';

CREATE TABLE sys_permission (
    id         BIGINT      NOT NULL COMMENT '主键',
    code       VARCHAR(64) NOT NULL COMMENT '权限码：资源_动作，如 DOCTOR_APPROVE',
    name       VARCHAR(50) NOT NULL,
    type       VARCHAR(20) NOT NULL DEFAULT 'API' COMMENT 'API/BUTTON',
    status     VARCHAR(20) NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted    TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_permission_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '权限';

CREATE TABLE sys_menu (
    id         BIGINT       NOT NULL COMMENT '主键',
    parent_id  BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单 ID，0 为根',
    name       VARCHAR(50)  NOT NULL,
    path       VARCHAR(200) NULL COMMENT '路由路径',
    component  VARCHAR(200) NULL COMMENT '前端组件路径',
    icon       VARCHAR(100) NULL,
    sort       INT          NOT NULL DEFAULT 0,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ENABLED',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted    TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_sys_menu_parent (parent_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '菜单';

CREATE TABLE sys_user_role (
    id      BIGINT NOT NULL COMMENT '主键',
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_user_role (user_id, role_id),
    KEY idx_sur_role (role_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户-角色';

CREATE TABLE sys_role_permission (
    id            BIGINT NOT NULL COMMENT '主键',
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_permission (role_id, permission_id),
    KEY idx_srp_permission (permission_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色-权限';

CREATE TABLE sys_role_menu (
    id      BIGINT NOT NULL COMMENT '主键',
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_sys_role_menu (role_id, menu_id),
    KEY idx_srm_menu (menu_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '角色-菜单';

CREATE TABLE operation_log (
    id            BIGINT       NOT NULL COMMENT '主键',
    operator_id   BIGINT       NOT NULL COMMENT '操作人 ID',
    operator_name VARCHAR(50)  NOT NULL COMMENT '操作人名称',
    operation     VARCHAR(100) NOT NULL COMMENT '操作名称',
    target_type   VARCHAR(50)  NULL COMMENT '目标类型，如 DOCTOR',
    target_id     VARCHAR(64)  NULL COMMENT '目标 ID',
    request_id    VARCHAR(64)  NULL,
    ip            VARCHAR(50)  NULL,
    result        VARCHAR(20)  NOT NULL COMMENT 'SUCCESS/FAIL',
    detail        VARCHAR(1000) NULL COMMENT '附加信息 JSON',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_operation_log_operator (operator_id, created_at),
    KEY idx_operation_log_target (target_type, target_id),
    KEY idx_operation_log_created (created_at)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '操作日志（说明书 25 章）';
