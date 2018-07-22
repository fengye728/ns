import psycopg2 as db
import numpy as np
import pandas as pd

import traceback
import time
import datetime
import os

# ----------------- Begin Configurations -------------
# dabase config
stock_database = 'ps'
stock_host = '52.205.81.17'
stock_port = '5432'
# 52.205.81.17
option_database = 'aolang'
option_host = '52.205.81.17'
option_port = '5432'

# constants
EXPIRATION_EVENT_GAP = 14

OPTION_START_DAY = 180101
OPTION_DAY_STR_FORMAT = '%y%m%d'
STOCK_DAY_STR_FORMAT = '%Y%m%d'
QUARTER_STR_FORMAT = '%y%m'

DAY_FILE_PATH = 'gourd2_day_file'
DAY_FILE_NAME_FORMAT = os.path.join(DAY_FILE_PATH, 'al_gourd2_{0}.csv')

SUMMARY_FILENAME = 'al_gourd2_all.csv'


OPTION_COLUMNS_LIST = ['event_day', 'stock_symbol', 'call_put','expiration', 'strike', 'stock_price', 'price', 'direction', 'count', 'thlVolume', 'millionD', 'pattern', 'market_capB']

# ----------------- Begin SQL Statement ---------------
STOCK_SQL = 'SELECT symbol, ROUND(close::numeric,2)::double precision, ROUND(((sta.shs_outstand * close) / 1000000000)::numeric, 4)::double precision market_capB \
    FROM company com LEFT JOIN company_statistics sta ON com.id = sta.company_id LEFT JOIN stock_quote quo ON com.id = quo.company_id AND quo.quote_date = {0}'

# std
OPTION_HPLC_GOURD_WITH_STOCK = " \
SELECT event_day, stock_symbol, call_put, expiration, strike, {3}::double precision AS stock_price, ROUND(AVG(price), 2)::double precision as price, 'Sell' AS direction , COUNT(size) as count, SUM(size) as volume, ROUND(SUM(price * size / 10000), 3)::double precision as millionD, 'std_gourd' as pattern, {4}::double precision AS market_capB \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Sell%' \
    AND ( (call_put = 'P' AND strike >= {3} * 1.1) OR (call_put = 'C' AND strike <= {3} * 0.9) ) \
GROUP BY event_day, stock_symbol, strike, expiration, call_put, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
"

# std
OPTION_HPLC_GOURD = " \
SELECT event_day, stock_symbol, call_put, expiration, strike, NULL AS stock_price, ROUND(AVG(price), 2)::double precision as price, 'Sell' AS direction , COUNT(size) as count, SUM(size) as volume, ROUND(SUM(price * size / 10000), 3)::double precision as millionD, 'std_gourd' as pattern, NULL AS market_capB \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Sell%' \
GROUP BY event_day, stock_symbol, strike, expiration, call_put, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
    AND AVG(price) / strike > 0.2 \
"

# std
OPTION_LPHC_GOURD_WITH_STOCK = " \
SELECT event_day, stock_symbol, call_put, expiration, strike, {3}::double precision AS stock_price, ROUND(AVG(price), 2)::double precision as price, 'Buy' AS direction , COUNT(size) as count, SUM(size) as volume, ROUND(SUM(price * size / 10000), 3) as millionD, 'std_gourd' as pattern, {4}::double precision AS market_capB \
FROM option_trade_{0} \
WHERE \
    stock_symbol = '{1}' \
    AND event_day = {2} \
    AND direction LIKE 'Buy%' \
    AND ( (call_put = 'P' AND strike <= {3} * 0.9) OR (call_put = 'C' AND strike >= {3} * 1.1) ) \
GROUP BY event_day, stock_symbol, strike, expiration, call_put, size \
HAVING sum(size) >= 100 AND COUNT(size) >= 20 \
"

PRE_OPEN_INTEREST_SEARCH = " \
SELECT open_interest \
FROM option_open_interest_{0} \
WHERE event_day = {1} \
    AND stock_symbol = '{2}' \
    AND strike = {3} \
    AND call_put = '{4}' \
    AND expiration = {5} \
LIMIT 1 \
"

OPEN_INTEREST_SEARCH = " \
SELECT open_interest \
FROM option_open_interest_{0} \
WHERE event_day > {1} \
    AND stock_symbol = '{2}' \
    AND strike = {3} \
    AND call_put = '{4}' \
    AND expiration = {5} \
ORDER BY event_day ASC \
LIMIT 1 \
"

