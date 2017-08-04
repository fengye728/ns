#include"stdafx.h"

// -----------------------------------------------------------------------------------------------------------------------
// ------------------------------------------- OptionTradeInfo Class -----------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------------

// ------------- Public Functions ------------------------------------
OptionTradeInfo::OptionTradeInfo(const std::string& optSymbol)
{
	this->optSymbol = optSymbol;
}
OptionTradeInfo::~OptionTradeInfo()
{

}

/*
Add a ask quote into previous quote list that just stores previous [QUOTE_TRADE_MS_INTERVAL] time interval quotes plus one whose time last closing to this quote (time - minMSInterval).
*/
void OptionTradeInfo::PushAskQuote(const QuotePair& quote)
{
	this->PushQuote2List(quote, &(this->preAskQuotePairList));
}

/*
Add a bid quote into previous quote list that just stores previous [QUOTE_TRADE_MS_INTERVAL] time interval quotes plus one whose time last closing to this quote (time - minMSInterval).
*/
void OptionTradeInfo::PushBidQuote(const QuotePair& quote)
{
	this->PushQuote2List(quote, &(this->preBidQuotePairList));
}

/*
Add previous trade information.
*/
void OptionTradeInfo::PushTrade(const TradePair& trade)
{
	this->preTrade = trade;
}

// --------------------------Private Functions ----------------------------------------------
/*
Add the new pair into list, and deal with superfluous pair.
*/
void OptionTradeInfo::PushQuote2List(const QuotePair& quote, QuotePairList* list)
{
	if (quote.price > 0)
	{
		if (list->empty())	// when list is empty
		{
			list->push_back(quote);
		}
		else
		{
			// deal with same time and the same class quote
			auto pBackPair = list->back();
			if (quote.msOfDay == pBackPair->msOfDay)
			{
				pBackPair->price = quote.price;
			}
			else
			{
				// push in back
				list->push_back(quote);
			}
		}
	}
}

/*
Get pre trade info and coresponding ask info and bid info.
*/
TradeHint OptionTradeInfo::GetTradeHint(const unsigned long& msOfDay)
{
	TradeHint result;

	// get pre trade
	result.preTrade = preTrade;

	// get ask
	preAskQuotePairList.shrink(msOfDay);
	// all will be setted to 0 if preAskQuotePairList is empty
	result.ask = *preAskQuotePairList.front();
	result.askGap = preAskQuotePairList.getGap();

	// get bid
	preBidQuotePairList.shrink(msOfDay);
	// all will be setted to 0 if preBidQuotePairList is empty
	result.bid = *preBidQuotePairList.front();
	result.bidGap = preBidQuotePairList.getGap();

	return result;
}