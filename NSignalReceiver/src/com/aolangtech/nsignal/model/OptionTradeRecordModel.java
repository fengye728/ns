package com.aolangtech.nsignal.model;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.constants.CommonConstants.OptionType;
import com.aolangtech.nsignal.constants.CommonConstants.TickTestTradeCategory;

public class OptionTradeRecordModel {

	private Integer eventDate;		// yyMMdd
	
	private Integer eventTime;		// hhmmsslll
	
	private String stockSymbol;		// stock symbol
	
	private OptionType optionType;	// 'C': Call; 'P': Put
	
	private Double strikePrice;		// strike price
	
	private Integer expireDate;		// yyMMdd
	
	private Double price;			// option trade price
	
	private Integer size;			// option trade size
	
	private Double lastPrice;		// last option price
	
	private Double askPrice;
	private Integer askInterval;	// time interval between ask and trade.	A unit of millisecond
	private Integer askGap;			// the time gap between this ask and last ask. A unit of millisecond
	
	private Double bidPrice;
	private Integer bidInterval;	// time interval between bid and trade. A unit of millisecond
	private Integer bidGap;			// the time gap between this bid and last bid. A unit of millisecond
	
	private Integer reportExg;
	
	private Integer condition;		// the trade condition
	
	private Long sequenceId;
	
	// Extra properties
	private TickTestTradeCategory tickTestTD;		// The trade direction inferred by method tick test
	
	private int bidAskTD;			// The trade direction inferred by method ask-bid quote based
	
	/**
	 * Parse a option trade record to Class from string.
	 * 
	 * @param strRecord
	 * @return
	 */
	public static OptionTradeRecordModel Parse(String strRecord){
		String[] fields = strRecord.split(CommonConstants.OPTION_TRADE_RECORD_SEPARATOR);
		if(fields.length != CommonConstants.OPTION_TRADE_RECORD_FIELD_NUMBER){
			// The record is error
			return null;
		}
		try{
			OptionTradeRecordModel result = new OptionTradeRecordModel();
			
			parseDate(result, fields[0]);
			parseSymbol(result, fields[1]);
			
			result.price = Double.valueOf(fields[2]);
			result.size = Integer.valueOf(fields[3]);
			result.lastPrice = Double.valueOf(fields[4]);
			
			result.askPrice = Double.valueOf(fields[5]);
			result.askInterval = Integer.valueOf(fields[6]);
			result.askGap = Integer.valueOf(fields[7]);
			
			result.bidPrice = Double.valueOf(fields[8]);
			result.bidInterval = Integer.valueOf(fields[9]);
			result.bidGap = Integer.valueOf(fields[10]);
			
			result.reportExg = Integer.valueOf(fields[11]);
			result.condition = Integer.valueOf(fields[12]);
			result.sequenceId = Long.valueOf(fields[13]);
			
			return result;
		}
		catch(Exception exc){
			exc.printStackTrace();
			return null;
		}
		
		
	}
	
	/**
	 * Get event date and event time of day from date and set these into result.
	 * 
	 * @param result
	 * @param date
	 */
	private static void parseDate(OptionTradeRecordModel result, String date){
		// Input date format: yy-MM-dd hh:mm:ss.lll, length: 21
		result.eventDate = getDigit(date.charAt(0)) * 100000 + getDigit(date.charAt(1)) * 10000	// yy
				+ getDigit(date.charAt(3)) * 1000 + getDigit(date.charAt(4)) * 100				// MM
				+ getDigit(date.charAt(6)) * 10 + getDigit(date.charAt(7));						// dd
		
		result.eventTime = getDigit(date.charAt(9)) * 100000000 + getDigit(date.charAt(10)) * 10000000	// hh
				+ getDigit(date.charAt(12)) * 1000000 + getDigit(date.charAt(13)) * 100000				// mm
				+ getDigit(date.charAt(15)) * 10000 + getDigit(date.charAt(16)) * 1000					// ss
				+ getDigit(date.charAt(18)) * 100 + getDigit(date.charAt(19)) * 10 + getDigit(date.charAt(20));	// lll
	
	}
	
