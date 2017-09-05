import csv
import StringIO

def loadTradeRecord(line):
    input = StringIO.StringIO(line)
    reader = csv.DictReader(input, fieldsnames = [])
    return reader.next()

inputFile = "file://F:/Codes/spring-development/OptionData/20160811.DO.nxc.trade"
input = sc.textFile(inputFile).map(loadTradeRecord)
