import psycopg2
import numpy as np
import pandas as pd
import os

# ---------- Get connection ----------------
print('Connecting server...')
conn = psycopg2.connect(database = 'aolang', user = 'postgres', password = 'AL', host = '52.205.81.17', port = '5432')
cur = conn.cursor()
print('Connecting server success!')

# --------------- SQL Statement --------------
OPTION_NORMAL_COLUMNS = ['stock_symbol', 'event_day', 'event_time', 'call_put', 'expiration', 'strike', 'size', 'price', 'direction', 'condition', 'sequence_id', 'leg_sequence_id']
OPTION_NORMAL_SQL = " \
SELECT stock_symbol, event_day, event_time / 1000 as event_time, call_put, expiration, strike, size, price, direction, condition, sequence_id, leg_sequence_id \
FROM option_trade_{0} \
WHERE stock_symbol = '{1}' \
	AND event_day = {2} \
    AND expiration = {3} \
    AND strike = {4} \
    AND call_put = '{5}' \
ORDER BY stock_symbol, event_day, event_time \
"

OI_QUARTER_SEARCH_SQL_COLUMNS = ['event_day', 'open_interest']
OI_QUARTER_SEARCH_SQL = "\
SELECT event_day, open_interest \
FROM option_open_interest_{0} \
WHERE stock_symbol = '{1}' \
    AND expiration = {2} \
    AND strike = {3} \
    AND call_put = '{4}' \
ORDER BY event_day DESC \
"

OPTION_MILLD_SQL_COLUMNS = ['stock_symbol', 'strike', 'expiration', 'direc', 'group_count', 'volume', 'millD']
OPTION_MILLD_SQL = " \
SELECT stock_symbol, strike, expiration, SUBSTRING(direction, 1, 1) as direc, COUNT(*) AS group_count,SUM(size) as volume, SUM(size * price) / 10000 as millD \
FROM option_trade_{0} \
WHERE stock_symbol = '{1}' \
    AND event_day = {2} \
    AND expiration = {3} \
    AND strike = {4} \
    AND call_put = '{5}' \
GROUP BY stock_symbol, call_put, strike, expiration, direc \
ORDER BY millD DESC \
"

SEQUENCE_SEARCH_SQL = " \
SELECT stock_symbol, event_day, event_time / 1000 as event_time, call_put, expiration, strike, size, price, direction, condition, sequence_id, leg_sequence_id \
FROM option_trade_{0} \
WHERE stock_symbol = '{1}' \
    AND event_day = {2} \
    AND sequence_id = {3} \
"

# ------------------- Search Functions --------------------
def to_quarter(yyMMdd):
    yyMMdd = int(yyMMdd)
    yyMM = int(yyMMdd / 100)
    yy = int(yyMM / 100)
    m = int(yyMM % 100)
    q = int((m - 1) / 3 + 1)
    return yy * 10 + q

# i.e: option_normal('BIIB', 180423, 190118, 370, 'P')
def option_normal(symbol, event_day, expiration, strike, call_put, path = '.'):
    FILENAME_PATTERN = 'normal_{0}-{1}-{2}-{3}{4}.csv'

    quarter = to_quarter(event_day)
    filename = os.path.join(path, FILENAME_PATTERN.format(symbol, event_day, expiration, strike, call_put))
    
    sql = OPTION_NORMAL_SQL.format(quarter, symbol, event_day, expiration, strike, call_put)
    cur.execute(sql)
    result = pd.DataFrame(data = cur.fetchall(), columns = OPTION_NORMAL_COLUMNS)
    result.to_csv(filename)
    
    #print(symbol, 'option_normal search success')
    

def quarter_oi(symbol, event_day, expiration, strike, call_put, path = '.'):
    FILENAME_PATTERN = 'oi_{0}-{1}-{2}-{3}{4}.csv'

    quarter = to_quarter(event_day)
    filename = os.path.join(path, FILENAME_PATTERN.format(symbol, quarter, expiration, strike, call_put))
    
    sql = OI_QUARTER_SEARCH_SQL.format(quarter, symbol, expiration, strike, call_put)
    cur.execute(sql)
    result = pd.DataFrame(data = cur.fetchall(), columns = OI_QUARTER_SEARCH_SQL_COLUMNS)
    
    result.to_csv(filename)
    
    #print(symbol, 'quarter_oi search success')

def option_milld(symbol, event_day, expiration, strike, call_put, path = '.'):
    FILENAME_PATTERN = 'millD_{0}-{1}-{2}-{3}{4}.csv'
    
    quarter = to_quarter(event_day)
    filename = os.path.join(path, FILENAME_PATTERN.format(symbol, event_day, expiration, strike, call_put))
     
    
    sql = OPTION_MILLD_SQL.format(quarter, symbol, event_day, expiration, strike, call_put)
    cur.execute(sql)
    result = pd.DataFrame(data = cur.fetchall(), columns = OPTION_MILLD_SQL_COLUMNS)
    
    result.to_csv(filename)
    
    #print(symbol, 'option_milld search success')

def _sequence_search(symbol, event_day, sequence_id):

    quarter = to_quarter(event_day)
    
    sql = SEQUENCE_SEARCH_SQL.format(quarter, symbol, event_day, sequence_id)
    cur.execute(sql)
    return pd.DataFrame(data = cur.fetchall(), columns = OPTION_NORMAL_COLUMNS)

def spread_search(symbol, event_day, sequence_id, path = "."):
    FILENAME_PATTERN = 'sequence_{0}_{1}_{2}.csv'
    filename = os.path.join(path, FILENAME_PATTERN.format(symbol, event_day, sequence_id))

    result = None
    sequence_set = set()
    sequence_set.add(sequence_id)
    
    leg_sequence_ids = set()
    leg_sequence_ids.add(sequence_id)
    while True:
        new_leg_ids = []

        for leg_id in leg_sequence_ids:
            tmp = _sequence_search(symbol, event_day, leg_id)
            if result is None:
                result = tmp
            else:
                result = result.append(tmp)
            new_leg_ids.extend( list(tmp.leg_sequence_id) )

        new_leg_ids = set(new_leg_ids).difference(sequence_set)
        if len(new_leg_ids) == 0:
            break
        else:
            sequence_set = sequence_set.union(new_leg_ids)
            leg_sequence_ids = new_leg_ids

    result = result[result.sequence_id.isin(sequence_set)].drop_duplicates()
 
    result.to_csv(filename)
    print(symbol, 'spread search success')
    
    
def batch_search(symbol, event_day, expiration, strike, call_put):
    PATH_PATTERN = '{0}_{1}_{2}_{3}{4}'
    path = PATH_PATTERN.format(symbol, event_day, expiration, strike, call_put)

    if not os.path.exists(path):
        os.mkdir(path)
        option_normal(symbol, event_day, expiration, strike, call_put, path)
        
        #quarter_oi(symbol, event_day, expiration, strike, call_put, path)
        
        option_milld(symbol, event_day, expiration, strike, call_put, path)
        print(path, 'search success!')
    else:
        print(path, 'already exists')

# search

# stock, event_day, expiration, strike, call_put
batch_search('VALE', 180516, 200117, 15, 'P')

