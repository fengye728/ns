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
	symbol text
);

4. Hodling Report:
	SER FORM: 13F-HR

5. Senior management holding report:
	SEC FORM: 4
	
	
6.Remark
	Quarter fetching cik data is most recent quarter.
	
7.Other links
	https://www.sec.gov/dera/data/financial-statement-data-sets.html
	ftp://ftp.nasdaqtrader.com
	https://www.sec.gov/edgar/searchedgar/accessing-edgar-data.htm
	https://quant.stackexchange.com