# 📘 변수 정의 문서 (API & DB & 앱 공통)

본 문서는 FastAPI 백엔드, MySQL DB, Android 앱에서 **공통으로 사용하는 변수 정의서**입니다.

---

## 📦 Subject 데이터 구조

| 변수명        | 타입   | 설명              | 예시           |
|--------------|--------|-------------------|----------------|
| `subject_id` | string | 과목 고유 코드     | "CSE101"       |
| `subject_name`       | string | 과목명            | "인공지능개론" |
| `professor`  | string | 담당 교수 이름     | "김교수"       |
| `day`        | string | 수업 요일          | "화"           |
| `time`       | int    | 교시 번호          | 3              |
| `credit`     | int    | 학점              | 3              |
| `type`       | string | 전공/교양/선택     | "전공"         |

---

## 📨 API 요청 예시

```http
GET /subjects?day=화&time=3&type=전공
