#include "stdafx.h"

// -----------------------------------------------------------------------------------------------------------------------
// -------------------------------------------- NxCoreReader Class -------------------------------------------------------
// -----------------------------------------------------------------------------------------------------------------------

bool NxCoreReader::LoadNxCore(const char*dllPath)
{
	// Load NxCore API dll
	if (nxCoreClass.LoadNxCore(dllPath) ||
		nxCoreClass.LoadNxCore("NxCoreAPI.dll"))
	{
		return true;
	}
	else
	{
		return false;
	}
}

/*
	Get the symbol following OSI or OPRA format from NxCoreMessage.
*/
std::string NxCoreReader::getSymbol(const NxCoreMessage* pNxCoreMsg)
{
	char symbol[OPTION_SYMBOL_MAX_SIZE];

	// Is this a valid option?    
	if ((pNxCoreMsg->coreHeader.pnxStringSymbol->String[0] == 'o') && (pNxCoreMsg->coreHeader.pnxOptionHdr))
	{
		// If pnxsDateAndStrike->String[1] == ' ', then this symbol is in new OSI format. 	
		if (pNxCoreMsg->coreHeader.pnxOptionHdr->pnxsDateAndStrike->String[1] == ' ')
		{
			sprintf_s(symbol, sizeof(symbol), "%s%02d%02d%02d%c%08d",
				pNxCoreMsg->coreHeader.pnxStringSymbol->String,
				pNxCoreMsg->coreHeader.pnxOptionHdr->nxExpirationDate.Year - 2000,
				pNxCoreMsg->coreHeader.pnxOptionHdr->nxExpirationDate.Month,
				pNxCoreMsg->coreHeader.pnxOptionHdr->nxExpirationDate.Day,
				(pNxCoreMsg->coreHeader.pnxOptionHdr->PutCall == 0) ? 'C' : 'P',
				pNxCoreMsg->coreHeader.pnxOptionHdr->strikePrice);
		}
		// Otherwise the symbol is in old OPRA format.
		else
		{
			sprintf_s(symbol, sizeof(symbol), "%s%c%c",
				pNxCoreMsg->coreHeader.pnxStringSymbol->String,
				pNxCoreMsg->coreHeader.pnxOptionHdr->pnxsDateAndStrike->String[0],
				pNxCoreMsg->coreHeader.pnxOptionHdr->pnxsDateAndStrike->String[1]);
		}
	}
	// Not an option, just copy the symbol
	else
	{
		strcpy_s(symbol, sizeof(symbol), pNxCoreMsg->coreHeader.pnxStringSymbol->String);
	}
	std::string ret = symbol;
	return ret;
}


// ***********************************************************************************************
// ****************************** NxCoreTradeReader Class ****************************************
// ***********************************************************************************************

