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
#'52.205.81.17'
option_database = 'aolang'
option_host = '52.205.81.17'
option_port = '5432'

# constants
EXPIRATION_EVENT_GAP = 14

OPTION_START_DAY = 180718
OPTION_DAY_STR_FORMAT = '%y%m%d'
STOCK_DAY_STR_FORMAT = '%Y%m%d'
QUARTER_STR_FORMAT = '%y%m'

DAY_FILE_PATH = 'oichange_day_file'
DAY_FILE_NAME_FORMAT = os.path.join(DAY_FILE_PATH, 'al_oichange_{0}.csv')

SUMMARY_FILENAME = 'al_oichange_all.csv'

INDEX_LIST = ['SPY', 'SPX', 'SPXW', 'QQQ', 'TQQQ', 'RUT', 'RUTW', 'VIX', 'VXX', 'UVXY', 'IWM', 'EEM', 'EWZ', 'FXI', 'NDX', 'NDXP', 'XLF', 'XLU', 'XLV', 'XOP', 'XBI', 'TLT',
              'IYR', 'GDX', 'GDXJ', 'FXY', 'EWW', 'DUST', 'DXJ', 'DIA', 'KBE', 'KRE', 'LQD']

OUTPUT_COLUMNS = ['Stock', 'EventD',  'ER_Date', 'ER_A/B', 'Div_Date', 'OptionEX', 'OView', 'Buy_%', 'Sell_%', 'C/P', 'Strike', 'StockV', 'Rank','Avg_Price', 'Volume', 'OIa', 'OI_Change', 'MillionD', 'MaxD', 'Huge']
# ----------------- Begin SQL Statement ---------------

OI_CHANGE_SQL_COLUMNS = ['Stock', 'EventD', 'OptionEX', 'C/P', 'Strike', 'Avg_Price', 'Volume', 'OIa', 'OI_Change', 'MillionD', 'Buy_%', 'Sell_%']
# parameter: 0: current event_day quarter, 1: next event_day quarter, 2: event_day
OI_CHANGE_SQL = " \
 WITH oi_pre AS \
 ( \
        SELECT event_day, stock_symbol, call_put, expiration, strike, open_interest \
        FROM option_open_interest_{0} \
        WHERE event_day = {2} \
 ), oi_cur AS \
 ( \
        SELECT event_day, stock_symbol, call_put, expiration, strike, open_interest \
        FROM option_open_interest_{1} \
        WHERE event_day = ( \
                SELECT MIN(event_day) \
                FROM option_open_interest_{1} \
                WHERE event_day > {2} \
        ) \
 ), ot_cur AS \
 ( \
        SELECT event_day, stock_symbol, call_put, expiration, strike, AVG(price) as avg_price, SUM(size) as volume, \
               SUM ( CASE SUBSTRING(direction, 1, 1) WHEN 'B' THEN size ELSE 0 END)::numeric as buy, \
               SUM ( CASE SUBSTRING(direction, 1, 1) WHEN 'S' THEN size ELSE 0 END)::numeric as sell \
        FROM option_trade_{0} \
        WHERE event_day = {2} \
        GROUP BY event_day, stock_symbol, call_put, expiration, strike \
 ) \
 SELECT ot_cur.stock_symbol::text, ot_cur.event_day, ot_cur.expiration, ot_cur.call_put, ot_cur.strike::double precision, ROUND(ot_cur.avg_price, 3)::double precision, ot_cur.volume, oi_pre.open_interest, (oi_cur.open_interest - oi_pre.open_interest) as oi_change,\
        ROUND((oi_cur.open_interest - oi_pre.open_interest) * ot_cur.avg_price / 10000, 3)::double precision as millionD, \
        ROUND(ot_cur.buy / ot_cur.volume * 100, 2) as Buy, \
        ROUND(ot_cur.sell / ot_cur.volume * 100, 2) as Sell \
 FROM oi_cur LEFT JOIN oi_pre ON ( \
        oi_pre.stock_symbol = oi_cur.stock_symbol\
        AND oi_pre.strike = oi_cur.strike \
        AND oi_pre.call_put = oi_cur.call_put \
        AND oi_pre.expiration = oi_cur.expiration \
        ) LEFT JOIN ot_cur ON ( \
        ot_cur.stock_symbol = oi_cur.stock_symbol \
        AND ot_cur.strike = oi_cur.strike \
        AND ot_cur.call_put = oi_cur.call_put \
        AND ot_cur.expiration = oi_cur.expiration \
                ) \
WHERE ABS((oi_cur.open_interest - oi_pre.open_interest) * ot_cur.avg_price ) > 3000 \
ORDER BY oi_cur.event_day, oi_cur.stock_symbol, millionD \
"

# parameters: 0: event_day
STOCK_SQL = 'SELECT symbol, ROUND(close::numeric,2)::double precision \
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

# parameers: 0: event day
DIV_DATE_SQL = ' \
SELECT symbol, report_date \
FROM dividend_date ed1 \
WHERE report_date >= {0} \
    AND NOT EXISTS \
    (   SELECT * \
        FROM dividend_date ed2\
        WHERE ed2.symbol = ed1.symbol\
        AND report_date >= {0} \
        AND ed2.report_date < ed1.report_date \
    ) \
'

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

