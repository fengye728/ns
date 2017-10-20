#include<fstream>
#include<string>
#include<vector>
#include<list>
#include"PaperOne.h"

#define	PASCAL_DELIM_STR	"\t \n();"

#define	TYPE_BEGIN_END	1
#define	TYPE_IF_THEN	2

#define	LEVEL_START		1	//	keys to start pair
#define	LEVEL_CLOSE		2	//	keys to close pair;
#define LEVEL_FOLLOWED	3	//	keys may followed by a optional key
#define LEVEL_OPTION	4	//	optional keys

using namespace std;

struct Keyword
{
	Keyword(char *word, int type, int level)
	{
		this->word = word;
		this->type = type;
		this->level = level;
	}

	char* word;		// the keyword string
	int type;		// type of keyword pair
	int level;
};

vector<Keyword> InitPascalKeywords()
{
	vector<Keyword> keywords;

	Keyword keyBegin("begin", TYPE_BEGIN_END, LEVEL_START);
	Keyword keyEnd("end", TYPE_BEGIN_END, LEVEL_CLOSE);

	Keyword keyIf("if", TYPE_IF_THEN, LEVEL_START);
	Keyword keyThen("then", TYPE_IF_THEN, LEVEL_FOLLOWED);
	Keyword keyElse("else", TYPE_IF_THEN, LEVEL_OPTION);

	keywords.push_back(keyBegin);
	keywords.push_back(keyEnd);

	keywords.push_back(keyIf);
	keywords.push_back(keyThen);
	keywords.push_back(keyElse);

	return keywords;
}


const Keyword* FindKeyWord(vector<Keyword>& keywords, const string& word)
{
	for (vector<Keyword>::iterator it = keywords.begin(); it != keywords.end(); it++)
	{
		if (strcmp(it->word, word.c_str()) == 0) 
		{
			return &(*it);
		}
	}
	return NULL;
}

list<string> split(const string& content, const string& delim)
{
	list<string> words;

	const int len = content.length();
	char* buffer = new char[len + 1];
	strcpy(buffer, content.c_str());
	char *strToken = strtok(buffer, delim.c_str());
	while (strToken != NULL)
	{
		words.push_back(strToken);
		strToken = strtok(NULL, delim.c_str());
	}

	delete [] buffer;

	return words;
}

void CloseFollowedPair(list<Keyword>& stack)
{
	while (stack.size() >= 2)
	{
		Keyword followed = stack.back();
		stack.pop_back();
		Keyword start = stack.back();
		
		if (followed.level == LEVEL_FOLLOWED && start.level == LEVEL_START) 
		{
			stack.pop_back();
		}
		else
		{
			stack.push_back(followed);
			return ;
		}
	}
}
bool AddNewWord2Stack(list<Keyword>& stack, const Keyword* pKeyword)
{
	if (pKeyword->level == LEVEL_START)
	{
		// push this keyword into stack
		stack.push_back(*pKeyword);
		return true;
	}

	if (pKeyword->level == LEVEL_CLOSE)
	{
		// close the followed pair
		CloseFollowedPair(stack);
		if (stack.empty())
		{
			return false;
		}
		if (stack.back().type == pKeyword->type && stack.back().level == LEVEL_START)
		{
			// pop start level
			stack.pop_back();
			return true;
		}
		else
		{
			return false;
		}
	}
	else if (pKeyword->level == LEVEL_FOLLOWED)
	{
		if (stack.empty())
		{
			return false;
		}
		if (stack.back().type == pKeyword->type && stack.back().level == LEVEL_START)
		{
			stack.push_back(*pKeyword);
			return true;
		}
		else
		{
			return false;
		}
	}
	else if (pKeyword->level == LEVEL_OPTION)
	{
		if (stack.empty())
		{
			return false;
		}
		if (stack.back().type == pKeyword->type && stack.back().level == LEVEL_FOLLOWED)
		{
			// pop followed level
			stack.pop_back();
			// pop start level
			stack.pop_back();
			return true;
		}
		else
		{
			return false;
		}
	}
	else 
	{
		return false;
	}
}
/*
	Check the pascal code.

	Return the line number error occured. otherwies 0.

*/
int CheckPascal(string filename)
{
	ifstream file;
	file.open(filename, ios::in);

	if (!file.is_open())
	{
		throw new exception("File not found!");
	}
	else
	{
		vector<Keyword> keywords = InitPascalKeywords();
		list<Keyword> stack;
		string line;
		int lineNum = 0;
		while (getline(file, line))
		{
			++lineNum;
			// deal with each line
			list<string> words = split(line, PASCAL_DELIM_STR);

			for (list<string>::iterator it = words.begin(); it != words.end(); it++)
			{
				// check if the word is keyword
				const Keyword* pKeyword = FindKeyWord(keywords, *it);
				if (pKeyword != NULL)
				{
					// the word is keyword
					if (AddNewWord2Stack(stack, pKeyword) == false)
					{
						return lineNum;
					}

				}
	
			}
		}

		CloseFollowedPair(stack);
		if (stack.empty())
		{
			return 0;	// correct
		}
		else
		{
			return lineNum;
		}
	}
	
}