package com.aolangtech.nsignal.models;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.utils.CommonUtil;

public class OptionOIModel {

	private String stockSymbol;
	
	private Integer eventDay;	// yyMMdd
	
	private Integer expiration;	// yyMMdd
	
	private Character callPut;	// 'C' or 'P'
	
	private Double strike;
	
	private Integer openInterest;

	public String getStockSymbol() {
		return stockSymbol;
	}

	public void setStockSymbol(String stockSymbol) {
		this.stockSymbol = stockSymbol;
	}

	public Integer getEventDay() {
		return eventDay;
	}

	public void setEventDay(Integer eventDay) {
		this.eventDay = eventDay;
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

	public Integer getOpenInterest() {
		return openInterest;
	}

	public void setOpenInterest(Integer openInterest) {
		this.openInterest = openInterest;
	}
	
	public static OptionOIModel parse(String record) {
		if(record == null) {
			return null;
		}
		
		String[] fields = record.split(CommonConstants.OPTION_TRADE_RECORD_SEPARATOR);
		if(fields.length != CommonConstants.OPTION_OI_RECORD_FIELD_NUMBER) {
			return null;
		}
		
		try {
			OptionOIModel result = new OptionOIModel();
			
			result.eventDay = CommonUtil.parseDay(fields[0]);
			
			String[] optionFields = CommonUtil.parseOptionSymbol(fields[1]);
			result.stockSymbol = optionFields[0];
			result.callPut = optionFields[1].charAt(0);
			result.expiration = Integer.valueOf(optionFields[2]);
			result.strike = Double.valueOf(optionFields[3]);
			
			result.openInterest = Integer.valueOf(fields[2]);
			
			return result;
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}
}
