package com.aolangtech.nsignal.utils;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import com.aolangtech.nsignal.constants.CommonConstants;

public class CommonUtil {
	
	/**
	 * Get stock symbol, option expiration date, option type and option strike price from input symbol and set these into result.
	 * 
	 * @param symbol
	 * @return [stock_symbol, call_put, expiration, strike]
	 * @throws Exception
	 */
	public static String[] parseOptionSymbol(String symbol) throws Exception{
		if(symbol.charAt(0) != 'o'){
			throw new Exception("The trade is not option trade!");
		}
		
		int stockSymbolBeginIndex = 1;
		int stockSymbolEndIndex = 1;
		int symbolLength = symbol.length();
		int index = 0;
		
		String[] result = new String[10]; // [stock_symbol, call_put, expiration, strike]
		
		// get stock symbol
		while((++index) < symbolLength && !Character.isDigit(symbol.charAt(index)));
		stockSymbolEndIndex = index;
		
		result[0] = symbol.substring(stockSymbolBeginIndex, stockSymbolEndIndex);
		
		// get option type and its index
		while((++index) < symbolLength) {
			char ch = symbol.charAt(index);
			if(ch == CommonConstants.OPTION_TRADE_OPTION_TYPE_CALL
					|| ch == CommonConstants.OPTION_TRADE_OPTION_TYPE_PUT) {
				
				result[1] = "" + ch;
				break;
			}
		}
		
		// get option expiration date
		result[2] = symbol.substring(stockSymbolEndIndex, index);
		
		// get option strike price
		result[3] = String.valueOf(Integer.valueOf(symbol.substring(index + 1, symbolLength)) / 1000.0);
		
		return result;
	}
	
	/**
	 * Get day and event time of day from date.
	 * @param date
	 * @return [day, time]
	 */
	public static Integer[] parseDayAndTime(String date){
		Integer[] result = new Integer[2];
		// Input date format: yy-MM-dd hh:mm:ss.lll, length: 21
		result[0] = getDigit(date.charAt(0)) * 100000 + getDigit(date.charAt(1)) * 10000	// yy
				+ getDigit(date.charAt(3)) * 1000 + getDigit(date.charAt(4)) * 100				// MM
				+ getDigit(date.charAt(6)) * 10 + getDigit(date.charAt(7));						// dd
		
		result[1] = getDigit(date.charAt(9)) * 100000000 + getDigit(date.charAt(10)) * 10000000	// hh
				+ getDigit(date.charAt(12)) * 1000000 + getDigit(date.charAt(13)) * 100000				// mm
				+ getDigit(date.charAt(15)) * 10000 + getDigit(date.charAt(16)) * 1000					// ss
				+ getDigit(date.charAt(18)) * 100 + getDigit(date.charAt(19)) * 10 + getDigit(date.charAt(20));	// lll
	
		return result;
	}
	
	/**
	 * Get integer day from string day
	 * @param date
	 * @return
	 */
	public static Integer parseDay(String date){
		// Input date format: yy-MM-dd, length: 8
		Integer result = getDigit(date.charAt(0)) * 100000 + getDigit(date.charAt(1)) * 10000	// yy
				+ getDigit(date.charAt(3)) * 1000 + getDigit(date.charAt(4)) * 100				// MM
				+ getDigit(date.charAt(6)) * 10 + getDigit(date.charAt(7));						// dd
	
		return result;
	}
	
	/**
	 * Convert int date to date date.
	 * @param nDate
	 * @return
	 */
	public static Date nDate2dDate(Integer nDate) {
		
		try {
			return CommonConstants.DAY_DATE_FORMATTER.parse("" + nDate);
		} catch (ParseException e) {
			return null;
		}
		
	}
	
	/**
	 * Convert date date to int date
	 * @param dDate
	 * @return
	 */
	public static Integer dDate2nDate(Date dDate) {
		return Integer.valueOf(CommonConstants.DAY_DATE_FORMATTER.format(dDate));
	}
	
	/**
	 * Change date with offset.
	 * 
	 * @param nDate integer date (yyMMdd)
	 * @param offset
	 * @return resulted integer date (yyMMdd)
	 */
	public static Integer changeNDate(Integer nDate, int offset) {
		Date eventDay = CommonUtil.nDate2dDate(nDate);
		
		Calendar date = Calendar.getInstance();
		date.setTime(eventDay);
		date.set(Calendar.DATE, date.get(Calendar.DATE) + offset);
		
		return CommonUtil.dDate2nDate(date.getTime());
	}

	/**
	 * Change date with offset.
	 * 
	 * @param dDate date whose type is date
	 * @param offset
	 * @return resulted date whose type is date
	 */
	public static Date changeDDate(Date dDate, int offset) {
		
		Calendar date = Calendar.getInstance();
		date.setTime(dDate);
		date.set(Calendar.DATE, date.get(Calendar.DATE) + offset);
		
		return date.getTime();
	}
	
	/**
	 * Get the quarter(yyn) this record belong to.
	 * 
	 * @return
	 */
	public static String getQuarterByDay(Integer day) {
		int year = day / 10000;
		int quarterInYear = ((day % 10000) / 100 - 1) / 3 + 1;
		
		return String.valueOf(year) + String.valueOf(quarterInYear);
	}
	
	
	public static int getDigit(char ch){
		return ch - '0';
	}
	
}
