#include<iostream>
#include<fstream>
#include<string>
#include<vector>
#include"PaperOne.h"

using namespace std;


void testPascal()
{
	char *filename = ".\\pascal";
	try
	{
		int lineNum = CheckPascal(filename);
		if (lineNum == 0)
		{
			cout << "The pascal code is corrected!" << endl;
		}
		else
		{
			cout << "Syntax error in line: " << lineNum << endl;
		}
	}
	catch (exception)
	{
		cout << "Error: File " << filename << " not existed!" << endl;
	}
}

void testGenericAlgo()
{
	char *filename = ".\\package";
	const int MAX_VOLUME = 75;
	const int MAX_WEIGHT = 80;

	const int ORIGIN_ENTITY_NUM = 10;
	const int MAX_GENERATIONS = 50;
	const double CROSSOVER_PROB = 0.8;
	const double MUTATE_PROB = 0.001;
	

	vector<int *> records;

	// load records
	ifstream file;
	file.open(filename, ios::in);
	if (!file.is_open())
	{
		cout << filename << " not found!" << endl;
		return;
	}
	else
	{
		while (!file.eof())
		{
			int *pItem = new int[3];
			file >> pItem[0];
			file >> pItem[1];
			file >> pItem[2];
			records.push_back(pItem);
		}
	}

	GoodsPopulation pop(records, MAX_VOLUME, MAX_WEIGHT);

	GeneticAlgo algo(&pop, ORIGIN_ENTITY_NUM, MAX_GENERATIONS, CROSSOVER_PROB, MUTATE_PROB);

	algo.Generate();

	// free memory
	for (auto it = records.begin(); it != records.end(); it++)
	{
		delete[](*it);
	}
}

int main(int argc, char* argv[]) 
{
	testPascal();
	testGenericAlgo();
}