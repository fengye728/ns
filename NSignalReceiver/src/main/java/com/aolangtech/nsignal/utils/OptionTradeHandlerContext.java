package com.aolangtech.nsignal.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.aolangtech.nsignal.constants.CommonConstants;
import com.aolangtech.nsignal.models.OptionOIModel;
import com.aolangtech.nsignal.models.OptionTradeModel;
import com.aolangtech.nsignal.services.OptionOIService;
import com.aolangtech.nsignal.services.OptionTradeService;
import com.aolangtech.nsignal.services.impl.OptionOIServiceImpl;
import com.aolangtech.nsignal.services.impl.OptionTradeServiceImpl;

public class OptionTradeHandlerContext{
	
	private static Logger logger = Logger.getLogger(OptionTradeHandlerContext.class);
	
	private Map<String, List<OptionTradeModel>> tradeMap = new HashMap<>();;	//  <Stock symbol, List of trade>
	
	private List<OptionOIModel> oiList = new ArrayList<>();	

	private OptionTradeService optionTradeService = new OptionTradeServiceImpl();
	
	private OptionOIService optionOIService = new OptionOIServiceImpl();
	
	/**
	 * Handles one line record and stores the result into tradeMap.
	 * 
	 * 	PS: Stripped the message type.
	 * 
	 * @param recordLine
	 */
	public boolean handleOneLineRecord(String msg) {
		// get message type
		int msgEndIndex = msg.indexOf(CommonConstants.OPTION_TRADE_RECORD_SEPARATOR);
		if(msgEndIndex == -1) {
			return false;
		}
		int msgType = Integer.valueOf(msg.substring(0, msgEndIndex));
		String record = msg.substring(msgEndIndex + 1);
		boolean status = false;
		
		// process record depend on message type
		switch(msgType) {
		case CommonConstants.RECORD_TYPE_TRADE:
			status = handleTrade(record);
			break;
		case CommonConstants.RECORD_TYPE_OI:
			status = handleOI(record);
			break;
		case CommonConstants.RECORD_TYPE_TRADE_FINISH:
			persistTrade();
			break;
		case CommonConstants.RECORD_TYPE_OI_FINISH:
			persistOI();
			break;
		}
		
		return status;
	}
	
	private boolean handleTrade(String recordLine) {
		if(null == recordLine)
			return false;
		
		// parse option trade model
		OptionTradeModel record = OptionTradeModel.Parse(recordLine);
		// parse fail
		if(null == record)
			return false;
		
		// inferring trade direction by bid-ask
		TradeDirectInferUtil.bidAskTest(record);
		
		// just for agreeing with database field
		record.formDirection();
		
		// add the record into map.
		addOptionTrade2Map(record);
		
		return true;		
	}
	
	/**
	 * Handle OI record and add valid record into oiList
	 * 
	 * 	// OI records of one day contain the previous previous day oi and previous day oi
	 * @param recordLine
	 * @return
	 */
	private boolean handleOI(String recordLine) {
		if(null == recordLine)
			return false;
		
		OptionOIModel record = OptionOIModel.parse(recordLine);
		if(record == null) {
			return false;
		} else {
			// insert or update oi
			oiList.add(record);
			return true;
		}
		
	}
	
	/**
	 * Get the date of option trade info map stored.
	 * 
	 * @return
	 */
	public int getOptionTradeDate() {
		Collection<List<OptionTradeModel>> values = tradeMap.values();
		if(!values.isEmpty()){
			int eventDay = values.iterator().next().get(0).getEventDay();
			return eventDay;
		}
		else
			return 0;
	}
	
	/**
	 * Persist all records of open interest into database
	 * @return
	 */
	public int persistOI() {
		OptionOIModel maxOIEvnetDay = oiList.stream().max((x1, x2) -> x1.getEventDay() - x2.getEventDay()).get();
		int oiRecordCount = optionOIService.insertList(oiList);
		logger.info("Persist OI records success - Date of "+ maxOIEvnetDay.getEventDay() + ". Count: " + oiRecordCount);
		return oiRecordCount;
	}
	/**
	 * Persist all records of trades into database.
	 * 
	 * @return The number of records persisted.
	 */
	public int persistTrade() {
		// tagging trades
		tagTrade();
		
		int tradeRecordCount = optionTradeService.insertByMap(tradeMap);
		logger.info("Persist trade records success - Date of "+ getOptionTradeDate() + ". Count: " + tradeRecordCount);
		return tradeRecordCount;
	}
	
	/**
	 * Process all trades for tagging.
	 * 
	 */
	public void tagTrade() {
		// spread tag
		combineTradeLeg();
		
		setBigTradeFlag();
	}

	/**
	 * Find trade leg in tradeMap.
	 * 
	 */
	private void combineTradeLeg() {
		// classify by reportExg and filter condition
		for(List<OptionTradeModel> tradeList : tradeMap.values()) {
			// <ReportExg, List of optionTrade which is leg condition>
			Map<Short, List<OptionTradeModel>> exgLegMap = tradeList.stream().filter( optionTrade -> optionTrade.isLegCondition()).collect(Collectors.groupingBy(OptionTradeModel::getReportExg));
			
			for(Short reportExg : exgLegMap.keySet()) {
				// <Condition, List of optionTrade>
				Map<Integer, List<OptionTradeModel>> conditionLegMap = exgLegMap.get(reportExg).stream().collect(Collectors.groupingBy(OptionTradeModel::getCondition));
				
				for(Integer condition : conditionLegMap.keySet()) {
					// combine trade leg
					
					List<OptionTradeModel> legList = conditionLegMap.get(condition);
					// sort by sequenceId
					Collections.sort(legList, (trade1, trade2) -> (int)(trade1.getSequenceId() - trade2.getSequenceId()));
					
					Long firstLegSequenceId = null;
					for(int i = 0; i < legList.size() - 1; ++i) {
						if(isLegBySequenceId(legList.get(i), legList.get(i + 1))) {
							legList.get(i).setLegSequenceId(legList.get(i + 1).getSequenceId());
							if(firstLegSequenceId == null) {
								firstLegSequenceId = legList.get(i).getSequenceId();
							}
						} else {
							if(firstLegSequenceId != null) {
								legList.get(i).setLegSequenceId(firstLegSequenceId);
								firstLegSequenceId = null;
							}
						}
					}
					if(firstLegSequenceId != null) {
						legList.get(legList.size() - 1).setLegSequenceId(firstLegSequenceId);
					}

				}
			}
			
		}
	}
	
	private boolean isLegBySequenceId(OptionTradeModel trade1, OptionTradeModel trade2) {
		final long LEG_SEQUENCE_ID_THRESHOLD = 100; 
		if(Math.abs(trade1.getSequenceId() - trade2.getSequenceId()) <= LEG_SEQUENCE_ID_THRESHOLD) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Find and set big trade flag.
	 * 
	 */
	private void setBigTradeFlag() {
		// TODO set big trade flag
	}
	
	/**
	 * Add a option trade record into map and would create a new map if the map is null.
	 * 
	 * @param quarter
	 * @param record
	 */
	private void addOptionTrade2Map(OptionTradeModel record) {
		List<OptionTradeModel> tradeList = tradeMap.get(record.getStockSymbol());
		
		if(null == tradeList) {
			tradeList = new ArrayList<>();
			tradeList.add(record);
			
			tradeMap.put(record.getStockSymbol(), tradeList);
		} else {
			tradeList.add(record);
		}
	}	
	
}
