import requests

from api import base_url, timeout


def test_products_page():
    response = requests.get(
        base_url + "/products",
        params={"page": 1, "size": 8},
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert data["data"]["page"] == 1, data
    assert data["data"]["size"] == 8, data
    assert isinstance(data["data"]["records"], list), data

def test_products_notexits():
    response = requests.get(
        base_url + "/products/99999",
        timeout=timeout,
    )
    data = response.json()
    assert response.status_code == 404, response.text
    assert data["code"] == 404, data
    assert data["message"] == "商品不存在"


def test_product_detail():
    response = requests.get(
        base_url + "/products",
        params={"page": 1, "size": 8},
        timeout=timeout,
    )

    data = response.json()
    assert response.status_code == 200, response.text
    assert data["code"] == 200, data
    assert data["data"]["records"], data

    product_id = data["data"]["records"][0]["id"]
    response = requests.get(
        base_url + f"/products/{product_id}",
        timeout=timeout,
    )

    detail_data = response.json()
    assert response.status_code == 200, response.text
    assert detail_data["code"] == 200, detail_data
    assert detail_data["data"]["id"] == product_id, detail_data
