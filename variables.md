# 📘 변수 정의 문서 (API & DB & 앱 공통)

> 이 문서는 FastAPI 백엔드, SQLite DB, Android 앱에서 **공통으로 사용하는 강의 데이터 구조**입니다.

---

## 🔸 Subject 데이터 구조

| 변수명           | 타입      | 설명                                   | 예시                                |
|------------------|-----------|----------------------------------------|-------------------------------------|
| `dept`           | `string`  | 학과 이름                              | `"소프트웨어학과"`                 |
| `grade`          | `string`  | 학년 정보                              | `"3학년"`                           |
| `class_id`       | `string`  | 과목 고유 ID (학사시스템 기준 코드)    | `"123456"`                          |
| `class_num`      | `int`     | 분반 번호                              | `101`                               |
| `class_name`     | `string`  | 강의 이름                              | `"소프트웨어개발실습3"`            |
| `professor`      | `string`  | 교수 이름                         | `"김동현"`                           |
| `credit`         | `int`     | 학점                                   | `3`                                 |
| `schedule`       | `list`    | 시간표 배열 (요일 + 교시)              | `[{"day": "화", "time": 1}, {"day": "화", "time": 2}]` |
| `type`           | `string`  | 이수구분                               | `"전공필수"`                         |
| `general_area`   | `string`  | 수업 장소 건물 (강의실 위치)           | `"UIT관"`                            |
| `lecture_area`   | `string`  | 강의 영역 (교선 5가지 영역)     | `null` 또는 `"자연과 기술"`            |

---

## 🧪 `schedule` 필드 예시 (복수 가능)

```json
"schedule": [
  { "day": "월", "time": 2 },
  { "day": "월", "time": 3 },
  { "day": "화", "time": 1 }
]
