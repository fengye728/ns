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
	
	private static final int MAX_INSERT_RECORDS_NUM = 1000;
	
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
			
			int count = 0;
			int left = list.size();
			
			while(left > 0) {
				int newCount = mapper.insertList(tradeTablePrefix + quarter, list.subList(count, left < MAX_INSERT_RECORDS_NUM ? count + left : count + MAX_INSERT_RECORDS_NUM));
				count += newCount;
				left -= newCount;
			}
			
			this.closeMapper();
			return count;
		}
	}

	@Override
	public int insertByMap(Map<String, List<OptionTradeModel>> tradeMap) {
		int persistCount = 0;
		
		// create table if table is not exists
		Integer eventDay = tradeMap.get(tradeMap.keySet().toArray()[0]).get(0).getEventDay();
		String tableName = tradeTablePrefix + CommonUtil.getQuarterByDay(eventDay);
		OptionTradeMapper mapper = this.openMapper();
		mapper.createTable(tableName);
		this.closeMapper();
		
		// insert trades
		for(List<OptionTradeModel> list : tradeMap.values()) {			
			persistCount += this.insertList(list);

		}
		return persistCount;
	}
}