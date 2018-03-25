# -*- encoding:UTF-8 -*-

'''
	Usage: 
		HRSpider companyFile output_path
'''
import urllib.request
import contextlib
import socket

import re
import os
import sys
import time

from SECUtil import *

import random

''' Constants '''
CODING_FORMAT = 'UTF-8'

REQUEST_WAIT_TIME = 5 # unit: second


# Global Configuration

socket.setdefaulttimeout(30)    # 设置socket层超时时间为30s


''' Functions Definition'''

def safeUrlOpen(url):
    retry_time = 3
    content = ''
    
    for i in range(retry_time):
        try:
            with contextlib.closing(urllib.request.urlopen(url)) as response:
                content = response.read()
                
            break
        except BaseException as e:
            print('Error in safeUrlOpen',e)
            time.sleep(REQUEST_WAIT_TIME)

    return content
        

def parseSECTableRow(rowStr):
    return Filing.parseFilingXMLRow(rowStr)

def parseSECTable(tableStr):
    '''Parse SEC Filings table and get all filings without info'''
    table = []

    rows = re.findall(SEC_TABLE_ROW_REG, tableStr, re.S)

    for i in range(1, len(rows)):
        filing = Filing.parseFilingXMLRow(rows[i])
        table.append(filing)

    return table


def parseFilingList(params):
    ''' Get all filings without info corresponding to params'''
    params['start'] = 0

    LEFT_FLAG_REG = 'Next ' + str(params['count'])

    companyName = None
    filings = []
    while True:
        fullUrl = SEC_SEARCH_URL + urllib.parse.urlencode(params)
        
        content = safeUrlOpen(fullUrl)
        content = content.decode(CODING_FORMAT)
        
        if not companyName:
            companyName = re.search(SEC_COMPANY_NAME_REG, content).group(1)
            print(companyName)
        
        tableMatch = re.search(SEC_TABLE_REG, content, re.S)

        
        if tableMatch:
            filings.extend(parseSECTable(tableMatch.group(1)))
        else:
            return filings
            
        nextMatch = re.search(LEFT_FLAG_REG, content)
        if nextMatch:
            params['start'] = len(filings)
        else:
            return filings

def parseHRUrl(content):

    hrUrl = re.search(SEC_FILING_HR_URL_REG, content, re.S)

    if hrUrl:
        return hrUrl.group(1)
    else:
        return

    
def parseHRTable(content):
    return HRList.parseHRListXML(content)
    
def spideHRFilings(stockSymbol, filingType, startDate):
    
    params = {}
    params['owner'] = 'exclude'
    params['count'] = 100
    params['CIK'] = stockSymbol
    params['type'] = filingType
    #params['dateb'] = 20160313
    

    filings = parseFilingList(params)

    ''' Get the content of all filings '''
    for filing in filings[:]:
        try:
            filingUrl = SEC_HOME_URL + filing.url
            filingContent = safeUrlOpen(filingUrl)
            filingContent = filingContent.decode(CODING_FORMAT)

            filing.stockSymbol = stockSymbol
            filing.reportDate = re.search(SEC_FILING_REPORT_DATE_REG, filingContent, re.S).group(1)
            filing.reportDate = Filing.convertDate2Num(filing.reportDate)

            if(filing.reportDate <= startDate):
                filings = filings[:filings.index(filing)]
                break
            
            hrUrl = parseHRUrl(filingContent)
            if not hrUrl:
                filings.remove(filing)
                continue
            
            hrUrl = SEC_HOME_URL + hrUrl
            hrContent = safeUrlOpen(hrUrl)
            hrContent = hrContent.decode(CODING_FORMAT)
            
            filing.info = parseHRTable(hrContent)

            if len(filing.info.hrRecordList) == 0:
                filings.remove(filing)
                continue
            
            # Persist Holding Report
            if not os.path.exists(outputPath):
                os.mkdir(outputPath)
                
            filing.info.refine()
            print(stockSymbol + '-' + str(filing.reportDate), len(filing.info.hrRecordList))
            filing.persistInfoToDisk(outputPath)
        except BaseException as e:
            print(e.reason)
            print('--------- Error happened in', stockSymbol, filing.reportDate)
            filings.remove(filing)

    return filings

def persistToDisk(stockSymbol, filings, outputPath):
    if not os.path.exists(outputPath) and len(filings) > 0:
        os.mkdir(outputPath)
    
    for filing in filings:
        filing.info.refine()
        print(stockSymbol + '-' + str(filing.reportDate), len(filing.info.hrRecordList))
        filing.persistInfoToDisk(outputPath)


'''Statement'''

#Get all target companies

companyFile = 'companies'
destDirectory = os.getcwd()

filingType = '13F-HR'

if(len(sys.argv) == 1):
    pass
elif(len(sys.argv) == 2):
    companyFile = sys.argv[1]
elif(len(sys.argv) == 3):
    companyFile = sys.argv[1]
    destDirectory = sys.argv[2]
else:
    print('Arguments wrong!')
    sys.exit()

# Check if output folder exists, if not then create folder
if not os.path.exists(destDirectory):
    os.mkdir(destDirectory)

# get cik list for fetching filings
stockSymbolList = []
with open(companyFile, 'r') as cf:
    for line in cf.readlines():
        if(line[-1] == '\n'):
            line = line[:-1].rstrip()
        else:
            line = line.rstrip()

        if(line != ''):
            stockSymbolList.append(line)


# 随机打乱 cik list
random.shuffle(stockSymbolList)

for stockSymbol in stockSymbolList:
    
    outputPath = destDirectory + os.path.sep + stockSymbol
    
    if os.path.exists(outputPath):
        files = os.listdir(outputPath)
        files = list(filter(lambda name : re.match('^' + stockSymbol + HR_FILE_SEP + '.*?hr$', name), files))
        if len(files) == 0:
            startDate = 0
        else:
            lastFile = max(files)
            startDate = int(re.search('(\d+)\.hr', lastFile[len(stockSymbol) + len(HR_FILE_SEP):]).group(1))
    else:
        startDate = 0

    print(stockSymbol, startDate, end = ' | ')
    filings = spideHRFilings(stockSymbol, filingType, startDate)
    # Persist all filings of this company
    #persistToDisk(stockSymbol, filings, outputPath)

