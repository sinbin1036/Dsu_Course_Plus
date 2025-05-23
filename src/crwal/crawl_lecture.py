import os
import time
import re
import json
import csv
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait, Select
from selenium.webdriver.support import expected_conditions as EC

# === Chrome Driver Setup ===
base_dir = os.path.dirname(os.path.abspath(__file__))
driver_path = os.path.join(base_dir, '..', 'chromedriver', 'chromedriver.exe')

options = Options()
options.add_argument("--no-sandbox")
options.add_argument("--disable-dev-shm-usage")
options.add_argument("--disable-blink-features=AutomationControlled")
options.add_experimental_option("excludeSwitches", ["enable-automation"])
options.add_experimental_option("useAutomationExtension", False)

service = Service(executable_path=driver_path)
driver = webdriver.Chrome(service=service, options=options)
driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
    "source": "Object.defineProperty(navigator, 'webdriver', {get: () => undefined})"
})

wait = WebDriverWait(driver, 10)

# === Site Access ===
driver.get("https://sugang.dongseo.ac.kr/")
input("\n[로그인 후 엔터]")
time.sleep(1)

# === Dynamic iframe 탐색 ===
for frame in driver.find_elements(By.TAG_NAME, "iframe"):
    driver.switch_to.default_content()
    driver.switch_to.frame(frame)
    try:
        wait.until(EC.presence_of_element_located((By.ID, "pComboIsu")))
        print("iframe 진입 완료")
        break
    except:
        continue

# === Dropdown 정의 ===
type_select = Select(driver.find_element(By.ID, "pComboIsu")) #이수구분
departments = Select(driver.find_element(By.ID, "pComboHakgwa"))#학과
grades = Select(driver.find_element(By.ID, "pComboGrade")) #학년
area_select = lambda: Select(driver.find_element(By.ID, "pComboTheme")) #교선 영역
#이수구분배열
type_map = {
    '교양필수': [],
    '교선균형': ['인간과역사', '사회와가치', '자연과기술', '문학과예술', '세계와문화'],
    '전공선택': [],
    '전공필수': []
}
#학과 배열
target_departments = ["건축공학과", "건축학과", "게임학과", "경찰학과", "경영학과", "경영학전공", "공연영상학과", "관광경영·컨벤션학과", "국제물류학과", "국제통상학과", "디자인학부", "디자인문화학과", 
        "디지털공학과", "뮤지컬엔터테인먼트과", "미용예술학과", "방송영상학과", "반려동물학과", "방사선학과", "보건행정학과", "소프트웨어학과", "스마트모빌리티학부", "스포츠레저학과", "스포츠재활학과", "실용콘텐츠 창작학과", 
        "안전관리학과", "연기과", "영화과", "영상애니메이션학과", "운동처방학과", "웰빙조리과", "일본어학과", "자유전공학부", "중국어학과", "치위생학과", "캠퍼스아시아학과", "컴퓨터공학과", "콘텐츠융합학과", "패션디자인학과", "호텔경영학과", 
        "화장품과학과", "ICT응용학과"]

FIELDNAMES=["dept", "grade","class_id", "class_num", "class_name", "professor", "credit", "schedule", "type", "general_area","lecture_area"]
results = []

# === 저장 로직 ===
def save_results_partial(json_path="dsu_courses.json", csv_path="dsu_courses.csv"):
    # JSON 저장
    with open(json_path, "w", encoding="utf-8") as f:
        json.dump(results, f, ensure_ascii=False, indent=2)
    print(f"[INFO] JSON 저장 완료 ({json_path})")

    # CSV 저장
    with open(csv_path, "w", encoding="utf-8", newline='') as f:
        writer = csv.DictWriter(f, fieldnames=FIELDNAMES)
        writer.writeheader()
        for item in results:
            item_copy = item.copy()
            item_copy["schedule"] = json.dumps(item_copy["schedule"], ensure_ascii=False)
            writer.writerow(item_copy)
    print(f"[INFO] CSV 저장 완료 ({csv_path})")

# === 크롤링 루프 ===

## === 클릭 로직 ===
def click_first_td(subj):
    try:
        td = subj.find_element(By.CSS_SELECTOR, "td[role='gridcell']")
        driver.execute_script("arguments[0].scrollIntoView({behavior: 'instant', block: 'center'});", td)
        driver.execute_script("""
            const evt = new MouseEvent('click', { bubbles: true, cancelable: true, view: window });
            arguments[0].dispatchEvent(evt);
        """, td)
        return True
    except Exception as e:
        print(f"[ERROR] TD 클릭 실패: {e}")
        return False

