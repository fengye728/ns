#include<list>
#include<vector>
#include<string>
#include<iostream>
#include"PaperOne.h"

using namespace std;

// -------------------------- GeneticAlgo Public Functions --------------------
std::string GeneticAlgo::Generate()
{
	std::vector<std::string> generation = this->initOriginEntities();

	std::string bestEntity;
	int bestFitness = 0;
	int bestGenerationNum = 0;

	for (int i = 0; i < this->maxGenerations; ++i)
	{
		generation = this->selection(generation);

		// find best fitness
		for (auto it = generation.begin(); it != generation.end(); it++)
		{
			int fitness = this->pPopulation->GetFitness(*it);

			if (fitness > bestFitness)
			{
				bestFitness = fitness;
				bestEntity = *it;
				bestGenerationNum = i;
			}
		}
	}
	cout << "Best: GenerationNum - " << bestGenerationNum << ", Fitness - " << bestFitness << ", Entity - " << bestEntity << endl;
	return bestEntity;
}

// -------------------------- GeneticAlgo Private Functions -------------------
std::vector<std::string> GeneticAlgo::initOriginEntities()
{
	vector<string> generation;
	while (generation.size() < this->originEntityNum)
	{
		string entity = "";
		for (int i = this->pPopulation->GetGenePoolSize(); i > 0; --i) 
		{
			char gene = rand() % 2 == 1 ? GENE_EXIST_CHAR : GENE_NOT_EXIT_CHAR;
			entity += gene;
		}
		
		// check if the entity already existed
		auto it = std::find(generation.begin(), generation.end(), entity);
		if (it == generation.end())
		{
			generation.push_back(entity);
		}

	}
	return generation;
}

std::vector<std::string> GeneticAlgo::selection(std::vector<std::string> entities)
{
	int size = entities.size();

	int* fitnesses = new int[size];
	int totalFitness = 0;

	// calc fitness
	for (int i = 0; i < size; ++i)
	{
		fitnesses[i] = this->pPopulation->GetFitness(entities[i]);
		totalFitness += fitnesses[i];
	}

	std::vector<std::string> newGeneration;

	// generate newGeneration 
	while(newGeneration.size() < size)
	{
		// select parents
		int parentPapa = rouletteSelect(fitnesses, totalFitness, size);
		int parentMama = rouletteSelect(fitnesses, totalFitness, size);

		std::string child1;
		std::string child2;

		// check if crossover
		if (rand() % 10 / 10.0 <= this->crossoverProb)
		{
			// crossover
			this->crossover(entities.at(parentPapa), entities.at(parentMama), child1, child2);
		}
		else
		{
			child1 = entities.at(parentPapa);
			child2 = entities.at(parentMama);
		}

		// check if mutate
		if (rand() % 10 / 10.0 <= this->mutateProb)
		{
			this->mutate(child1);
		}
		if (rand() % 10 / 10.0 <= this->mutateProb)
		{
			this->mutate(child2);
		}

		// add new children

		// check if the children can live and already existed
		if (this->pPopulation->CanLive(child1))
		{
			auto it = std::find(newGeneration.begin(), newGeneration.end(), child1);
			if (it == newGeneration.end())
			{
				newGeneration.push_back(child1);
			}
		}
		
		if (this->pPopulation->CanLive(child2))
		{
			auto it = std::find(newGeneration.begin(), newGeneration.end(), child2);
			if (it == newGeneration.end())
			{
				newGeneration.push_back(child2);
			}
		}
	}
	return newGeneration;
}

void GeneticAlgo::crossover(std::string entityPapa, std::string entityMama, std::string& child1, std::string child2)
{
	int pos = rand() % this->pPopulation->GetGenePoolSize();
	child1 = entityPapa.substr(0, pos) + entityMama.substr(pos);
	child2 = entityMama.substr(0, pos) + entityPapa.substr(pos);
}

std::string GeneticAlgo::mutate(std::string entity)
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