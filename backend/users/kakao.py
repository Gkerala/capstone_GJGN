import requests

def get_kakao_user_info(access_token: str):
    """
    카카오 access_token으로 사용자 정보 가져오기
    """
    kakao_user_info_url = "https://kapi.kakao.com/v2/user/me"
    headers = {"Authorization": f"Bearer {access_token}"}
    response = requests.get(kakao_user_info_url, headers=headers)

    if response.status_code != 200:
        raise Exception("카카오 사용자 정보 요청 실패")

    data = response.json()
    kakao_id = data.get("id")
    kakao_account = data.get("kakao_account", {})
    profile = kakao_account.get("profile", {})

    return {
        "id": kakao_id,
        "email": kakao_account.get("email"),
        "nickname": profile.get("nickname"),
    }