## === 크롤링 init 로직 ===
def parse_subject_detail(type_name, dept_name=None, grade=None, area=None):
    try:
        time.sleep(2)
        rows = driver.find_elements(By.CSS_SELECTOR, "#GridLecture tr[role='row']")
    except Exception as e:
        print(f"[ERROR] 로딩 대기 실패: {e}")
        return

    print(f"[DEBUG] rows 개수: {len(rows)}")
    for idx, row in enumerate(rows):
        print(f"[DEBUG] row[{idx}] HTML: {row.get_attribute('outerHTML')}")
        cols = row.find_elements(By.CSS_SELECTOR, 'td[role="gridcell"]')
        print(f"[DEBUG] row[{idx}] cols 수: {len(cols)}")
        if len(cols) < 10:
            continue

        try:
            print(f"[DEBUG] 값 추출 시도")
            class_id = cols[1].text.strip()
            class_num = cols[2].text.strip()
            class_name = cols[3].text.strip()
            credit_text = cols[5].text.strip()
            credit = int(float(credit_text)) if credit_text else 0
            professor = cols[7].text.strip()
            time_raw = cols[8].text.strip()
            location = cols[8].get_attribute("title").strip()

            print(f"[RESULT] {class_id=}, {class_num=}, {class_name=}, {credit_text=}, {professor=}, {time_raw=}")
            

            location_match = re.search(r'\((.*?)\)', location)
            lecture_area = location_match.group(1) if location_match else None
            schedule = []
            for d, start, end in re.findall(r'(\w)\s*(\d+)[^\d]?-?\s*(\d+)?', time_raw):
                for t in range(int(start), int(end or start)+1):
                    schedule.append({"day": d, "time": t})

            results.append({
                "dept": dept_name,
                "grade": grade,
                "class_id": class_id,
                "class_num": int(class_num) if class_num.isdigit() else 0,
                "class_name": class_name,
                "professor": professor,
                "credit": credit,
                "schedule": schedule,
                "type": type_name,
                "general_area": area,
                "lecture_area": lecture_area 
            })
            if len(results) % 10 == 0:
                save_results_partial()
        except Exception as e:
            print(f"[ERROR] 상세 행 처리 실패: {e}")

## === 강의 Search & init 로직 ===
def process_subjects(type_name: str, grade: str = None, dept: str = None, area: str = None):
    subjects = driver.find_elements(By.CSS_SELECTOR, "#GridArea.ui-jqgrid-btable tbody tr[id]")
    print(f"[DEBUG] {len(subjects)}개 과목 로드됨")

    for i, subj in enumerate(subjects):
        print(f"[DEBUG] {i+1}번째 과목 클릭 시도")
        if not click_first_td(subj):
            print(f"[WARN] 클릭 실패했지만 강제로 상세정보 진입 시도 중")
        parse_subject_detail(type_name, grade=grade, dept_name=dept, area=area)

## === 교양강의 크롤링 로직 ===
def crawl_general_subjects(type_name: str, area_list: list[str] = None):
    if type_name == '교선균형':
        for area in area_list:
            try:
                print(f"[INFO] 영역 선택 중: {area}")
                area_select().select_by_visible_text(area)
                time.sleep(0.6)
            except Exception as e:
                print(f"[ERROR] 영역 선택 실패: {e}")
                continue

            print(f"[INFO] 학년 선택 중: 1학년")
            grades.select_by_visible_text("1학년")
            time.sleep(0.6)

            process_subjects(type_name,  area=area)

    elif type_name == '교양필수':
        for grade in ['1학년', '2학년']:
            print(f"[INFO] 학년 선택 중: {grade}")
            grades.select_by_visible_text(grade)
            time.sleep(0.6)

            process_subjects(type_name, grade=grade)

## === 전공강의 크롤링 로직 ===
def crawl_major_subjects():  
    for dept in target_departments:
        try:
            departments.select_by_visible_text(dept)
        except Exception as e:
            print(f"[WARN] 학과 '{dept}' 선택 불가: {e}")
            continue

        for grade_opt in grades.options:
            grade = grade_opt.text.strip()
            if not grade or grade == "학년":
                continue

            grades.select_by_visible_text(grade)
            print(f"[INFO] 학년 선택 중: {grade}")
            time.sleep(0.6)

            for type_name in ['전공필수', '전공선택']:  # 전공필수, 전공선택 모두 실행
                print(f"[INFO] 이수구분 처리 중: {type_name} | {dept=} {grade=}")
                type_select.select_by_visible_text(type_name)
                time.sleep(0.6)
                process_subjects(type_name, grade=grade, dept=dept)

## === Main Crawling ===
for type_name, area_list in type_map.items():
    try:
        print(f"[INFO] 이수구분 선택 중: {type_name}")
        type_select.select_by_visible_text(type_name)
        time.sleep(0.7)

        crawl_general_subjects(type_name, area_list)
    except Exception as e:
        print(f"[ERROR] 이수구분 처리 실패: {e}")

crawl_major_subjects()

# === 마지막 정리 ===
print(f"총 {len(results)}개 강의 데이터를 저장했습니다.")
input("\n[엔터를 누르면 종료]")
driver.quit()