package ai.neat;

import game.Game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import processing.core.PApplet;
import ai.neat.bot.NeuralNetBot;
import ai.neat.genes.LinkGene;
import ai.neat.genes.NeuronGene;
import ai.neat.innovations.Innovations;

public class Neat
{
	ArrayList<Genome> population = new ArrayList<Genome>();
	ArrayList<Specie> species = new ArrayList<Specie>();
	Innovations innovations = new Innovations(NeatParams.inputNodesN, NeatParams.outputNodesN);
	PApplet processingApplet;
	
	public Neat(PApplet processingApplet)
	{
		this.processingApplet = processingApplet;
		
		// make initial random population
		Specie newSpecie = new Specie(innovations.getNewSpecieId());
		for(int i = 0; i < NeatParams.populationSize; i++)
		{
			Genome newOrganism = new Genome(innovations.getNewGenomeId(), innovations);
			
			// create appropriate neural net
			newOrganism.createPhenotype();
			
			// add it to the only specie
			newSpecie.addMember(newOrganism);
			population.add(newOrganism);
		}
		species.add(newSpecie);
	}
	
	public void train()
	{
		// all the generations
		for(int generationI = 0; generationI < NeatParams.generationsN; generationI++)
		{
			// new generation
			epoch();
		}
	}

	// play one game between two bots, from which the fitness will be calculated
	void playGame(NeuralNetBot bot1, NeuralNetBot bot2)
	{
		bot1.gameStarted();
		bot2.gameStarted();
		
		Game game = new Game(processingApplet, bot1, bot2);		
		while(!game.isGameFinished())
		{
			game.update(null, null);
		}
	}
	
	// play the whole tournament, from which fitness will be calculated
	public void playTournament()
	{
		// swiss system
		SwissPairingSystem swiss = new SwissPairingSystem(population.size(), NeatParams.numberOfRounds);
		
		// play the tournament
		for(int roundI = 0; roundI < NeatParams.numberOfRounds; roundI++)
		{
			int[][] pairings = swiss.makeNewPairings();
			
			for(int pairI = 0; pairI < pairings.length; pairI++)
			{
				int team1 = pairings[pairI][0];
				int team2 = pairings[pairI][1];
				
				NeuralNetBot bot1 = population.get(team1).phenotype;
				NeuralNetBot bot2 = population.get(team2).phenotype;
								
				playGame(bot1, bot2);
				
				swiss.gameFinished(team1, team2, bot1.getGameFitness(), bot2.getGameFitness());
			}
		}
		
		// put score as genomes fitness
		for(int genomeI = 0; genomeI < population.size(); genomeI++)
		{
			population.get(genomeI).fitness = swiss.scores[genomeI];
		}
	}	
	
	// pass one generation
	// make species
	// etc
	public void epoch()
	{		
		// play tournament to determine all the fitness scores
		playTournament();
		
		Genome bestOrganism = null;
		
		// sort by fitness in every old specie and update age
		for(Specie s : species)
		{
			s.newEpoch();
			
			if(bestOrganism == null || s.leader.fitness > bestOrganism.fitness)
			{
				bestOrganism = s.leader;
			}
		}

		for(int specieI = 0; specieI < species.size(); specieI++)
		{
			Specie s = species.get(specieI);
			if(s.generationsWithNoImprovement > NeatParams.generationWithoutImprovementMaxNum
					&& bestOrganism != s.leader)
			{
				// this specie is not improving for too long
				// and the best organism is not in this specie
				// remove this specie
				species.remove(specieI);
				specieI--;
			}
		}
		
		// make new population
		population.clear();
		
		for(int specieI = 0; specieI < species.size(); specieI++)
		{
			// make new generation in every specie
			Specie s = species.get(specieI);
			
			int numToSpawn = (int) Math.round( s.spawnRequired );
			
			if(numToSpawn == 0)
			{
				species.remove(specieI);
				specieI--;
			}
			
			boolean madeLeader = false;
			
			for(int spawnI = 0; spawnI < numToSpawn; spawnI++)
			{
				Genome baby = null;
				
				if(!madeLeader)
				{
					// leader goes to the next generation
					baby = new Genome(s.leader);
					madeLeader = true;
				}
				else if(s.getMembersMatingNum() == 1
						|| Math.random() > NeatParams.crossoverRate)
				{
					// only one member for mating in the specie, or not crossovering for this one
					// just mutating chosen genome
					baby = new Genome(s.spawn());
				}
				else
				{
					// crossovering
					Genome mum = s.spawn();
					Genome dad = s.spawn();
					
					while(mum == dad)
					{
						dad = s.spawn();
					}
					
					baby = crossover(mum, dad);
				}
				
				// mutation
				baby.mutateNewNeuron();
				baby.mutateNewLink();
				baby.mutateWeights();
				baby.mutateActivationResponse();
				
				// create appropriate neural net
				baby.createPhenotype();
				
				// add to the new population
				population.add(baby);
			}			
		}
		
		// empty all the species
		for(Specie s : species)
		{
			s.clearMembers();
		}
		
		// divide genomes into species
		for(Genome g : population)
		{
			Specie closeSpecie = null;
			double closeSpecieDist = -1;
			
			for(Specie s : species)
			{
				if(closeSpecie == null || compatibilityDistance(g, s.leader) < closeSpecieDist)
				{
					closeSpecie = s;
					closeSpecieDist = compatibilityDistance(g, s.leader); 
				}
			}
			
			if(closeSpecieDist > NeatParams.specieCompatibilityTreshold)
			{
				// no specie close enough
				// this one should be in new specie
				Specie newSpecie = new Specie(innovations.getNewSpecieId());
				newSpecie.addMember(g);
				species.add(newSpecie);
			}
			else
			{
				closeSpecie.addMember(g);
			}
		}
	}
	
