#pragma once

class NxCoreOutputStream
{
public:
	virtual bool Open() = 0;
	virtual int Write(const char* line) = 0;
	virtual void Close() = 0;

};

class NxCoreOutputFileStream : public NxCoreOutputStream
{
public:
	NxCoreOutputFileStream(const std::string& filename)
	{
		this->outFilename = filename;
	}

public:
	virtual bool Open()
	{
		this->outStream.open(outFilename, std::ios::out);
		if (!this->outStream.is_open())
			return false;
		else
		{
			return true;
		}
	}
	virtual int Write(const char* line)
	{
		outStream << line;
		return (int)strlen(line);
	}

	virtual void Close()
	{
		outStream.close();
	}

private:
	std::string outFilename;	// The output file name
	std::ofstream outStream;	// The stream for outputing result file
};

class NxCoreOutputTCPStream : public NxCoreOutputStream
{
private:
	SOCKET sockClient;
	std::string serverUrl;
	unsigned short serverPort;

public:
	NxCoreOutputTCPStream(const std::string& serverUrl, unsigned short serverPort)
	{
		this->serverUrl = serverUrl;
		this->serverPort = serverPort;
	}

public:
	virtual bool Open()
	{
		WSADATA wsaData;

		if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0)
		{
			printf("Failed to load Winsock\n");
			return true;
		}

		SOCKADDR_IN addrSrv;
		addrSrv.sin_family = AF_INET;
		addrSrv.sin_port = htons(this->serverPort);
		addrSrv.sin_addr.S_un.S_addr = inet_addr(this->serverUrl.c_str());

		sockClient = socket(AF_INET, SOCK_STREAM, 0);

		if (SOCKET_ERROR == sockClient)
		{
			printf("Socket() error: %d", WSAGetLastError());
			return false;
		}
		if (connect(sockClient, (sockaddr*)&addrSrv, sizeof(addrSrv)) == INVALID_SOCKET)
		{
			printf("Connect failed: %d", WSAGetLastError());
			return false;
		}		
		return true;
	}

	virtual int Write(const char* line)
	{
		return send(this->sockClient, line, (int)strlen(line), 0);
	}

	virtual void Close()
	{
		closesocket(this->sockClient);
		WSACleanup();
	}
};