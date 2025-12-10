# GJGN Diet App – Full Project (Android + Django + MySQL)

> 🚀 **GitHub 업로드용 최적화 README 버전**
> 깔끔하고 전문적인 GitHub 문서 스타일로 재작성된 버전입니다.

---

## 📌 프로젝트 개요

AI 기반 식단 분석 + 건강 관리 앱. Android 앱이 Django REST API 서버와 통신하여 음식 인식, 식단 기록, 영양 분석 등을 수행합니다.

### **구성 요소**

* **📱 Android App (`GJGN_02v/`)**
* **🖥 Django Backend (`backend/`)**
* **🗄 MySQL Database**

---

## 📁 프로젝트 구조

```
root/
├─ backend/                 # Django REST API 서버
│  ├─ config/               # Django 설정
│  ├─ api/                  # 핵심 API (User, Meal, OCR 등)
│  ├─ ml/                   # YOLO 음식 인식 모델
│  ├─ static/ & media/      # 업로드 파일
│  └─ requirements.txt
│
└─ GJGN_02v/                # Android 클라이언트
   ├─ core/                 # Network / Token / Retrofit
   ├─ features/             # 화면별 기능 모듈
   └─ ui/                   # Activity / Fragment / Adapter

---

## 🚀 주요 기능
### ✅ Android App
- YOLO 기반 음식 인식(카메라/갤러리)
- 자동 영양 분석 및 칼로리 계산
- 일일/주간 영양 그래프 표시
- 사용자 정보 및 목표 설정
- JWT 로그인 / 회원가입
- 식단 기록 CRUD

### 🖥 Django Backend
- RESTful API 제공
- YOLOv8 모델 추론
- 사용자별 식단/체중 데이터 관리
- JWT 인증
- MySQL 기반 영속성 저장

---

## 🔧 기술 스택
### Android
- Kotlin
- MVVM
- Retrofit + OkHttp
- MPAndroidChart
- Glide
- CameraX

### Backend
- Django 4.2
- Django REST Framework
- SimpleJWT
- MySQL
- Ultralytics YOLOv8
- OpenCV

---

## 🗄️ Database 핵심 테이블
- **User** – 사용자 정보
- **UserGoal** – 목표 칼로리/영양 비율
- **MealRecord** – 날짜별 식단 기록
- **MealFood** – 한 끼의 음식 리스트
- **WeightRecord** – 체중 기록

---

## 🔌 API Endpoint 요약

```

POST   /auth/login/              # 로그인
POST   /auth/register/           # 회원가입
GET    /users/me/                # 내 정보 조회
PATCH  /users/me/                # 내 정보 수정

POST   /meal/records/            # 식단 기록 생성
GET    /meal/records/            # 식단 목록 조회
GET    /meal/records/{id}/       # 상세 조회
PATCH  /meal/records/{id}/       # 수정
DELETE /meal/records/{id}/       # 삭제

POST   /meal/food/analyze/       # 음식 이미지 분석

POST   /weight/                  # 체중 기록
GET    /weight/                  # 목록 조회

```
POST   /auth/login/              # 로그인
POST   /auth/register/           # 회원가입
GET    /users/me/                # 내 정보 조회
PATCH  /users/me/                # 내 정보 수정

POST   /meal/records/            # 식단 기록 생성
GET    /meal/records/            # 식단 목록 조회
GET    /meal/records/{id}/       # 상세 조회
PATCH  /meal/records/{id}/       # 수정
DELETE /meal/records/{id}/       # 삭제

POST   /meal/food/analyze/       # 음식 이미지 분석

POST   /weight/                  # 체중 기록
GET    /weight/                  # 목록 조회
```

---

## ⚙️ 설치 및 실행 가이드

### 1) Backend 설정

```bash
cd backend
pip install -r requirements.txt
python manage.py migrate
python manage.py runserver
```

### 2) Android 실행

* Android Studio 열기
* `GJGN_02v/` 폴더 Import
* Run ▶️ 실행

---

