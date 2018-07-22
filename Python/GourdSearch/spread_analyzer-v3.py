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
#'54.210.133.145'
option_database = 'aolang'
option_host = '52.205.81.17'
option_port = '5432'

# constants

OPTION_START_DAY = 180718#160812
OPTION_DAY_STR_FORMAT = '%y%m%d'
STOCK_DAY_STR_FORMAT = '%Y%m%d'
QUARTER_STR_FORMAT = '%y%m'

DAY_FILE_PATH = 'spread_day_file'
DAY_FILE_NAME_FORMAT = os.path.join(DAY_FILE_PATH, 'al_spread_{0}.csv')

SUMMARY_FILENAME = 'al_spread_all.csv'

INDEX_LIST = ['SPY', 'SPX', 'SPXW', 'QQQ', 'TQQQ', 'RUT', 'RUTW', 'VIX', 'VXX', 'UVXY', 'IWM', 'EEM', 'EWZ', 'FXI', 'NDX', 'NDXP', 'XLF', 'XLU', 'XLV', 'XOP', 'XBI', 'TLT',
              'IYR', 'GDX', 'GDXJ', 'FXY', 'EWW', 'DUST', 'DXJ', 'DIA', 'KBE', 'KRE', 'LQD']
# ----------------- Begin SQL Statement ---------------
OUT_COLUMNS = ['Stock', 'EventD', 'EventT', 'ER_Date', 'ER_A/B','OptionEX', 'C/P', 'Strike', 'StockV', 'Height','Size', 'OPrice', 'Direction', 'Condition', 'MillionD', 'OIa', 'OIChange', 'MaxMillionD', 'MaxStrike', 'MinHeight', 'MaxHeight', 'MarketCapB', 'Index']

SPREAD_SQL_COLUMNS = ['Stock', 'EventD', 'EventT','OptionEX', 'C/P', 'Strike', 'Size', 'OPrice', 'Direction', 'Condition', 'Seq', 'LegSeq', 'MillionD']

# parameter: 0: current event_day quarter, 1: event_day
SPREAD_SQL = " \
WITH op_code AS \
( \
    SELECT code_id, name \
    FROM option_code \
    WHERE category = '2' \
) \
SELECT stock_symbol::text, event_day, event_time / 1000 as event_time, expiration, call_put, strike::double precision, size, price, direction, op_code.name, sequence_id, leg_sequence_id, ROUND(price * size / 10000, 2) \
FROM option_trade_{0} o1 JOIN op_code ON op_code.code_id = o1.condition \
WHERE condition IN (35, 36, 37) \
    AND event_day = {1} \
    AND size >= 100 \
    AND exists( \
		SELECT sequence_id \
		FROM option_trade_{0} o2 \
		WHERE o2.stock_symbol = o1.stock_symbol \
			AND o2.event_day = o1.event_day\
			AND o2.call_put = o1.call_put \
			AND o2.report_exg = o1.report_exg \
			AND o2.sequence_id = o1.leg_sequence_id	 \
	) \
ORDER BY stock_symbol, event_day, event_time \
"

# paramters: 0: event_day
STOCK_PRICE_SQL = 'SELECT symbol, ROUND(close::numeric,2)::double precision \
    FROM company com LEFT JOIN company_statistics sta ON com.id = sta.company_id LEFT JOIN stock_quote quo ON com.id = quo.company_id AND quo.quote_date = {0}'

MARKET_CAP_SQL = 'SELECT symbol, ROUND(((sta.shs_outstand * close) / 1000000000)::numeric, 1)::double precision market_capB \
    FROM company com LEFT JOIN company_statistics sta ON com.id = sta.company_id LEFT JOIN stock_quote quo ON com.id = quo.company_id AND quo.quote_date = {0}'

