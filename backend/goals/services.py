def calculate_bmr(sex: str, weight: float, height: float, age: int) -> float:
    if sex == "male":
        return 88.36 + (13.4 * weight) + (4.8 * height) - (5.7 * age)
    return 447.6 + (9.2 * weight) + (3.1 * height) - (4.3 * age)

def activity_multiplier(level: str) -> float:
    table = {
        "low": 1.2,
        "normal": 1.375,
        "high": 1.55,
        "very_high": 1.725,
    }
    return table.get(level, 1.375)

def auto_macro_split(calorie: float) -> tuple:
    # P:30% / C:40% / F:30%
    protein_cal = calorie * 0.30
    carb_cal = calorie * 0.40
    fat_cal = calorie * 0.30


    return (
        round(protein_cal / 4), # g
        round(carb_cal / 4), # g
        round(fat_cal / 9), # g
    )