import psycopg2 as db
import numpy as np
import pandas as pd

import os
import datetime
import time

# constant
GOURD_COLUMN_LIST = ['event_day', 'symbol', 'strike', 'expiration', 'call_put', 'Direction', 'price', 'size', 'size_count', 'total_size', 'millionD']
GOURD_SQL = " \
SELECT event_day, stock_symbol, strike, expiration, call_put, 'Sell', ROUND(AVG(price), 2) as price, size, COUNT(size) as size_count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millionD \
FROM option_trade_{0} \
WHERE \
    direction LIKE 'Sell%' \
    AND expiration - event_day >= 300 \
GROUP BY event_day, stock_symbol, strike, expiration, call_put, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
    AND AVG(price) / strike > 0.2 \
ORDER BY stock_symbol, event_day, strike DESC, total_size DESC \
"

GOURD_FILENAME = r'./al_gourd1_all.csv'

START_QUARTER = 181

EXPIRATION_EVENT_GAP = 30 * 3

def gap_days(iStart, iEnd):
    FORMAT = '%y%m%d'
    iStart = int(iStart) % 1000000
    iEnd = int(iEnd) % 1000000
    try: 
        dStart = datetime.datetime.strptime(str(iStart), FORMAT)
        dEnd = datetime.datetime.strptime(str(iEnd), FORMAT)
        return (dEnd - dStart).days
    except:
        return 0
 
def execute_sql(cursor, sql):
    cursor.execute(sql)
    return list(cursor.fetchall())

def conbine_gourd_sql(quarter):
    return GOURD_SQL.format(quarter)

def to_quarter(ym):
    y = int(ym / 100)
    m = int(ym % 100)
    m = int((m - 1) / 3 + 1)
    return y * 10 + m

def get_quarter_list():
    ''' Get quarter list for searching sql'''
    start_quarter = START_QUARTER
    end_quarter = to_quarter(int(time.strftime('%y%m')))
    
    quarter_list = []
    cur_quarter = start_quarter
    while cur_quarter <= end_quarter:
        quarter_list.append(cur_quarter)
        if cur_quarter % 10 == 4:
            cur_quarter = int(cur_quarter / 10 + 1) * 10 + 1
        else:
            cur_quarter += 1
    
    return quarter_list
    

# connect db
print('Connectting server...')
server_conn = db.connect(database = 'aolang', user = 'postgres', password = 'AL', host = '52.205.81.17', port = '5432')
server_cursor = server_conn.cursor()

quarter_list = get_quarter_list()

# get records from db
records = []
for quarter in quarter_list:
    print('Searching', quarter)

    sql = conbine_gourd_sql(quarter)
    cursor = server_cursor
		
    result = execute_sql(cursor, sql)
    records.extend(result)


records = pd.DataFrame(data = records, columns = GOURD_COLUMN_LIST)
# append old records
if os.path.exists('old_gourd.csv'):
    old_records = pd.read_csv('old_gourd.csv')
    records = records.append(old_records)

records = records[ records.apply(lambda r: gap_days(r.event_day, r.expiration) > EXPIRATION_EVENT_GAP, axis = 1)]
records = records.sort_values(by = ['event_day', 'millionD'], ascending = False)

records.to_csv(GOURD_FILENAME, index = False, columns = GOURD_COLUMN_LIST)

print('Search completed!')
# close db
server_conn.close()
