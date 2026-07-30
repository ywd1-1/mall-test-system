import pytest
import requests
from api import get_headers,base_url,get_product_id,timeout

#当有引入headers时执行
@pytest.fixture(scope="session")
def headers():
    return get_headers()

@pytest.fixture
def product_id(headers):
    return get_product_id(headers)

@pytest.fixture
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


