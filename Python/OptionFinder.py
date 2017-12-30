import psycopg2
import numpy as np
import pandas as pd

# Constant
GOURD_SQL = "\
SELECT stock_symbol, event_day, strike, expiration, size, COUNT(size) as count_size, SUM(price * size / 10000) as million \
FROM %s \
WHERE price >= 10 \
    AND call_put = 'P' \
    AND direction LIKE 'Sell%' \
    AND size <= 30 \
    AND expiration - event_day > 500 \
GROUP BY event_day, stock_symbol, strike, expiration, size \
HAVING sum(size) >= 100 AND count_size >= 20 \
ORDER BY stock_symbol, event_day, strike, count_size DESC \
"

GOURD_COLUMNS = ['stock_symbol', 'event_day', 'strike', 'expiration', 'size', 'count_size', 'million']

TABLE_PREFIX = 'option_trade_'

# Get connection
conn = psycopg2.connect(database = 'aolang', user = 'postgres', password = 'al', host = '192.168.0.249', port = '5432')
cur = conn.cursor()

'''
    quarter: yyQ (eg: 173)
'''
def get_gourd(quarter):
    # construct sql
    table_name = TABLE_PREFIX + str(quarter)
    sql = GOURD_SQL % (table_name)
    # execute sql
    cur.execute(sql)
    return pd.DataFrame(data = cur.fetchall(), columns = GOURD_COLUMNS)

