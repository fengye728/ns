-- groud search --
SELECT event_day, stock_symbol, strike, expiration, size, COUNT(size) as num, SUM(price * size / 10000) as millD
FROM option_trade_163
WHERE price >= 10
    AND call_put = 'P'
    AND direction LIKE 'Sell%'
    AND size <= 30
    -- AND (stock_symbol LIKE 'A%' OR stock_symbol LIKE 'C%')
    AND expiration - event_day > 500
	AND stock_symbol NOT IN ('SPY', 'SPXW', 'SVXY', 'QQQ', 'VXX', 'UVXY', 'DUST', 'JDST', 'RUT', 'RUTW')

GROUP BY event_day, stock_symbol, strike, expiration, size
HAVING sum(size) >= 100 AND COUNT(size) >= 20
ORDER BY stock_symbol, event_day, strike DESC, num DESC

-- groud search 2.0 --
SELECT event_day, stock_symbol, strike, expiration, ROUND(AVG(price), 2) as price,size, COUNT(size) as size_count, SUM(size) as total_size, ROUND(SUM(price * size / 10000), 3) as millD
FROM option_trade_171
WHERE 
	(price >= 10 OR (price / strike > 0.2 AND price >= 2))
    AND call_put = 'P'
    AND direction LIKE 'Sell%'
    AND expiration - event_day > 300
	AND stock_symbol NOT IN ('DUST', 'JDST', 'RUT', 'RUTW')

GROUP BY event_day, stock_symbol, strike, expiration, size
HAVING sum(size) >= 100 AND COUNT(size) >= 20
ORDER BY stock_symbol, event_day, strike DESC, total_size DESC

-- Spread search ---------
SELECT stock_symbol, event_day, event_time / 1000 as event_time, call_put, expiration, strike, size, price, direction, condition, sequence_id, leg_sequence_id
FROM option_trade_173 o1
WHERE call_put = 'C'
	AND condition = 35
    AND size >= 5000
    AND price / strike <= 0.05
    AND stock_symbol NOT IN ('VXX', 'VIX', 'SPY')
    AND expiration - event_day > 300
	-- AND event_day BETWEEN 180100 AND 180124
	AND exists(
	
		SELECT sequence_id
		FROM option_trade_173 o2
		WHERE o2.stock_symbol = o1.stock_symbol
			AND o2.event_day = o1.event_day
			AND o2.call_put = o1.call_put
			AND (o2.strike > o1.strike * 1.1 OR o2.strike < o1.strike * 0.9)
			AND o2.report_exg = o1.report_exg
			AND o2.condition = o1.condition
			AND o2.sequence_id = o1.leg_sequence_id	
	)
ORDER BY stock_symbol, event_day, event_time

-- One Spread Search ----------------
SELECT stock_symbol, event_day, event_time / 1000 as event_time, call_put, expiration, strike, size, price, direction, condition, sequence_id, leg_sequence_id
FROM option_trade_181
WHERE stock_symbol = 'SNAP'
	AND event_day = 180118
	AND ( sequence_id = 49585533 OR leg_sequence_id = 49585533)
ORDER BY stock_symbol, event_day, event_time


-- Big Trade Event Day Search --------
SELECT stock_symbol, event_day, call_put, strike, expiration, SUM(size) as volume, SUM(size * price) / 10000 as millD
FROM option_trade_181
WHERE stock_symbol = 'AAPL'
	AND event_day BETWEEN 180100 AND 180131
    AND expiration - event_day > 300
GROUP BY stock_symbol, event_day, call_put, strike, expiration
HAVING SUM(size * price) / 10000 > 1
ORDER BY millD DESC

-- Specified Option millD Search ----
SELECT stock_symbol, strike, expiration, SUBSTRING(direction, 1, 1) as direc, COUNT(*) AS group_count,SUM(size) as volume, SUM(size * price) / 10000 as millD
FROM option_trade_181
WHERE stock_symbol = 'AAPL'
	AND event_day BETWEEN 180100 AND 180131
    AND expiration = 180720
    AND strike = 90
    AND call_put = 'C'
GROUP BY stock_symbol, call_put, strike, expiration, direc
ORDER BY millD DESC

-- Normal search --
SELECT stock_symbol, event_day, event_time / 1000 as event_time, call_put, expiration, strike, size, price, direction, condition, sequence_id, leg_sequence_id
FROM option_trade_181
WHERE stock_symbol = 'AAPL'
	AND event_day = 180109
    AND expiration = 200117
    AND strike = 100
    AND call_put = 'C'
ORDER BY stock_symbol, event_day, event_time