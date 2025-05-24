import json
import sqlite3
from pathlib import Path

# JSON 파일 경로
json_path = Path("dsu_courses.json")  # 필요 시 사용자 입력 경로로 변경 가능

# SQLite DB 연결
conn = sqlite3.connect("main_dsu_courses.db")
c = conn.cursor()

# 테이블 생성
c.execute("""
CREATE TABLE IF NOT EXISTS subjects (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    dept TEXT,
    grade TEXT,
    class_id TEXT,
    class_num INTEGER,
    class_name TEXT,
    professor TEXT,
    credit INTEGER,
    type TEXT,
    general_area TEXT,
    lecture_area TEXT
)
""")

c.execute("""
CREATE TABLE IF NOT EXISTS schedules (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    subject_id INTEGER,
    day TEXT,
    time INTEGER,
    FOREIGN KEY(subject_id) REFERENCES subjects(id) ON DELETE CASCADE
)
""")

# JSON 로드
with open(json_path, "r", encoding="utf-8") as f:
    data = json.load(f)

# INSERT
for subject in data:
    # subjects 테이블
    subject_values = (
        subject.get("dept"),
        subject.get("grade"),
        subject.get("class_id"),
        subject.get("class_num"),
        subject.get("class_name"),
        subject.get("professor"),
        subject.get("credit"),
        subject.get("type"),
        subject.get("general_area"),
        subject.get("lecture_area")
    )
    c.execute("""
        INSERT INTO subjects (
            dept, grade, class_id, class_num, class_name, professor,
            credit, type, general_area, lecture_area
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """, subject_values)

    subject_id = c.lastrowid

    # schedules 테이블
    for sched in subject.get("schedule", []):
        c.execute("""
            INSERT INTO schedules (subject_id, day, time) VALUES (?, ?, ?)
        """, (subject_id, sched.get("day"), sched.get("time")))

# 커밋 및 종료
conn.commit()
conn.close()
print("✅ subjects.db 생성 완료!")