	// making baby genome from mum and dad genomes
	public static Genome crossover(Genome mum, Genome dad)
	{		
		// find the fitter parent, as we will use the disjoint/excess genes from that parent
		
		Genome fitterParent;
		
		// compare by fitness / the more the better
		if(mum.fitness == dad.fitness)
		{
			// they both have the same fitness
			
			// compare by genes count - the less the better
			if(mum.links.size() == dad.links.size())
			{
				// they both have the same genes count
				
				// take a random parent
				fitterParent = (Math.random() < 0.5)? mum : dad;
			}
			else if(mum.links.size() < dad.links.size())
			{
				fitterParent = mum;
			}
			else
			{
				fitterParent = dad;
			}
		}
		else if(mum.fitness > dad.fitness)
		{
			fitterParent = mum;
		}
		else
		{
			fitterParent = dad;
		}
		
		// iterate through it's genes
		Iterator<Map.Entry<Integer, LinkGene>> mumIterator = mum.links.entrySet().iterator();
		Iterator<Map.Entry<Integer, LinkGene>> dadIterator = dad.links.entrySet().iterator();
		
		LinkGene currentMumGene = mumIterator.next().getValue();
		LinkGene currentDadGene = dadIterator.next().getValue();

		TreeMap<Integer, NeuronGene> babyNeurons = new TreeMap<Integer, NeuronGene>();
		TreeMap<Integer, LinkGene> babyLinks = new TreeMap<Integer, LinkGene>();
		
		LinkGene selectedGene;
		// while there's more genes
		while(currentMumGene != null || currentDadGene != null)
		{
			selectedGene = null;
			
			// mum has no more genes
			if(currentMumGene == null)
			{
				// dad is fitter, he adds excess genes
				if(dad == fitterParent)
				{
					selectedGene = currentDadGene;
				}
				
				currentDadGene = null;
				if(dadIterator.hasNext())
				{
					currentDadGene = dadIterator.next().getValue();					
				}
			}
			// dad has no more genes
			else if(currentDadGene == null)
			{
				// mum is fitter, she adds excess genes
				if(mum == fitterParent)
				{
					selectedGene = currentMumGene;
				}
				
				currentMumGene = null;
				if(mumIterator.hasNext())
				{
					currentMumGene = mumIterator.next().getValue();					
				}
			}
			else
			{
				// both mum and dad have more genes
				
				if(currentMumGene.getInnovationId() < currentDadGene.getInnovationId())
				{
					// mum is fitter, she adds disjoint genes
					if(mum == fitterParent)
					{
						selectedGene = currentMumGene;
					}
					
					currentMumGene = null;
					if(mumIterator.hasNext())
					{
						currentMumGene = mumIterator.next().getValue();					
					}	
				}
				else if(currentDadGene.getInnovationId() < currentMumGene.getInnovationId())
				{
					// dad is fitter, he adds disjoint genes
					if(dad == fitterParent)
					{
						selectedGene = currentDadGene;
					}
					
					currentDadGene = null;
					if(dadIterator.hasNext())
					{
						currentDadGene = dadIterator.next().getValue();					
					}		
				}
				else
				{
					// they both have this gene
					// take a random one
					
					if(Math.random() < 0.5)
					{
						selectedGene = currentMumGene;
					}
					else
					{
						selectedGene = currentDadGene;						
					}
					
					currentMumGene = null;
					if(mumIterator.hasNext())
					{
						currentMumGene = mumIterator.next().getValue();					
					}
					
					currentDadGene = null;
					if(dadIterator.hasNext())
					{
						currentDadGene = dadIterator.next().getValue();					
					}
				}
				
				if(selectedGene != null)
				{
					// add selected link
					babyLinks.put(selectedGene.getInnovationId(), selectedGene);
					// add neurons
					addNeuronToBaby(mum, dad, fitterParent, babyNeurons, selectedGene.getFromNeuron());
					addNeuronToBaby(mum, dad, fitterParent, babyNeurons, selectedGene.getToNeuron());
				}
			}
		}
		
		Genome baby = new Genome(mum.innovations.getNewGenomeId(), mum.inputNum, mum.outputNum, mum.innovations,
				babyNeurons, babyLinks);
		return baby;
	}

