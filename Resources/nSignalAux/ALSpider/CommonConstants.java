/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.constants;

public class CommonConstants {
	
	public final static String NULL_STRING = "null";
	
	public final static String FILE_PREFIX_STOCK = "STOCK_";
	
	public final static int BUFFER_SIZE_OF_READER = 524288;	// 512 * 1024
	
	public final static int REQUEST_MAX_RETRY_TIMES = 3;
	
	// --------------- Properties Default Value ---------------------
	public final static int DEFAULT_MAX_THREADS = 5;
	
	public final static int DEFAULT_STATISTICS_UPDATE_PERIOD = 7;
	
	public final static int DEFAULT_QUOTES_UPDATE_PERIOD = 1;
	
	public final static String DEFAULT_BACKUP_PATH = "StockQuotes";
	
	// -------------- Date Format -----------------------------------
	public final static String DATE_FORMAT_OUT = "yyyyMMdd";
	
	public final static String DATE_FORMAT_NASDAQ_IN = "yyyy/MM/dd";
	
	// ---------------- CSV ------------------------------
	public final static String CSV_SEPRATOR_BETWEEN_FIELD = "\",\"";
	public final static String CSV_SURROUNDER_OF_FIELD = "\"";
	public final static String CSV_NEWLINE = "\n";
	public final static String CSV_NEWLINE_REG = "[\r]?\n";
	
	
	// --------------- NASDAQ CONSTANTS -------------------------- 
	public final static String NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD = ",[\r]?\n";
	
	public final static int NASDAQ_COMPANY_LIST_RECORD_FIELDS_NUMBER = 9;
	
	public final static String URL_GET_COMPANY_LIST_NASDAQ = "https://www.nasdaq.com/screening/companies-by-industry.aspx?render=download";
	
	public final static String URL_GET_STOCK_QUOTES_NASDAQ_PREFIX = "https://www.nasdaq.com/symbol/";
	
	public final static String URL_GET_STOCK_QUOTES_NASDAQ_SUFFIX = "/historical";
	
	// ----------------------- FINVIZ CONSTANTS ---------------------------------
	public final static String URL_GET_COMPANY_STATISTICS_FINVIZ = "https://www.finviz.com/quote.ashx?t=";
	
	// ------------------------ YAHOO CONSTANTS ---------------------------
	public final static String URL_GET_COMPANY_STATISTICS_YAHOO = "https://finance.yahoo.com/quote/%s/key-statistics?p=%s";
	
}