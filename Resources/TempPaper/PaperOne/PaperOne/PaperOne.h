#pragma once

int CheckPascal(std::string statement);

#define	GENE_NOT_EXIT_CHAR	'0'
#define	GENE_EXIST_CHAR		'1'

class Population
{
public:

	virtual bool CanLive(std::string chrom) = 0;

	virtual int GetFitness(std::string chrom) = 0;

	virtual int GetGenePoolSize() = 0;
};

class GoodsPopulation : public Population
{
public:

	virtual bool CanLive(std::string chrom)
	{
		int volume = 0;
		int weight = 0;
		for (int i = chrom.size() - 1; i >= 0; --i)
		{
			if (chrom.at(i) == GENE_EXIST_CHAR)
			{
				volume += this->genePool[i][1];
				weight += this->genePool[i][2];
			}
		}
		if (volume > this->maxVolume || weight > this->maxWeight) 
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	virtual int GetFitness(std::string chrom)
	{
		int value = 0;
		for (int i = chrom.size() - 1; i >= 0; --i)
		{
			if (chrom.at(i) == GENE_EXIST_CHAR)
			{
				value += this->genePool[i][0];
			}
		}
		return value;
	}

	virtual int GetGenePoolSize()
	{
		return genePool.size();
	}

public:
	GoodsPopulation(std::vector<int*>& genePool, int maxVolume, int maxWeight)
	{
		this->genePool = genePool;
		this->maxVolume = maxVolume;
		this->maxWeight = maxWeight;
	}

private:
	std::vector<int*> genePool;
	int maxVolume;
	int maxWeight;
};

class GeneticAlgo
{
public:

	std::string Generate();

public:
	GeneticAlgo(Population *pPopulation, int originEntityNum, int maxGenerations, double crossoverProb, double mutateProb)
	{
		this->pPopulation = pPopulation;
		this->originEntityNum = originEntityNum;
		this->maxGenerations = maxGenerations;
		this->crossoverProb = crossoverProb;
		this->mutateProb = mutateProb;
	}

private:
	std::vector<std::string> initOriginEntities();

	std::vector<std::string> selection(std::vector<std::string> entities);

	void GeneticAlgo::crossover(std::string entityPapa, std::string entiryMama, std::string& child1, std::string child2);

	std::string mutate(std::string entity);

	int rouletteSelect(int* fitnesses, int totalFitness, int size);

private:
	Population* pPopulation;
	int originEntityNum;
	int maxGenerations;
	double crossoverProb;
	double mutateProb;
};