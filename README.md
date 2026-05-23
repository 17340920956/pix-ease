# PixEase Backend

PixEase 后端服务，提供用户认证、注册、管理接口。

## 技术栈

- [Spring Boot](https://spring.io/projects/spring-boot) 3.x
- [MyBatis Plus](https://baomidou.com) — ORM
- [MySQL](https://www.mysql.com) — 数据库
- [Redis](https://redis.io) — 缓存
- [JWT](https://jwt.io) — 认证令牌
- [JavaMail](https://javaee.github.io/javamail/) — 邮件服务

## 接口

| 接口 | 方法 | 说明 |
|---|---|---|
| `/api/user/send-code` | POST | 发送邮箱验证码 |
| `/api/user/register` | POST | 用户注册 |
| `/api/user/login` | POST | 用户登录 |
| `/api/user/info` | GET | 获取当前用户信息 |
| `/api/user/update` | PUT | 更新用户信息 |
| `/api/admin/login` | POST | 管理员登录 |
| `/api/admin/users` | GET | 用户列表（管理端） |

所有接口需要 `timestamp`、`nonce`、`sign` 签名验证。

## 本地开发

```bash
# 确保本地 MySQL 和 Redis 已启动

# 修改 src/main/resources/application.yml 中的数据库连接

# 启动服务
./mvnw spring-boot:run
```

服务默认运行在 `http://localhost:8080`。

## 构建部署

```bash
# 打包
./mvnw clean package -DskipTests

# Docker 部署
docker build -t pix-ease-backend .
```

Docker Compose 一键部署见 `deploy/docker-compose.yml`。

## 多环境配置

| 环境 | 配置文件 |
|---|---|
| 开发 | `application.yml` |
| 测试 | `application-qa.yml` |
| 生产 | `application-prod.yml` |

## 项目结构

```
src/main/java/com/pixease/
├── common/         # 公共类
│   ├── config/     # 配置类
│   ├── constant/   # 常量
│   ├── enums/      # 枚举
│   ├── exception/  # 异常处理
│   ├── result/     # 统一响应
│   └── utils/      # 工具类
├── controller/     # 控制器
├── dto/            # 数据传输对象
├── entity/         # 实体类
├── filter/         # 过滤器（签名验证、限流）
├── interceptor/    # 拦截器（认证）
├── mapper/         # MyBatis Mapper
├── service/        # 服务层
└── vo/             # 视图对象
```

## 日志

日志按年月分文件夹、按天分文件，见 `logback-spring.xml`。