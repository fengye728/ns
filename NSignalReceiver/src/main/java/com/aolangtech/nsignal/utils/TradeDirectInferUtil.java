/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.utils;

import com.aolangtech.nsignal.models.OptionTradeModel;

public class TradeDirectInferUtil {
	
	/**
	 * Set the trade direction inferred by bid-ask quote based method.
	 * 
	 * 	Remark: -100 ~ 0: Sell; 0 ~ 100: Buy.
	 * 
	 * @param record
	 */
	public static void bidAskTest(OptionTradeModel record) {
		
		if(record.getPrice() <= record.getBidPrice())
			record.setBidAskTD(-100);
		else if(record.getPrice() >= record.getAskPrice()) {
			record.setBidAskTD(100);
		} else {
			int ask = (int)(record.getAskPrice() * 100);
			int bid = (int)(record.getBidPrice() * 100);
			int price = (int)(record.getPrice() * 100);

			int td = (2 * price - ask - bid) * 100 / (ask - bid);
			record.setBidAskTD(td);
		}
	}
}