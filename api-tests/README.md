# 电商订单管理系统接口自动化测试

基于 `pytest + requests` 的接口自动化测试项目，用于验证登录、商品、购物车和订单查询的核心接口。

## 项目结构

```text
api-tests/
├── test/
│   ├── api.py                # 登录、token 和公共配置
│   ├── test_auth.py          # 登录测试
│   ├── test_products.py      # 商品查询测试
│   ├── test_cart.py          # 购物车测试
│   └── test_orders.py        # 订单查询测试
├── .gitignore
├── pytest.ini
└── requirements.txt
```

测试地址、测试账号、密码和超时时间都写在 `test/api.py` 顶部，便于学习和调试。

## 安装与运行

```bash
pip install -r requirements.txt
pytest -v
```

调试时显示接口输出：

```bash
pytest -s -v
```

## 当前覆盖范围

| 模块 | 场景 |
| --- | --- |
| 登录 | 正确账号密码、错误密码 |
| 商品 | 分页查询、商品详情查询 |
| 购物车 | 正常加购、无 token 加购、查询、修改数量、删除 |
| 订单 | 查询订单、无 token 查询订单 |

购物车测试会创建临时购物车数据，并在成功后删除。测试账号应只用于自动化测试，避免与手工数据混用。

下单、支付、发货等接口会永久生成业务记录，后续应在独立测试环境和专用测试账号中补充。
