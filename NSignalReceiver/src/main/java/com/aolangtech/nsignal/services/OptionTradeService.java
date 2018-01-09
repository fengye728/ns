/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.services;

import java.util.List;
import java.util.Map;

import com.aolangtech.nsignal.models.OptionTradeModel;

public interface OptionTradeService {
	
	/**
	 * Insert a list of OptionTradeModels whose event date is same with each other into database.
	 * @param list
	 * @return
	 */
	int insertList(List<OptionTradeModel> list);
	
	/**
	 * Insert OptionTradeModels which in a map <symbol, a list of it>
	 * @param tradeMap
	 * @return
	 */
	int insertByMap(Map<String, List<OptionTradeModel>> tradeMap);
	
}