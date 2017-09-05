# -*- encoding:UTF-8 -*-
import urllib.request
import re

CODING_FORMAT = 'UTF-8'

SEC_HOME_URL = 'https://www.sec.gov/'

SEC_SEARCH_URL = "https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&"

SEC_COMPANY_NAME_REG = 'class="companyName"[^>]*>([^<]+)<'

SEC_TABLE_REG = 'summary="Results".*?>(.*?)</table>'

SEC_TABLE_ROW_REG = '<tr.*?>(.*?)</tr>'

SEC_TABLE_DATA_REG = '<td.*?>(.*?)</td>'

SEC_TABLE_DATE_URL_REG = 'href="(.*?)".*?id="documentsbutton".*?>'

SEC_HR_URL_REG = 'href="(.*?)".*?>form13fInfoTable.xml<'

SEC_INFO_TABLE_REG = '<infoTable>(.*?)</infoTable>'
SEC_INFO_TABLE_ISSUER_NAME_REG = '<nameOfIssuer>(.*?)</nameOfIssuer>'
SEC_INFO_TABLE_CLASS_REG = '<titleOfClass>(.*?)</titleOfClass>'
SEC_INFO_TABLE_CUSIP_REG = '<cusip>(.*?)</cusip>'
SEC_INFO_TABLE_VALUE_REG = '<value>(.*?)</value>'
SEC_INFO_TABLE_AMOUNT_SH_PRN_REG = '<sshPrnamt>(.*?)</sshPrnamt>'
SEC_INFO_TABLE_SH_OR_PRN_REG = '<sshPrnamtType>(.*?)</sshPrnamtType>'
SEC_INFO_TABLE_INVEST_D_REG = '<investmentDiscretion>(.*?)</investmentDiscretion>'
#SEC_INFO_TABLE_OTHER_MAG_REG = '<otherManager>(.*?)</otherManager>'
SEC_INFO_TABLE_VOTE_SOLE_REG = '<Sole>(.*?)</Sole>'
SEC_INFO_TABLE_VOTE_SHARED_REG = '<Shared>(.*?)</Shared>'
SEC_INFO_TABLE_VOTE_NONE_REG = '<None>(.*?)</None>'




FILING_TYPE = 'type'

FILING_URL = 'url'

FILING_DATE = 'date'

FILING_REPORT_DATE = 'reportDate'
    


def parseSECTableRow(rowStr):

    datas = re.findall(SEC_TABLE_DATA_REG, rowStr, re.S)

    filing = {}

    filing[FILING_TYPE] = datas[0]
    filing[FILING_URL] = re.search(SEC_TABLE_DATE_URL_REG, datas[1], re.S).group(1)
    filing[FILING_DATE] = datas[3]
    
    return filing

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

    hrUrl = re.search(SEC_HR_URL_REG, content, re.I)

    if hrUrl:
        return hrUrl.group(1)
    else:
        return


def parseInfoTable(content):
    info = {}
    
    re.search()

    
def parseHRTable(content):
    urllib.parse.
    

def spideHR(stockSymbol, filingType):
    
    params = {}
    params['owner'] = 'exclude'
    params['count'] = 100
    params['CIK'] = stockSymbol
    params['type'] = filingType
    

    filings = parseFilingList(params)

    for filing in filings:
        filingUrl = SEC_HOME_URL + filing[FILING_URL]
        content = urllib.request.urlopen(filingUrl).read()
        content = content.decode(CODING_FORMAT)

        hrUrl = parseHRUrl(content)
        if not hrUrl:
            continue
        hrUrl = SEC_HOME_URL + hrUrl
        content = urllib.request.urlopen(hrUrl).read()
        content = content.decode(CODING_FORMAT)
        parseHRTable(content)


    
table = spideHR('BLK', '13F-HR')

