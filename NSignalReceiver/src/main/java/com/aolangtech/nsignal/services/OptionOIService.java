package com.aolangtech.nsignal.services;

import java.util.List;

import com.aolangtech.nsignal.models.OptionOIModel;

public interface OptionOIService {
	
	/**
	 * Insert a list of OptionOIModels whose event date is same with each other into database.
	 * @param list
	 * @return
	 */
	int insertList(List<OptionOIModel> list);
}
