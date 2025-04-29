# 📚 DCP - DSU Course Plus

> **동서대학교 학생들을 위한 수업 필터링 보조 앱**  
> 요일·시간·전공 여부 등으로 수업을 빠르게 찾을 수 있도록 돕는 프로젝트입니다.

---

## ✅ 프로젝트 개요

- 기존 수강신청 시스템은 원하는 수업을 **직접 일일이 찾아야 하는 불편함**이 있음
- DCP는 수강신청 정보를 기반으로 **요일 / 시간 / 전공/교양 여부로 필터링**을 제공하는 앱
- 수업 정보를 **크롤링 → DB 저장 → API 제공 → 앱에서 검색** 구조로 구성

---

## 🛠️ 기술 스택

| 파트 | 기술 |
|:--|:--|
| 백엔드 | Python, FastAPI, Selenium |
| DB | MySQL |
| 앱 | Android Studio (Java or Kotlin), Retrofit |
| 기타 | GitHub, Postman (API 테스트) |

---

## 📂 프로젝트 구조 (예정)

```plaintext
📁 backend/
   └── app.py               # FastAPI 메인 서버
   └── crawler.py           # Selenium 크롤러
   └── models.py            # Pydantic 모델
   └── db.py                # DB 연결 및 쿼리
📁 android/
   └── app/src/...          # 안드로이드 프로젝트

📄 README.md
