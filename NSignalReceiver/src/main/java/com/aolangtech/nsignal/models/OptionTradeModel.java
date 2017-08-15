package com.aolangtech.nsignal.models;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.constants.CommonConstants.TickTestTradeCategory;

public class OptionTradeModel {

	private Long id;
	
	private String stockSymbol;		// stock symbol
	
	private Integer expiration;		// yyMMdd

	private Character callPut;		// 'C': Call; 'P': Put
	
	private Double strike;		// strike price
	
	private Integer eventDay;		// yyMMdd
		
	private Integer eventTime;		// hhmmsslll
	
	private Double price;			// option trade price

	private Integer size;			// option trade size
	
	private Double previousPrice;		// last option price
	
	private Double askPrice;
	
	private Integer tradeAskInterval;	// time interval between ask and trade.	A unit of millisecond
	
	private Integer askAskInterval;			// the time gap between this ask and last ask. A unit of millisecond
	
	private Double bidPrice;
	
	private Integer tradeBidInterval;	// time interval between bid and trade. A unit of millisecond
	
	private Integer bidBidInterval;			// the time gap between this bid and last bid. A unit of millisecond
	
	private Short reportExg;	// The exchange putting order
	
	private Integer condition;		// the trade condition
	
	private Long sequenceId;

	private String direction;		// The trade direction 
	
	private Long legSequenceId;		// The sequenceId of the other leg
	
	private Boolean bigTrade;
	
	// Extra properties
	private TickTestTradeCategory tickTestTD;		// The trade direction inferred by method tick test
	
	private int bidAskTD;			// The trade direction inferred by method ask-bid quote based.	-100 ~ 0: Sell; 0 ~ 100: Buy.
	
	/**
	 * Generate the direction field. Just for agreeing with database field.
	 * 
	 */
	public void formDirection() {
		final String BUY = "Buy";
		final String SELL = "Sell";
		final String UNKNOWN = "Un";
		
		final String UNIT = "%";
		
		if(this.bidAskTD < 0) {
			// bid-ask sell
			
			this.direction = BUY + (-this.bidAskTD) + UNIT;
			
		} else if(this.bidAskTD > 0) {
			// bid-ask buy
			
			this.direction = SELL + this.bidAskTD + UNIT;
			
		} else {
			// bid-ask is invalid, then use tick test
			
			switch(this.tickTestTD) {
			case UPTICK:
			case ZERO_UPTICK:
				this.direction = BUY + 0 + UNIT;
				break;
			case DOWNTICK:
			case ZERO_DOWNTICK:
				this.direction = SELL + 0 + UNIT;
				break;
			case UNKNOWN:
				this.direction = UNKNOWN + 0 + UNIT;
			}
		}
	}
	
	/**
	 * Get the quarter(yyn) this record belong to.
	 * 
	 * @return
	 */
	public String getQuarter() {
		int year = this.eventDay / 10000;
		int quarterInYear = ((this.eventDay % 10000) / 100) / 4 + 1;
		
		return String.valueOf(year) + String.valueOf(quarterInYear);
	}
	
