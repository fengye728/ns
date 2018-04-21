import psycopg2 as db
import numpy as np
import pandas as pd

import time
import datetime
import os

# ----------------- Begin Configurations -------------
# dabase config
stock_database = 'ps'
stock_host = 'localhost'
stock_port = '5432'
#'54.210.133.145'
option_database = 'aolang'
option_host = 'localhost'
option_port = '5432'

# constants
EXPIRATION_EVENT_GAP = 30 * 3

OPTION_START_DAY = 160811
OPTION_DAY_STR_FORMAT = '%y%m%d'
STOCK_DAY_STR_FORMAT = '%Y%m%d'
QUARTER_STR_FORMAT = '%y%m'

DAY_FILE_PATH = 'gourd_day_file'
DAY_FILE_NAME_FORMAT = os.path.join(DAY_FILE_PATH, 'al_gourd_{0}.csv')

SUMMARY_FILENAME = 'al_gourd_all.csv'


OPTION_COLUMNS_LIST = ['event_day', 'symbol', 'call_put','expiration', 'strike', 'stock_price', 'price', 'direction', 'count', 'total_zie', 'millionD', 'pattern']

# ----------------- Begin SQL Statement ---------------
STOCK_SQL = 'SELECT symbol, ROUND(close::numeric,2) \
    FROM company com LEFT JOIN stock_quote quo ON com.id = quo.company_id AND quo.quote_date = {0}'

# std union smalls
OPTION_HPLC_GOURD_WITH_STOCK = " \
SELECT event_day, stock_symbol, call_put, expiration, strike, {3} AS stock_price, ROUND(AVG(price), 2) as price, 'Sell' AS direction , COUNT(size) as count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millionD, 'std_gourd' as pattern \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Sell%' \
    AND expiration - event_day >= 300 \
    AND ( (call_put = 'P' AND strike >= {3} * 1.1) OR (call_put = 'C' AND strike <= {3} * 0.9) ) \
GROUP BY event_day, stock_symbol, strike, expiration, call_put, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
\
UNION ALL \
SELECT event_day, stock_symbol, call_put, expiration, strike, {3} AS stock_price, ROUND(AVG(price), 2) as price, 'Sell' AS direction , COUNT(size) as count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millionD, 'small_gourd' as pattern \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Sell%' \
    AND expiration - event_day >= 300 \
    AND size <= 100 \
    AND ( (call_put = 'P' AND strike >= {3} * 1.1) OR (call_put = 'C' AND strike <= {3} * 0.9) ) \
GROUP BY event_day, stock_symbol, strike, expiration, call_put \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
"

# std union small
OPTION_HPLC_GOURD = " \
SELECT event_day, stock_symbol, call_put, expiration, strike, 0 AS stock_price, ROUND(AVG(price), 2) as price, 'Sell' AS direction , COUNT(size) as count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millionD, 'std_gourd' as pattern \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Sell%' \
    AND expiration - event_day >= 300 \
GROUP BY event_day, stock_symbol, strike, expiration, call_put, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
    AND AVG(price) / strike > 0.2 \
\
UNION ALL \
SELECT event_day, stock_symbol, call_put, expiration, strike, 0 AS stock_price, ROUND(AVG(price), 2) as price, 'Sell' AS direction , COUNT(size) as count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millionD, 'small_gourd' as pattern \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Sell%' \
    AND expiration - event_day >= 300 \
GROUP BY event_day, stock_symbol, strike, expiration, call_put \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
    AND AVG(price) / strike > 0.2 \
"

