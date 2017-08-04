#ifndef NSIGNAL_TRADEINFO_H_
#define NSIGNAL_TRADEINFO_H_

#define OPEN_MARKET_MSOFDAY		34200000
#define CLOSE_MARKET_MSOFDAY	57600000

#define QUOTE_TRADE_MS_INTERVAL	5000

#define QUOTEPAIR_LIST_MAX_SIZE	205		// The interval between two quotes from nxcore is at least 25ms.	5 * 1000 / 25 = 200(item)

struct QuotePair
{
	double price = 0;
	unsigned long msOfDay = 0;
};

struct TradePair
{
	double				price = 0;
	unsigned long		msOfDay = 0;
	unsigned __int64	TotalVolume = 0;
	unsigned int		TickVolume = 0;
	int					Open = 0;
	int					High = 0;
	int					Low = 0;
	int					Last = 0;
	int					Tick = 0;
	int					NetChange = 0;
};

struct TradeHint
{
	QuotePair ask;
	unsigned short askGap;
	QuotePair bid;
	unsigned short bidGap;
	TradePair preTrade;
};


class QuotePairList
{
private:
	QuotePair dataset[QUOTEPAIR_LIST_MAX_SIZE];
	unsigned short head = 0;
	unsigned short tail = 0;
	unsigned short gap = 0;		// the time gap between the head and the the head - 1

public:
	bool empty()
	{
		return head == tail;
	}

	bool full()
	{
		return (head % QUOTEPAIR_LIST_MAX_SIZE) == ((tail + 1) % QUOTEPAIR_LIST_MAX_SIZE);
	}

	unsigned short size()
	{
		return (tail - head + QUOTEPAIR_LIST_MAX_SIZE) % QUOTEPAIR_LIST_MAX_SIZE;
	}

	// The caller need to guarantee that the size is not zero
	QuotePair* front()
	{
		return &dataset[head];
	}

	// The caller need to guarantee that the size big than one
	QuotePair* second_front()
	{
		return &dataset[(head + 1) % QUOTEPAIR_LIST_MAX_SIZE];
	}

	// The caller need to guarantee that the size is not zero
	QuotePair* back()
	{
		return &dataset[(tail - 1 + QUOTEPAIR_LIST_MAX_SIZE) % QUOTEPAIR_LIST_MAX_SIZE];
	}

	// The dataset is impossibility fulled.	
	void push_back(const QuotePair& data)
	{
		if (full())
		{
			pop_front();
		}
		dataset[tail++] = data;
		tail %= QUOTEPAIR_LIST_MAX_SIZE;
	}

	// The caller need to guarantee that the size is not zero
	void pop_front()
	{
		head = (head + 1) % QUOTEPAIR_LIST_MAX_SIZE;
	}

	/* 
		Just stores one or zero record whose msOfDay is less than or equal msOfDay.

			Remark: The list is impossiblely empty unless no record has ever been pushed to the list.
	*/
	void shrink(const unsigned long& msOfDay)
	{
		auto listSize = size();
		while (--listSize > 0 && msOfDay - second_front()->msOfDay >= QUOTE_TRADE_MS_INTERVAL)
		{
			pop_front();
		}

		if (size() == 1)
		{
			gap = (unsigned short)(msOfDay - dataset[head].msOfDay);
		}
		else
		{
			gap = (unsigned short)(dataset[(head + 1 + QUOTEPAIR_LIST_MAX_SIZE) % QUOTEPAIR_LIST_MAX_SIZE].msOfDay - dataset[head].msOfDay);
		}
	}

	unsigned short getGap()
	{
		return gap;
	}
};

class OptionTradeInfo
{
public:
	OptionTradeInfo(const std::string& optSymbol);
	~OptionTradeInfo();

public:
	void PushAskQuote(const QuotePair& quote);
	void PushBidQuote(const QuotePair& quote);
	void PushTrade(const TradePair& trade);

public:
	const char* GetSymbol()
	{
		return optSymbol.c_str();
	}
	TradeHint GetTradeHint(const unsigned long& msOfDay);

private:
	void PushQuote2List(const QuotePair& quote, QuotePairList* list);

private:
	QuotePairList	preAskQuotePairList;	// The list storing all pre ask quote pair
	QuotePairList	preBidQuotePairList;	// The list storing all pre bid quote pair
	TradePair		preTrade;				// The list storing the price of previous trade

	std::string		optSymbol;				// The option symbol

};

#endif