import pytest
import requests

from api import base_url, password, timeout, username

#成功登录
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
#参数化执行失败登录
@pytest.mark.parametrize(
    "login_username,login_password,expected_status,expected_code",
    [
        (username + "wrong", password, 400, 400),
        (username, password + "-wrong", 400, 400),
        ("", password, 400, 400),
        (username, "", 400, 400),
    ]
)
def test_login_failure(
    login_username,
    login_password,
    expected_status,
    expected_code,
):
    response = requests.post(
        base_url + "/login",
        json={
            "username": login_username,
            "password": login_password,
        },
        timeout=timeout,
    )

    data = response.json()
    assert response.status_code == expected_status, response.text
    assert data["code"] == expected_code, data
    assert data["message"], data