	/**
	 * Parse a option trade record to Class from string.
	 * 
	 * @param strRecord
	 * @return
	 */
	public static OptionTradeModel Parse(String strRecord){
		String[] fields = strRecord.split(CommonConstants.OPTION_TRADE_RECORD_SEPARATOR);
		if(fields.length != CommonConstants.OPTION_TRADE_RECORD_FIELD_NUMBER){
			// The record is error
			return null;
		}
		try{
			OptionTradeModel result = new OptionTradeModel();
			
			parseDate(result, fields[0]);
			parseSymbol(result, fields[1]);
			
			result.price = Double.valueOf(fields[2]);
			result.size = Integer.valueOf(fields[3]);
			result.previousPrice = Double.valueOf(fields[4]);
			
			result.askPrice = Double.valueOf(fields[5]);
			result.tradeAskInterval = Integer.valueOf(fields[6]);
			result.askAskInterval = Integer.valueOf(fields[7]);
			
			result.bidPrice = Double.valueOf(fields[8]);
			result.tradeBidInterval = Integer.valueOf(fields[9]);
			result.bidBidInterval = Integer.valueOf(fields[10]);
			
			result.reportExg = Short.valueOf(fields[11]);
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
	private static void parseDate(OptionTradeModel result, String date){
		// Input date format: yy-MM-dd hh:mm:ss.lll, length: 21
		result.eventDay = getDigit(date.charAt(0)) * 100000 + getDigit(date.charAt(1)) * 10000	// yy
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
	private static void parseSymbol(OptionTradeModel result, String symbol) throws Exception{
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
		while((++index) < symbolLength) {
			char ch = symbol.charAt(index);
			if(ch == CommonConstants.OPTION_TRADE_OPTION_TYPE_CALL
					|| ch == CommonConstants.OPTION_TRADE_OPTION_TYPE_PUT) {
				
				result.callPut = ch;
				break;
			}
		}
		
		// get option expiration date
		result.expiration = Integer.valueOf(symbol.substring(stockSymbolEndIndex, index));
		
		// get option strike price
		result.strike = (Integer.valueOf(symbol.substring(index + 1, symbolLength)) / 1000.0);
		
	}
	
	private static int getDigit(char ch){
		return ch - '0';
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getStockSymbol() {
		return stockSymbol;
	}

	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}

	public Integer getExpiration() {
		return expiration;
	}

	public void setExpiration(Integer expiration) {
		this.expiration = expiration;
	}

	public Character getCallPut() {
		return callPut;
	}

	public void setCallPut(Character callPut) {
		this.callPut = callPut;
	}

	public Double getStrike() {
		return strike;
	}

	public void setStrike(Double strike) {
		this.strike = strike;
	}

	public Integer getEventTime() {
		return eventTime;
	}

	public void setEventTime(Integer eventTime) {
		this.eventTime = eventTime;
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

	public Double getPreviousPrice() {
		return previousPrice;
	}

	public void setPreviousPrice(Double previousPrice) {
		this.previousPrice = previousPrice;
	}

	public Double getAskPrice() {
		return askPrice;
	}

	public void setAskPrice(Double askPrice) {
		this.askPrice = askPrice;
	}

	public Integer getTradeAskInterval() {
		return tradeAskInterval;
	}

	public void setTradeAskInterval(Integer tradeAskInterval) {
		this.tradeAskInterval = tradeAskInterval;
	}

	public Integer getAskAskInterval() {
		return askAskInterval;
	}

	public void setAskAskInterval(Integer askAskInterval) {
		this.askAskInterval = askAskInterval;
	}

	public Double getBidPrice() {
		return bidPrice;
	}

	public void setBidPrice(Double bidPrice) {
		this.bidPrice = bidPrice;
	}

	public Integer getTradeBidInterval() {
		return tradeBidInterval;
	}

	public void setTradeBidInterval(Integer tradeBidInterval) {
		this.tradeBidInterval = tradeBidInterval;
	}

	public Integer getBidBidInterval() {
		return bidBidInterval;
	}

	public void setBidBidInterval(Integer bidBidInterval) {
		this.bidBidInterval = bidBidInterval;
	}

	public Short getReportExg() {
		return reportExg;
	}

	public void setReportExg(Short reportExg) {
		this.reportExg = reportExg;
	}

	public Long getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(Long sequenceId) {
		this.sequenceId = sequenceId;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public Long getLegSequenceId() {
		return legSequenceId;
	}

	public void setLegSequenceId(Long legSequenceId) {
		this.legSequenceId = legSequenceId;
	}

	public Boolean getBigTrade() {
		return bigTrade;
	}

	public void setBigTrade(Boolean bigTrade) {
		this.bigTrade = bigTrade;
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

	public Integer getEventDay() {
		return eventDay;
	}

	public void setEventDay(Integer eventDay) {
		this.eventDay = eventDay;
	}

	public Integer getCondition() {
		return condition;
	}

	public void setCondition(Integer condition) {
		this.condition = condition;
	}
	
}
