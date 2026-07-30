import allure
import pytest
import requests
from api import get_headers,base_url,get_product_id,timeout

#当有引入headers时执行
@pytest.fixture(scope="session")
@allure.title("登录并准备认证请求头")
def headers():
    return get_headers()

@pytest.fixture
@allure.title("准备可用于购物车测试的商品")
def product_id(headers):
    return get_product_id(headers)

@pytest.fixture
@allure.title("准备收货地址")
def address_id(headers):
    response = requests.get(
        base_url + "/addresses",
        headers=headers,
        timeout=timeout
    )
    data = response.json()

    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert data["data"], "当前账号没有收货地址"

    return data["data"][0]["id"]