void NxCoreTradeReader::ProcessTradeMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage)
{
	const NxCoreTrade&	nt = pNxCoreMessage->coreData.Trade;
	const NxCoreHeader& ch = pNxCoreMessage->coreHeader;
	const NxDate&		date = pNxCoreSys->nxDate;
	const NxTime&       t = pNxCoreSys->nxTime;

	std::string symbol = this->getSymbol(pNxCoreMessage);
	if (symbol.at(0) != 'o')
	{
		return;
	}

	// get the option info
	auto optInfo = findOrCreateOptionInfo(symbol);

	TradeHint hint = optInfo->GetTradeHint(t.MsOfDay);

	char lineBuffer[200];
	// format trade item 
	sprintf_s(lineBuffer, sizeof(lineBuffer),
		"%d,"		// type of record
		"%.2d-%.2d-%.2d %.2d:%.2d:%.2d.%.3d,"	//date 	// yy-MM-dd hh:mm:ss.lll	
		"%s,"		// symbol
		"%.2lf,"	// price
		"%ld,"		// size

		"%.2lf,"	// last trade price

		"%.2lf,"	// ask price
		"%d,"		// time interval between ask and trade
		"%d,"		// the time gap between this ask and last ask

		"%.2lf,"	// bid price
		"%d,"		// time interval between bid and trade
		"%d,"		// the time gap between this bid and last bid

		"%d,"		// reporting exchange
		"%d,"		// condition
		"%d\n"		// sequenceId
		,

		RECORD_TYPE_TRADE,
		(int)(date.Year % 100), (int)date.Month, (int)date.Day, (int)t.Hour, (int)t.Minute, (int)t.Second, (int)t.Millisecond,
		symbol.c_str(),
		this->nxCoreClass.PriceToDouble(nt.Price, nt.PriceType),
		nt.Size,

		hint.preTrade.price,

		hint.ask.price,
		hint.ask.msOfDay == 0 ? 0 : t.MsOfDay - hint.ask.msOfDay,
		hint.askGap,

		hint.bid.price,
		hint.bid.msOfDay == 0 ? 0 : t.MsOfDay - hint.bid.msOfDay,
		hint.bidGap,

		(int)ch.ReportingExg,
		(int)nt.TradeCondition,
		(int)nt.ExgSequence);

	// output into output stream
	this->outputStream->Write(lineBuffer);

	// stores into pre trade
	TradePair trade;
	trade.price = this->nxCoreClass.PriceToDouble(nt.Price, nt.PriceType);
	trade.msOfDay = t.MsOfDay;
	trade.High = nt.High;
	trade.Last = nt.Last;
	trade.Low = nt.Low;
	trade.Open = nt.Open;
	trade.NetChange = nt.NetChange;
	trade.TotalVolume = nt.TotalVolume;
	trade.Tick = nt.Tick;
	trade.TickVolume = nt.TickVolume;

	// stores pre trade for the next trade
	optInfo->PushTrade(trade);
}
/*
	Stores quotes for calculating trade direction
*/
void NxCoreTradeReader::ProcessQuoteMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage)
{
	QuotePair ask;
	QuotePair bid;

	std::string symbol = this->getSymbol(pNxCoreMessage);
	auto optInfo = findOrCreateOptionInfo(symbol);
	if (symbol.at(0) != 'o')
	{
		return;
	}

	const auto& coreQuote = pNxCoreMessage->coreData.ExgQuote.coreQuote;
	// get ask info
	ask.price = this->nxCoreClass.PriceToDouble(coreQuote.AskPrice, coreQuote.PriceType);
	ask.msOfDay = pNxCoreSys->nxTime.MsOfDay;

	// get bid info
	bid.price = this->nxCoreClass.PriceToDouble(coreQuote.BidPrice, coreQuote.PriceType);
	bid.msOfDay = pNxCoreSys->nxTime.MsOfDay;

	// stores quotes for calculating trade direction
	optInfo->PushAskQuote(ask);
	optInfo->PushBidQuote(bid);

}

/*
	Deal with Category message. (Now just for open interest)
*/
void NxCoreTradeReader::ProcessCategoryMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage) 
{
	const NxDate&	date = pNxCoreSys->nxDate;
	const auto&		category = pNxCoreMessage->coreData.Category;

	std::string symbol = this->getSymbol(pNxCoreMessage);
	if (symbol.at(0) != 'o')
	{
		return;
	}

	switch (category.pnxStringCategory->Atom)
	{
	case 67:
		if (category.pnxFields[0].Set == 1)
		{
			// Open Interest
			int oi = category.pnxFields[0].data.i32Bit;

			char lineBuffer[100];
			// format trade item 
			sprintf_s(lineBuffer, sizeof(lineBuffer),
				"%d,"	// type of record
				"%.2d-%.2d-%.2d,"	// date:yy-MM-dd
				"%s,"	// symbol of option
				"%d\n"	// oi
				,

				RECORD_TYPE_OI,
				(int)(date.Year % 100), (int)date.Month, (int)date.Day,
				symbol.c_str(),
				oi);

			// output into output stream
			this->outputStream->Write(lineBuffer);
		}
		break;

	}

}

bool NxCoreTradeReader::OpenReader()
{
	return this->outputStream->Open();
}
void NxCoreTradeReader::CloseReader()
{
	this->outputStream->Close();
}

// ---------------------------------------------- Private Functions ------------------------------------------------

/*
	Clear the old memory and initialize new memory.
*/
void NxCoreTradeReader::refreshMapMemory()
{
	for (auto it = this->optionInfoMap.begin(); it != this->optionInfoMap.end(); it++)
	{
		delete it->second;
	}
}

/*
	Return the option info of specified symbol. Return the existed one if exist, otherwise creates and initializes one then return it.
*/
inline OptionTradeInfo* NxCoreTradeReader::findOrCreateOptionInfo(const std::string& symbol)
{
	auto optInfoIt = this->optionInfoMap.find(symbol);

	if (optInfoIt == this->optionInfoMap.end())
	{
		// insert one if didn't exist
		optInfoIt = optionInfoMap.insert({ symbol, new OptionTradeInfo(symbol) }).first;
	}
	return optInfoIt->second;
}