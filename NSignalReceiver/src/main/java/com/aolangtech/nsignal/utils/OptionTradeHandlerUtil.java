package com.aolangtech.nsignal.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aolangtech.nsignal.models.OptionTradeModel;
import com.aolangtech.nsignal.services.OptionTradeService;
import com.aolangtech.nsignal.services.impl.OptionTradeServiceImpl;

public class OptionTradeHandlerUtil{

	private Logger logger = Logger.getLogger(OptionTradeHandlerUtil.class);
	
	private Map<String, List<OptionTradeModel>> symbolMap = new HashMap<>();;	//  <Stock symbol, List of trade>
	
	/**
	 * Handles one line record and stores the result into symbolMap.
	 * 
	 * @param recordLine
	 */
	public boolean handleOneLineRecord(String recordLine) {
		if(null == recordLine)
			return false;
		
		// parse option trade model
		OptionTradeModel record = OptionTradeModel.Parse(recordLine);
		// parse fail
		if(null == record)
			return false;
		
		// inferring trade direction by bid-ask
		TradeDirectInferUtil.bidAskTest(record);
		
		// inferring trade direction by tick test
		TradeDirectInferUtil.tickTest(findLastTrade(record), record);
		
		// just for agreeing with database field
		record.formDirection();
		
		// add the record into map.
		addOptionTrade2Map(record);
		
		return true;
	}
	
	/**
	 * Process all lists in map when full data set is in map.
	 * 
	 */
	public void processForMap() {

		findTradeLeg();
		setBigTradeFlag();
	}

	/**
	 * Find trade leg in symbolMap.
	 * 
	 */
	private void findTradeLeg() {
		// TODO find trade leg
	}
	
	/**
	 * Find and set big trade flag.
	 * 
	 */
	private void setBigTradeFlag() {
		// TODO set big trade flag
	}
	
	/**
	 * Persist the data set in symbolMap into database.
	 * 
	 */
	public void persist() {
		OptionTradeService optionTradeService = new OptionTradeServiceImpl();
		long orignCount = 0;
		long persistCount = 0;
		for(List<OptionTradeModel> list : symbolMap.values()) {
			orignCount += list.size();
			
			persistCount += optionTradeService.insertList(list);
			
			if(orignCount != persistCount)
			{
				logger.error("Count fail:" + list.get(0).getStockSymbol());
			}
		}
		
		logger.info("Original count:" + orignCount);
		logger.info("Persist count:" + orignCount);
	}
	
	/**
	 * Get last option trade record. 
	 * 
	 * @param record
	 * @return the last option trade record if existed, otherwise null.
	 */
	private OptionTradeModel findLastTrade(OptionTradeModel record) {
		List<OptionTradeModel> tradeList = symbolMap.get(record.getStockSymbol());
		
		if(null != tradeList) {
			OptionTradeModel tmp;
			for(int i = tradeList.size() - 1; i >= 0; --i) {
				tmp = tradeList.get(i);
				if(tmp.getExpiration() == record.getExpiration() && tmp.getCallPut() == record.getCallPut()) {
					return tmp;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Add a option trade record into map and would create a new map if the map is null.
	 * 
	 * @param quarter
	 * @param record
	 */
	private void addOptionTrade2Map(OptionTradeModel record) {
		List<OptionTradeModel> tradeList = symbolMap.get(record.getStockSymbol());
		
		if(null == tradeList) {
			tradeList = new ArrayList<>();
			tradeList.add(record);
			
			symbolMap.put(record.getStockSymbol(), tradeList);
		} else {
			tradeList.add(record);
		}
	}	
	
}
