-- system_db V3：一期种子数据
-- 权限矩阵（产品说明书 4.2）：签约医生管理（含审核）→ 运营管理员；医院/科室维护、RBAC、审计日志 → 系统管理员

INSERT INTO sys_role (id, code, name, status, remark) VALUES
(1, 'SYS_ADMIN', '系统管理员', 'ENABLED', '系统级管理权限'),
(2, 'OPS_ADMIN', '运营管理员', 'ENABLED', '客户、客服及签约医生资源运营');

INSERT INTO sys_permission (id, code, name, type) VALUES
(100, 'DOCTOR_VIEW', '医生查看', 'API'),
(101, 'DOCTOR_CREATE', '新增医生', 'API'),
(102, 'DOCTOR_UPDATE', '编辑医生', 'API'),
(103, 'DOCTOR_APPROVE', '医生审核通过', 'API'),
(104, 'DOCTOR_REJECT', '医生审核拒绝', 'API'),
(105, 'DOCTOR_SUSPEND', '医生暂停', 'API'),
(106, 'DOCTOR_TERMINATE', '医生解约', 'API'),
(107, 'DOCTOR_RESUBMIT', '医生重新提交审核', 'API'),
(108, 'DOCTOR_ON_SHELF', '医生上架', 'API'),
(109, 'DOCTOR_OFF_SHELF', '医生下架', 'API'),
(110, 'DOCTOR_SERVICE_VIEW', '医生服务查看', 'API'),
(111, 'DOCTOR_SERVICE_CREATE', '医生服务新增', 'API'),
(112, 'DOCTOR_SERVICE_UPDATE', '医生服务编辑', 'API'),
(113, 'DOCTOR_SERVICE_ON_SHELF', '医生服务上架', 'API'),
(114, 'DOCTOR_SERVICE_OFF_SHELF', '医生服务下架', 'API'),
(120, 'HOSPITAL_VIEW', '医院查看', 'API'),
(121, 'HOSPITAL_CREATE', '医院新增', 'API'),
(122, 'HOSPITAL_UPDATE', '医院编辑', 'API'),
(123, 'HOSPITAL_DELETE', '医院删除/禁用', 'API'),
(130, 'DEPARTMENT_VIEW', '科室查看', 'API'),
(131, 'DEPARTMENT_CREATE', '科室新增', 'API'),
(132, 'DEPARTMENT_UPDATE', '科室编辑', 'API'),
(133, 'DEPARTMENT_DELETE', '科室删除/禁用', 'API'),
(200, 'USER_VIEW', '后台用户查看', 'API'),
(201, 'USER_CREATE', '后台用户新增', 'API'),
(202, 'USER_UPDATE', '后台用户编辑', 'API'),
(203, 'USER_DISABLE', '后台用户启停', 'API'),
(204, 'USER_RESET_PASSWORD', '重置后台用户密码', 'API'),
(205, 'USER_ASSIGN_ROLE', '分配后台用户角色', 'API'),
(210, 'ROLE_VIEW', '角色查看', 'API'),
(211, 'ROLE_CREATE', '角色新增', 'API'),
(212, 'ROLE_UPDATE', '角色编辑', 'API'),
(213, 'ROLE_ASSIGN', '角色授权（权限/菜单）', 'API'),
(220, 'MENU_VIEW', '菜单查看', 'API'),
(221, 'MENU_UPDATE', '菜单编辑', 'API'),
(230, 'LOG_VIEW', '操作日志查看', 'API');

INSERT INTO sys_menu (id, parent_id, name, path, component, sort, status) VALUES
(10, 0, '医生管理', '/doctor', NULL, 1, 'ENABLED'),
(11, 10, '医生列表', '/doctor/list', 'doctor/list', 1, 'ENABLED'),
(12, 10, '医生审核', '/doctor/audit', 'doctor/audit', 2, 'ENABLED'),
(20, 0, '基础数据', '/base', NULL, 2, 'ENABLED'),
(21, 20, '医院管理', '/base/hospital', 'hospital/list', 1, 'ENABLED'),
(22, 20, '科室管理', '/base/department', 'department/list', 2, 'ENABLED'),
(30, 0, '系统管理', '/system', NULL, 3, 'ENABLED'),
(31, 30, '用户管理', '/system/user', 'system/user', 1, 'ENABLED'),
(32, 30, '角色管理', '/system/role', 'system/role', 2, 'ENABLED'),
(33, 30, '菜单管理', '/system/menu', 'system/menu', 3, 'ENABLED'),
(34, 30, '操作日志', '/system/log', 'system/log', 4, 'ENABLED');

-- 系统管理员：全部权限、全部菜单
INSERT INTO sys_role_permission (id, role_id, permission_id)
SELECT p.id + 900000, 1, p.id FROM sys_permission p;

INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT m.id + 900000, 1, m.id FROM sys_menu m;

-- 运营管理员：仅医生管理相关权限与菜单（医院/科室维护属系统管理员）
INSERT INTO sys_role_permission (id, role_id, permission_id) VALUES
(950000, 2, 100), (950001, 2, 101), (950002, 2, 102), (950003, 2, 103),
(950004, 2, 104), (950005, 2, 105), (950006, 2, 106), (950007, 2, 107),
(950008, 2, 108), (950009, 2, 109), (950010, 2, 110), (950011, 2, 111),
(950012, 2, 112), (950013, 2, 113), (950014, 2, 114);

INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
(960000, 2, 10), (960001, 2, 11), (960002, 2, 12);

-- 初始管理员（固定 ID=1，由 AdminUserInitializer 创建）绑定系统管理员角色
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);
