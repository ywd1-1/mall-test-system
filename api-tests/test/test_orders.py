import requests

from api import base_url, get_headers, timeout


def test_view_orders():
    headers = get_headers()

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


def test_view_orders_without_token():
    response = requests.get(
        base_url + "/orders",
        params={"page": 1, "size": 10},
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 401, response.text
    assert data["code"] == 401, data
    assert "token" in data["message"], data
