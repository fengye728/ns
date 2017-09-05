# -*- encoding:UTF-8 -*-
import urllib.request
import re
from SECUtil import *

CODING_FORMAT = 'UTF-8'

def parseSECTableRow(rowStr):
    return Filing.parseFilingXMLRow(rowStr)

def parseSECTable(tableStr):
    table = []

    rows = re.findall(SEC_TABLE_ROW_REG, tableStr, re.S)

    for i in range(1, len(rows)):
        filing = parseSECTableRow(rows[i])
        table.append(filing)

    return table


def parseFilingList(params):

    params['start'] = 0

    LEFT_FLAG_REG = 'Next ' + str(params['count'])

    companyName = None
    filings = []
    while True:
        fullUrl = SEC_SEARCH_URL + urllib.parse.urlencode(params)
        
        content = urllib.request.urlopen(fullUrl).read()
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

    hrUrl = re.search(SEC_FILING_HR_URL_REG, content, re.I)

    if hrUrl:
        return hrUrl.group(1)
    else:
        return

    
def parseHRTable(content):
    hrList = HRList.parseInfoTableListXML(content)
    return hrList
    

def spideHR(stockSymbol, filingType):
    
    params = {}
    params['owner'] = 'exclude'
    params['count'] = 100
    params['CIK'] = stockSymbol
    params['type'] = filingType
    

    filings = parseFilingList(params)

    for filing in filings:
        filingUrl = SEC_HOME_URL + filing.url
        filingContent = urllib.request.urlopen(filingUrl).read()
        filingContent = filingContent.decode(CODING_FORMAT)

        filing.stockSymbol = stockSymbol
        filing.reportDate = re.search(SEC_FILING_REPORT_DATE_REG, filingContent, re.S).group(1)
        
        hrUrl = parseHRUrl(filingContent)
        if not hrUrl:
            continue
        
        hrUrl = SEC_HOME_URL + hrUrl
        hrContent = urllib.request.urlopen(hrUrl).read()
        hrContent = hrContent.decode(CODING_FORMAT)
        
        filing.info = parseHRTable(hrContent)
        print(stockSymbol + filing.reportDate, len(filing.info.infoTableList))
        filing.persistToDisk()

    
table = spideHR('BLK', '13F-HR')

