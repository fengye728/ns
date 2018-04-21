package com.aolangtech.nsignal.services.impl;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	 * Get expected oi event_day
	 * 
	 * @param collection
	 * @return
	 */
	public static int getExpectedOIEventDay(Collection<OptionOIModel> collection) {
		int maxEventDay = 0;
		Map<Integer, Long> collect = collection.stream().collect(Collectors.groupingBy(OptionOIModel::getEventDay, Collectors.counting()));
		Long maxCount = 0L;
		for(Integer eventDay : collect.keySet()) {
			if(collect.get(eventDay) > maxCount) {
				maxEventDay = eventDay;
				maxCount = collect.get(eventDay);
			}
		}
		return maxEventDay;
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
			// filter oi records
			final int targetEventDay = getExpectedOIEventDay(list);
			list = list.stream().filter( oiModel -> oiModel.getEventDay() >= targetEventDay).collect(Collectors.toList());

			// create table if not exists
			OptionOIMapper mapper = this.openMapper();
			String tableName = oiTablePrefix + CommonUtil.getQuarterByDay(targetEventDay);
			mapper.createTable(tableName);
			
			// clear old records of specified day
			mapper.deleteByEventDay(tableName, targetEventDay);
			this.closeMapper();
			
			// insert records
			mapper = this.openMapper();
			int count = 0;
			int failCount = 0;
			
			for(count = 0; count < list.size(); count++) {
				try{
					
					mapper.insert(tableName, list.get(count));
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
