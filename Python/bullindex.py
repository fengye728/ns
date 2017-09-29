import zipfile
import csv
import os
import datetime
import sys

DATE_FORMAT = '%Y%m%d'

SYMBOL_FILENAME = './symbol'

# Input process
if (len(sys.argv) != 2):
    print('Wrong arguments!')
    sys.exit()

DATA_PATH = sys.argv[1]


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


def getSymbolMap():
    stock_map = {}
    with open(SYMBOL_FILENAME, 'r') as f:
        for symbol in f.readlines():
            if symbol[:-1] == '':
                continue
            stock_map[symbol[:-1]] = []

    return stock_map


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
    stock_map = getSymbolMap()
    print(len(stock_map))
    for file in files:
        try:
            content = getEquityFilenameFromZip(data_path + os.sep + file)
        except BaseException:
            continue

        cur_dt = getFileDate(file)
        print('Load file:', file, len(content), cur_dt)

        # read file content
        for line in content.split('\n'):
            row = line.split(',')
            if len(row) < 8 or not row[0] in stock_map:
                continue
            row[2] = cur_dt
            row[3] = float(row[3])
            row[4] = float(row[4])
            row[5] = float(row[5])
            row[6] = float(row[6])
            row[7] = int(row[7])

            if row[3] == 0 or row[4] == 0 or row[5] == 0 or row[6] == 0:
                continue

            stock_map[row[0]].append(row)

    return stock_map


'''
quote_weight_list item:
    price spread with previous close price
'''


def preprocessQuoteList(quotelist):
    # set first
    quote_weight_list = [0] * len(quotelist)

    # pre process
    for i in range(1, len(quotelist)):
        quote_weight_list[i] = quotelist[i][6] - quotelist[i - 1][6]

    return quote_weight_list


'''
up_item:
0 - max value that sum from first element to itselt
1 - index of start element
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

'''
row columns:
0 - symbol
1 - start date
2 - trading days between start date and target date
3 - up rate
4 - up days
5 - mean up percent of day
6 - down days
7 - mean down percent of day
'''
bull_results = [] * len(stock_map)

for key, value in stock_map.items():
    if len(value) == 0:
        continue
    cur_up_item = bullindexForLongest(value)
    cur_dt = value[cur_up_item[1]][2]
    targ_dt = value[-1][2]
    minus_days = len(value) - cur_up_item[1]
    up_rate = value[-1][6] / value[cur_up_item[1]][6] - 1

    up_days = 0
    mean_up = 0.0
    down_days = 0
    mean_down = 0.0
    if cur_up_item[1] == 0:
        up_days += 1
        start = 1
    else:
        start = cur_up_item[1]
    for i in range(start, len(value)):
        magnitude = value[i][6] / value[i - 1][6] - 1
        if magnitude >= 0:
            up_days += 1
            mean_up += magnitude
        else:
            down_days += 1
            mean_down += magnitude
    if not up_days == 0:
        mean_up = mean_up / up_days
    if not down_days == 0:
        mean_down = mean_down / down_days

    bull_results.append((key, cur_dt, minus_days, up_rate, mean_up, up_days, mean_down, down_days))

bull_results.sort(key=lambda bull: -bull[3])

with open('./bull_index', 'w') as f:
    for bull in bull_results:
        f.write(str(bull) + '\n')
