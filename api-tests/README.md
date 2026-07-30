# 电商订单管理系统接口自动化测试

基于 `pytest + requests + Allure` 的接口自动化测试，覆盖登录、商品、购物车和用户订单核心接口。当前共收集 17 条测试实例，2026-07-30 本地执行结果为 `17 passed`。

## 项目结构

```text
api-tests/
├── test/
│   ├── api.py                # 测试地址、账号、Token 和公共接口函数
│   ├── conftest.py           # headers、product_id、address_id 公共 fixture
│   ├── test_auth.py          # 登录及参数化异常场景
│   ├── test_products.py      # 商品分页、详情和不存在场景
│   ├── test_cart.py          # 购物车增查改删、鉴权和库存场景
│   └── test_orders.py        # 创建、查询、取消和鉴权场景
├── .gitignore              # 忽略虚拟环境和本地 Allure 生成目录
├── pytest.ini              # pytest 收集规则及 Allure 结果输出配置
└── requirements.txt
```

测试地址、测试账号、密码和超时时间位于 `test/api.py` 顶部。账号应为专用测试账号，避免与手工测试数据混用。

## 安装与运行

```bash
pip install -r requirements.txt
pytest -v
allure serve allure-results
```

`pytest.ini` 已配置每次运行前清理旧数据，并将本次结果写入 `allure-results`。查看报告前需要在本机安装 Allure CLI，并确保 `allure --version` 能正常显示版本号。

只运行某个模块：

```bash
pytest test/test_cart.py -v
pytest test/test_orders.py -v
```

## Allure 测试报告

测试用例通过 `feature`、`story` 和 `title` 分别展示业务模块、功能场景和中文用例名称；公共接口函数通过 `step` 展示登录、加购、删除购物车、创建订单、查询订单和取消订单等调用步骤。

运行 pytest 后直接打开临时报告：

```bash
allure serve allure-results
```

需要生成固定报告目录时执行：

```bash
allure generate allure-results -o allure-report --clean
allure open allure-report
```

`allure-results/` 和 `allure-report/` 均为本地生成内容，已加入 `.gitignore`，不提交到仓库。

## 当前覆盖范围

| 模块 | 测试实例 | 场景 |
| --- | ---: | --- |
| 登录 | 5 | 正常登录；错误用户名、错误密码、用户名为空、密码为空 |
| 商品 | 3 | 分页查询、商品详情、不存在的商品 |
| 购物车 | 6 | 正常加购、无 Token、查询、修改数量、删除、库存不足 |
| 订单 | 3 | 无 Token 创建、创建—查询—取消、订单列表 |
| 合计 | 17 | 正向、异常、鉴权、边界及接口关联 |

登录异常场景使用 `@pytest.mark.parametrize` 生成四条带中文 `ids` 的测试实例。`headers` fixture 在测试会话中登录一次并复用 Token；`product_id` 和 `address_id` fixture 提供动态关联数据。Allure 报告按登录、商品、购物车和订单四个模块展示这些用例。

购物车测试通过 `yield` fixture 在测试前创建临时数据，并在测试结束后自动删除。订单正向用例执行“加购—创建—查询—取消”，使用 `try...finally` 保证已创建订单能够取消并恢复库存。

支付、管理员发货和确认收货会产生不可逆的订单状态记录，因此不放入默认 pytest 回归；完整订单状态流由 `docs/postman_collection.json` 和 Newman 回归覆盖。
