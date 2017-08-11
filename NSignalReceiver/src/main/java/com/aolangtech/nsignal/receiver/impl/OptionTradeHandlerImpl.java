package com.aolangtech.nsignal.receiver.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.aolangtech.nsignal.exceptions.NsignalException;
import com.aolangtech.nsignal.models.OptionTradeModel;
import com.aolangtech.nsignal.receiver.NSignalReceiver;
import com.aolangtech.nsignal.receiver.OptionTradeHandler;
import com.aolangtech.nsignal.utils.TradeDirectInferUtil;

public class OptionTradeHandlerImpl implements OptionTradeHandler {
	
	private Logger logger = Logger.getLogger(OptionTradeHandlerImpl.class);

	private Map<String, List<OptionTradeModel>> symbolMap;	//  <Stock symbol, List of trade>
	
	private NSignalReceiver receiver;
	
	public OptionTradeHandlerImpl(NSignalReceiver receiver) {
		this.receiver = receiver;
		symbolMap = new HashMap<>();
	}
	
	@Override
	public void run() {
		long errorCount = 0;
		
		// open receiver
		this.receiver.open();
		
		// read all records and store in memory
		try {
			String recordLine = null;
			// read all records and add them into map
			while((recordLine = this.receiver.receiveRecord()) != null) {
				OptionTradeModel record = OptionTradeModel.Parse(recordLine);
				// parse fail
				if(null == record) {
					errorCount++;
				} else {
					// inferring trade direction by bid-ask
					TradeDirectInferUtil.bidAskTest(record);
					
					// inferring trade direction by tick test
					TradeDirectInferUtil.tickTest(findLastTrade(record), record);
					
					// just for agreeing with database field
					record.formDirection();
					
					addOptionTrade2Map(record);
				}
				
			}
		} catch (NsignalException e) {
			// TODO 
			logger.error(e.getContent());
		}
		
		// TODO find trade leg
		
		
		// TODO set big trade flag
		
		
		logger.info("The count of error records is " + errorCount);
	}

	@Override
	public void persist() {
		// TODO Auto-generated method stub

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
