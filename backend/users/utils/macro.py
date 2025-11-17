def calculate_macro_distribution(target_calories: float):
    """
    Return grams of carbs/protein/fat based on calorie distribution:
    Carbs: 50%, Protein: 25%, Fat: 25%
    """

    carbs_kcal = target_calories * 0.50
    protein_kcal = target_calories * 0.25
    fat_kcal = target_calories * 0.25

    return {
        "carbs": round(carbs_kcal / 4),      # 4 kcal per 1g
        "protein": round(protein_kcal / 4),  # 4 kcal per 1g
        "fat": round(fat_kcal / 9)           # 9 kcal per 1g
    }
