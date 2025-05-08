# file: crawler_dcp_selenium.py

import os
import time
import re
import json
import csv
from bs4 import BeautifulSoup
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
        wait.until(EC.presence_of_element_located((By.ID, "sDept")))
        break
    except:
        continue

# === Dropdown 정의 ===
type_select = Select(driver.find_element(By.ID, "sItype"))
type_map = {
    '교양필수': [],
    '교선균형': ['인간과역사', '사회와가치', '자연과기술', '문학과예술', '세계와문화'],
    '전공선택': [],
    '전공필수': []
}

departments = Select(driver.find_element(By.ID, "sDept"))
grades = Select(driver.find_element(By.ID, "sGrade"))
area_select = lambda: Select(driver.find_element(By.ID, "sArea"))

results = []

# === 크롤링 루프 ===
for type_name, area_list in type_map.items():
    type_select.select_by_visible_text(type_name)
    time.sleep(0.7)

    if type_name == '교선균형':
        for area in area_list:
            try:
                area_select().select_by_visible_text(area)
                time.sleep(0.6)
                subjects = driver.find_elements(By.CSS_SELECTOR, "#gridSubjL tr[role='row']")[1:]
                for subj in subjects:
                    try:
                        subj.click()
                        wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "#gridSubjR tr[role='row']")))
                        time.sleep(0.5)

                        soup = BeautifulSoup(driver.page_source, "html.parser")
                        details = soup.select("#gridSubjR tr[role='row']")
                        for row in details:
                            cols = row.select('td[role="gridcell"]')
                            if len(cols) < 10:
                                continue
                            try:
                                class_id = cols[1].get_text(strip=True)
                                class_num = cols[2].get_text(strip=True)
                                class_name = cols[3].get_text(strip=True)
                                credit_text = cols[5].get_text(strip=True)
                                credit = int(float(credit_text)) if credit_text else 0
                                professor = cols[7].get_text(strip=True)
                                time_raw = cols[8].get_text(separator=" / ", strip=True)

                                schedule = []
                                time_pattern = re.findall(r'(\w)\s*(\d+)[^\d]?-?\s*(\d+)?', time_raw)
                                for d, start, end in time_pattern:
                                    for t in range(int(start), int(end or start) + 1):
                                        schedule.append({"day": d, "time": t})

                                results.append({
                                    "class_id": class_id,
                                    "class_num": int(class_num) if class_num.isdigit() else 0,
                                    "class_name": class_name,
                                    "professor": professor,
                                    "credit": credit,
                                    "schedule": schedule,
                                    "type": type_name,
                                    "area": area
                                })
                            except:
                                continue
                    except:
                        continue
            except:
                continue
    else:
        for dept_opt in departments.options:
            dept = dept_opt.text.strip()
            if not dept or dept == "학과":
                continue
            departments.select_by_visible_text(dept)

            for grade_opt in grades.options:
                grade = grade_opt.text.strip()
                if not grade or grade == "학년":
                    continue
                grades.select_by_visible_text(grade)
                time.sleep(0.6)

                subjects = driver.find_elements(By.CSS_SELECTOR, "#gridSubjL tr[role='row']")[1:]
                for subj in subjects:
                    try:
                        subj.click()
                        wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, "#gridSubjR tr[role='row']")))
                        time.sleep(0.5)

                        soup = BeautifulSoup(driver.page_source, "html.parser")
                        details = soup.select("#gridSubjR tr[role='row']")
                        for row in details:
                            cols = row.select('td[role="gridcell"]')
                            if len(cols) < 10:
                                continue
                            try:
                                class_id = cols[1].get_text(strip=True)
                                class_num = cols[2].get_text(strip=True)
                                class_name = cols[3].get_text(strip=True)
                                credit_text = cols[5].get_text(strip=True)
                                credit = int(float(credit_text)) if credit_text else 0
                                professor = cols[7].get_text(strip=True)
                                time_raw = cols[8].get_text(separator=" / ", strip=True)

                                schedule = []
                                time_pattern = re.findall(r'(\w)\s*(\d+)[^\d]?-?\s*(\d+)?', time_raw)
                                for d, start, end in time_pattern:
                                    for t in range(int(start), int(end or start) + 1):
                                        schedule.append({"day": d, "time": t})

                                results.append({
                                    "class_id": class_id,
                                    "class_num": int(class_num) if class_num.isdigit() else 0,
                                    "class_name": class_name,
                                    "professor": professor,
                                    "credit": credit,
                                    "schedule": schedule,
                                    "type": type_name,
                                    "area": None
                                })
                            except:
                                continue
                    except:
                        continue

# === Save to JSON & CSV ===
with open("dsu_courses.json", "w", encoding="utf-8") as f:
    json.dump(results, f, ensure_ascii=False, indent=2)

with open("dsu_courses.csv", "w", encoding="utf-8", newline='') as f:
    writer = csv.DictWriter(f, fieldnames=["class_id", "class_num", "class_name", "professor", "credit", "schedule", "type", "area"])
    writer.writeheader()
    for item in results:
        item_copy = item.copy()
        item_copy["schedule"] = json.dumps(item_copy["schedule"], ensure_ascii=False)
        writer.writerow(item_copy)

print(f"총 {len(results)}개 강의 데이터를 저장했습니다.")
input("\n[엔터를 누르면 종료]")
driver.quit()
