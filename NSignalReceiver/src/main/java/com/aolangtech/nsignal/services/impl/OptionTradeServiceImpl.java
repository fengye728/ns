/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.services.impl;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.aolangtech.nsignal.Application;
import com.aolangtech.nsignal.mappers.OptionTradeMapper;
import com.aolangtech.nsignal.models.OptionTradeModel;
import com.aolangtech.nsignal.services.OptionTradeService;

public class OptionTradeServiceImpl implements OptionTradeService {
	
	private static final int MAX_INSERT_RECORDS_NUM = 1000;
	
	private static String tablePrefix = "option_trade_";
	
	private SqlSession session;
	
	@Override
	public int insertList(List<OptionTradeModel> list) {		
		
		if(list.isEmpty()) {
			return 0;
		}
		else {
			OptionTradeMapper mapper = this.openMapper();
			String quarter = list.get(0).getQuarter();
			
			int count = 0;
			int left = list.size();
			
			while(left > 0) {
				int newCount = mapper.insertList(tablePrefix + quarter, list.subList(count, left < MAX_INSERT_RECORDS_NUM ? count + left : count + MAX_INSERT_RECORDS_NUM));
				count += newCount;
				left -= newCount;
			}
			
			this.closeMapper();
			return count;
		}
	}
	
	
	private OptionTradeMapper openMapper() {
		session = Application.sqlSessionFactory.openSession();
		return session.getMapper(OptionTradeMapper.class);
	}
	
	private void closeMapper() {
		session.commit();
		session.close();
	}
}