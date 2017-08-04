#ifndef NSIGNAL_READER_H_
#define NSIGNAL_READER_H_

#define OPTION_SYMBOL_MAX_SIZE		23

#define	NSIGNAL_OPEN_READER_FAIL	-14

class NxCoreReader
{
public:
	virtual void ProcessTradeMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage) {}
	virtual void ProcessQuoteMsg(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage) {}

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