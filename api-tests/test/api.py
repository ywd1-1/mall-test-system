import requests


base_url = "http://123.56.127.167/api"
username = "ywd"
password = "123456"
timeout = 10


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


def get_headers():
    token = get_token()

    return {
        "Authorization": f"Bearer {token}"
    }
