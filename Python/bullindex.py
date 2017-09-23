import zipfile
import csv
import os
import datetime

DATA_PATH = 'D:\AFolderForPeter\stricknetData\Data2017'
DATE_FORMAT = '%Y%m%d'

def getEquityFilenameFromZip(zip_filename):
    if not os.path.isfile(zip_filename):
        return None
    tmp_zip = zipfile.ZipFile(zip_filename, 'r')
    
    for filename in tmp_zip.namelist():
        if filename.find('Equitiesm') >= 0:
            equityFilename = filename
            break
    return tmp_zip.read(equityFilename).decode('utf-8')

'''
   num_dt - yyyyMMDD 
'''
def increaseOneNumDt(cur_num_dt):
    cur_num_dt += 1
    DD = cur_num_dt % 100
    MM = cur_num_dt % 10000 / 100
    yyyy = cur_num_dt / 10000
    if DD > 31:
        DD = 1
        MM += 1
        if MM > 12:
            MM = 1
            yyyy += 1

    return (yyyy * 10000 + MM * 100 + DD)

def getFileDate(filename):
    return filename[5:13]

'''
stock_map: <symbol, List of row>

row column:
0 - symbol
1 - name
2 - date
3 - open
4 - high
5 - low
6 - close
'''           
def loadStockData(data_path):
    files = os.listdir(data_path)
    stock_map = {}
    for file in files:
        content = getEquityFilenameFromZip(data_path + os.sep + file)
        cur_dt = getFileDate(file)
        
        print('Load file:', file, len(content), cur_dt)
        
        # read file content
        for line in content.split('\n'):
            row = line.split(',')
            if len(row) < 8:
                continue
            row[2] = cur_dt
            row[3] = float(row[3])
            row[4] = float(row[4])
            row[5] = float(row[5])
            row[6] = float(row[6])
            row[7] = int(row[7])

            if row[3] == 0 or row[4] == 0 or row[5] == 0 or row[6] == 0:
                continue
            
            if row[0] in stock_map:
                stock_map[row[0]].append(row)
            else:
                quotelist = [row]
                stock_map[row[0]] = quotelist

    return stock_map

'''
quote_weight: price spread with previous close price
'''
def preprocessQuoteList(quotelist):
    # set first
    quote_weight_list = [0]
    
    # pre process
    for i in range(1, len(quotelist)):
        quote_weight_list.append(quotelist[i][6] - quotelist[i - 1][6])

    return quote_weight_list

'''
up_item:
0 - max value that sum from first element to itselt
1 - index of first element
'''
def bullindexForLongest(quotelist):
    quote_weight_list = preprocessQuoteList(quotelist)
    total_size = len(quote_weight_list)
    
    up_item = [0.0, 0]
    maxUps = [up_item] * total_size
    for i in range(1, total_size):
        if maxUps[i - 1][0] > 0:
            maxUps[i][0] = maxUps[i - 1][0] + quote_weight_list[i]
            maxUps[i][1] = maxUps[i - 1][1]
        else:
            maxUps[i][0] = quote_weight_list[i]
            maxUps[i][1] = i            

    return maxUps[-1]

stock_map = loadStockData(DATA_PATH)
print(len(stock_map))

key = 'SDRL'
value = stock_map[key]
'''
row columns:
0 - symbol
1 - start date
2 - days between start date and target date
3 - up rate
'''
bull_results = [] * len(stock_map)

for key, value in stock_map.items():
    cur_up_item = bullindexForLongest(value)
    
    cur_dt = value[cur_up_item[1]][2]
    targ_dt = value[-1][2]
    minus_days = (datetime.datetime.strptime(targ_dt, DATE_FORMAT) - datetime.datetime.strptime(cur_dt, DATE_FORMAT)).days
    up_rate = (value[-1][3] - value[cur_up_item[1]][6]) / value[cur_up_item[1]][6]
    bull_results.append((key, cur_dt, minus_days, up_rate))

bull_results.sort(key = lambda bull : -bull[3])

with open('D:/bull_index', 'w') as f:  
    for bull in bull_results:
        f.write(str(bull) + '\n')

