from fastapi import FastAPI, Query, HTTPException
from pydantic import BaseModel
from typing import List, Optional
import sqlite3
import json
import os

app = FastAPI()

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DB_PATH = os.path.join(BASE_DIR, "..", "db", "main_dsu_courses.db")
print("[DEBUG] DB 경로:", DB_PATH)
print("[DEBUG] 존재 여부:", os.path.exists(DB_PATH))

class Schedule(BaseModel):
    day: str
    time: int

class Subject(BaseModel):
    id: int
    dept: Optional[str]
    grade: Optional[str]
    class_id: Optional[str]
    class_num: Optional[int]
    class_name: Optional[str]
    professor: Optional[str]
    credit: Optional[int]
    type: Optional[str]
    general_area: Optional[str]
    lecture_area: Optional[str]
    schedule: List[Schedule] = []

@app.get("/subjects", response_model=List[Subject])
def get_subjects(
    type: Optional[str] = Query(None),
    day: Optional[str] = Query(None),
    time_start: Optional[int] = Query(None),
    time_end: Optional[int] = Query(None),
    dept: Optional[str] = Query(None),
    professor: Optional[str] = Query(None),
    lecture_area: Optional[str] = None,
    general_area: Optional[str] = None
):
    print("=== [DEBUG] 쿼리 파라미터 ===")
    print(f"{day=}, {time_start=}, {time_end=}, {type=}, {dept=}, {professor=}")

    if not os.path.exists(DB_PATH):
        raise HTTPException(status_code=500, detail="DB 파일을 찾을 수 없습니다")

    conn = sqlite3.connect(DB_PATH)
    conn.row_factory = sqlite3.Row
    c = conn.cursor()

    query = """
    SELECT s.*, json_group_array(json_object('day', sc.day, 'time', sc.time)) AS schedule_json
    FROM subjects s
    LEFT JOIN schedules sc ON s.id = sc.subject_id
    WHERE 1=1
    """
    params = []
    if day:
        query += " AND sc.day = ?"
        params.append(day)
    if time_start is not None:
        query += " AND sc.time >= ?"
        params.append(time_start)
    if time_end is not None:
        query += " AND sc.time <= ?"
        params.append(time_end)
    if type:
        query += " AND s.type = ?"
        params.append(type)
    if general_area:
        query += " AND s.general_area = ?"
        params.append(general_area)
    if dept:
        query += " AND s.dept = ?"
        params.append(dept)
    if lecture_area:
        query += " AND s.lecture_area LIKE ?"
        params.append(f"{lecture_area}-%")
    if professor:
        query += " AND s.professor LIKE ?"
        params.append(f"%{professor}%")

    query += " GROUP BY s.id"

    print("=== [DEBUG] 실행할 SQL ===")
    print(query)
    print("=== [DEBUG] 파라미터 ===")
    print(params)

    try:
        c.execute(query, params)
        rows = c.fetchall()
        print(f"=== [DEBUG] 조회된 row 수: {len(rows)} ===")
    except Exception as e:
        print(f"[ERROR] SQL 실행 실패: {e}")
        raise HTTPException(status_code=500, detail=f"SQL 실행 실패: {e}")

    results = []
    for row in rows:
        print(f"--- [DEBUG] row id={row['id']} ---")
        schedule_raw = row["schedule_json"]
        print(f"[DEBUG] schedule_raw: {schedule_raw}")
        schedule = []
        try:
            if schedule_raw:
                schedule = [Schedule(**item) for item in json.loads(schedule_raw)]
        except Exception as e:
            print(f"[ERROR] schedule 파싱 실패: {e}")

        subject = Subject(
            id=row["id"],
            dept=row["dept"],
            grade=row["grade"],
            class_id=row["class_id"],
            class_num=row["class_num"],
            class_name=row["class_name"],
            professor=row["professor"],
            credit=row["credit"],
            type=row["type"],
            general_area=row["general_area"],
            lecture_area=row["lecture_area"],
            schedule=schedule
        )
        results.append(subject)

    print("=== [DEBUG] 최종 결과 개수 ===")
    print(len(results))
    return results