VOLUME_SEARCH = " \
SELECT SUM(size)\
FROM option_trade_{0} \
WHERE event_day = {1} \
    AND stock_symbol = '{2}' \
    AND strike = {3} \
    AND call_put = '{4}' \
    AND expiration = {5} \
"

ALL_OPTION_CAP_SEARCH = " \
WITH option_price AS ( \
	SELECT event_day, stock_symbol, strike, call_put, expiration, AVG(price) m_price \
	FROM option_trade_{0} \
	WHERE event_day = {1} \
		AND stock_symbol = '{2}' \
	GROUP BY event_day, stock_symbol, strike, call_put, expiration \
) \
SELECT ROUND(SUM(oi.open_interest * m_price) / 10000, 2)::double precision as all_option_capM \
FROM option_open_interest_{0} oi JOIN option_price op ON \
	oi.event_day = op.event_day AND oi.stock_symbol = op.stock_symbol AND oi.strike = op.strike AND oi.call_put = op.call_put AND oi.expiration = op.expiration \
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
    except:
        option_conn.rollback()
        traceback.print_exc()
        return []

def to_quarter(ym):
    ym = int(ym)
    y = int(ym / 100)
    m = int(ym % 100)
    m = int((m - 1) / 3 + 1)
    return y * 10 + m

def to_next_quarter(cur_quarter):
    cur_quarter += 1
    yy = int(cur_quarter / 10)
    q = int(cur_quarter % 10)
    if q > 4:
        yy += 1
        q = 1
        
    return int(yy * 10 + q)

def combine_day_filename(cur_dt):
    return DAY_FILE_NAME_FORMAT.format(cur_dt.strftime(STOCK_DAY_STR_FORMAT))

def read_day_file(cur_dt):
    try:
        filename = combine_day_filename(cur_dt)
        if os.path.exists(filename):
            return pd.read_csv(filename)
        else:
            return None
    except:
        traceback.print_exc()
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
        if stock[2] is None:
            mc = 0
        else:
            mc = stock[2]
        sql = union_sql(OPTION_HPLC_GOURD_WITH_STOCK.format(quarter, stock[0], cur_dt_option, stock[1], mc), OPTION_LPHC_GOURD_WITH_STOCK.format(quarter, stock[0], cur_dt_option, stock[1], mc))

    return sql

# return oi(a int in alist)
def get_pre_oi(record):
    ''' record of option_search result,'''

    quarter = to_quarter( int(record['event_day'] / 100) )
    sql = PRE_OPEN_INTEREST_SEARCH.format(quarter, record['event_day'], record['stock_symbol'], record['strike'], record['call_put'], record['expiration'])

    return execute_sql(option_cursor, sql)

# return oi(a int in alist)
def get_oi(record):
    tmp = time.strptime(str(record['event_day']), OPTION_DAY_STR_FORMAT)
    tmp = time.strftime(QUARTER_STR_FORMAT,tmp)
    
    quarter = to_quarter(tmp)
    
    sql = OPEN_INTEREST_SEARCH.format(quarter, record['event_day'], record['stock_symbol'], record['strike'], record['call_put'], record['expiration'])
    oi = execute_sql(option_cursor, sql)
    
    # the currecnt oi maybe in the next quarter table
    if len(oi) == 0:
        today_dt = datetime.datetime.today()
        today_quarter = to_quarter(today_dt.strftime(QUARTER_STR_FORMAT))
        nxt_quarter = to_next_quarter(quarter)
        
        if today_quarter >= nxt_quarter:
            sql = OPEN_INTEREST_SEARCH.format(nxt_quarter, record['event_day'], record['stock_symbol'], record['strike'], record['call_put'], record['expiration'])
            oi = execute_sql(option_cursor, sql)
    return oi

# return volume of a record
def get_volume(record):
    quarter = to_quarter( int(record.event_day / 100) )
    sql = VOLUME_SEARCH.format(quarter, record.event_day, record.stock_symbol, record.strike, record.call_put, record.expiration)

    try:
        return execute_sql(option_cursor, sql)[0][0]
    except:
        traceback.print_exc()
        return None

