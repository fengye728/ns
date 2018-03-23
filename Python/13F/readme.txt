1.Stock symbol and CIK
	1)URL: www.sec.gov/files/dera/data/financial-statement-data-sets/yyyyqX.zip (eg:2017q4.zip)
	2)target file: sub.txt in above zip file.
	3)CIK: cik column in target file
	4)Stock symbol: instance column in target file. format: symbol[_-]yyyyMMdd.xml (eg: aapl-20091226.xml)
	
2.Create Table SQL:

CREATE TABLE fund
(
	id bigserial PRIMARY KEY,
	cik bigint NOT NULL UNIQUE,
	name text NOT NULL,
	symbol text DEFAULT NULL,
	cusip text DEFAULT NULL
);

CREATE TABLE holding_report
(
	id bigserial PRIMARY KEY,
	fund_id bigint REFERENCES fund(id) NOT NULL,
	cusip text NOT NULL,
	amt_shares integer,
	quarter integer NOT NULL,			-- yyyyQ
	
	CONSTRAINT hr_unique UNIQUE(fund_id, quarter, cusip)
);

CREATE TABLE earning
(
	id bigserial PRIMARY KEY,
	symbol text NOT NULL,
	quarter integer NOT NULL,			-- yyyyQ
	earning_date integer DEFAULT NULL,
	earning_ab char(1) DEFAULT NULL,	-- A: after market close; B: before market open
	
	CONSTRAINT earning_unique UNIQUE(symbol, quarter)
);

3.Remark
	Quarter fetching cik data is most recent quarter.
	
4.Other links
	https://www.sec.gov/dera/data/financial-statement-data-sets.html
	ftp://ftp.nasdaqtrader.com
	https://www.sec.gov/edgar/searchedgar/accessing-edgar-data.htm
	https://quant.stackexchange.com