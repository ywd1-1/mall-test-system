# mall-test-system V2

面向软件测试实习项目的中等复杂度电商订单管理系统。V2 保留商品、购物车和订单能力，重点增加收货地址快照、订单状态机、状态日志、权限校验、组合查询、数据库锁和软删除等可测试场景。

## 技术栈

| 模块 | 技术 |
| --- | --- |
| 后端 | Spring Boot 2.7 + Spring MVC + Spring Data JPA |
| 前端 | Vue3 + Vite + Element Plus |
| 数据库 | MySQL 8.0 / InnoDB |
| 接口文档 | springdoc-openapi / Swagger UI |
| 部署 | Docker Compose + Nginx |

## 默认账号

| 场景 | 用户名 | 密码 | 状态 |
| --- | --- | --- | --- |
| 普通用户 | `user` | `123456` | 启用 |
| 管理员 | `admin` | `123456` | 启用 |
| 禁用登录 | `disabled_user` | `123456` | 禁用 |

表中管理员密码用于本地初始化；Docker 部署会使用 `deploy/.env` 中的 `ADMIN_PASSWORD` 覆盖它。

初始化数据还包含两个收货地址、正常/低/零库存商品、下架商品、软删除商品，以及 `CREATED`、`PAID`、`SHIPPED`、`COMPLETED`、`CANCELLED` 各一条订单。

## 启动

Windows 本地启动：双击根目录的 `start-project.bat`。脚本会检查 MySQL `3308`、启动 Spring Boot `8080` 和 Vue/Vite `5173`；首次运行缺少前端依赖时会自动执行 `pnpm install`。

Docker 一键启动：

```bash
cd mall-test-system/deploy
cp .env.example .env
# 修改 .env 中的三个密码后再启动
docker compose up -d --build
```

云服务器只需公开 `80`；MySQL `3306` 和后端 `8080` 默认只绑定服务器本机。完整步骤见 [deploy/部署说明.md](deploy/部署说明.md)。

本地开发需要 Java 8、Maven、Node.js、pnpm 和 MySQL 8.0。先准备 `mall_test_system` 数据库并执行 `sql/init.sql`，再启动前后端：

```bash
cd backend
mvn spring-boot:run

cd ../frontend
pnpm install
pnpm dev
```

访问地址：

- 前端：`http://localhost:5173`
- 后端：`http://localhost:8080`
- Swagger：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`

MySQL 首次启动会执行 [sql/init.sql](sql/init.sql)。已有 Docker 数据卷不会自动重放脚本；测试环境重置数据前请确认没有需要保留的数据，再执行：

```bash
cd deploy
docker compose down -v
docker compose up -d --build
```

## 鉴权与响应

登录后使用 `Authorization: Bearer {token}`。用户密码使用 BCrypt 保存；旧数据库中的明文密码会在启动时自动迁移。Docker 部署时，管理员密码由 `deploy/.env` 的 `ADMIN_PASSWORD` 设置。所有接口统一返回：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

非法参数和 JSON 返回 400，未登录返回 401，无权限返回 403，资源或接口不存在返回 404。token 保存在后端内存中，服务重启后需要重新登录。

## 接口清单

### 认证与用户

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/login` | 登录，禁用用户不能登录 |
| POST | `/api/register` | 注册普通用户 |
| GET | `/api/user/current` | 当前用户 |
| GET | `/api/admin/users?username=&status=&page=1&size=10` | 管理员分页查询普通用户 |
| PUT | `/api/admin/users/{id}/status` | 启用或禁用普通用户 |

### 收货地址

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/addresses` | 查询当前用户地址 |
| POST | `/api/addresses` | 新增地址 |
| PUT | `/api/addresses/{id}` | 修改自己的地址 |
| DELETE | `/api/addresses/{id}` | 删除自己的地址 |
| PUT | `/api/addresses/{id}/default` | 设置唯一默认地址 |

### 商品与购物车

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/products?keyword=&category=&page=1&size=8` | 可售商品分页 |
| GET | `/api/products/{id}` | 可售商品详情 |
| GET | `/api/admin/products?keyword=&category=&status=&page=1&size=8` | 管理员商品分页，默认排除软删除商品 |
| POST | `/api/admin/products` | 新增商品 |
| PUT | `/api/admin/products/{id}` | 修改商品 |
| PUT | `/api/admin/products/{id}/status` | 上下架 |
| PUT | `/api/admin/products/{id}/stock` | 修改库存 |
| DELETE | `/api/admin/products/{id}` | 软删除，删除后不能再次上架 |
| GET | `/api/cart` | 查询购物车 |
| POST | `/api/cart` | 添加购物车 |
| PUT | `/api/cart/{id}` | 修改数量 |
| DELETE | `/api/cart/{id}` | 删除购物车条目 |

