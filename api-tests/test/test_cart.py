import requests

from api import base_url, get_headers, timeout


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


def add_cart(headers):
    product_id = get_product_id(headers)

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


def delete_cart(headers, cart_id):
    response = requests.delete(
        base_url + f"/cart/{cart_id}",
        headers=headers,
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 200, response.text
    assert data["code"] == 200, data


def test_add_cart_success():
    headers = get_headers()
    cart_item = add_cart(headers)

    assert cart_item["id"], cart_item

    delete_cart(headers, cart_item["id"])


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


def test_view_cart():
    headers = get_headers()
    cart_item = add_cart(headers)

    response = requests.get(
        base_url + "/cart",
        headers=headers,
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert any(item["id"] == cart_item["id"] for item in data["data"]), data

    delete_cart(headers, cart_item["id"])


def test_change_cart_quantity():
    headers = get_headers()
    cart_item = add_cart(headers)

    response = requests.put(
        base_url + f"/cart/{cart_item['id']}",
        json={
            "quantity": 2,
        },
        headers=headers,
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert data["data"]["quantity"] == 2, data

    delete_cart(headers, cart_item["id"])


def test_delete_cart():
    headers = get_headers()
    cart_item = add_cart(headers)

    delete_cart(headers, cart_item["id"])

    response = requests.get(
        base_url + "/cart",
        headers=headers,
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert not any(item["id"] == cart_item["id"] for item in data["data"]), data
