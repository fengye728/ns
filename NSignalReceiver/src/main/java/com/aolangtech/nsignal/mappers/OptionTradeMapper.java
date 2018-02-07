/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.aolangtech.nsignal.models.OptionTradeModel;

public interface OptionTradeMapper {
	
	OptionTradeModel selectByPrimaryKey(@Param("tableName") String tableName, @Param("id") Long id);
	
	int deleteByEventDay(@Param("tableName") String tableName, @Param("eventDay") Integer eventDay);
	
	int insert(@Param("tableName") String tableName, @Param("item") OptionTradeModel record);
	
	int insertList(@Param("tableName") String tableName, @Param("recordList") List<OptionTradeModel> recordList);
	
	void createTable(@Param("tableName") String tableName);
}