import requests

EDAMAM_APP_ID = "81eaaf9d"
EDAMAM_APP_KEY = "59e4c0340075a5295da857753fceb859"

# 요청할 음식
food_name = "pizza"

def check_nutrition(food):
    url = "https://api.edamam.com/api/nutrition-data"
    params = {
        "app_id": EDAMAM_APP_ID,
        "app_key": EDAMAM_APP_KEY,
        "ingr": f"100g {food_name}"
    }
    
    r = requests.get(url, params=params)
    print("Status:", r.status_code)

    data = r.json()
    print(data)

check_nutrition(food_name)
