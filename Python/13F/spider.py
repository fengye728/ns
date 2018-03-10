#导入需要用到的包
import requests
from lxml import etree
import pandas as pd
# import pymysql
# from sqlalchemy import create_engine
import time
from config import base_url_1, base_url2_1, base_url2_2

#定义url补全函数
def url_Completion(companyURLs_list):
	#初始化i,作为companyURLs_list的索引
	i = 0
	for url in companyURLs_list:
	    if url[:4] != 'http':
	        url = 'https://www.sec.gov' + url
	        companyURLs_list[i] = url
	        i+=1
	    else:
	        companyURLs_list[i] = url
	        i+=1
	return companyURLs_list
	# companyURLs_list[:5]

#定义获取url的text函数
def getHTMLText(url):
    #设置requests的请求头:headers,代理IP:proxies
    #proxies = {
    #    "https": "http://66.81.206.241:3128" #国外代理IP,需要实时更新，IP更新地址：http://www.goubanjia.com/free/gwgn/index3.shtml
    #    }
    headers = {
    	'User-Agent':'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11',
        'Accept':'text/html;q=0.9,*/*;q=0.8',
        'Accept-Charset':'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
        'Accept-Encoding':'gzip',
        'Connection':'close',
        'Referer':'http://www.baidu.com/link?url=_andhfsjjjKRgEWkj7i9cFmYYGsisrnm2A-TN3XZDQXxvGsM9k9ZZSnikW2Yds4s&amp;amp;wd=&amp;amp;eqid=c3435a7d00146bd600000003582bfd1f'
    }
    try:
        ###加上proxies参数就返回Raise Exception???
        r = requests.get(url, headers=headers)
        r.raise_for_status()
        return r.text
    except:
        return "Raise Exception"

def getHTMLText_SSL_verify(url):
    #设置requests的请求头:headers,代理IP:proxies，同时关掉SSL证书验证verify
    proxies = {
        "https": "http://165.138.225.250:8080" #国外代理IP,可能需要实时更新，IP更新地址：http://www.goubanjia.com/free/gwgn/index3.shtml
        }
    headers = {
    	'User-Agent':'Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11',
        'Accept':'text/html;q=0.9,*/*;q=0.8',
        'Accept-Charset':'ISO-8859-1,utf-8;q=0.7,*;q=0.3',
        'Accept-Encoding':'gzip',
        'Connection':'close',
        'Referer':'http://www.baidu.com/link?url=_andhfsjjjKRgEWkj7i9cFmYYGsisrnm2A-TN3XZDQXxvGsM9k9ZZSnikW2Yds4s&amp;amp;wd=&amp;amp;eqid=c3435a7d00146bd600000003582bfd1f'
    }
    try:
        r = requests.get(url, headers = headers, proxies=proxies, verify=False)
        r.raise_for_status()
        return r.text
    except:
        return "Raise Exception"

def get_CIK(companyURLs_list):
	print("获取公司CIK进度：")
	CIK_list = []
	i = 0.0
	for url in companyURLs_list:
	    text2 = getHTMLText(url)
	    selector = etree.HTML(text2)
	    cik = selector.xpath('//*[@id="filerDiv"]/div[3]/span/a/text()') #list类型
	    
	    if len(cik) == 1:
	        cik = cik[0][:10]
	        i += 1
	        print(i)
	        print(cik)
	        CIK_list.append(cik)
	    elif len(cik) > 1:
	        for item in cik:
	            #出错：item是一个字符串，不是一个list,所以不能item[0][:10]
	            item = item[:10]
	            i += 1
	            print(i)
	            print(item)
	            CIK_list.append(item)
	    #cik可能取到空列表，else为了避免报IndexError错
	    else:
	        cik = ""
	        i += 1
	        print(i)
	        print(cik)
	        CIK_list.append(cik)
	    time.sleep(2)
	return CIK_list


#part1的公司名列表
company_list_1 = []
#part1的公司名url列表
companyURLs_list_1 = []
#part1的公司名url下的CIK列表
CIK_list_1 = []


#part1的公司名列表
company_list_2 = []
#part1的公司名url列表
companyURLs_list_2 = []
#part1的公司名url下的CIK列表
CIK_list_2 = []

######################################################
#PART 1
print('Part 1' + '#'*50)

#抓取首字母从A到K的网页源码
#offset:url的偏移量
offset = 1

# base_url_1 = 'https://www.sec.gov/cgi-bin/srch-edgar?text=10-K%20not%20NT&count=100&first=2017&last=2017&start='

#存储所有页面的html文本
text = ""
print("PART1,爬取网页进度：")
#先取到首字母为‘K’的一页，即start=3801
#for i in range(39):
for i in range(1):
	print("%.3f" % (i/39.0))
	url = base_url_1 + str(offset + i*100)
	text += getHTMLText(url)
	# print("%.3f" % (i/39.0))
	# url = base_url_1 + str(offset + i*100)
	# text += getHTMLText(url)
#     text += "*"*100
# len(text)

selector = etree.HTML(text)
company_list_1 = selector.xpath('//tr/td[@valign="top"][2]/a/text()')

companyURLs_list_1 = selector.xpath('//tr/td[@valign="top"][2]/a/@href')

companyURLs_list_1 = url_Completion(companyURLs_list_1)

CIK_list_1 = get_CIK(companyURLs_list_1)




### 调试到Part1获取CIK列表了




#######################################################
#PART 2
print('Part 2' + '#'*50)
#公司名首字母字符串
prefix_str = 'lmnopqrstuvwxyz'
print("PART1,爬取公司列表：")###待改
for char in prefix_str:
    url2 = base_url2_1 + char + base_url2_2
    while True:
        company_list_2 = []
        text2 = getHTMLText_SSL_verify(url2)

        #服务器响应超时，返回的网页源码长度小于10000时，重新请求
        if len(text2)<=10000:
            continue
        
        selector = etree.HTML(text2)

        ##获取单页的公司名以字符串char开头的url
        company_list_2 = selector.xpath('//tr/td[@valign="top"][2]/a/text()')
        temp_companyURLs_list = selector.xpath('//tr/td[@valign="top"][2]/a/@href')
        for i in range(len(company_list_2)):
            if company_list_2[i].startswith((char, char.upper())):
                print(company_list_2[i])
                companyURLs_list_2.append(temp_companyURLs_list[i])
            else:
                continue
        node_list = selector.xpath('//div/center[1]/a[contains(text(),\"[NEXT]\")]') #获取a标签下文本内容包含'[NEXT]'的节点
        if len(node_list)==1:  #如果获取到'[NEXT]'节点，则将节点url赋给url2,并自动补全
            url2 = 'https://www.sec.gov' + node_list[0].xpath('./@href')[0]
        else:
            break

companyURLs_list_2 = url_Completion(companyURLs_list_2)

CIK_list_2 = get_CIK(companyURLs_list_2)

#去重
CIK_series_1 = pd.Series(CIK_list_1)
CIK_series_2 = pd.Series(CIK_list_2)
CIK_concat = pd.concat([CIK_series_1, CIK_series_2], ignore_index=True)
CIK_concat.drop_duplicates(inplace=True)
CIK_concat.reset_index(drop=True)

with open('cik.txt', 'w') as f:
    for item in CIK_concat:
    	print(item)
    	f.write(item + '\n')