# parameers: 0: event day
ER_DATE_SQL = ' \
SELECT symbol, report_date, time \
FROM earning_date ed1 \
WHERE report_date >= {0} \
    AND NOT EXISTS \
    (   SELECT * \
        FROM earning_date ed2\
        WHERE ed2.symbol = ed1.symbol\
        AND report_date >= {0} \
        AND ed2.report_date < ed1.report_date \
    ) \
'

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

# return oi(a int in alist)
def get_pre_oi(record):
    ''' record of option_search result,'''

    quarter = to_quarter( int(record['EventD'] / 100) )
    sql = PRE_OPEN_INTEREST_SEARCH.format(quarter, record.EventD, record.Stock, record.Strike, record['C/P'], record['OptionEX'])

    return execute_sql(option_cursor, sql)

# return oi(a int in alist)
def get_oi(record):
    tmp = time.strptime(str(record['EventD']), OPTION_DAY_STR_FORMAT)
    tmp = time.strftime(QUARTER_STR_FORMAT,tmp)
    
    quarter = to_quarter(tmp)
    
    sql = OPEN_INTEREST_SEARCH.format(quarter, record.EventD, record.Stock, record.Strike, record['C/P'], record['OptionEX'])
    oi = execute_sql(option_cursor, sql)
    
    # the currecnt oi maybe in the next quarter table
    if len(oi) == 0:
        today_dt = datetime.datetime.today()
        today_quarter = to_quarter(today_dt.strftime(QUARTER_STR_FORMAT))
        nxt_quarter = to_next_quarter(quarter)
        
        if today_quarter >= nxt_quarter:
            sql = OPEN_INTEREST_SEARCH.format(nxt_quarter, record.EventD, record.Stock, record.Strike, record['C/P'], record['OptionEX'])
            oi = execute_sql(option_cursor, sql)
    return oi


# return ( dict<symbol, er_date>, dict<symbol, time> )
def earning_date_search(cur_dt):
    cur_dt_stock = cur_dt.strftime(STOCK_DAY_STR_FORMAT)
    result = execute_sql(stock_cursor, ER_DATE_SQL.format(cur_dt_stock))
    result = np.array(result)
    if len(result) == 0:
        return dict([]), dict({})
    else:
        return dict(result[:, [0,1]]), dict(result[:, [0, 2]]) 


# return ( dict<symbol, price>, dict<symbol, markect_cap> )
def stock_search(cur_dt):
    cur_dt_stock = cur_dt.strftime(STOCK_DAY_STR_FORMAT)
    price_res = execute_sql(stock_cursor, STOCK_PRICE_SQL.format(cur_dt_stock))
    mc_res = execute_sql(stock_cursor, MARKET_CAP_SQL.format(cur_dt_stock))
    return dict(price_res), dict(mc_res)

# return search result (records in DataFrame)
def option_search(cur_dt):
    quarter = to_quarter(cur_dt.strftime(QUARTER_STR_FORMAT))
    cur_dt_option = cur_dt.strftime(OPTION_DAY_STR_FORMAT)

    sql = SPREAD_SQL.format(quarter, cur_dt_option)

    result = execute_sql(option_cursor, sql)

    result = pd.DataFrame(data = result, columns = SPREAD_SQL_COLUMNS)
    
    return result



# process all legs wiht index of records
def legs_process(records, index):
    seqs = set()
    seqs.add(records['Seq'].iloc[index])
    legs = records.iloc[index]
    count = 0
    while count != len(legs):
        count = len(legs)
        legs = records[records['Seq'].isin(seqs) | records['LegSeq'].isin(seqs)]
        seqs = seqs.union(legs['Seq'])
        

    # set MaxMillionD and MaxStrike
    max_strike = legs['Strike'].max()
    max_mill_d = legs.MillionD.max()
    max_height = legs.Height.max()
    min_height = legs.Height.min()
    max_size = legs.Size.max()

    tag = ord('A')
    legs = legs.sort_values(by = ['Strike'], ascending = False)
    for i in legs.index:
        records.at[i, 'MaxStrike'] = max_strike
        records.at[i, 'MaxMillionD'] = max_mill_d
        records.at[i, 'MaxHeight'] = max_height
        records.at[i, 'MinHeight'] = min_height
        records.at[i, 'MaxSize'] = max_size
        records.at[i, 'Index'] = records.at[i, 'Stock'] + '_' + str(int(records.at[i, 'EventD'])) + '_' + str(int(records.at[i, 'EventT'])) + '_' + chr(tag)
        tag += 1
    
    
    
