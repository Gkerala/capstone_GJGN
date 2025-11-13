from django.contrib import admin
from .models import UserInfo, FoodNutrition, DietRecord


@admin.register(UserInfo)
class UserInfoAdmin(admin.ModelAdmin):
    list_display = (
        'id', 'user','username', 'email', 'birth_date', 'gender',
        'height_cm', 'weight_kg', 'goal_type', 'target_weight_kg', 'activity_level', 'created_at'
    )
    search_fields = ('username', 'email')
    list_filter = ('gender', 'created_at')


@admin.register(FoodNutrition)
class FoodNutritionAdmin(admin.ModelAdmin):
    list_display = ('food_name', 'calories', 'carbohydrate', 'protein', 'fat')
    search_fields = ('food_name',)
    list_filter = ('calories',)


@admin.register(DietRecord)
class DietRecordAdmin(admin.ModelAdmin):
    list_display = ('user', 'food', 'meal_type', 'date', 'portion')
    list_filter = ('meal_type', 'date')
    search_fields = ('user__username', 'food__food_name')
