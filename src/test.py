# file: crawler_dcp_selenium.py

import os
import time
import re
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC

# 현재 파일 기준 chromedriver 경로 설정
base_dir = os.path.dirname(os.path.abspath(__file__))
driver_path = os.path.join(base_dir, '..', 'chromedriver', 'chromedriver.exe')

# Chrome 옵션 설정
options = Options()
# options.add_argument("--headless")
options.add_argument("--no-sandbox")
options.add_argument("--disable-dev-shm-usage")
options.add_argument("--disable-blink-features=AutomationControlled")
options.add_experimental_option("excludeSwitches", ["enable-automation"])
options.add_experimental_option("useAutomationExtension", False)

# 셀레니움 드라이버 실행
service = Service(executable_path=driver_path)
driver = webdriver.Chrome(service=service, options=options)
driver.execute_cdp_cmd("Page.addScriptToEvaluateOnNewDocument", {
    "source": """
    Object.defineProperty(navigator, 'webdriver', {get: () => undefined})
    """
})

wait = WebDriverWait(driver, 10)

# 수강 신청 사이트 접속
driver.get("https://sugang.dongseo.ac.kr/")

# 로그인 후 수동 진행 대기
input("로그인 완료 후 엔터를 누르세요...")

# iframe 전환
driver.switch_to.frame("Main")

# 수업 목록 로딩 대기
wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, 'tr[role="row"]')))

# HTML 파싱
html = driver.page_source
soup = BeautifulSoup(html, 'html.parser')

# 수업 행 파싱
rows = soup.select('tr[role="row"]')

for row in rows:
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

        # 강의시간 파싱 → 요일/교시 단위 schedule 리스트 구성
        schedule = []
        time_pattern = re.findall(r'(월|화|수|목|금|토|일)[^\d]*(\d+)\s*-\s*(\d+)', time_raw)
        for day, start, end in time_pattern:
            for t in range(int(start), int(end) + 1):
                schedule.append({"day": day, "time": t})

        subject = {
            "class_id": class_id,
            "class_num" : class_num,
            "class_name": class_name,
            "professor": professor,
            "credit": credit,
            "schedule": schedule
        }

        print(subject)

    except Exception as e:
        print(f"[ERROR] 한 수업 파싱 실패: {e}")

input("엔터를 누르면 브라우저 종료...")
driver.quit()