# 健康管理平台 TODO 清单

> 依据:《健康管理平台Coding开发说明书(第一版).md》(业务规则唯一权威)与 `docs/DoD-第一阶段.md` 验收清单。
> 生成日期:2026-08-26。完成一项请勾选并注明日期。

## 当前总体状态

- 一期(M1-M7)代码已全部完成并提交:后端 10 commits、前端 1 commit,55 个单元测试通过
- 2026-08-26 起进入**运行时验证阶段**(Docker 环境已就绪,4 个基础容器全部健康)
- 验证中已发现并修复 3 个运行时缺陷(见下),**修复代码尚未提交**

---

## P0 - 立即处理

- [ ] **提交今天的 4 个修复文件**(循环依赖 / loadbalancer / -parameters):
  `pom.xml`、`common/common-security/pom.xml`、`HealthSecurityAutoConfiguration.java`、`RedisPermissionProvider.java`
- [ ] IDEA 中 Reload Maven + Rebuild Project + 重启所有服务(使 `-parameters` 修复生效,doctor-service 报错已修,待重启验证)

## P1 - 一期 DoD 运行时验证(说明书第 38 章,共 20 项待验)

### 服务启动(1/5)

- [x] system-service 启动(9102,IDEA 中运行正常)— 2026-08-26
- [x] auth-service 启动(9101,迁移完成、注册 Nacos)— 2026-08-26
- [ ] doctor-service 启动验证(9103,等 -parameters 重建后重启)
- [ ] file-service 启动验证
- [ ] gateway-service 启动验证(9000)

### 核心链路

- [ ] Gateway 正常路由:`GET http://localhost:9000/api/v1/auth/captcha` 返回验证码
- [ ] 后台登录闭环:前端登录页 admin / Admin@123456 + 图形验证码,获取 Token
- [ ] JWT 正常:无 Token 访问 `/api/v1/doctors` 返回 401 统一结构;带 Token 正常返回
- [ ] RBAC 正常:admin 可见全部菜单;仅授 DOCTOR_* 的角色访问 ROLE_VIEW 返回 403

### 医生管理模块(核心业务)

- [ ] 医生列表(分页、筛选)
- [ ] 医生新增
- [ ] 医生编辑
- [ ] 医生详情(含医院/科室名、附件)
- [ ] 医生审核通过
- [ ] 医生驳回(原因必填校验)
- [ ] 医生暂停(联动全部服务下架)
- [ ] 医生解约(cooperation_status=TERMINATED)
- [ ] 医生上下架(仅 APPROVED 可上架)
- [ ] 医院管理 CRUD + 启停(被引用的仅允许禁用)
- [ ] 科室管理 CRUD + 启停
- [ ] 医生资质文件上传(jpg/png/pdf → file_id 存 doctor_attachment)

### 系统与安全

- [ ] 操作日志:登录/审核/上下架后 `GET /api/v1/system/operation-logs` 可查
- [ ] 越权访问被拦截(无权限角色调用 DOCTOR_APPROVE 返回 403001)

## P2 - 一期范围内的遗留功能项

- [ ] **前端 401 静默刷新 Token 未接入**(后端 `/api/v1/auth/refresh` 已就绪,前端 axios 拦截器待补)
- [ ] 前端页面对"操作成功/失败反馈、危险操作确认"逐页自查(说明书第 33 章要求,未逐页验证)

## 阻塞项 - 必须先问项目负责人(说明书第 40 章)

- [ ] 医生 **SUSPENDED 状态的流出转换**(恢复?解约?)说明书第 16 章未定义 → 实现任何"恢复"类操作前必须先问,不得猜测

## 环境遗留

- [ ] 确认系统 JAVA_HOME 是否仍指向无效路径 `D:\JavaApps\JDK8`(应为 `D:\Application\JDK`;当前终端 mvn 可用,可能已修,确认一次即可)
- [ ] Docker Desktop 磁盘镜像位置确认在 D 盘(今日已配置,重启 Docker 后确认生效即可)

---

## 二期及以后(本期明确不做,防止提前实现)

已确认决策(2026-08-25):一期不含小程序/user-service;行级数据权限(客服/医生/财务隔离)二期;文件访问权限细化(按角色)二期客户报告上线前必须完成。

代码中已预留的接口/TODO(说明书第 15、31 章,代码内已有标记):

| 预留点 | 位置 | 说明书依据 |
|---|---|---|
| `DoctorAssignmentService`(Manual/Rule/AI 策略扩展) | doctor-service/assignment/ | 第 15 章 AI 自动分配 |
| `FileStorageService` 抽象(本地/OSS 已实现) | file-service | 第 7 章 |
| 医疗敏感数据字段级加密 TODO | 代码 TODO 标记 | 第 15/30 章 |

## 长期 TODO(说明书第 41 章原清单,均为二/三期)

TODO-001 医疗敏感数据字段级加密 

 TODO-002 AI 自动分配医生科室 

TODO-003 微信真实支付 

TODO-004 支付宝真实支付 

TODO-005 积分体系 

TODO-006 积分提现 

TODO-007 完整退款体系 

TODO-008 体检报告解读 

TODO-009 在线健康咨询 

TODO-010 消息通知体系 

TODO-011 生产 Kubernetes 

TODO-012 数据库主从与高可用 

TODO-013 Redis 集群 

TODO-014 CDN 

TODO-015 APM 与生产监控告警