	// add neuron to the baby in the crossover
	public static void addNeuronToBaby(Genome mum, Genome dad, Genome fitterParent,
			TreeMap<Integer, NeuronGene> babyNeurons, int neuronId)
	{
		if(mum.neuronExists(neuronId) && dad.neuronExists(neuronId))
		{
			if(mum == fitterParent)
			{
				babyNeurons.put(neuronId, mum.neurons.get(neuronId));
			}
			else
			{
				babyNeurons.put(neuronId, dad.neurons.get(neuronId));
			}
		}
		else if(mum.neuronExists(neuronId))
		{
			babyNeurons.put(neuronId, mum.neurons.get(neuronId));						
		}
		else
		{
			babyNeurons.put(neuronId, dad.neurons.get(neuronId));
		}
	}
	
	// compatibility distance calculation between two genome, used for speciation
	public static double compatibilityDistance(Genome g1, Genome g2)
	{
		// params for compatibility distance calculation
		int numExcess = 0;
		int numDisjoint = 0;
		int numMatched = 0;
		double weightDifference = 0;
		int longerGenomeSize = Math.max(g1.links.size(), g2.links.size());
		
		// iterate through it's genes
		Iterator<Map.Entry<Integer, LinkGene>> g1Iterator = g1.links.entrySet().iterator();
		Iterator<Map.Entry<Integer, LinkGene>> g2Iterator = g2.links.entrySet().iterator();
		
		LinkGene currentG1Gene = g1Iterator.next().getValue();
		LinkGene currentG2Gene = g2Iterator.next().getValue();
		
		while(currentG1Gene != null || currentG2Gene != null)
		{
			if(currentG1Gene == null)
			{
				// g1 has no more genes
				// g2 has this excess gene
				numExcess++;
				
				currentG2Gene = null;
				if(g2Iterator.hasNext())
				{
					currentG2Gene = g2Iterator.next().getValue();					
				}
			}
			else if(currentG2Gene == null)
			{
				// g2 has no more genes
				// g1 has this excess gene
				numExcess++;
				
				currentG1Gene = null;
				if(g1Iterator.hasNext())
				{
					currentG1Gene = g1Iterator.next().getValue();					
				}
			}
			else
			{
				// this is not excess gene
				
				if(currentG1Gene.getInnovationId() < currentG2Gene.getInnovationId())
				{
					// disjoint gene in g1
					numDisjoint++;
					
					currentG1Gene = null;
					if(g1Iterator.hasNext())
					{
						currentG1Gene = g1Iterator.next().getValue();					
					}	
				}
				else if(currentG2Gene.getInnovationId() < currentG1Gene.getInnovationId())
				{
					// disjoint gene in g2
					numDisjoint++;
					
					currentG2Gene = null;
					if(g2Iterator.hasNext())
					{
						currentG2Gene = g2Iterator.next().getValue();					
					}
				}
				else
				{
					// matched genes
					numMatched++;
					weightDifference += Math.abs(currentG1Gene.getWeight() - currentG2Gene.getWeight());
					
					currentG1Gene = null;
					if(g1Iterator.hasNext())
					{
						currentG1Gene = g1Iterator.next().getValue();					
					}

					currentG2Gene = null;
					if(g2Iterator.hasNext())
					{
						currentG2Gene = g2Iterator.next().getValue();					
					}
				}
			}
		}
		
		double score = NeatParams.excessCoef * numExcess / longerGenomeSize
				+ NeatParams.disjointCoef * numDisjoint / longerGenomeSize
				+ NeatParams.matchedCoef * weightDifference / numMatched;
		return score;
	}
}