# return ( dict<symbol, er_date>, dict<symbol, time> )
def earning_date_search(cur_dt):
    cur_dt_stock = cur_dt.strftime(STOCK_DAY_STR_FORMAT)
    result = execute_sql(stock_cursor, ER_DATE_SQL.format(cur_dt_stock))
    result = np.array(result)
    return dict(result[:, [0,1]]), dict(result[:, [0, 2]])    

# return dict<symbol, div_date>
def dividend_date_search(cur_dt):
    cur_dt_stock = cur_dt.strftime(STOCK_DAY_STR_FORMAT)
    result = execute_sql(stock_cursor, DIV_DATE_SQL.format(cur_dt_stock))
    return dict(result)  

# return dict<symbol, price>
def stock_search(cur_dt):
    cur_dt_stock = cur_dt.strftime(STOCK_DAY_STR_FORMAT)
    result = execute_sql(stock_cursor, STOCK_SQL.format(cur_dt_stock))
    return dict(result)

# return search result (records in DataFrame)
def option_search(cur_dt):
    quarter = to_quarter(cur_dt.strftime(QUARTER_STR_FORMAT))
    cur_dt_option = cur_dt.strftime(OPTION_DAY_STR_FORMAT)

    sql = OI_CHANGE_SQL.format(quarter, quarter, cur_dt_option)

    result = execute_sql(option_cursor, sql)
    # next event day is next quarter
    if len(result) == 0:
        nxt_quarter = to_next_quarter(quarter)
        sql = OI_CHANGE_SQL.format(quarter, nxt_quarter, cur_dt_option)
        result = execute_sql(option_cursor, sql)

    return pd.DataFrame(data = result, columns = OI_CHANGE_SQL_COLUMNS)
    
# return DataFrame
def get_day_result(cur_dt, his_records):

    all_s = time.time()
    
    # get stock data
    stocks = stock_search(cur_dt)
    
    # get option records
    records = option_search(cur_dt)
    # filter index
    records = records[~records['Stock'].isin(INDEX_LIST)]

    # other columns process

    # Earning Date Process
    er_dates, er_times = earning_date_search(cur_dt)
    records['ER_Date'] = [ er_dates.get(records.iloc[i].Stock) for i in range(len(records))]
    records['ER_A/B'] = [ er_times.get(records.iloc[i].Stock) for i in range(len(records))]

    # Dividend Date Process
    divs = dividend_date_search(cur_dt)
    records['Div_Date'] = [ divs.get(records.iloc[i].Stock) for i in range(len(records))]

    # Stock Price process
    records['StockV'] = [ stocks.get(records.iloc[i].Stock) for i in range(len(records))]

    # Rank( stockV / strike)
    records['Rank'] =[np.round(records.iloc[i].StockV / records.iloc[i].Strike * 100) if records.iloc[i].StockV is not None else None for i in range(len(records))]

    # MaxD process
    maxD = dict(records.groupby(['Stock'])['MillionD'].max().reset_index().values)
    minD = dict(records.groupby(['Stock'])['MillionD'].min().reset_index().values)
    records['MaxD'] = [ abs(maxD[records.iloc[i].Stock]) if abs(maxD[records.iloc[i].Stock]) > abs(minD[records.iloc[i].Stock]) else abs(minD[records.iloc[i].Stock]) for i in range(len(records))]

    # Huge columns process
    tmp_records = records[['Stock', 'EventD', 'MaxD']].drop_duplicates()    
    if len(his_records) != 0:
        tmp_records = tmp_records.append(his_records[['Stock', 'EventD', 'MaxD']]).drop_duplicates()

    huges = []
    for i in range(len(records)):
        tmp_maxd_rank = tmp_records[tmp_records.Stock == records.iloc[i].Stock].MaxD.rank(method = 'min', ascending = False)
        
        cur_record_index = tmp_records[(tmp_records.Stock == records.iloc[i].Stock) & (tmp_records.EventD == records.iloc[i].EventD)].index
        huge = int(tmp_maxd_rank.loc[cur_record_index].iloc[0])
        huge = str(huge) + '---' + str(len(tmp_maxd_rank))
        
        huges.append(huge)
    records['Huge'] = huges

    # OView Process
    oviews = []
    direcs = ['Up', 'Down', 'Unknow']
    for i in range(len(records)):
        tmp = records.iloc[i]
        tmp_view = direcs[2]
        if tmp['C/P'] == 'C':
            if tmp['Buy_%'] > tmp['Sell_%']:
                tmp_view = direcs[0]
            elif tmp['Buy_%'] < tmp['Sell_%']:
                tmp_view = direcs[1]
        elif tmp['C/P'] == 'P':
            if tmp['Buy_%'] > tmp['Sell_%']:
                tmp_view = direcs[1]
            elif tmp['Buy_%'] < tmp['Sell_%']:
                tmp_view = direcs[0]
        oviews.append(tmp_view)
    records['OView'] = oviews

        
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

            day_records = get_day_result(start_dt, program_results)
            # persist to disk
            if len(day_records) > 0:
                if day_records['OI_Change'].notnull().sum() > 10:
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

# sort and reset columns position
program_results = program_results.sort_values(by = ['EventD', 'MaxD', 'Stock', 'MillionD'], ascending = False)
program_results = program_results.ix[:, OUTPUT_COLUMNS]

# save to file
program_results.to_csv(SUMMARY_FILENAME, index = False)

# close db
stock_conn.close()
option_conn.close()
