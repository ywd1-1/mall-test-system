import pytest
import requests

from api import base_url,  timeout

@pytest.fixture#得到cartid
def cart_item(headers, product_id):
    item = add_cart(headers, product_id)

    yield item

    delete_cart(headers, item["id"])
#加入购物车
def add_cart(headers,product_id):
    response = requests.post(
        base_url + "/cart",
        json={
            "productId": product_id,
            "quantity": 1,
        },
        headers=headers,
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert data["data"]["productId"] == product_id, data
    assert data["data"]["quantity"] == 1, data

    return data["data"]
#修改购物车中的数量
def test_change_cart_quantity(headers,cart_item):

    cart_id = cart_item["id"]

    response = requests.put(
        base_url + f"/cart/{cart_id}",
        json={
            "quantity": 2
        },
        headers=headers,
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 200,response.text
    assert data["code"]==200,data
    assert data["data"]["quantity"]==2,data

# 库存不足时加入购物车失败
def test_add_cart_insufficient_stock(headers):
    response = requests.post(
        base_url + "/cart",
        json={
            "productId": 4,
            "quantity": 1,
        },
        headers=headers,
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 400, response.text
    assert data["code"] == 400, data
    assert data["message"] == "商品库存不足", data

#删除购物车
def test_delete_cart(headers,product_id):

    cart_item = add_cart(headers, product_id)
    cart_id = cart_item["id"]
    delete_cart(headers, cart_id)

    response = requests.get(
        base_url + f"/cart",
        headers=headers,
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data

    cart_ids = []
    for item in data["data"]:
        cart_ids.append(item["id"])
    assert cart_id not in cart_ids, data

#定义购物车中删除函数
def delete_cart(headers, cart_id):
    response = requests.delete(
        base_url + f"/cart/{cart_id}",
        headers=headers,
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data

#验证商品加入购物车成功
def test_add_cart_success(cart_item):
    assert cart_item["id"], cart_item


#不带token添加购物车
def test_add_cart_without_token():
    response = requests.post(
        base_url + "/cart",
        json={
            "productId": 10,
            "quantity": 1,
        },
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 401, response.text
    assert data["code"] == 401, data
    assert "token" in data["message"], data

#查看购物车
def test_view_cart(headers,cart_item):


    response = requests.get(
        base_url + "/cart",
        headers=headers,
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data

    cart_ids = []
    for item in data["data"]:
        cart_ids.append(item["id"])
    assert cart_item["id"] in cart_ids, data




