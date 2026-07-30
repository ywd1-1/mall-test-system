import requests

from api import base_url,  timeout
#创建订单函数
def create_order(headers, product_id, address_id):
    # 1. 加入购物车
    cart_response = requests.post(
        base_url + "/cart",
        json={
            "productId": product_id,
            "quantity": 1
        },
        headers=headers,
        timeout=timeout
    )
    cart_data = cart_response.json()
    assert cart_response.status_code == 200, cart_response.text
    assert cart_data["code"] == 200, cart_data

    # 2. 从加购响应中取得购物车ID
    cart_id = cart_data["data"]["id"]

    # 3. 创建订单
    order_response = requests.post(
        base_url + "/orders",
        json={
            "cartIds": [cart_id],
            "addressId": address_id
        },
        headers=headers,
        timeout=timeout
    )
    order_data = order_response.json()

    assert order_response.status_code == 200, order_response.text
    assert order_data["code"] == 200, order_data
    assert order_data["data"]["id"], order_data
    return order_data["data"]["id"] #创建订单返回的id下面调用这个函数直接就是order_id
#查询订单的普通函数
def get_order(headers, order_id):
    response = requests.get(
        base_url + f"/orders/{order_id}",
        headers=headers,
        timeout=timeout
    )
    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data

    return data["data"]

#取消订单函数
def cancel_order(headers, order_id):
    response = requests.delete(
        base_url + f"/orders/{order_id}",
        headers=headers,
        timeout=timeout
    )
    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data

#无token创建订单
def test_notoken_order():
    response = requests.post(
        base_url + "/orders",
        json={
            "cartIds": [1],
            "addressId": 1,
        },
        timeout=timeout
    )
    data = response.json()
    assert response.status_code == 401, response.text
    assert data["code"] == 401, data
    assert data["message"] == "未登录或 token 缺失"

#执行取消订单
def test_order_flow(headers, product_id, address_id):
    order_id = create_order(headers, product_id, address_id)
    try:
        order_item = get_order(headers, order_id)
        assert order_item["status"] == "CREATED", order_item
    finally:
        cancel_order(headers, order_id)

    cancelled_order = get_order(headers, order_id)
    assert cancelled_order["status"] == "CANCELLED", cancelled_order


#查看订单
def test_view_orders(headers):
    response = requests.get(
        base_url + "/orders",
        params={"page": 1, "size": 10},
        headers=headers,
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert data["data"]["page"] == 1, data
    assert isinstance(data["data"]["records"], list), data