	/**
	 * Get stock symbol, option expiration date, option type and option strike price from input symbol and set these into result.
	 * 
	 * @param result
	 * @param symbol
	 * @throws Exception
	 */
	private static void parseSymbol(OptionTradeRecordModel result, String symbol) throws Exception{
		if(symbol.charAt(0) != 'o'){
			throw new Exception("The trade is not option trade!");
		}
		
		int stockSymbolBeginIndex = 1;
		int stockSymbolEndIndex = 1;
		int symbolLength = symbol.length();
		int index = 0;
		
		// get stock symbol
		while((++index) < symbolLength && !Character.isDigit(symbol.charAt(index)));
		stockSymbolEndIndex = index;
		result.stockSymbol = symbol.substring(stockSymbolBeginIndex, stockSymbolEndIndex);
		
		// get option type and its index
		while((++index) < symbolLength && (result.optionType = CommonConstants.OptionType.getEnumItem(symbol.charAt(index))) == null);
		
		// get option expiration date
		result.expireDate = Integer.valueOf(symbol.substring(stockSymbolEndIndex, index));
		
		// get option strike price
		result.strikePrice = (Integer.valueOf(symbol.substring(index + 1, symbolLength)) / 1000.0);
		
	}
	
	private static int getDigit(char ch){
		return ch - '0';
	}

	public Integer getEventDate() {
		return eventDate;
	}

	public void setEventDate(Integer eventDate) {
		this.eventDate = eventDate;
	}

	public Integer getEventTime() {
		return eventTime;
	}

	public void setEventTime(Integer eventTime) {
		this.eventTime = eventTime;
	}

	public String getStockSymbol() {
		return stockSymbol;
	}

	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}

	public OptionType getOptionType() {
		return optionType;
	}

	public void setOptionType(OptionType optionType) {
		this.optionType = optionType;
	}

	public Double getStrikePrice() {
		return strikePrice;
	}

	public void setStrikePrice(Double strikePrice) {
		this.strikePrice = strikePrice;
	}

	public Integer getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Integer expireDate) {
		this.expireDate = expireDate;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public Double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(Double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public Double getAskPrice() {
		return askPrice;
	}

	public void setAskPrice(Double askPrice) {
		this.askPrice = askPrice;
	}

	public Integer getAskInterval() {
		return askInterval;
	}

	public void setAskInterval(Integer askInterval) {
		this.askInterval = askInterval;
	}

	public Integer getAskGap() {
		return askGap;
	}

	public void setAskGap(Integer askGap) {
		this.askGap = askGap;
	}

	public Double getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(Double bidPrice) {
		this.bidPrice = bidPrice;
	}

	public Integer getBidInterval() {
		return bidInterval;
	}

	public void setBidInterval(Integer bidInterval) {
		this.bidInterval = bidInterval;
	}

	public Integer getBidGap() {
		return bidGap;
	}

	public void setBidGap(Integer bidGap) {
		this.bidGap = bidGap;
	}

	public Integer getReportExg() {
		return reportExg;
	}

	public void setReportExg(Integer reportExg) {
		this.reportExg = reportExg;
	}

	public Integer getCondition() {
		return condition;
	}

	public void setCondition(Integer condition) {
		this.condition = condition;
	}

	public Long getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(Long sequenceId) {
		this.sequenceId = sequenceId;
	}

	public TickTestTradeCategory getTickTestTD() {
		return tickTestTD;
	}

	public void setTickTestTD(TickTestTradeCategory tickTestTD) {
		this.tickTestTD = tickTestTD;
	}

	public int getBidAskTD() {
		return bidAskTD;
	}

	public void setBidAskTD(int bidAskTD) {
		this.bidAskTD = bidAskTD;
	}
	
}
