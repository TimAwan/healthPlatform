# 健康管理平台（后端）

健康管理平台 V1.0 后端微服务。业务规则以《健康管理平台Coding开发说明书（第一版）.md》为准。

## 技术栈

Java 17 / Spring Boot 3.2.x / Spring Cloud 2023.0.x / Spring Cloud Alibaba 2023.0.1.x / Spring Cloud Gateway / Nacos / MySQL 8.0 / Redis 7 / RabbitMQ / MyBatis-Plus / Flyway / JWT

## 模块

| 模块 | 说明 |
|------|------|
| common/common-core | 统一响应、错误码、分页、业务异常、常量 |
| common/common-web | 全局异常处理、request_id 过滤器、Jackson 配置 |
| common/common-security | JWT 工具、用户上下文、权限注解与拦截器 |
| common/common-log | 操作日志注解与 AOP |
| gateway-service | API 统一入口、路由、JWT 校验 |
| auth-service | 登录、JWT 签发/刷新/注销、登录失败控制、图形验证码 |
| system-service | 后台用户、角色、权限、菜单、RBAC、操作日志 |
| doctor-service | 签约医生、审核状态机、医院、科室、医生服务 |
| file-service | 文件存储抽象、本地/OSS 实现 |

## 快速启动（开发环境）

前置要求：JDK 17、Maven 3.8+、Docker Desktop。

```bash
# 1. 准备环境变量
cp .env.example .env   # 按需修改

# 2. 启动依赖（MySQL/Redis/RabbitMQ/Nacos）
docker compose up -d

# 3. 编译
mvn clean install

# 4. 启动服务（顺序建议）
#   system-service -> auth-service -> doctor-service -> file-service -> gateway-service
#   各服务在各自模块目录下执行: mvn spring-boot:run
```

默认端口规划：gateway 9000、auth 9101、system 9102、doctor 9103、file 9104、Nacos 控制台 8848（nacos/nacos）。

## 初始管理员

system-service 首次启动且 `sys_user` 为空时自动创建系统管理员（ID 固定为 1，并绑定 SYS_ADMIN 角色）：

- 用户名：环境变量 `ADMIN_USERNAME`（默认 `admin`）
- 初始密码：环境变量 `ADMIN_INITIAL_PASSWORD`（默认 `Admin@123456`），登录后请由系统管理员重置

种子角色与权限：SYS_ADMIN（全部权限/菜单）、OPS_ADMIN（医生管理全部权限 + 医生管理菜单）。

## 环境注意

- 本机 JAVA_HOME 需指向 JDK 17 实际安装目录（如 `D:\Application\JDK`），若指向无效路径 Maven 会报 "JAVA_HOME is not defined correctly"
- 前端仓库：`../health-platform-web`（Vue 3 + Element Plus，dev 端口 5173，代理 `/api` 到 9000）

## 验收

第一阶段 22 项 DoD 清单与运行时验证步骤见 `docs/DoD-第一阶段.md`。

## 环境配置

敏感配置一律通过环境变量注入（DB_PASSWORD、REDIS_PASSWORD、JWT_SECRET、OSS_ACCESS_KEY 等），
禁止硬编码（说明书第 36 章）。环境 profile：dev / test / prod。

## 开发规范

- API 统一前缀 `/api/v1`，统一响应 `{code, message, data}`
- 提交信息：feat / fix / refactor / docs / test / chore
- 禁止提交 .env、密钥、数据库密码、JWT Secret
