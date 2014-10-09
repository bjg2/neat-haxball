package bot.neatOld;

import game.Game;
import haxball.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import processing.core.PApplet;

public class NeatTrainer
{
	// genetic algo arguments
	public static int population = 100;
	public static int gamesN = 10;
	public static int generationN = 1000;
	
	public static int inputNodesN = 13;
	public static int outputNodesN = 3;
	
	public static double elitePerc = 0.01;
	
	// mutation	
	public static double maxW = 5;
	public static double newArcProb = 0.3;
	public static double newNodeProb = 0.1;
	public static double edgeWMutationProb = 0.1;
	public static double smallMutationProb = 0.9;
	
	// fitness weights
	// TEST VALUES TO CHECK NETS ABILITY TO LEARN
	public static double scoreW = 0;
	public static double ballPlayerDistW = 0.1;
	public static double ballGoalDistW = 0.01;
	public static double attackW = 0;
	public static double defenceW = 0;
	
	ArrayList<NeuralNetBot> generation = new ArrayList<NeuralNetBot>();
	
	int newNodeNum;
	HashMap<ArcGene, Integer> nodeBetweanNodes
		= new HashMap<ArcGene, Integer>();
	
	HashSet<Integer> inputNodes = new HashSet<Integer>();
	HashSet<Integer> outputNodes = new HashSet<Integer>();
	
	PApplet parent;
	
	public NeatTrainer(PApplet parent)
	{
		this.parent = parent;
				
		for(int i = 0; i < inputNodesN; i++)
		{
			inputNodes.add(i);
		}
		for(int i = 0; i < outputNodesN; i++)
		{
			outputNodes.add(inputNodesN + i);
		}
		
		newNodeNum = inputNodesN + outputNodesN;
		
		for(int i = 0; i < population; i++)
		{
			generation.add( new NeuralNetBot(this, inputNodes, outputNodes, true) );
		}
	}
	
	// create net from the input file
	public NeuralNetBot getNeuralNet(String netPath)
	{
		NeuralNetBot net = new NeuralNetBot(this, inputNodes, outputNodes, false);
		net.inputFromFile(netPath);
		return net;
	}
	
	// train the neural net
	public void train(String netsString)
	{
		Utils.createFolderIfNotExists(netsString);
		for(int i = 0; i < generationN; i++)
		{
			PApplet.println("GENERATION " + i);
			makeNewGeneration(netsString + "/" + i);
		}
	}

	// make new generation for training
	void makeNewGeneration(String generationString)
	{
		Utils.createFolderIfNotExists(generationString);
		
		// play all games
		SwissPairingSystem swiss = new SwissPairingSystem(population, gamesN, generationString + "/000tournament.txt");
		for(int roundI = 0; roundI < gamesN; roundI++)
		{
			System.out.print("ROUND " + roundI + " PAIRING...");
			int[][] pairings = swiss.makeNewPairings();
			System.out.println(" PAIRED.");
			
			for(int pairI = 0; pairI < pairings.length; pairI++)
			{
				System.out.print("PAIR " + pairI + "...");
				int team1 = pairings[pairI][0];
				int team2 = pairings[pairI][1];
				
				NeuralNetBot net1 = generation.get(team1);
				NeuralNetBot net2 = generation.get(team2);
				
				net1.gameFitness = 0;
				net2.gameFitness = 0;
				
				playGame(net1, net2);
				
				swiss.gameFinished(team1, team2, net1.gameFitness, net2.gameFitness);
			}
		}
		swiss.logScores();
		
		// make the fitness score in swiss tournament
		for(int i = 0; i < population; i++)
		{
			generation.get(i).score = swiss.scores[i];
		}
		
		// sort by fitness
		Collections.sort(generation);
		
		PApplet.println("BEST SCORE: " + generation.get(0).score);
		
		// output all of the nets
		for(int i = 0; i < population; i++)
		{
			generation.get(i).outputToFile(generationString + "/" + i + ".txt");
		}
		
		ArrayList<NeuralNetBot> newGeneration = new ArrayList<NeuralNetBot>();
				
		// keep the elite
		int eliteNum = (int) Math.ceil(elitePerc * population);
		for(int i = 0; i < eliteNum; i++)
		{
			newGeneration.add(generation.get(i));
		}
		
		double scoresSum = 0;
		for(NeuralNetBot net : generation)
		{
			scoresSum += net.score;
		}
		
		// crossover other
		for(int j = eliteNum; j < population; j++)
		{
			NeuralNetBot net1 = pickCrossoverBot(scoresSum);
			NeuralNetBot net2 = pickCrossoverBot(scoresSum);
			NeuralNetBot newBot = NeuralNetBot.crossover(net1, net2);
			newBot.mutation();
			newGeneration.add(newBot);
		}
		
		generation = newGeneration;
	}
	
	// pick a bot related to its score (fitness)
	NeuralNetBot pickCrossoverBot(double scoresSum)
	{
		int i;
		do
		{
			double r = Math.random();
			double curR = 0;
			for(i = 0; i < population && curR < r; i++)
			{
				curR += generation.get(i).score / scoresSum;
			}
		} while(i == population);
		return generation.get(i);
	}
	
	// play the game until it's finished
	void playGame(NeuralNetBot net1, NeuralNetBot net2)
	{
		Game game = new Game(parent, net1, net2);
		
		while(!game.isGameFinished())
		{
			game.update(null, null);
		}

		PApplet.println(" " + game.scoreString());
	}
	
	// between two nodes with id's id1 and id2 for every net will be id3
	int getNodeBetweanNodes(ArcGene gene)
	{
		if(nodeBetweanNodes.containsKey(gene))
		{
			return nodeBetweanNodes.get(gene);
		}
		
		nodeBetweanNodes.put(gene, newNodeNum);
		return newNodeNum++;
	}
}