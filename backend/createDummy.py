import os
import django
from datetime import datetime, date, time, timedelta
import random

# ----------------------------------------
# 🔧 Django settings 로드
# ----------------------------------------
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "SmartDiet_backend.settings")
django.setup()

from django.contrib.auth import get_user_model
from records.models import MealRecord, MealFood, WeightRecord

User = get_user_model()

# 테스트용 USER 선택 (ID=2)
user = User.objects.get(id=2)

# ----------------------------------------
# 🔥 더미 데이터 생성 범위
# ----------------------------------------
start_date = date(2025, 11, 10)
end_date = date(2025, 11, 16)

meal_types = ["breakfast", "lunch", "dinner"]

# ----------------------------------------
# 🔥 임의 음식 데이터 생성 함수
# ----------------------------------------
def create_dummy_meal(record_date):
    """하루 식사 2번 생성"""
    chosen_meals = random.sample(meal_types, 2)

    for meal_type in chosen_meals:
        # MealRecord 생성
        meal_time = datetime.combine(record_date, time(random.randint(7, 20), random.randint(0, 59)))

        record = MealRecord.objects.create(
            user=user,
            meal_time=current,
            memo="더미 데이터"
        )


        total_kcal = 0

        # 음식 2~4개 생성
        for i in range(random.randint(2, 4)):
            kcal = random.randint(80, 300)
            carb = random.randint(10, 40)
            protein = random.randint(5, 20)
            fat = random.randint(3, 15)
            sugar = random.randint(1, 15)

            MealFood.objects.create(
                record=record,
                food_name=f"더미 음식 {i+1}",
                amount=random.randint(50, 300),
                kcal=kcal,
                carb=carb,
                protein=protein,
                fat=fat,
                sugar=sugar,
            )

            total_kcal += kcal

        # 총 칼로리 업데이트
        record.total_calories = total_kcal
        record.save()

# ----------------------------------------
# 🔥 체중 기록 생성 함수
# ----------------------------------------
def create_dummy_weight(record_date):
    """하루 체중 2번 생성 (아침, 저녁)"""
    times = [time(8, random.randint(0, 59)), time(20, random.randint(0, 59))]
    weights = [
        round(random.uniform(95.0, 100.0), 1), 
        round(random.uniform(95.0, 100.0), 1)
    ]

    for t, w in zip(times, weights):
        WeightRecord.objects.create(
            user=user,
            date=record_date,
            weight=w
        )


# ----------------------------------------
# 🔥 날짜별 루프 돌면서 데이터 생성
# ----------------------------------------
current = start_date

while current <= end_date:
    print(f"📌 생성 중: {current}")

    create_dummy_meal(current)
    create_dummy_weight(current)

    current += timedelta(days=1)

print("✅ 더미 데이터 생성 완료!")
