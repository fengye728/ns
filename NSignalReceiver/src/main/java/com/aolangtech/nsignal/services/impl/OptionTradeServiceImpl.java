/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.services.impl;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.aolangtech.nsignal.Application;
import com.aolangtech.nsignal.mappers.OptionTradeMapper;
import com.aolangtech.nsignal.models.OptionTradeModel;
import com.aolangtech.nsignal.services.OptionTradeService;
import com.aolangtech.nsignal.utils.CommonUtil;

public class OptionTradeServiceImpl implements OptionTradeService {
	
	// private static final int MAX_INSERT_RECORDS_NUM = 1000;
	
	private static String tradeTablePrefix = "option_trade_";
	
	private SqlSession session;
	
	/**
	 * Open a session and get a mapper for this session.
	 * 
	 * @return mapper
	 */
	private OptionTradeMapper openMapper() {
		session = Application.sqlSessionFactory.openSession();
		return session.getMapper(OptionTradeMapper.class);
	}
	
	/**
	 * Commit the trasaction and close this session.
	 */
	private void closeMapper() {
		session.commit();
		session.close();
	}
	
	@Override
	public int insertList(List<OptionTradeModel> list) {		
		
		if(list.isEmpty()) {
			return 0;
		}
		else {
			OptionTradeMapper mapper = this.openMapper();
			String quarter = CommonUtil.getQuarterByDay(list.get(0).getEventDay());
			
			int failCount = 0;
			int count = 0;
			
			for(count = 0; count < list.size(); count++) {
				// insert one by one
				try {
					mapper.insert(tradeTablePrefix + quarter, list.get(count));
					session.commit();
				} catch (Exception ex) {
					session.rollback();
					++failCount;
				}			
			}
			
			this.closeMapper();
			return count - failCount;
		}
	}

	/**
	 * Insert records of one day.
	 */
	@Override
	public int insertByMap(Map<String, List<OptionTradeModel>> tradeMap) {
		int persistCount = 0;
		
		// create table if table is not exists
		Integer eventDay = tradeMap.get(tradeMap.keySet().toArray()[0]).get(0).getEventDay();
		String tableName = tradeTablePrefix + CommonUtil.getQuarterByDay(eventDay);
		OptionTradeMapper mapper = this.openMapper();
		mapper.createTable(tableName);
		// clear old records of specified day
		mapper.deleteByEventDay(tableName, eventDay);
		this.closeMapper();
		
		
		// insert trades
		for(List<OptionTradeModel> list : tradeMap.values()) {			
			persistCount += this.insertList(list);

		}
		return persistCount;
	}
}