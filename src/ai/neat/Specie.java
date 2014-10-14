package ai.neat;

import haxball.Logger;
import haxball.Utils;

import java.util.ArrayList;
import java.util.Collections;

public class Specie implements Comparable<Specie>
{
	// the best member of the specie
	Genome leader;
	// different for every specie
	int specieId;
	// all members of this specie
	ArrayList<Genome> members = new ArrayList<Genome>();
	// average fitness in this specie
	double avgAdjustedFitness;
	// how many generations this specie exists
	int age;
	// how many generation there was no improvement in fitness
	int generationsWithNoImprovement;
	// how many members should this specie spawn in the next generation
	double spawnRequired;
	
	public Specie(int specieId)
	{
		this.specieId = specieId;
	}
	
	public void addMember(Genome g)
	{
		members.add(g);
		g.setSpecieId(specieId);
		
		if(leader == null)
		{
			leader = g;
		}
	}
	
	// boost the fitness for the young species
	// penalize the fitness for the old species
	// share fitness
	public void adjustFitness()
	{
		double totalFitness = 0;
		
		for(Genome g : members)
		{
			double fitness = g.getFitness();
			
			// boost the fitness if this is young specie
			if(age < NeatParams.youngAgeThreshold)
			{
				fitness *= NeatParams.youngAgeBonus;
			}
						
			// punish if old specie
			if(age > NeatParams.oldAgeThreshold)
			{
				fitness *= NeatParams.oldAgePenalty;
			}
			
			totalFitness += fitness;
			
			// shared fitness
			double adjustedFitness = fitness / members.size();
			g.setAdjustedFitness(adjustedFitness);
		}
		
		avgAdjustedFitness = totalFitness / members.size();
	}
	
	// calculate how many offspring should this specie spawn
	public void calculateSpawnAmount()
	{
		adjustFitness();
		spawnRequired = 0;
		for(Genome g : members)
		{
			g.amountToSpawn = g.adjustedFitness / avgAdjustedFitness;
			spawnRequired += g.amountToSpawn; 
		}
	}
	
	// sort by fitness, find the leader and update age and generationsWithNoImprovement, and calculate spawn amount
	public void newEpoch()
	{
		Collections.sort(members);
		
		Genome newLeader = members.get(0);
		age++;
		generationsWithNoImprovement++;
		if(newLeader.fitness > leader.fitness)
		{
			generationsWithNoImprovement = 0;
		}
		
		leader = newLeader;
		
		calculateSpawnAmount();
	}
	
	public int getMembersMatingNum()
	{
		int membersMatingNum = (int) Math.round(members.size() * NeatParams.populationPercentMating);
		if(membersMatingNum == 0)
		{
			membersMatingNum = 1;
		}
		return membersMatingNum;
	}
	
	// spawn one genome from the specie
	public Genome spawn()
	{
		double fitnessSum = 0;
		int membersMatingNum = getMembersMatingNum();
		
		for(int i = 0; i < membersMatingNum; i++)
		{
			fitnessSum += members.get(i).fitness;
		}
		
		double randomFitness = Math.random() * fitnessSum;
		double currentFitnessSum = 0;
				
		for(int memberI = 0; memberI < membersMatingNum; memberI++)
		{
			Genome g = members.get(memberI);
			
			currentFitnessSum += g.fitness;
			if(currentFitnessSum > randomFitness)
			{
				return g;
			}
		}
		
		return members.get(membersMatingNum - 1); // should never happen
	}
	
	// clear all old generation members before adding new generation
	public void clearMembers()
	{
		members.clear();
	}
	
	// save to file
	public void saveSpecie(String savePath)
	{
		Utils.createFolderIfNotExists(savePath);
		
		String specieText = "specieId: " + specieId + "\r\n";
		specieText += "members size: " + members.size() + "\r\n";
		specieText += "members:";
		for(Genome g : members)
		{
			specieText += " " + g.genomeId;
		}
		specieText +=  "\r\n";
		specieText += "leader: " + leader.genomeId + "\r\n";
		specieText += "age: " + age + "\r\n";
		specieText += "generations without improvement: " + generationsWithNoImprovement + "\r\n";
		specieText += "avgAdjustedFitness: " + avgAdjustedFitness + "\r\n";
		specieText += "spawnRequired: " + spawnRequired + "\r\n";
		
		Logger.logToFile(savePath + "/" + specieId + ".txt", specieText);
	}

	// compare by best fitness in the specie
	public int compareTo(Specie s)
	{
		if(leader.fitness - s.leader.fitness > 0)
		{
			return 1;
		}
		if(leader.fitness - s.leader.fitness < 0)
		{
			return -1;
		}
		return 0;
	}
}
