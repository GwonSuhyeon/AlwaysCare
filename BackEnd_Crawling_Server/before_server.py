import os
from time import sleep
import numpy

from pyvirtualdisplay import Display
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import NoSuchElementException
from selenium.common.exceptions import ElementNotInteractableException
from selenium.common.exceptions import StaleElementReferenceException
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
from bs4 import BeautifulSoup

#ChromeDriver setting
options = webdriver.ChromeOptions()
options.add_argument('headless')
options.add_argument('lang=ko_KR')

driver = webdriver.Chrome(service= Service(ChromeDriverManager().install()), options = options)
name_list = []
all_review = []
score_list = []


def main():
    global driver, load_wb, review_num

    driver.implicitly_wait(4)  # 렌더링 될때까지 기다린다 4초
    driver.get('https://map.kakao.com/')  # 주소 가져오기

    # 검색할 목록 -> server로 변경시 지역 이름 받아와서 검색
    place_infos = ['상도 동물병원']

    for i, place in enumerate(place_infos):
        # delay
        if i % 4 == 0 and i != 0:
            sleep(5)
        print("start")
        search(place)

    driver.quit()
    print("finish")


def search(place):
    global driver

    search_area = driver.find_element(By.XPATH, '//*[@id="search.keyword.query"]')  # 검색 창
    search_area.send_keys(place)  # 검색어 입력
    driver.find_element(By.XPATH, '//*[@id="search.keyword.submit"]').send_keys(Keys.ENTER)  # Enter로 검색
    sleep(1)

    # 검색된 정보가 있는 경우에만 탐색
    # 1번 페이지 place list 읽기
    html = driver.page_source

    soup = BeautifulSoup(html, 'html.parser')
    place_lists = soup.select('.placelist > .PlaceItem') # 검색된 장소 목록

    try : 
        # 검색된 첫 페이지 장소 목록 크롤링하기 -> 첫 페이지에서 최대 7개 = 거리순 정렬
        crawling(place, place_lists)
        search_area.clear()
    except ElementNotInteractableException :
        print('not found')


def crawling(place, place_lists):
    """
    페이지 목록을 받아서 크롤링 하는 함수
    :param place: 리뷰 정보 찾을 장소이름
    """

    while_flag = False
    for i, place in enumerate(place_lists):
        # 광고에 따라서 index 조정해야함

        place_name = place.select('.head_item > .tit_name > .link_name')[0].text  # place name
        place_address = place.select('.info_item > .addr > p')[0].text  # place address

        detail_page_xpath = '//*[@id="info.search.place.list"]/li[' + str(i + 1) + ']/div[5]/div[4]/a[1]'
        driver.find_element(By.XPATH, detail_page_xpath).send_keys(Keys.ENTER)
        driver.switch_to.window(driver.window_handles[-1])  # 상세정보 탭으로 변환
        sleep(1)
        name_list.append(place_name)
        # print('####', place_name)

        # 첫 페이지 -> 일단은 첫 페이지만 review 저장
        extract_review(place_name)

        # # 2-5 페이지 -> 혹시 변경사항 생길 수 있어서 남겨둔 코드
        # idx = 3
        # try:
        #     page_num = len(driver.find_elements(By.CLASS_NAME, 'link_page')) # 페이지 수 찾기
        #     for i in range(page_num-1):
        #         # css selector를 이용해 페이지 버튼 누르기
        #         driver.find_element_by_css_selector('#mArticle > div.cont_evaluation > div.evaluation_review > div > a:nth-child(' + str(idx) +')').send_keys(Keys.ENTER)
        #         sleep(1)
        #         extract_review(place_name)
        #         idx += 1
        #     driver.find_element(By.LINK_TEXT, '다음').send_keys(Keys.ENTER) # 5페이지가 넘는 경우 다음 버튼 누르기
        #     sleep(1)
        #     extract_review(place_name) # 리뷰 추출
        # except (NoSuchElementException, ElementNotInteractableException):
        #     print("no review in crawling")

        # # 그 이후 페이지
        # while True:
        #     idx = 4
        #     try:
        #         page_num = len(driver.find_elements(By.CLASS_NAME,'link_page'))
        #         for i in range(page_num-1):
        #             driver.find_element_by_css_selector('#mArticle > div.cont_evaluation > div.evaluation_review > div > a:nth-child(' + str(idx) +')').send_keys(Keys.ENTER)
        #             sleep(1)
        #             extract_review(place_name)
        #             idx += 1
        #         driver.find_element(By.LINK_TEXT,'다음').send_keys(Keys.ENTER) # 10페이지 이상으로 넘어가기 위한 다음 버튼 클릭
        #         sleep(1)
        #         extract_review(place_name) # 리뷰 추출
        #     except (NoSuchElementException, ElementNotInteractableException):
        #         print("no review in crawling")
        #         break

        driver.close()
        driver.switch_to.window(driver.window_handles[0])  # 검색 탭으로 전환


def extract_review(place_name):
    global driver

    ret = True

    html = driver.page_source
    soup = BeautifulSoup(html, 'html.parser')

    # 첫 페이지 리뷰 목록 찾기
    review_lists = soup.select('.list_evaluation > li')
    #별점 가져오기
    num_rate = soup.select('.grade_star > em')
    rating = num_rate[0].text
    score_list.append(rating.replace('점',''))
    # print(rating)

    # 리뷰가 있는 경우
    if len(review_lists) != 0:
        reviews = []
        for i, review in enumerate(review_lists):
            comment = review.select('.txt_comment > span') # 리뷰
            val = ''
            if len(comment) != 0:
                val = comment[0].text + '/0'
                # print(val)
                reviews.append(val)
        all_review.append(reviews)

    else:
        print('no review in extract')
        ret = False

    return ret


if __name__ == "__main__":
    main()
    print(name_list)
    print(score_list)
    print(all_review)