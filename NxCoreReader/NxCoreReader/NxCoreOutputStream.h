#pragma once

class NxCoreOutputStream
{
public:
	virtual bool Open() = 0;
	virtual size_t Write(const char* line) = 0;
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
	virtual size_t Write(const char* line)
	{
		outStream << line;
		return strlen(line);
	}

	virtual void Close()
	{
		outStream.close();
	}

private:
	std::string outFilename;	// The output file name
	std::ofstream outStream;	// The stream for outputing result file
};