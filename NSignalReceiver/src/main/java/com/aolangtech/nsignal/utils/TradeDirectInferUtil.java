/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.utils;

import java.util.List;

import com.aolangtech.nsignal.constants.CommonConstants.TickTestTradeCategory;
import com.aolangtech.nsignal.models.OptionTradeModel;

public class TradeDirectInferUtil {
	
	/**
	 * Set the trade direction inferred by tick test method.
	 * 
	 * @param lastTrade
	 * @param curTrade
	 */
	public static void tickTest(OptionTradeModel lastTrade, OptionTradeModel curTrade) {
		if(null == lastTrade) {
			curTrade.setTickTestTD(TickTestTradeCategory.UNKNOWN);
		}
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
	public static void bidAskTest(OptionTradeModel record) {
		double mid = (record.getAskPrice() + record.getBidPrice()) / 2;
		record.setBidAskTD((int)((record.getPrice() - mid) / mid * 100));
	}
	
	public static void findTradeLeg(List<OptionTradeModel> list) {
		
	}
}