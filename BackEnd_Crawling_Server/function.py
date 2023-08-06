from selenium.webdriver.common.keys import Keys
from selenium.common.exceptions import ElementNotInteractableException
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By
from bs4 import BeautifulSoup
from time import sleep

def createDriver() -> webdriver.Chrome:
    options = webdriver.ChromeOptions()
    options.add_argument('headless')
    options.add_argument('lang=ko_KR')
    driver = webdriver.Chrome(service= Service(ChromeDriverManager().install()), options = options)

    return driver

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