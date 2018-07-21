package com.aolangtech.nsignal.services.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.aolangtech.nsignal.Application;
import com.aolangtech.nsignal.mappers.OptionOIMapper;
import com.aolangtech.nsignal.models.OptionOIModel;
import com.aolangtech.nsignal.services.OptionOIService;
import com.aolangtech.nsignal.utils.CommonUtil;

public class OptionOIServiceImpl implements OptionOIService {
	
	// private static final int MAX_INSERT_RECORDS_NUM = 1000;
	
	private static String oiTablePrefix = "option_open_interest_";
	
	private SqlSession session;
	
	/**
	 * Open a session and get a mapper for this session.
	 * 
	 * @return mapper
	 */
	private OptionOIMapper openMapper() {
		session = Application.sqlSessionFactory.openSession();
		return session.getMapper(OptionOIMapper.class);
	}
	
	/**
	 * Commit the trasaction and close this session.
	 */
	private void closeMapper() {
		session.commit();
		session.close();
	}
	
	/**
	 * Get quarters contained.
	 * 
	 * @param collection
	 * @return
	 */
	public static Set<String> getContainQuarters(Collection<OptionOIModel> collection) {
		Set<String> quarters = new HashSet<>();
		
		for( OptionOIModel item : collection) {
			quarters.add(CommonUtil.getQuarterByDay(item.getEventDay()));
		}
		return quarters;
	}
	
	/**
	 * Insert a list of oi of one day records into database. Create table if it not exists
	 */
	@Override
	public int insertList(List<OptionOIModel> list) {		
		
		if(list.isEmpty()) {
			return 0;
		}
		else {
			
			// create table if not exists
			OptionOIMapper mapper = this.openMapper();
			Set<String> quarters = getContainQuarters(list);
			for(String quarter : quarters) {
				String tableName = oiTablePrefix + quarter;
				mapper.createTable(tableName);
				session.commit();
			}
			this.closeMapper();
			
			// insert records
			mapper = this.openMapper();
			int count = 0;
			int failCount = 0;
			for(count = 0; count < list.size(); count++) {
				try{
					String tableName = oiTablePrefix + CommonUtil.getQuarterByDay(list.get(count).getEventDay());
					mapper.insertWithoutConflict(tableName, list.get(count));
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
}
