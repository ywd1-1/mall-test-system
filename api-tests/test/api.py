import allure
import pytest
import requests


base_url = "http://123.56.127.167/api"
username = "ywd"
password = "123456"
timeout = 10


#提取token
@allure.step("获取登录 Token")
def get_token():
    response = requests.post(
        base_url + "/login",
        json={
            "username": username,
            "password": password,
        },
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 200, response.text
    assert data["code"] == 200, data

    return data["data"]["token"]

#提取请求头
@allure.step("生成携带 Token 的请求头")
def get_headers():
    token = get_token()

    return {
        "Authorization": f"Bearer {token}"
    }


#得到商品id
@allure.step("选择库存充足且未加入购物车的商品")
def get_product_id(headers):
    response = requests.get(
        base_url + "/cart",
        headers=headers,
        timeout=timeout,
    )
    cart_data = response.json()
    assert response.status_code == 200, response.text
    assert cart_data["code"] == 200, cart_data

    cart_product_ids = []
    for item in cart_data["data"]:
        cart_product_ids.append(item["productId"])

    response = requests.get(
        base_url + "/products",
        params={"page": 1, "size": 100},
        timeout=timeout,
    )
    product_data = response.json()

    assert response.status_code == 200, response.text
    assert product_data["code"] == 200, product_data

    for product in product_data["data"]["records"]:
        if product["id"] not in cart_product_ids and product["stock"] >= 2:
            return product["id"]

    assert False, "没有可用于购物车自动化测试的在售商品"