# std union small
OPTION_LPHC_GOURD_WITH_STOCK = " \
SELECT event_day, stock_symbol, call_put, expiration, strike, {3} AS stock_price, ROUND(AVG(price), 2) as price, 'Buy' AS direction , COUNT(size) as count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millionD, 'std_gourd' as pattern \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Buy%' \
    AND expiration - event_day >= 300 \
    AND ( (call_put = 'P' AND strike <= {3} * 0.9) OR (call_put = 'C' AND strike >= {3} * 1.1) ) \
GROUP BY event_day, stock_symbol, strike, expiration, call_put, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
\
UNION ALL \
SELECT event_day, stock_symbol, call_put, expiration, strike, {3} AS stock_price, ROUND(AVG(price), 2) as price, 'Buy' AS direction , COUNT(size) as count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millionD, 'small_gourd' as pattern \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Buy%' \
    AND expiration - event_day >= 300 \
    AND ( (call_put = 'P' AND strike <= {3} * 0.9) OR (call_put = 'C' AND strike >= {3} * 1.1) ) \
GROUP BY event_day, stock_symbol, strike, expiration, call_put \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
"

OPEN_INTEREST_SEARCH = " \
SELECT open_interest \
FROM option_open_interest_{0} \
WHERE event_day = {1} \
    AND stock_symbol = '{2}' \
    AND strike = {3} \
    AND call_put = '{4}' \
    AND expiration = {5} \
LIMIT 1 \
"

PRE_OPEN_INTEREST_SEARCH = " \
SELECT open_interest \
FROM option_open_interest_{0} \
WHERE event_day < {1} \
    AND stock_symbol = '{2}' \
    AND strike = {3} \
    AND call_put = '{4}' \
    AND expiration = {5} \
 \
ORDER BY event_day DESC \
LIMIT 1 \
"

# ----------------- Begin Connecting db ---------------
print('Connecting stock database', stock_host, stock_database, '...', end = '')
stock_conn = db.connect(user = 'postgres', password = 'AL', database = stock_database, host = stock_host, port = stock_port)
stock_cursor = stock_conn.cursor()
print('Done!')

print('Connecting option database', option_host, option_database, '...', end = '')
option_conn = db.connect(user = 'postgres', password = 'AL', database = option_database, host = option_host, port = option_port)
option_cursor = option_conn.cursor()
print('Done!')

# ----------------- Begin Functions ------------------
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

def event_expiration_process(records):
    if len(records) == 0:
        return records
    else:
        return records[ records.apply(lambda r: gap_days(r.event_day, r.expiration) > EXPIRATION_EVENT_GAP, axis = 1)]

def union_sql(*sql):
    UNION = " UNION ALL "
    target_sql = None
    for i in range(len(sql)):
        if sql[i] is not None and sql[i] != '':
            if target_sql is None:
                target_sql = sql[i]
            else:
                target_sql += (UNION + sql[i])

    return target_sql

def execute_sql(cursor, sql):
    try:
        cursor.execute(sql)
        return list(cursor.fetchall())
    except Exception as e:
        print(e)
        return []

def to_quarter(ym):
    ym = int(ym)
    y = int(ym / 100)
    m = int(ym % 100)
    m = int((m - 1) / 3 + 1)
    return y * 10 + m

def combine_day_filename(cur_dt):
    return DAY_FILE_NAME_FORMAT.format(cur_dt.strftime(STOCK_DAY_STR_FORMAT))

def read_day_file(cur_dt):
    try:
        filename = combine_day_filename(cur_dt)
        if os.path.exists(filename):
            return pd.read_csv(filename)
        else:
            return None
    except e:
        print(e.message)
        return None

def stock_search(cur_dt):
    cur_dt_stock = cur_dt.strftime(STOCK_DAY_STR_FORMAT)
    return execute_sql(stock_cursor, STOCK_SQL.format(cur_dt_stock))

# return search sql
def option_search(stock, cur_dt):
    quarter = to_quarter(cur_dt.strftime(QUARTER_STR_FORMAT))
    cur_dt_option = cur_dt.strftime(OPTION_DAY_STR_FORMAT)

    if stock[1] is None:
        sql = OPTION_HPLC_GOURD.format(quarter, stock[0], cur_dt_option)
    else:
        sql = union_sql(OPTION_HPLC_GOURD_WITH_STOCK.format(quarter, stock[0], cur_dt_option, stock[1]), OPTION_LPHC_GOURD_WITH_STOCK.format(quarter, stock[0], cur_dt_option, stock[1]))

    return sql

