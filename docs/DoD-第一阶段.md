# 第一阶段 Definition of Done 验收清单（说明书第 38 章）

状态说明：
- ✅ 已验证（编译 / 单元测试 / 构建产物）
- ⏳ 待运行时验证（本机未安装 Docker，依赖环境就绪后按本文档逐项执行）

| # | 验收项 | 状态 | 验证方式 |
|---|--------|------|----------|
| 1 | 后端服务可以启动 | ⏳ | 依次启动 system/auth/doctor/file/gateway，观察 Nacos 注册 |
| 2 | Gateway 可以正常路由 | ⏳ | `GET http://localhost:9000/api/v1/auth/captcha` 返回验证码 |
| 3 | 后台用户可以登录 | ⏳ | 前端登录页或 `POST /api/v1/auth/login`（admin / ADMIN_INITIAL_PASSWORD 环境变量，默认 Admin@123456） |
| 4 | JWT 正常工作 | ⏳ | 无 Token 访问 `/api/v1/doctors` 返回 401 统一结构；携带 Token 正常返回 |
| 5 | RBAC 正常工作 | ⏳ | admin（系统管理员）可见全部菜单；仅授予 DOCTOR_* 的角色无法访问 ROLE_VIEW 接口（403） |
| 6 | 医生列表正常 | ⏳ | `GET /api/v1/doctors` 分页、筛选 |
| 7 | 医生新增正常 | ⏳ | `POST /api/v1/doctors`（单测覆盖校验逻辑 DoctorManageServiceTest） |
| 8 | 医生编辑正常 | ⏳ | `PUT /api/v1/doctors/{id}` |
| 9 | 医生详情正常 | ⏳ | `GET /api/v1/doctors/{id}`（含医院/科室名、附件） |
| 10 | 医生审核正常 | ⏳ | `POST /api/v1/doctors/{id}/approve`（状态机全矩阵单测通过 DoctorAuditStateMachineTest） |
| 11 | 医生驳回正常（原因必填） | ⏳ | `POST /api/v1/doctors/{id}/reject`，缺原因返回业务错误（单测覆盖） |
| 12 | 医生暂停正常（联动下架） | ⏳ | suspend 后医生与全部服务项目 OFF_SHELF（单测覆盖） |
| 13 | 医生解约正常（合作终止） | ⏳ | terminate 后 cooperation_status=TERMINATED（单测覆盖） |
| 14 | 医生上下架正常 | ⏳ | 仅 APPROVED 可上架（单测覆盖）；下架联动服务下架 |
| 15 | 医院管理正常 | ⏳ | `/api/v1/hospitals` CRUD + 启停 |
| 16 | 科室管理正常 | ⏳ | `/api/v1/departments` CRUD + 启停 |
| 17 | 医生资质文件上传正常 | ⏳ | 前端上传 jpg/png/pdf → file_id 存 doctor_attachment → 详情可查看（类型白名单单测覆盖） |
| 18 | 操作日志正常 | ⏳ | 登录/审核/上下架后 `GET /api/v1/system/operation-logs` 可查 |
| 19 | 越权访问被拦截 | ⏳ | 无权限角色调用 DOCTOR_APPROVE 接口返回 403001 统一结构 |
| 20 | 基础测试通过 | ✅ | `mvn clean install`：9 模块全部 BUILD SUCCESS，55 个单元测试通过（JWT/登录/验证码/RBAC/状态机/上架规则/删除保护/文件校验） |
| 21 | Docker Compose 可以启动依赖 | ⏳ | `docker compose up -d` 后 MySQL(4 schema)/Redis/RabbitMQ/Nacos 健康检查通过（配置已就绪，本机待装 Docker Desktop） |
| 22 | README 可让新开发者启动项目 | ✅ | 后端 README（快速启动/端口/环境变量）+ 前端 README（开发/构建/约定） |

## 运行时验证执行步骤（Docker 就绪后）

```bash
# 1. 依赖
cd health-platform && cp .env.example .env && docker compose up -d

# 2. 后端（顺序启动，或 IDEA 中全选启动）
mvn spring-boot:run -pl system-service
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl doctor-service
mvn spring-boot:run -pl file-service
mvn spring-boot:run -pl gateway-service

# 3. 前端
cd ../health-platform-web && npm install && npm run dev

# 4. 浏览器访问 http://localhost:5173 登录并逐项勾掉上表 ⏳ 项
```

注意：系统 JAVA_HOME 当前指向无效路径 `D:\JavaApps\JDK8`，需改为 `D:\Application\JDK`（JDK 17 实际位置），否则 Maven/IDEA 报错。

## 已知边界（代码中已留 TODO）

- 医疗敏感数据字段级加密（TODO-001，说明书第 15 章）
- AI 自动分配医生/科室（TODO-002，预留 DoctorAssignmentService 接口）
- 医生 SUSPENDED 状态的流出转换（恢复/解约）说明书未定义，实现前需项目负责人确认
- 行级数据权限（客服仅自己客户、医生仅分配数据、财务隔离）留二期
- 文件访问权限细化（当前仅要求登录；二期客户报告上线前必须按角色细化）
- 前端 401 静默刷新 Token 未接入（后端 /auth/refresh 已就绪）
