/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.utils;

import com.aolangtech.nsignal.constants.CommonConstants.TickTestTradeCategory;
import com.aolangtech.nsignal.model.OptionTradeRecordModel;

public class TradeDirectInferUtil {
	
	public static void tickTest(OptionTradeRecordModel lastTrade, OptionTradeRecordModel curTrade) {
		if(lastTrade.getAskPrice() < curTrade.getPrice()) {
			curTrade.setTickTestTD(TickTestTradeCategory.UPTICK);
		}
		else if(lastTrade.getAskPrice() > curTrade.getPrice()) {
			curTrade.setTickTestTD(TickTestTradeCategory.DOWNTICK);
		}
		else {
			if(lastTrade.getTickTestTD().equals(TickTestTradeCategory.DOWNTICK) || lastTrade.getTickTestTD().equals(TickTestTradeCategory.ZERO_DOWNTICK)) {
				curTrade.setTickTestTD(TickTestTradeCategory.ZERO_DOWNTICK);
			}
			else {
				curTrade.setTickTestTD(TickTestTradeCategory.ZERO_UPTICK);
			}
		}
	}
	
	/**
	 * Set the trade direction inferred by bid-ask quote based method.
	 * 
	 * 	Remark: -100 ~ 0: Sell; 0 ~ 100: Buy.
	 * 
	 * @param record
	 */
	public static void bidAskTest(OptionTradeRecordModel record) {
		double mid = (record.getAskPrice() + record.getBidPrice()) / 2;
		record.setBidAskTD((int)((record.getPrice() - mid) / mid * 100));
	}
}