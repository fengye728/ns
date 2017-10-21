#include<list>
#include<vector>
#include<string>
#include<iostream>
#include"PaperOne.h"

#define RAND_PRECENT()	(rand() % 10000 / 10000.0)

using namespace std;

// -------------------------- GeneticAlgo Public Functions --------------------
std::string GeneticAlgo::Generate()
{
	std::vector<std::string> oldGeneration;
	std::vector<std::string> newGeneration;

	std::string bestEntity;
	int bestFitness = 0;
	int bestGenerationNum = 0;

	// loop for best
	for (int i = 0; i < this->maxGenerations; ++i)
	{
		if (i == 0)
		{
			// init origin generation
			this->initOriginEntities(oldGeneration);
			newGeneration = oldGeneration;
		}
		else
		{
			// select new from old
			this->selection(oldGeneration, newGeneration);
		}

		// find best fitness
		for (auto it = newGeneration.begin(); it != newGeneration.end(); it++)
		{
			int fitness = this->pPopulation->GetFitness(*it);

			if (fitness > bestFitness)
			{
				bestFitness = fitness;
				bestEntity = *it;
				bestGenerationNum = i;
			}
		}
		// For debug
#ifdef PAPER_DEBUG
		if (i % 100 == 0)
		{
			cout << "Loop in " << i  << "\tBest fit: " << bestFitness << endl;
		}
#endif
	}
	cout << "Best: GenerationNum - " << bestGenerationNum << ", Fitness - " << bestFitness << ", Entity - " << bestEntity << endl;
	return bestEntity;
}

// -------------------------- GeneticAlgo Private Functions -------------------
void GeneticAlgo::initOriginEntities(std::vector<std::string>& generation)
{
	const int GENE_POOL_SIZE = this->pPopulation->GetGenePoolSize();
	// allocate memory
	char *pChrom = new char[GENE_POOL_SIZE + 1];

	// init
	generation.clear();

	// For debug
#ifdef PAPER_DEBUG
	int geneCount[32] = {};
#endif

	while (generation.size() < this->originEntityNum)
	{
		// init chrom
		for (int i = 0; i <= GENE_POOL_SIZE; ++i)
		{
			pChrom[i] = GENE_NOT_EXIT_CHAR;
		}
		pChrom[GENE_POOL_SIZE] = '\0';

		// random set gene until this entity can not live or reach max size
		int randomPos = 0;
		bool canLive = true;

		for (int j = 0; j < GENE_POOL_SIZE && canLive; ++j)
		{
			randomPos = rand() % GENE_POOL_SIZE;
			pChrom[randomPos] = GENE_EXIST_CHAR;

			canLive = this->pPopulation->CanLive(pChrom);

#ifdef PAPER_DEBUG
			geneCount[randomPos] += 1;
#endif
		}

		// reset the last random position if this entity can not live
		if (!canLive)
		{
			pChrom[randomPos] = GENE_NOT_EXIT_CHAR;
		}
		
		// check if the entity already existed
		auto it = std::find(generation.begin(), generation.end(), pChrom);
		if (it == generation.end())
		{
			generation.push_back(pChrom);
		}

	}

	// free memory
	delete pChrom;
}
void GeneticAlgo::selection(const std::vector<std::string>& srcEntities, std::vector<std::string>& newGeneration)
{
	// init
	int size = srcEntities.size();
	int totalFitness = 0;
	newGeneration.clear();

	// allocate memory
	int* fitnesses = new int[size];

	// calc fitness
	for (int i = 0; i < size; ++i)
	{
		fitnesses[i] = this->pPopulation->GetFitness(srcEntities[i]);
		totalFitness += fitnesses[i];
	}


	// generate newGeneration 
	while(newGeneration.size() < size)
	{
		// select parents
		int parentPapa = rouletteSelect(fitnesses, totalFitness, size);
		int parentMama = rouletteSelect(fitnesses, totalFitness, size);

		std::string child1;
		std::string child2;

		// check if crossover
		if (RAND_PRECENT() <= this->crossoverProb)
		{
			// crossover
			this->crossover(srcEntities.at(parentPapa), srcEntities.at(parentMama), child1, child2);
		}
		else
		{
			child1 = srcEntities.at(parentPapa);
			child2 = srcEntities.at(parentMama);
		}

		// check if mutate
		if (RAND_PRECENT() <= this->mutateProb)
		{
			child1 = this->mutate(child1);
		}
		if (RAND_PRECENT() <= this->mutateProb)
		{
			child2 = this->mutate(child2);
		}

		// add new children

		// check if the children can live and already existed
		if (this->pPopulation->CanLive(child1))
		{
			newGeneration.push_back(child1);
			/*
			auto it = std::find(newGeneration.begin(), newGeneration.end(), child1);
			if (it == newGeneration.end())
			{
				newGeneration.push_back(child1);
			}
			*/
		}
		
		if (this->pPopulation->CanLive(child2))
		{
			newGeneration.push_back(child2);
			/*
			auto it = std::find(newGeneration.begin(), newGeneration.end(), child2);
			if (it == newGeneration.end())
			{
				newGeneration.push_back(child2);
			}
			*/

		}
	}
	// free memory
	delete[] fitnesses;
}

void GeneticAlgo::crossover(const std::string& entityPapa, const std::string& entityMama, std::string& child1, std::string& child2)
{
	int pos = rand() % this->pPopulation->GetGenePoolSize();
	child1 = entityPapa.substr(0, pos) + entityMama.substr(pos);
	child2 = entityMama.substr(0, pos) + entityPapa.substr(pos);
}

std::string GeneticAlgo::mutate(const std::string& entity)
{
	int mutationPos = rand() % this->pPopulation->GetGenePoolSize();

	char targetGene = entity.at(mutationPos) == GENE_EXIST_CHAR ? GENE_NOT_EXIT_CHAR : GENE_EXIST_CHAR;

	return entity.substr(0, mutationPos) + targetGene + entity.substr(mutationPos + 1);

}

int GeneticAlgo::rouletteSelect(int* fitnesses, int totalFitness, int size)
{
	int random = rand() % totalFitness;
	int acc = 0;
	for (int j = 0; j < size; ++j)
	{
		if (acc <= random && random < acc + fitnesses[j])
		{
			return j;
		}
		acc += fitnesses[j];
	}
}