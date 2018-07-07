#ifndef NSIGNAL_READER_H_
#define NSIGNAL_READER_H_

#define OPTION_SYMBOL_MAX_SIZE		23
#define OPTION_MAX_TRADE_GAP		7

#define	NSIGNAL_OPEN_READER_FAIL	-14

// The type of record
#define	RECORD_TYPE_TRADE			1
#define	RECORD_TYPE_OI				2
#define	RECORD_TYPE_TRADE_FINISH	3
#define	RECORD_TYPE_OI_FINISH		4

// End time of message
#define TIME_OI_END_MS		10800000		// 3 * 60 * 60 * 1000
#define TIME_TRADE_END_MS	57600000				// 16 * 60 * 60 * 1000

class NxCoreReader
{
public:
	virtual void ProcessTradeMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage) {}
	virtual void ProcessQuoteMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage) {}
	virtual void ProcessCategoryMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage) {}

public:
	virtual bool OpenReader() = 0;
	virtual void CloseReader() = 0;

public:
	virtual std::string getSymbol(const NxCoreMessage* pNxCoreMsg);

public:
	bool LoadNxCore(const char*dllPath);

public:
	NxCoreClass nxCoreClass;
};


class NxCoreTradeReader : public NxCoreReader
{
public:
	virtual void ProcessTradeMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage);
	virtual void ProcessQuoteMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage);
	virtual void ProcessCategoryMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage);

	virtual bool OpenReader();
	virtual void CloseReader();

	NxCoreTradeReader(NxCoreOutputStream* outputStream)
	{
		this->outputStream = outputStream;
	}

	~NxCoreTradeReader()
	{
		this->refreshMapMemory();
	}

public:
	char RECORD_SEPARATOR = ',';

private:
	OptionTradeInfo* findOrCreateOptionInfo(const std::string& symbol);
	void refreshMapMemory();

private:
	std::unordered_map<std::string, OptionTradeInfo*> optionInfoMap;	// The map storing all keys for symbol and value for pre ask quote pair
	NxCoreOutputStream* outputStream;				// The output stream
};

#endif