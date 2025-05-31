# 📘 DCP - DSU Course Plus

---

## 🛬 프로젝트 개요

> 기존 수강시청 시스템에는 수업을 요일별, 시간별로 **빠른 것을 찾는 기능이 없다.**  
> `DCP`는 **대학생이 원하는 시간대의 수업을 빠른 것으로 찾을 수 있도록 도움을 주는 보조 앱**이다.

---

## 🌟 주요 기능

- 요일 / 규식 / 전공 / 교양 필터 기능 제공
- 과념명, 교수명으로 검색 가능 (추가 예정)
- **모든 수업 데이터는 관리자 1인이 학교 수강시청 사이트에서 수정**
- 사용자 로그인 없이 누구나 앱에서 필터 기능 사용 가능

---

## 🧱 시스템 구성 요약

```
[관리자 1인]
    → Selenium으로 로그인 후 수업 전체 크롤링
    → 크롤링 결과를 DB(MySQL)에 저장

[FastAPI 서버]
    → /subjects API로 필터링된 수업 목록 제공 (day, time, type 등)

[Android 앱]
    → 서버 API를 호출해서 사용자에게 수업 필터 결과 표시
```

---

## 🔐 보안 정책

- 사용자는 도서대학교 계정으로 로그인할 필요 없음
- 서버는 크롤링을 복소하지 않음 → **학교 서버에 보도를 준 적 없음**
- 모든 수업 데이터는 수단 수정 → 안정적, 예측 가능

---

## ⚙️ 기술 스테크

| 영역 | 기술 |
|:--|:--|
| 백어드 | Python + FastAPI |
| DB | MySQL + SQLAlchemy |
| 크롤링 | Selenium (ChromeDriver) |
| 앱 | Android Studio (Java or Kotlin), Retrofit |
| API 테스트 | Swagger UI (FastAPI 내장) or Postman |

---

## SETUP

### 1. 파이썬 설치
https://www.python.org/downloads/

```
python --version
```

```
pip --version
```
제대로 설치되었는지 확인


### 2. vscode 필수 확장

#### ✅ FastAPI + Python 개발용 필수 확장
Python

⛳ 가장 기본. Linting, Debugging, 가상환경, 실행 다 됨

개발자: Microsoft

#### Pylance

🧠 자동완성 + 타입 추론 + IntelliSense 강화

꼭 Python 확장과 함께 써야 함

#### Jinja

만약 FastAPI에서 HTML 템플릿 쓸 생각 있다면 (Jinja2 지원)

#### REST Client

💡 FastAPI 테스트할 때, HTTP 요청을 .http 파일에서 바로 보낼 수 있음 (Postman 대체)

SQLite Viewer / SQLite

📊 .db 파일 직접 보고, 쿼리 날릴 수 있음

### chromedriver

https://storage.googleapis.com/chrome-for-testing-public/136.0.7103.94/win64/chromedriver-win64.zip

### 3. install module

```
pip install selenium
```

---

## 🤩 주요 API 명세

1. fast api 실행:
```
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```
2. ngrok도 서버 실행
```
ngrok http 8000
```
**응답 예시:**

```json
[
  {
    "dept": 소프트웨어학과,
    "grade": 3학년,
    "class_id": 123456,
    "class_num": 101,
    "class_name": 소프트웨어개발실습3,
    "professor": 김동현,
    "credit": 3,
    "schedule": {day: 화 ,time: 1-2},
    "type": 전공필수,
    "general_area": UIT관,
    "lecture_area": null 
  }
]
```

---

## 🚀 운영 화면 요약

| 단계 | 설명 |
|:--|:--|
| 1 | **관리자가 수강시청 사이트에 로그인 → 수업 목록 크롤링** |
| 2 | **크롤링 결과를 MySQL DB에 저장** |
| 3 | FastAPI 서버가 DB를 조회해서 API로 사용자에게 응답 |
| 4 | Android 앱에서 API를 호출해서 사용자에게 수업 필터 결과 표시 |

---

## 📅 개발 일정 (uc608시)

| 주차 | 목표 |
|:--|:--|
| 1주차 | 크롤링 구현 및 수업 데이터 샘플 확률 |
| 2주차 | MySQL DB 설계 및 연동 |
| 3주차 | FastAPI API 완성 및 테스트 |
| 4주차 | Android 앱 UI + API 연동 |
| 5주차 | 통합 테스트 / 발표 준비 |

---

## 🧠 마무리 요약

> **"우리는 학교 수강시청 시스템에 부담을 주지 않으며,  
> 사용자가 수업을 효율적으로 찾을 수 있도록 도움을 주는 앱을 만듭니다."**  
> 관리자 단일 계정으로 1회만 크롤링해서 효율성과 안정성 모두 확답합니다


