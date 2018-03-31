/**
 * 
 */
/**
 * @author Maple
 *
 */
package org.maple.profitsystem.spiders;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.maple.profitsystem.constants.CommonConstants;
import org.maple.profitsystem.exceptions.HttpException;
import org.maple.profitsystem.exceptions.PSException;
import org.maple.profitsystem.models.CompanyModel;
import org.maple.profitsystem.models.StockQuoteModel;
import org.maple.profitsystem.utils.CSVUtil;
import org.maple.profitsystem.utils.HttpRequestUtil;
import org.maple.profitsystem.utils.TradingDateUtil;

public class NASDAQSpider {
	
	private static Logger logger = Logger.getLogger(NASDAQSpider.class);
	
	private static Map<String, String> httpHeaders = null;
	
	private final static String STOCK_SYMBOL_REG = "[^$.^]*";
	
	static {
		// set headers of http for nasdaq
		httpHeaders = new HashMap<>();
		
		httpHeaders.put("Host", "www.nasdaq.com");
		httpHeaders.put("Origin", "http://www.nasdaq.com");
	}
	
	/**
	 * Fetch a list of companies base info from nasdaq.
	 * 
	 * @return List of CompanyInfoModel, otherwise a empty list.
	 * @throws HttpException 
	 */
	public static List<CompanyModel> fetchCompanyListWithBaseInfo() throws HttpException {
		List<CompanyModel> result = new ArrayList<>();
		
		String response = HttpRequestUtil.getMethod(CommonConstants.URL_GET_COMPANY_LIST_NASDAQ, httpHeaders, CommonConstants.REQUEST_MAX_RETRY_TIMES);
		String[] lines = response.split(CommonConstants.NASDAQ_COMPANY_LIST_SEPRATOR_OF_RECORD);
		for(int i = 1; i < lines.length; ++i) {
			try{
				result.add(CompanyModel.parseFromTransportCSV(lines[i]));
			} catch(Exception e) {
				// This company is which for nasdaq test or had been bankrupted.
				logger.error("Invalid company: " + lines[i]);
			}
		}
		// filter invalid company
		return result.stream()
				.filter(company -> company.getSymbol().matches(STOCK_SYMBOL_REG) )
				.collect(Collectors.toList());
	}
	
	
	/**
	 * Fetch the stock quotes newer than all of quotes in CompanyInfoModel.quoteList.
	 * 
	 * @param company
	 * @return List of StockQuoteModel
	 * @throws PSException
	 * @throws HttpException 
	 */
	public static List<StockQuoteModel> fetchStockQuotes(String symbol, Integer startDt) throws PSException, HttpException {
		List<StockQuoteModel> result = null;
		try {
			result = fetchHistoricalQuotes(symbol, startDt);
		} catch (Exception e) {
		}
		
		if(result != null) {
			return result;
		}
		
		// the other way fetching last period(3 months) quotes from nasdaq
		
		result = fetchLastQuotes(symbol, startDt);
		
		return result;
	}
	
	/**
	 * Get stock last quotes from startDt to current using parsing html.   
	 * @param symbol
	 * @param startDt
	 * @return
	 * @throws HttpException 
	 * @throws PSException 
	 */
	private static List<StockQuoteModel> fetchLastQuotes(String symbol, Integer startDt) throws HttpException, PSException {
		final String TABLE_REGX_STR = "<table>\\s*<thead>[\\s\\S]*<tbody>([\\s\\S]*)</tbody>";
		
		String baseUrl = combineHistoricalQuotesUrl(symbol);
		String responseStr = HttpRequestUtil.getMethod(baseUrl, httpHeaders, CommonConstants.REQUEST_MAX_RETRY_TIMES);
		
		// truncate string to short string just storing quotes
		// responseStr = responseStr.substring(105000, responseStr.length() - 30000);
		
		Pattern r = Pattern.compile(TABLE_REGX_STR);
		Matcher m = r.matcher(responseStr);
		if(!m.find()) {
			throw new PSException("Content of quotes error!");
		}
		// parse html table to csv
		String csv = m.group(1).replaceAll("<tr>(\\s*)<td>", "");
		csv = csv.replaceAll("</td>(\\s*?)<td>", CommonConstants.CSV_SEPRATOR_BETWEEN_FIELD);
		csv = csv.replaceAll("\\s*", "");
		csv = csv.replaceAll("</td>(\\s*)</tr>", "\n");
		
		// parse all csv records to model
		List<StockQuoteModel> result = new ArrayList<>();
		
		String[] records = csv.split(CommonConstants.CSV_NEWLINE_REG);
		// the first line is real-time quote so skip it
		for(int i = 1; i < records.length; ++i) {
			try {
				StockQuoteModel tmp = parseFromHtmlCSV(records[i]);
				if(tmp.getQuoteDate() <= startDt) {
					break;
				}
				result.add(tmp);
				
			} catch (PSException e) {
				logger.error("Parse a quote record failed -" + records[i]);
			}
		}
		return result;
	}
	