# return DataFrame
def get_day_result(cur_dt):

    all_s = time.time()
    
    # get stock data
    stockVs, marketCaps = stock_search(cur_dt)
    
    # get option records
    records = option_search(cur_dt)
    
    # Filter index
    records = records[~records['Stock'].isin(INDEX_LIST)]

    # other columns process
    
    # Stock Process
    records['StockV'] = [ stockVs.get(records.iloc[i].Stock) for i in range(len(records))]
    records['MarketCapB'] = [ marketCaps.get(records.iloc[i].Stock) for i in range(len(records))]
    
    # Earning Date Process
    er_dates, er_times = earning_date_search(cur_dt)
    records['ER_Date'] = [ er_dates.get(records.iloc[i].Stock) for i in range(len(records))]
    records['ER_A/B'] = [ er_times.get(records.iloc[i].Stock) for i in range(len(records))]


    # Height
    records['Height'] = [np.round(records.iloc[i].Strike * 100 / records.iloc[i].StockV) if records.iloc[i].StockV is not None else None for i in range(len(records))]    

    # Legs process
    records['MaxMillionD'] = [0.0] * len(records)
    records['MaxStrike'] = [0.0] * len(records)
    records['MinHeight'] = [None] * len(records)
    records['MaxHeight'] = [None] * len(records)
    records['MaxSize'] = [0] * len(records)
    records['Index'] = [''] * len(records)
    
    for i in range(len(records)):
        if records.iloc[i].MaxStrike == 0:
            legs_process(records, i)
    
    # Filter MaxMillionD or MaxSize
    records = records[(records['MaxMillionD'] >= 0.2) | (records['MaxSize'] >= 500)]
    # Filter height
    records = records[(records['MinHeight'] <= 90) | (records['MaxHeight'] >= 110)]

    # OI Process
    ois = []
    oi_changes = []
    for i in range(len(records)):
        # get oi
        tmp_oi = get_oi(records.iloc[i])
        tmp_pre_oi = get_pre_oi(records.iloc[i])

        if(len(tmp_oi) == 0):
            ois.append(None)
        else:
            ois.append(tmp_oi[0][0])

        if(len(tmp_oi) == 0 or len(tmp_pre_oi) == 0):
            oi_changes.append(None)
        else:
            oi_changes.append(tmp_oi[0][0] - tmp_pre_oi[0][0])

    records['OIa'] = ois
    records['OIChange'] = oi_changes
    
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

        # read the records of the day from file
        day_records = read_day_file(start_dt)
        if day_records is None:
            print(start_dt.strftime(STOCK_DAY_STR_FORMAT), 'read from database.')

            day_records = get_day_result(start_dt)
            # persist to disk
            if len(day_records) > 0:
                if day_records['OIChange'].notnull().sum() > 10:
                    day_records.to_csv(combine_day_filename(start_dt), index = False)
            print('Count:', len(day_records))
        else:
            print(start_dt.strftime(STOCK_DAY_STR_FORMAT), 'read from file. Count:', len(day_records))

        if len(program_results) == 0:
            program_results = day_records
        else:
            program_results = program_results.append(day_records)

    # loop
    start_dt += datetime.timedelta(days = 1)

print('All program cost time:', int(time.time() - program_s) / 60, 'm')

# sort
program_results = program_results.sort_values(by = ['EventD', 'MaxHeight', 'Index', 'MaxMillionD'], ascending = False)
# save to file
program_results.to_csv(SUMMARY_FILENAME, index = False, columns = OUT_COLUMNS)

# close db
stock_conn.close()
option_conn.close()