# return oi(a int in alist)
def get_oi(record):
    ''' record of option_search result,'''
    quarter = to_quarter(record.event_day.strptime(OPTION_DAY_STR_FORMAT).strftime(QUARTER_STR_FORMAT))
    sql = OPEN_INTEREST_SEARCH.format(quarter, record.event_day, record.stock_symbol, record.strike, record.call_put, record.expiration)

    # TODO
    print(sql)
    return execute_sql(option_cursor, sql)

# return oi(a int in alist)
def get_pre_oi(record):
    quarter = to_quarter(record.event_day.strptime(OPTION_DAY_STR_FORMAT).strftime(QUARTER_STR_FORMAT))
    sql = OPEN_INTEREST_SEARCH.format(quarter, record.event_day, record.stock_symbol, record.strike, record.call_put, record.expiration)

    oi = execute_sql(option_cursor, sql)
    # the previous oi maybe in the previous quarter table
    if len(oi) == 0:
        pre_quarter = int((quarter - 2) / 10) * 10 + min(3, int((quarter - 2) % 10)) + 1
        sql = OPEN_INTEREST_SEARCH.format(pre_quarter, record.event_day, record.stock_symbol, record.strike, record.call_put, record.expiration)

        oi = execute_sql(option_cursor, sql)

    return oi

# return DataFrame
def get_day_result(cur_dt):
    
    all_s = time.time()
    # get stock data
    stocks = stock_search(cur_dt)
    records = []

    # get option records
    i = 1
    sql = ''
    for stock in stocks:
        sql = union_sql(sql, option_search(stock, cur_dt))
        if i % 1000 == 0:

            # execute sql
            records.extend(execute_sql(option_cursor, sql))
            sql = ''
        
        i += 1

    records = pd.DataFrame(data = records, columns = OPTION_COLUMNS_LIST)
    # process gap between event_day and expiration
    records = event_expiration_process(records)

    # other columns process
    oi = []
    pre_oi = []
    
    for i in records.index:
        # get oi
        oi.extend(get_oi(records.iloc[i]))
        pre_oi.extend(get_pre_oi(records.iloc[i]))
    

    records['oi_a'] = oi
    records['oi_change'] = np.subtract(oi, pre_oi)
    records['option_cap_millD'] = np.multiply(records['oi_a'],records['price']) / 100
    records['sp_rank'] = np.divide(records['strike'], records['stock_price']) * 100
    
    print('Cost time:', int(time.time() - all_s) / 60, 'm')
    return records
    
# ----------------- Begin processing ----------------

# check day file folder exists
if not os.path.exists(DAY_FILE_PATH):
    os.makedirs(DAY_FILE_PATH)

start_dt = datetime.datetime.strptime(str(OPTION_START_DAY), OPTION_DAY_STR_FORMAT)
end_dt = datetime.datetime.today()

program_results = pd.DataFrame(columns = OPTION_COLUMNS_LIST)

program_s = time.time()
while start_dt <= end_dt:
    # check if the day is weekday for filtering weekend
    if start_dt.weekday() >= 0 and start_dt.weekday() <= 4:
        
        print(start_dt.strftime(STOCK_DAY_STR_FORMAT), end = ' ')
        # read the records of the day from file
        day_records = read_day_file(start_dt)
        if day_records is None:
            print('read from database.')
            
            day_records = get_day_result(start_dt)
            # persist to disk
            if len(day_records) > 0:
                day_records.to_csv(combine_day_filename(start_dt), index = False, columns = OPTION_COLUMNS_LIST)
            print('Count:', len(day_records))
        else:
            print('read from file. Count:', len(day_records))
          
        program_results = program_results.append(day_records)

    # loop
    start_dt += datetime.timedelta(days = 1)

print('All program cost time:', int(time.time() - program_s) / 60, 'm')

# sort
program_results = program_results.sort_values(by = ['event_day', 'symbol', 'millionD'], ascending = False)
# save to file
program_results.to_csv(SUMMARY_FILENAME, index = False)

# close db
stock_conn.close()
option_conn.close()

