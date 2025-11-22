# goals/utils.py

def calculate_bmr(gender, weight, height, age):
    if gender == "female":
        return 10 * weight + 6.25 * height - 5 * age - 161
    return 10 * weight + 6.25 * height - 5 * age + 5


def calculate_tdee(bmr, activity):
    return bmr * activity


def split_macros(kcal):
    return {
        "protein": round((kcal * 0.25) / 4, 1),
        "fat": round((kcal * 0.25) / 9, 1),
        "carbs": round((kcal * 0.5) / 4, 1),
    }
