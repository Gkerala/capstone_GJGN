UPDATE records_mealrecord
SET meal_time = DATE_SUB(meal_time, INTERVAL 9 HOUR);

UPDATE records_mealrecord
SET created_at = DATE_SUB(created_at, INTERVAL 9 HOUR);
