package com.aolangtech.nsignal.models;

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
	
	
}
