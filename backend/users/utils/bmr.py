# backend/users/utils/bmr.py

def calculate_bmr(gender: str, weight: float, height: float, age: int) -> float:
    """
    Calculate Basal Metabolic Rate (BMR) using Mifflin-St Jeor Equation.
    gender: 'male' or 'female'
    weight: kg
    height: cm
    age: years
    """
    if gender.lower() == "male":
        return 10 * weight + 6.25 * height - 5 * age + 5
    elif gender.lower() == "female":
        return 10 * weight + 6.25 * height - 5 * age - 161
    else:
        raise ValueError("gender must be 'male' or 'female'")

def calculate_tdee(bmr: float, activity_level: str) -> float:
    activity_factors = {
        "sedentary": 1.2,
        "light": 1.375,
        "moderate": 1.55,
        "active": 1.725,
        "very_active": 1.9,
    }

    if activity_level not in activity_factors:
        raise ValueError("Invalid activity level")

    return bmr * activity_factors[activity_level]

def auto_calorie_goal(tdee: float, mode: str = "maintain") -> float:
    if mode == "maintain":
        return tdee
    elif mode == "lose":
        return tdee - 400
    elif mode == "gain":
        return tdee + 400
    else:
        raise ValueError("mode must be 'maintain', 'lose', or 'gain'")
