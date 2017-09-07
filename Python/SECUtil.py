import re
import os

HR_FILE_SEP = '-'

SEC_HOME_URL = 'https://www.sec.gov/'

SEC_SEARCH_URL = "https://www.sec.gov/cgi-bin/browse-edgar?action=getcompany&"

SEC_COMPANY_NAME_REG = 'class="companyName"[^>]*>([^<]+)<'

SEC_TABLE_REG = 'summary="Results".*?>(.*?)</table>'

SEC_TABLE_ROW_REG = '<tr.*?>(.*?)</tr>'

SEC_TABLE_DATA_REG = '<td.*?>(.*?)</td>'

SEC_TABLE_DATE_URL_REG = 'href="(.*?)".*?id="documentsbutton".*?>'


SEC_FILING_REPORT_DATE_REG = 'Period of Report</div>.*?<div class="info">(.*?)</div>'
SEC_FILING_HR_URL_REG = '2</td>.*?href="([^\n]*?)"[^\n]*?xml</a>.*?INFORMATION TABLE'

SEC_INFO_TABLE_REG = '<[^<>\n]*?infoTable>(.*?)</[^<>\n]*?infoTable>'
SEC_INFO_TABLE_ISSUER_NAME_REG = '<[^<>\n]*?nameOfIssuer>(.*?)</[^<>\n]*?nameOfIssuer>'
SEC_INFO_TABLE_CLASS_REG = '<[^<>\n]*?titleOfClass>(.*?)</[^<>\n]*?titleOfClass>'
SEC_INFO_TABLE_CUSIP_REG = '<[^<>\n]*?cusip>(.*?)</[^<>\n]*?cusip>'
SEC_INFO_TABLE_VALUE_REG = '<[^<>\n]*?value>(.*?)</[^<>\n]*?value>'
SEC_INFO_TABLE_AMOUNT_SH_PRN_REG = '<[^<>\n]*?sshPrnamt>(.*?)</[^<>\n]*?sshPrnamt>'
SEC_INFO_TABLE_SH_OR_PRN_REG = '<[^<>\n]*?sshPrnamtType>(.*?)</[^<>\n]*?sshPrnamtType>'
SEC_INFO_TABLE_INVEST_D_REG = '<[^<>\n]*?investmentDiscretion>(.*?)</[^<>\n]*?investmentDiscretion>'
#SEC_INFO_TABLE_OTHER_MAG_REG = '<[^<>\n]*?otherManager>(.*?)</[^<>\n]*?otherManager>'
SEC_INFO_TABLE_VOTE_SOLE_REG = '<[^<>\n]*?Sole>(.*?)</[^<>\n]*?Sole>'
SEC_INFO_TABLE_VOTE_SHARED_REG = '<.*?Shared>(.*?)</[^<>\n]*?Shared>'
SEC_INFO_TABLE_VOTE_NONE_REG = '<[^<>\n]*?None>(.*?)</[^<>\n]*?None>'


'''Does not process options'''

class Filing:
    def __init__(self):
        self.stockSymbol = ''
        self.type = ''
        self.url = ''
        self.date = ''
        self.reportDate = ''
        '''HRList'''
        self.info = []  

    def parseFilingXMLRow(content):
        datas = re.findall(SEC_TABLE_DATA_REG, content, re.S)
        
        filing = Filing()
        filing.type = datas[0]
        filing.url = re.search(SEC_TABLE_DATE_URL_REG, datas[1], re.S).group(1)
        filing.date = Filing.convertDate2Num(datas[3])
        
        return filing

    def persistInfoToDisk(self, path):
        '''Format of file name : Symbol + ReportDate.hr'''
        filename = path + os.path.sep + self.stockSymbol + HR_FILE_SEP + str(self.reportDate) + '.hr'
        with open(filename, 'a') as f:
            f.write(self.info.toCSVString())

    def convertDate2Num(date):
        return int(date.replace('-', ''))


class InfoTable:
    def __init__(self):
        self.issuerName = ''
        self.titleClass = ''
        self.cusip = 0
        self.value = 0
        self.amountSHorPRN = 0
        self.SHorPRN = ''
        #self.otherManager = ''
        self.investD = ''
        self.voteSole = 0
        self.voteShared = 0
        self.voteNone = 0

    def parseInfoTableXML(content):
        self = InfoTable()
        try:
            self.issuerName = re.search(SEC_INFO_TABLE_ISSUER_NAME_REG, content, re.S).group(1)
            self.titleClass = re.search(SEC_INFO_TABLE_CLASS_REG, content, re.S).group(1)
            self.cusip = re.search(SEC_INFO_TABLE_CUSIP_REG, content, re.S).group(1)
            self.value = int(re.search(SEC_INFO_TABLE_VALUE_REG, content, re.S).group(1))
            self.amountSHorPRN = int(re.search(SEC_INFO_TABLE_AMOUNT_SH_PRN_REG, content, re.S).group(1))
            self.SHorPRN = re.search(SEC_INFO_TABLE_SH_OR_PRN_REG, content, re.S).group(1)
            #self.otherManager = re.search(SEC_INFO_TABLE_OTHER_MAG_REG, content, re.S).group(1)
            self.investD = re.search(SEC_INFO_TABLE_INVEST_D_REG, content, re.S).group(1)
            self.voteSole = int(re.search(SEC_INFO_TABLE_VOTE_SOLE_REG, content, re.S).group(1))
            self.voteShared = int(re.search(SEC_INFO_TABLE_VOTE_SHARED_REG, content, re.S).group(1))
            self.voteNone = int(re.search(SEC_INFO_TABLE_VOTE_NONE_REG, content, re.S).group(1))
        except AttributeError:
            print(content)
            raise

        return self

    def compare(self, target):
        if(self.cusip == target.cusip and self.titleClass == target.titleClass and self.investD == target.investD and self.SHorPRN == target.SHorPRN):
            return True
        else:
            return False

    def toString(self):
        return self.issuerName + ',' + self.titleClass + ',' + self.cusip + ',' + str(self.value) + ',' + str(self.amountSHorPRN) + ',' + self.SHorPRN + ',' + self.investD + ',' + str(self.voteSole) + ',' + str(self.voteShared) + ',' + str(self.voteNone)

class HRList:
    def __init__(self):
        self.infoTableList = []

    '''Class Method'''
    def parseInfoTableListXML(content):
        strInfoTableList = re.findall(SEC_INFO_TABLE_REG, content, re.S)

        self = HRList()
        self.infoTableList = []
        
        for strInfoTable in strInfoTableList:
            hrInfo = InfoTable.parseInfoTableXML(strInfoTable)
            self.infoTableList.append(hrInfo)

        return self

    def refine(self):
        issuerCusip = ''
        issuerHrList = []
        size = len(self.infoTableList)
        i = 0
        while(i < size):
            item = self.infoTableList[i]
            if(item.cusip == issuerCusip):
                for hrItem in issuerHrList:
                    if(item.compare(hrItem)):
                        hrItem.value += item.value
                        hrItem.amountSHorPRN += item.amountSHorPRN
                        hrItem.voteSole += item.voteSole
                        hrItem.voteShared += item.voteShared
                        hrItem.voteNone += item.voteNone
                        '''Delete item in self'''
                        self.infoTableList.pop(i)
                        i -= 1
                        size -= 1
                        break
                else:
                    ''' Add new item in hrList'''
                    issuerHrList.append(item)
            else:
                issuerCusip = item.cusip
                issuerHrList = [item]           
            i += 1

    def toCSVString(self):
        csv = ''
        for hrItem in self.infoTableList:
            csv += hrItem.toString() + '\n'

        return csv
