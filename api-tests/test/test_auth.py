import requests

from api import base_url, password, timeout, username


def test_login_success():
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
    assert data["data"]["token"], data
    assert data["data"]["user"]["username"] == username, data


def test_login_wrong_password():
    response = requests.post(
        base_url + "/login",
        json={
            "username": username,
            "password": password + "-wrong",
        },
        timeout=timeout,
    )
    data = response.json()

    assert response.status_code == 400, response.text
    assert data["code"] == 400, data
    assert data["message"] == "用户名或密码错误", data
