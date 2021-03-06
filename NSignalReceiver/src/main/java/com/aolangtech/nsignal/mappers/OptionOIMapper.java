package com.aolangtech.nsignal.mappers;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.aolangtech.nsignal.models.OptionOIModel;

public interface OptionOIMapper {

	OptionOIModel selectByPrimaryKey(@Param("tableName") String tableName, @Param("id") Long id);
	
	int deleteByEventDay(@Param("tableName") String tableName, @Param("eventDay") Integer eventDay);
	
	int insertWithoutConflict(@Param("tableName") String tableName, @Param("item") OptionOIModel item);
	
	int insertList(@Param("tableName") String tableName, @Param("recordList") List<OptionOIModel> recordList);
	
	void createTable(@Param("tableName") String tableName);
}