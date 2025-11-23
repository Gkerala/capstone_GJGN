def get_activity_factor(activity):
    factors = {
        1: 1.2,
        2: 1.375,
        3: 1.55,
        4: 1.725,
        5: 1.9,
    }
    return factors.get(activity, 1.2)  # 기본값 1.2


def calculate_daily_targets(gender, weight, height, age, activity):
    # --- 1. BMR 계산 ---
    if gender == "male":
        bmr = 10 * weight + 6.25 * height - 5 * age + 5
    else:
        bmr = 10 * weight + 6.25 * height - 5 * age - 161

    # --- 2. 활동계수 적용 ---
    tdee = bmr * get_activity_factor(activity)

    # --- 3. 탄단지 비율 ---
    carbs_kcal = tdee * 0.5
    protein_kcal = tdee * 0.2
    fat_kcal = tdee * 0.3

    carbs_g = carbs_kcal / 4
    protein_g = protein_kcal / 4
    fat_g = fat_kcal / 9

    # --- 4. 설탕 목표(WHO 기준 10%) ---
    sugar_kcal = tdee * 0.10
    sugar_g = sugar_kcal / 4

    return {
        "tdee": round(tdee),
        "carbs": round(carbs_g),
        "protein": round(protein_g),
        "fat": round(fat_g),
        "sugar": round(sugar_g),
    }