### 订单与统计

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/orders` | 创建订单，必须传 `addressId` 和非空 `cartIds` |
| GET | `/api/orders?status=&page=1&size=10` | 当前用户订单分页 |
| GET | `/api/orders/{id}` | 用户订单详情、地址快照和时间线 |
| POST | `/api/orders/{id}/pay` | 支付待支付订单 |
| DELETE | `/api/orders/{id}` | 取消待支付订单并恢复一次库存 |
| POST | `/api/orders/{id}/confirm-receipt` | 所属用户确认已发货订单收货 |
| GET | `/api/admin/orders?orderNo=&username=&status=&startTime=&endTime=&page=1&size=10` | 管理员组合查询订单 |
| GET | `/api/admin/orders/{id}` | 管理员订单详情 |
| POST | `/api/admin/orders/{id}/ship` | 管理员发货 |
| GET | `/api/statistics` | 当前角色可见范围内的统计数据 |

创建订单示例：

```json
{
  "cartIds": [1, 2],
  "addressId": 1
}
```

发货示例：

```json
{
  "shippingCompany": "顺丰速运",
  "trackingNumber": "SF1000000001"
}
```

时间参数使用 ISO 本地日期时间，例如 `2026-07-01T00:00:00`。

## 核心规则

- `CREATED` 可以支付或取消。
- `PAID` 只能由管理员发货。
- `SHIPPED` 只能由订单所属用户确认收货。
- `PAID`、`SHIPPED`、`COMPLETED` 不能直接取消；`CANCELLED` 不能支付。
- 重复支付、取消、发货和确认收货返回明确的 400 错误。
- 状态变更使用订单悲观写锁；取消库存恢复与状态日志写入在同一事务中完成。
- 每次创建、支付、发货、完成和取消都写入 `order_status_log`。
- 订单保存收货地址快照，地址修改或删除不影响历史订单。
- 同一用户最多一个默认地址，应用层用户锁和数据库唯一约束共同保证一致性。
- 普通用户只能操作自己的地址和订单，不能访问 `/api/admin/**`。

## 验证资料

- 测试点脑图：[docs/电商订单管理系统.xmind](docs/电商订单管理系统.xmind)
- 功能测试用例：[docs/电商订单管理系统测试用例.xlsx](docs/电商订单管理系统测试用例.xlsx)
- 接口测试用例：[docs/接口测试用例.xlsx](docs/接口测试用例.xlsx)
- 缺陷记录：[docs/缺陷用例.xlsx](docs/缺陷用例.xlsx)
- Postman Collection：[docs/postman_collection.json](docs/postman_collection.json)
- Postman Environment：[docs/postman_environment.json](docs/postman_environment.json)
- Newman HTML 报告：[docs/newman-report.html](docs/newman-report.html)
- JMeter 两用户并发下单脚本：[docs/并发测试.jmx](docs/并发测试.jmx)
- JMeter CSV 参数文件：[docs/信息.csv](docs/信息.csv)
- pytest 接口自动化：[api-tests](api-tests)
- 测试总结：[docs/测试总结报告.md](docs/测试总结报告.md)

本轮共执行 116 条功能测试用例和 49 条接口测试用例，最终均通过；累计发现 2 个高优先级缺陷，均已修复并完成回归。Newman 批量执行 54 个请求项和 21 条核心断言，最终失败数为 0。详细统计、风险和结论以测试总结报告为准。

pytest 接口自动化目前包含登录、商品、购物车和订单查询共 11 条用例。进入 `api-tests` 安装依赖后可执行：

```bash
pip install -r requirements.txt
pytest -v
```

后端可运行 `mvn test` 验证重复取消库存只恢复一次、已取消订单不能支付等规则；是否通过应以本机实际命令输出为准。
