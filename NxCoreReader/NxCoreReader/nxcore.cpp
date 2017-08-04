#include"stdafx.h"

#define NSIGNAL_DEBUG_

#ifdef NSIGNAL_DEBUG_

#include<time.h>

#endif

using namespace std;

NxCoreOutputFileStream* pFs = NULL;

NxCoreTradeReader* reader = NULL;

/*
Callback for NxCore for trade
*/
int __stdcall nxCoreCallbackForTrade(const NxCoreSystem* pNxCoreSys, const NxCoreMessage* pNxCoreMessage)
{
	const NxCoreTrade&	nt = pNxCoreMessage->coreData.Trade;
	const NxCoreHeader& ch = pNxCoreMessage->coreHeader;
	const NxDate&		date = pNxCoreSys->nxDate;
	const NxTime&       t = pNxCoreSys->nxTime;

	//NxCoreTradeReader* pReader = (NxCoreTradeReader *)pNxCoreSys->UserData;
	NxCoreTradeReader* pReader = reader;
#ifdef NSIGNAL_DEBUG_
	unsigned static char preSec;
	static clock_t preTime;
	if (t.Second != preSec)
	{
		auto nowTime = clock();
		cout << (int)t.Hour << " : " << (int)t.Minute << " : " << (int)t.Second << "\t" << (nowTime - preTime) << endl;
		preSec = t.Second;
		preTime = nowTime;
	}
#endif


	switch (pNxCoreMessage->MessageType)
	{
	case NxMSG_TRADE:
	{
		if (t.MsOfDay > CLOSE_MARKET_MSOFDAY)
			return NxCALLBACKRETURN_STOP;
		if (t.MsOfDay < OPEN_MARKET_MSOFDAY)
			break;

		pReader->ProcessTradeMsg(pNxCoreSys, pNxCoreMessage);

		break;
	}
	case NxMSG_EXGQUOTE:
	case NxMSG_MMQUOTE:
		if (t.MsOfDay > CLOSE_MARKET_MSOFDAY)
			return NxCALLBACKRETURN_STOP;
		if (t.MsOfDay < OPEN_MARKET_MSOFDAY)
			break;
		pReader->ProcessQuoteMsg(pNxCoreSys, pNxCoreMessage);
		break;
	}
	return NxCALLBACKRETURN_CONTINUE;
}

/*
Process tape named tapeFilename and output a result file named outFilename
*/
int ProcessTapeForTrade(const char* tapeFilename, NxCoreReader* pReader)
{
	if (nullptr == tapeFilename)
		return NSIGNAL_OPEN_READER_FAIL;

	int res;
	try
	{
		// open reader
		if (!pReader->OpenReader())
			return NSIGNAL_OPEN_READER_FAIL;

		// process tape
		res = pReader->nxCoreClass.ProcessTape(tapeFilename, 0, NxCF_EXCLUDE_CRC_CHECK, 0, nxCoreCallbackForTrade);

	}
	catch (bad_alloc& exc)
	{
		cout << "Exception:\t" << exc.what() << endl;
		res = NxAPIERR_EXCEPTION;
	}

	// close reader
	pReader->CloseReader();

	return res;
}

int main(int argc, char** argv)
{
	string tape = "C:\\NxCore\\20170421.DO.nxc";
	string outFilename = "out";

	if (argc == 3)
	{
		tape = argv[1];
		outFilename = argv[2];
	}
	else if (argc == 2)
	{
		tape = argv[1];
		outFilename = tape + ".out";
	}
	clock_t beginTime = clock();

	pFs = new NxCoreOutputFileStream(outFilename);
	reader = new NxCoreTradeReader(pFs);
	if (!reader->LoadNxCore("NxCoreAPI64.dll"))
	{
		cout << "Load NxCoreAPI64.dll failed!" << endl;
		delete reader;
		delete pFs;
		return 0;
	}
	else
	{
		ProcessTapeForTrade(tape.c_str(), reader);
		cout << "Total time is " << (clock() - beginTime) / CLOCKS_PER_SEC / 60 << endl;
		delete reader;
		delete pFs;
		return 1;
	}

}