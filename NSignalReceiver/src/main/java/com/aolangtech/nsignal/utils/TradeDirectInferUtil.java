/**
 * 
 */
/**
 * @author AOLANG
 *
 */
package com.aolangtech.nsignal.utils;

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
			return ;
		}
		
		if(lastTrade.getAskPrice() < curTrade.getPrice()) {
			curTrade.setTickTestTD(TickTestTradeCategory.UPTICK);
		}
		else if(lastTrade.getAskPrice() > curTrade.getPrice()) {
			curTrade.setTickTestTD(TickTestTradeCategory.DOWNTICK);
		}
		else {
			switch(lastTrade.getTickTestTD()) {
			case DOWNTICK:
			case ZERO_DOWNTICK:
				curTrade.setTickTestTD(TickTestTradeCategory.ZERO_DOWNTICK);
				break;
			case ZERO_UPTICK:
			case UPTICK:
				curTrade.setTickTestTD(TickTestTradeCategory.ZERO_UPTICK);
				break;
			case UNKNOWN:
				curTrade.setTickTestTD(TickTestTradeCategory.UNKNOWN);
				break;
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