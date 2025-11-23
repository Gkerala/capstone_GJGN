def get_activity_factor(activity):
    factors = {
        1: 1.2,
        2: 1.375,
        3: 1.55,
        4: 1.725,
        5: 1.9,
    }
    return factors.get(activity, 1.2)  # 기본값 1.2


def calculate_daily_targets(gender, weight, height, age, activity, goal_type):
    # --- 1. BMR ---
    if gender == "male":
        bmr = 10 * weight + 6.25 * height - 5 * age + 5
    else:
        bmr = 10 * weight + 6.25 * height - 5 * age - 161

    # --- 2. TDEE ---
    tdee = bmr * get_activity_factor(activity)

    # 3) goal_type 적용 (감량/유지/증가)
    if goal_type == 1:
        tdee *= 0.85   # 감량
    elif goal_type == 3:
        tdee *= 1.15   # 벌크업

    tdee = max(tdee, 1200)    # 안전 장치

    # --- 4. 탄단지(50/20/30) ---
    carbs = (tdee * 0.50) / 4
    protein = (tdee * 0.20) / 4
    fat = (tdee * 0.30) / 9

    # --- 5. 당 (WHO 10%) ---
    sugar = (tdee * 0.10) / 4

    return {
        "tdee": round(tdee),
        "carbs": round(carbs),
        "protein": round(protein),
        "fat": round(fat),
        "sugar": round(sugar),
    }
