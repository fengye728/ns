#include"stdafx.h"

#define NSIGNAL_DEBUG_

#include<time.h>
#include<string>


using namespace std;

const static char* PROPERTY_FILE_NAME = "nsignal.properties";

const static char* PROPERTY_NAME_SERVER_URL = "server.url";

const static char* PROPERTY_NAME_SERVER_PORT = "server.port";

NxCoreOutputStream* pFs = NULL;

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
	unsigned static char preMin;
	static clock_t preTime;
	if (t.Minute != preMin)
	{
		auto nowTime = clock();
		cout << (int)t.Hour << " : " << (int)t.Minute << "\tCost Time:" << (nowTime - preTime) / 1000 << "s" << endl;
		preMin = t.Minute;
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

string trim(char *word)
{
	int size = (int)strlen(word);
	int start = 0;
	int end = size;

	for (int i = 0; i < size; ++i)
	{
		if (word[i] != '\t' && word[i] != ' ')
		{
			start = i;
			break;
		}
	}
	for (int i = size - 1; i >= start; --i)
	{
		if (word[i] == '\t' || word[i] == ' ')
		{
			end = i;
		}
		else
		{
			break;
		}
	}
	string result = word;
	return result.substr(start, end - start);
}

/*
	Read properties from file and set server url and port.

*/
bool readProperties(string& url, unsigned short& port)
{
	const char * split = "=";
	
	// open file
	ifstream infile;
	infile.open(PROPERTY_FILE_NAME);
	if (!infile.is_open())
	{
		return false;
	}

	bool isReadUrl = false;
	bool isReadPort = false;

	string line;
	while (getline(infile, line))
	{
		// TODO cut string
		char buff[1024];
		line.copy(buff, line.size(), 0);
		buff[line.size()] = '\0';
		char * token = strtok(buff, split);
		if (trim(token).compare(PROPERTY_NAME_SERVER_URL) == 0)
		{
			token = strtok(NULL, split);
			url = trim(token);
			isReadUrl = true;
		}
		else if (trim(token).compare(PROPERTY_NAME_SERVER_PORT) == 0)
		{
			token = strtok(NULL, split);
			
			port = atoi(trim(token).c_str());
			isReadPort = true;
		}
	}

	if (!isReadUrl)
	{
		cout << "Property Error:server.url not in properties file:" << PROPERTY_FILE_NAME << endl;
		return false;
	}
	if (!isReadPort)
	{
		cout << "Property Error:server.port not in properties file:" << PROPERTY_FILE_NAME << endl;
		return false;
	}

	// close file
	infile.close();
	return true;
}

bool isFileExist(string filename)
{
	// open file
	ifstream infile;
	infile.open(filename);
	if (!infile.is_open())
	{
		return false;
	}
	else
	{
		infile.close();
		return true;
	}
}

int main(int argc, char** argv)
{
	const char REMOTE[] = "-r";
	const char REAL_TIME[] = "-t";

	bool remoteStatus = false;
	bool realTimeStatus = false;

	string tape = "";
	string outFilename = "";

	int otherArgc = 0;
	string firstArg;
	string secondArg;
	// parse arguments
	for (int i = 1; i < argc; ++i)
	{
		if (strcmp(argv[i], REMOTE) == 0)
		{
			remoteStatus = true;
		}
		else if (strcmp(argv[i], REAL_TIME) == 0)
		{
			realTimeStatus = true;
		}
		else 
		{
			++otherArgc;
			if (otherArgc == 1)
			{
				firstArg = argv[i];
			}
			else if(otherArgc == 2)
			{
				secondArg = argv[i];
			}
		}
	}

	if (remoteStatus)
	{
		// Write output data to remote

		// read properties file
		string serverUrl;
		unsigned short serverPort;

		readProperties(serverUrl, serverPort);

		if (realTimeStatus)
		{
			// Get real-time data
			if (otherArgc != 0)
			{
				printf("Arguments number is wrong: too much arguments!\n");
				return 0;
			}
			tape = "";
		}
		else
		{
			// Get data from file
			if (otherArgc == 1)
			{
				tape = firstArg;
			}
			else if(otherArgc < 1)
			{
				printf("Arguments number is wrong: lact some arguments!\n");
				return 0;
			}
			else
			{
				printf("Arguments number is wrong: too much arguments!\n");
				return 0;
			}
		}
		// get tcp output stream
		pFs = new NxCoreOutputTCPStream(serverUrl, serverPort);
	}
	else
	{
		// Write output data into the file named outFilename

		if (realTimeStatus)
		{
			// Get real-time data
			tape = "";

			if (otherArgc == 1)
			{
				outFilename = firstArg;

			}
			else if (otherArgc == 0)
			{
				// get current time
				char date[9];	// yyyyMMdd
				time_t tt = time(NULL);
				tm t;
				localtime_s(&t, &tt);

				sprintf_s(date, sizeof(date),"%04d%02d%02d",
					t.tm_year + 1900,
					t.tm_mon + 1,
					t.tm_mday);

				outFilename = date;
				outFilename.append(".DO.nxc.trade");
			}
			else
			{
				printf("Arguments number is wrong!\n");
				return 0;
			}
		}
		else
		{
			// Get data from file
			if (otherArgc == 1)
			{
				tape = firstArg;

				outFilename = tape + ".trade";
			}
			else if (otherArgc == 2)
			{
				tape = firstArg;
				outFilename = secondArg;
			}
			else
			{
				printf("Arguments number is wrong!\n");
				return 0;
			}

			// check if the tape file exist
			fstream tapeStream;
			tapeStream.open(tape, std::ios::in);
			if (!tapeStream)
			{
				cout << tape.c_str() << " is not existed!" << endl;
				tapeStream.close();
				return 0;
			}

		}
		// get file output stream
		pFs = new NxCoreOutputFileStream(outFilename);
	}

	// check if tape file exist
	if (!realTimeStatus)
	{
		if (!isFileExist(tape))
		{
			cout << tape << " is not existed!" << endl;
			return 0;
		}
	}

	clock_t beginTime = clock();

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
		int result = ProcessTapeForTrade(tape.c_str(), reader);
		switch (result)
		{
		case NSIGNAL_OPEN_READER_FAIL:
			cout << "Fail to open reader!" << endl;
			break;
		default:
			cout << "Total time is " << (clock() - beginTime) / CLOCKS_PER_SEC / 60 << endl;
		}
		delete reader;
		delete pFs;
		return 1;
	}

}