	/**
	 * Get stock quotes from startDt to current date using downloading csv file. 
	 * 
	 * @param symbol
	 * @param startDt
	 * @return
	 * @throws HttpException 
	 * @throws PSException 
	 */
	private static List<StockQuoteModel> fetchHistoricalQuotes(String symbol, Integer startDt) throws HttpException, PSException {
		final String DATA_FIELD_FIVE_DAY = "5d";
		final int FIVE_DAY_DAYS = 5;
		final String DATA_FIELD_SIX_MONTH = "6m";
		final int SIX_MONTH_DAYS = 150;
		final String DATA_FIELD_ONE_YEAR = "1y";
		final int ONE_YEAR_DAYS = 365;
		final String DATA_FIELD_TEN_YEAR = "10y";
		
		// construct web request
		String fieldDate = null;
		if(startDt <= 0) {
			fieldDate = DATA_FIELD_TEN_YEAR;
		} else {
			Calendar now = Calendar.getInstance();
			if(!TradingDateUtil.hasMarketOpened(now)) {
				now.add(Calendar.DAY_OF_MONTH, -1);
			}
			int intervalTradingDays = TradingDateUtil.betweenTradingDays(TradingDateUtil.convertNumDate2Date(startDt), now.getTime());
			if(intervalTradingDays <= 0) {
				return null;
			} else if(intervalTradingDays < FIVE_DAY_DAYS) {
				fieldDate = DATA_FIELD_FIVE_DAY;
			} else if(intervalTradingDays < SIX_MONTH_DAYS) {
				fieldDate = DATA_FIELD_SIX_MONTH;
			} else if(intervalTradingDays < ONE_YEAR_DAYS) {
				fieldDate = DATA_FIELD_ONE_YEAR;
			} else {
				fieldDate = DATA_FIELD_TEN_YEAR;
			}
		}
		
		String baseUrl = combineHistoricalQuotesUrl(symbol);
		String postData = fieldDate + "|true|" + symbol.toUpperCase();
		Map<String, String> propertyMap = new HashMap<>();
		propertyMap.put("Content-Type", "application/json");
		propertyMap.putAll(httpHeaders);
		
		// get response from web
		String responseStr = HttpRequestUtil.postMethod(baseUrl, propertyMap, postData, CommonConstants.REQUEST_MAX_RETRY_TIMES);
		
		// parse response and get quote list
		String[] records = responseStr.split(CommonConstants.CSV_NEWLINE_REG);
		// check if the content is quotes 
		if(records.length <= 5) {
			throw new PSException("Content of quote list error!");
		}
		List<StockQuoteModel> result = new ArrayList<>();
		for(int i = 2; i < records.length; ++i) {
			try {
				StockQuoteModel tmp = parseQuoteFromHistoricalCSV(records[i]);
				if(tmp.getQuoteDate() <= startDt) {
					break;
				}
				result.add(tmp);
				
			} catch (PSException e) {
				logger.error("Parse a quote record failed -" + records[i]);
			}
		}
		return result;
	}
	
	private static StockQuoteModel parseQuoteFromHistoricalCSV(String csvRecord) throws PSException {
		String[] fields = CSVUtil.splitCSVRecord(csvRecord);
		try{
			StockQuoteModel result = new StockQuoteModel();
			result.setQuoteDate(Integer.valueOf(fields[0].replaceAll("/", "")));
			result.setClose(Double.valueOf(fields[1]));
			result.setVolume(Double.valueOf(fields[2]).intValue());
			result.setOpen(Double.valueOf(fields[3]));
			result.setHigh(Double.valueOf(fields[4]));
			result.setLow(Double.valueOf(fields[5]));
			
			return result;
		} catch(Exception e) {
			throw new PSException(e.getMessage());
		}
	}
	
	/**
	 * Parse csv format record extracted from html to StockQuoteModel.
	 * @param csvRecord format <date, open, high, low, close, volume>
	 * @return
	 * @throws PSException
	 */
	private static StockQuoteModel parseFromHtmlCSV(String csvRecord) throws PSException {
		String[] fields = CSVUtil.splitCSVRecord(csvRecord);
		try{
			StockQuoteModel result = new StockQuoteModel();
			String quoteDate = fields[0].substring(6, 10) + fields[0].substring(0, 2) + fields[0].substring(3, 5);
			result.setQuoteDate(Integer.valueOf(quoteDate));
			result.setOpen(Double.valueOf(fields[1]));
			result.setHigh(Double.valueOf(fields[2]));
			result.setLow(Double.valueOf(fields[3]));
			result.setClose(Double.valueOf(fields[4]));
			result.setVolume(Integer.valueOf(fields[5].replaceAll(",", "")));
			
			return result;
		} catch(Exception e) {
			throw new PSException(e.getMessage());
		}
	}	
	
	/**
	 * Combine the url to fetch historical quotes of the symbol specified stock.
	 * 
	 * @param symbol
	 * @return
	 */
	private static String combineHistoricalQuotesUrl(String symbol) {
		return CommonConstants.URL_GET_STOCK_QUOTES_NASDAQ_PREFIX + symbol.toLowerCase() + CommonConstants.URL_GET_STOCK_QUOTES_NASDAQ_SUFFIX;
	}
}