def get_all_option_capM(record):
    quarter = to_quarter( int(record.event_day / 100) )
    sql = ALL_OPTION_CAP_SEARCH.format(quarter, record.event_day, record.stock_symbol)

    try:
        return execute_sql(option_cursor, sql)[0][0]
    except:
        return None

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
        if i % 500 == 0:

            # execute sql
            records.extend(execute_sql(option_cursor, sql))
            sql = ''

        i += 1
    else:
        if sql != '':
            records.extend(execute_sql(option_cursor, sql))

    records = pd.DataFrame(data = records, columns = OPTION_COLUMNS_LIST)
    # process gap between event_day and expiration
    records = event_expiration_process(records)

    # other columns process
    ois = []
    oi_changes = []
    option_caps = []
    volumes = []
    all_option_caps = []
    
    for i in range(len(records)):
        # get oi
        tmp_oi = get_oi(records.iloc[i])
        tmp_pre_oi = get_pre_oi(records.iloc[i])

        # get volume
        volumes.append(get_volume(records.iloc[i]))

        # get all option cap
        all_option_caps.append(get_all_option_capM(records.iloc[i]))

        if(len(tmp_oi) == 0):
            ois.append(None)
        else:
            ois.append(tmp_oi[0][0])

        if(len(tmp_oi) == 0 or len(tmp_pre_oi) == 0):
            oi_changes.append(None)
        else:
            oi_changes.append(tmp_oi[0][0] - tmp_pre_oi[0][0])

    records['oi_a'] = ois
    records['oi_change'] = oi_changes
    records['volume'] = volumes
    records['maxD'] = [ round(records['oi_change'].iloc[i] * records['price'].iloc[i] / 10000, 3) if(records['oi_change'].iloc[i] is not None) else None for i in range(len(records))]
    records['option_capM'] = [ round(records['price'].iloc[i] * records['oi_a'].iloc[i] / 10000, 3) if(records['oi_a'].iloc[i] is not None) else None for i in range(len(records))]
    records['all_option_capM'] = all_option_caps
    records['rank'] = [ round(float(records['strike'].iloc[i]) * 100 / records['stock_price'].iloc[i], 2) if(records['stock_price'].iloc[i] is not None) else None for i in range(len(records)) ]
    records['omc_rank'] = [ round(records['option_capM'].iloc[i] * 10000 / records['market_capB'].iloc[i], 3)
                               if(records['market_capB'].iloc[i] is not None and records['option_capM'].iloc[i] is not None) else None for i in range(len(records))]
    records['all_om_cap_rank'] = [ round(records['all_option_capM'].iloc[i] * 100 / records['market_capB'].iloc[i], 3)
                               if(records['market_capB'].iloc[i] is not None and records['all_option_capM'].iloc[i] is not None and records['market_capB'].iloc[i] > 0)
                                   else None for i in range(len(records))]

    records['o_huge'] = [ round(records['maxD'].iloc[i] * 100 / records['all_option_capM'].iloc[i], 3)
                               if(records['all_option_capM'].iloc[i] is not None and records['maxD'].iloc[i] is not None) else None for i in range(len(records))]
    records['omc_huge'] = [ round(records['maxD'].iloc[i] * 100 / records['market_capB'].iloc[i], 3)
                               if(records['market_capB'].iloc[i] is not None and records['maxD'].iloc[i] is not None) else None for i in range(len(records))]
    
    
    print('Cost time:', int(time.time() - all_s) / 60, 'm')
    return records

# ----------------- Begin processing ----------------

# check day file folder exists
if not os.path.exists(DAY_FILE_PATH):
    os.makedirs(DAY_FILE_PATH)

start_dt = datetime.datetime.strptime(str(OPTION_START_DAY), OPTION_DAY_STR_FORMAT)
end_dt = datetime.datetime.today()

program_results = []

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
                if (end_dt - start_dt).days > 3 or day_records['oi_change'].notnull().sum() > 10:
                    day_records.to_csv(combine_day_filename(start_dt), index = False)
            print('Count:', len(day_records))
        else:
            print('read from file. Count:', len(day_records))

        if len(program_results) == 0:
            program_results = day_records
        else:
            program_results = program_results.append(day_records)

    # loop
    start_dt += datetime.timedelta(days = 1)

print('All program cost time:', int(time.time() - program_s) / 60, 'm')

# sort
program_results = program_results.sort_values(by = ['event_day', 'stock_symbol', 'millionD'], ascending = [False, True, False])
# save to file
program_results.to_csv(SUMMARY_FILENAME, index = False)

# close db
stock_conn.close()
option_conn.close()

exit(0)
