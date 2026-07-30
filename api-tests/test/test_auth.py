import allure
import pytest
import requests

from api import base_url, password, timeout, username

#成功登录
@allure.feature("登录模块")
@allure.story("用户登录")
@allure.title("使用正确账号密码登录成功")
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
@allure.feature("登录模块")
@allure.story("用户登录")
@allure.title("登录失败：{param_id}")
@pytest.mark.parametrize(
    "login_username,login_password,expected_status,expected_code",
    [
        (username + "wrong", password, 400, 400),
        (username, password + "-wrong", 400, 400),
        ("", password, 400, 400),
        (username, "", 400, 400),
    ],
    ids=["用户名错误", "密码错误", "用户名为空", "密码为空"],
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
