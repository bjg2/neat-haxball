package bot.neatOld;

import haxball.Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.jbox2d.common.Vec2;

import bot.ABot;
import bot.GameMove;
import bot.GameState;

public class NeuralNetBot extends ABot implements Comparable<NeuralNetBot>
{
	NeatTrainer trainer;
	
	HashMap<Integer, HashMap<Integer, ArcGene>> graph
		= new HashMap<Integer, HashMap<Integer, ArcGene>>();
		
	ArrayList<ArcGene> genes = new ArrayList<ArcGene>();
	HashMap<ArcGene, ArcGene> genesHash
		= new HashMap<ArcGene, ArcGene>();
	ArrayList<Integer> nodes = new ArrayList<Integer>();
	HashSet<Integer> nodesSet = new HashSet<Integer>();
	
	HashSet<Integer> inputNodes = new HashSet<Integer>();
	HashSet<Integer> outputNodes = new HashSet<Integer>();
	
	double gameFitness = 0;
	// fitness in neural net terminology
	// tournament score
	double score = 0;
	
	public NeuralNetBot(NeatTrainer trainer,
			HashSet<Integer> inputNodes, HashSet<Integer> outputNodes, boolean initEdges)
	{
		this.trainer = trainer;
		this.inputNodes = inputNodes;
		this.outputNodes = outputNodes;
		
		if(initEdges)
		{
			for(int node1 : inputNodes)
			{
				for(int node2 : outputNodes)
				{
					addGeneToContext(new ArcGene(node1, node2));
				}
			}
		}
	}

	// update structures for new gene
	private void addGeneToContext(ArcGene gene)
	{
		genes.add(gene);
		genesHash.put(gene, gene);
		
		if(!graph.containsKey(gene.node1))
		{
			graph.put(gene.node1, new HashMap<Integer, ArcGene>());
		}
		if(!graph.containsKey(gene.node2))
		{
			graph.put(gene.node2, new HashMap<Integer, ArcGene>());
		}
		
		graph.get(gene.node1).put(gene.node2, gene);
		
		if(!nodesSet.contains(gene.node1))
		{
			nodesSet.add(gene.node1);
			nodes.add(gene.node1);
		}
		
		if(!nodesSet.contains(gene.node2))
		{
			nodesSet.add(gene.node2);
			nodes.add(gene.node2);
		}
	}
	
	// CALCULATING FITNESS
	
	// score diff fitness
	public void addEndFitness(GameState state)
	{
		// better score better bot
		gameFitness += (state.myScore - state.enemyScore) * NeatTrainer.scoreW;
	}
	
	// calculate attack fitness for resolved ball point
	// (original point or mirrored)
	public double attackFitnessForBall(Vec2 myPlayerPos, Vec2 enemyPlayerPos,
			Vec2 ballPos, Vec2 goalPos)
	{
		double playerBallDist = myPlayerPos.sub(ballPos).length();
		double goalBallDist = goalPos.sub(ballPos).length();
		
		return Utils.distFromLineSegment(ballPos, goalPos, enemyPlayerPos)
				/ playerBallDist
				/ goalBallDist;
	}
	
	// calculate attack fitness for ball 
	public double attackFitness(Vec2 myPlayerPos, Vec2 enemyPlayerPos,
			Vec2 ballPos, Vec2 goalPos)
	{
		// default pos
		Vec2 thisBallPos = ballPos;
		double ret = attackFitnessForBall(myPlayerPos, enemyPlayerPos,
				thisBallPos, goalPos);

		// mirrored up
		thisBallPos = new Vec2(ballPos.x,
				(GameState.realFieldH - GameState.fieldH) / 2
				- (ballPos.y - (GameState.realFieldH - GameState.fieldH) / 2));
		ret += attackFitnessForBall(myPlayerPos, enemyPlayerPos,
				thisBallPos, goalPos);

		// mirrored down
		thisBallPos = new Vec2(ballPos.x,
				(GameState.realFieldH + GameState.fieldH) / 2
				+ ((GameState.realFieldH + GameState.fieldH) / 2 - ballPos.y));
		ret += attackFitnessForBall(myPlayerPos, enemyPlayerPos,
				thisBallPos, goalPos);
		
		return ret;
	}
	
	// add all of the fitnesses in this round 
	public void addMoveFitness(GameState state)
	{
		if(!state.isPlaying)
		{
			// goal has just been scored
			return;
		}
		
		Vec2 enemyPlayerPos = state.enemyPlayersPos[0];
		
		double myBallDist = state.myPlayerPos.sub(state.ballPos).length();
		double enemyBallDist = enemyPlayerPos.sub(state.ballPos).length();
		
		// me closer to the ball the better
		gameFitness -= myBallDist * NeatTrainer.ballPlayerDistW;
		
		// enemy further from the ball the better
		gameFitness += enemyBallDist * NeatTrainer.ballPlayerDistW;
		
		Vec2 myGoalCenter = new Vec2((GameState.realFieldW - GameState.fieldW) / 2,
				GameState.realFieldH / 2);
		Vec2 enemyGoalCenter = new Vec2((GameState.realFieldW + GameState.fieldW) / 2,
				GameState.realFieldH / 2);
		double ballMyGoalDist = myGoalCenter.sub(state.ballPos).length();
		double ballEnemyGoalDist = enemyGoalCenter.sub(state.ballPos).length();

		// ball closer to the enemy goal the better
		gameFitness -= ballEnemyGoalDist * NeatTrainer.ballPlayerDistW;

		// ball further from my goal the better
		gameFitness += ballMyGoalDist * NeatTrainer.ballPlayerDistW;
		
		// how good am i attacking
		gameFitness += attackFitness(state.myPlayerPos, enemyPlayerPos,
				state.ballPos, enemyGoalCenter);
		
		// how good am i defending
		gameFitness -= attackFitness(enemyPlayerPos, state.myPlayerPos,
				state.ballPos, myGoalCenter);
	}
	
	// game is finished, update fitnesses
	public void gameFinished(GameState state)
	{
		addEndFitness(state);
	}

	// PASSING THROUGH NEURAL NET
	
	// make move for this situation
	public GameMove makeMove(GameState state)
	{
		// update fitness calculations
		addMoveFitness(state);

		Vec2 enemyPlayerPos = state.enemyPlayersPos[0];
		Vec2 enemyPlayerVelocity = state.enemyPlayersPos[0];
		
		// prepare input		
		HashMap<Integer, Double> nodeWeights = new HashMap<Integer, Double>();
		HashMap<Integer, Integer> nodesInCount = new HashMap<Integer, Integer>();

		for(int node : nodes)
		{
			nodesInCount.put(node, 0);
			nodeWeights.put(node, 0.0);
		}
		
		nodeWeights.put(0, (double) state.myPlayerPos.x / GameState.realFieldW);
		nodeWeights.put(1, (double) state.myPlayerPos.y / GameState.realFieldH);
		nodeWeights.put(2, (double) state.myPlayerVelocity.x / GameState.maxPlayerVelocity);
		nodeWeights.put(3, (double) state.myPlayerVelocity.y / GameState.maxPlayerVelocity);
		nodeWeights.put(4, (double) enemyPlayerPos.x / GameState.realFieldW);
		nodeWeights.put(5, (double) enemyPlayerPos.y / GameState.realFieldH);
		nodeWeights.put(6, (double) enemyPlayerVelocity.x / GameState.maxPlayerVelocity);
		nodeWeights.put(7, (double) enemyPlayerVelocity.y / GameState.maxPlayerVelocity);
		nodeWeights.put(8, (double) state.ballPos.x / GameState.realFieldW);
		nodeWeights.put(9, (double) state.ballPos.y / GameState.realFieldH);
		nodeWeights.put(10, (double) state.ballVelocity.x / GameState.maxShootVelocity);
		nodeWeights.put(11, (double) state.ballVelocity.y / GameState.maxShootVelocity);
		nodeWeights.put(12, 1.0); // bias
		
		// init in degrees
		for(Map.Entry<Integer, HashMap<Integer, ArcGene>>  nodeNeighs
				: graph.entrySet())
		{
			for(int neighNode : nodeNeighs.getValue().keySet())
			{
				int inCount = nodesInCount.get(neighNode) + 1;
				nodesInCount.put(neighNode, inCount);
			}
		}

		// add 0 in degrees nodes in queue (input nodes)
		ArrayList<Integer> queue = new ArrayList<Integer>();
		for(int i = 0; i < NeatTrainer.inputNodesN; i++)
		{
			queue.add(i);
		}
		
		// topologic pass...
		for(int i = 0; i < queue.size(); i++)
		{
			int node = queue.get(i);
			HashMap<Integer, ArcGene> nodeConnections = graph.get(node);
			
			// activation is sigmoid func from sum of node inputs
			double activation = Utils.sigmoid(nodeWeights.get(node));
			nodeWeights.put(node, activation);
			
			// iterate over every its neighbor
			for(Map.Entry<Integer, ArcGene> con : nodeConnections.entrySet())
			{
				int neighNode = con.getKey();
				double conW = con.getValue().w;
				
				int neighNodeInCount = nodesInCount.get(neighNode) - 1;
				nodesInCount.put(neighNode, neighNodeInCount);
				if(neighNodeInCount == 0)
				{
					// it's out of inputs
					queue.add(neighNode);
				}
				
				double neighPreActivation = nodeWeights.get(neighNode) + activation * conW;
				nodeWeights.put(neighNode, neighPreActivation);
			}
		}
		
		// read output
		double moveXWeight = nodeWeights.get(13);
		double moveYWeight = nodeWeights.get(14);
		double shootWeight = nodeWeights.get(15);
		
		int xMove = 0;
		if(moveXWeight < 0.33)
		{
			xMove = -1;
		}
		if(moveXWeight > 0.66)
		{
			xMove = 1;
		}
		
		int yMove = 0;
		if(moveYWeight < 0.33)
		{
			yMove = -1;
		}
		if(moveYWeight > 0.66)
		{
			yMove = 1;
		}
		
		boolean shoot = false;
		if(shootWeight > 0.5)
		{
			shoot = true;
		}
		
		return new GameMove(xMove, yMove, shoot);
	}
	
	// GENETIC ALGORITHM FUNCTIONS
	
	// crossover of two nets
	public static NeuralNetBot crossover(NeuralNetBot net1, NeuralNetBot net2)
	{
		// let net1 be the fitter one
		if(net2.score > net1.score)
		{
			return crossover(net2, net1);
		}
		
		double fitnessSum = net1.score + net2.score;
		double myProb = 0.5;
		if(fitnessSum != 0)
		{
			myProb = net1.score / fitnessSum;
		}
		
		// make new net
		NeuralNetBot newNet = new NeuralNetBot
				(net1.trainer, net1.inputNodes, net1.outputNodes, false);
		
		// for every gene from the fitter net
		for(ArcGene gene : net1.genes)
		{
			if(Math.random() < myProb || !net2.genesHash.containsKey(gene))
			{
				// if net2 doesn't contains that gene
				// or net1 randomly gain the chance to pass it
				newNet.addGeneToContext(new ArcGene(gene));
			}
			else
			{
				// net2 has this gene and it has randomly gain the chance to pass it
				newNet.addGeneToContext(new ArcGene(net2.genesHash.get(gene)));
			}
		}
		
		return newNet;
	}
	
	// mutate net
	public void mutation()
	{
		if(Math.random() < NeatTrainer.newNodeProb)
		{
			mutateNewNode();			
		}
		if(Math.random() < NeatTrainer.newArcProb)
		{
			mutateNewArc();		
		}
		mutateArcs();
	}
	
	// add a new node in some arc
	private void mutateNewNode()
	{
		ArcGene oldGene = genes.get( (int)(Math.random() * genes.size()) );
		while(!oldGene.active)
		{
			// need an active gene
			// this should be very small number of iterations
			// as the active genes are prevailing
			oldGene = genes.get( (int)(Math.random() * genes.size()) );
		}
		
		// get global node id
		int newNode = trainer.getNodeBetweanNodes(oldGene);
		
		// for x = 0.659046, x = sig(x)
		// so, it the net will work the same as before adding new node
		// cause in gene AB we added C as ACB but
		// we carried A information into C undiffered
		ArcGene gene1 = new ArcGene(oldGene.node1, newNode, 0.659046, true);
		ArcGene gene2 = new ArcGene(newNode, oldGene.node2, oldGene.w, true);
		oldGene.active = false;
		
		// add new genes
		addGeneToContext(gene1);
		addGeneToContext(gene2);
	}
	
	// add new arc between some two unconnected nodes that could be connected
	private void mutateNewArc()
	{
		// set of nodes that can reach this node
		HashMap<Integer, HashSet<Integer>> isReachableFrom
			= new HashMap<Integer, HashSet<Integer>>();
		for(int node : nodes)
		{
			// everyone is reachable from itself
			isReachableFrom.put(node, new HashSet<Integer>());
			isReachableFrom.get(node).add(node);
		}
		
		// topological pass
		
		// init in degree of each node
		HashMap<Integer, Integer> inCount = new HashMap<Integer, Integer>();
		for(int node : nodes)
		{
			inCount.put(node, 0);
		}

		// calculate in degree of each node
		for(Map.Entry<Integer, HashMap<Integer, ArcGene>> neighs : graph.entrySet())
		{
			for(int neighNode : neighs.getValue().keySet())
			{
				int inCnt = inCount.get(neighNode) + 1;
				inCount.put(neighNode, inCnt);
			}
		}
		
		// add zero in degree ones in queue
		ArrayList<Integer> queue = new ArrayList<Integer>();
		for(Map.Entry<Integer, Integer> nodeInCount : inCount.entrySet())
		{
			if(nodeInCount.getValue() == 0)
			{
				queue.add(nodeInCount.getKey());
			}
		}
		
		// pass the graph topologically
		for(int i = 0; i < queue.size(); i++)
		{
			int node = queue.get(i);
			
			for(int neighNode : graph.get(node).keySet())
			{
				// all that can reach node can also reach neighNode
				isReachableFrom.get(neighNode).addAll(isReachableFrom.get(node));
				
				// lower the neigh node in count
				int inCnt = inCount.get(neighNode) - 1;
				inCount.put(neighNode, inCnt);
				if(inCnt == 0)
				{
					// it has no more in nodes - add to queue
					queue.add(neighNode);
				}
			}
		}
		
		// generate all possible genes
		ArrayList<ArcGene> possibleGenes = new ArrayList<ArcGene>();
		for(int node : nodes)
		{
			if(!outputNodes.contains(node))
			{
				// its not in output nodes - arc could go out of him
				for(int neighNode : nodes)
				{
					if(!inputNodes.contains(neighNode)
							&& !graph.get(node).containsKey(neighNode)
							&& !isReachableFrom.get(node).contains(neighNode))
					{
						// its not in input nodes - arc could go in him
						// this edge does not exists already
						// can't reach node from neighNode - we're not making a cycle
						possibleGenes.add(new ArcGene(node, neighNode));
					}
				}
			}
		}
		
		if(possibleGenes.size() == 0)
		{
			// we can't make any new gene
			return;
		}
		
		int randomGeneI = (int) (Math.random() * possibleGenes.size());
		addGeneToContext( possibleGenes.get(randomGeneI) );
	}
	
	// try mutate arcs
	private void mutateArcs()
	{
		for(ArcGene gene : genes)
		{
			if(gene.active && Math.random() < NeatTrainer.edgeWMutationProb)
			{
				if(Math.random() < NeatTrainer.smallMutationProb)
				{
					// mutate it in it's small surrounding
					gene.smallRandomizeW(NeatTrainer.maxW / 10);
				}
				else
				{
					// totally change the arc weight
					gene.randomizeW();					
				}
			}
		}
	}
	
	// INPUT OUTPUT
	
	// read a net fromthe file
	public void inputFromFile(String filename)
	{
		try
		{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String s;
			while((s = br.readLine()) != null)
			{
				if(s.equals(""))
				{
					continue;
				}
				addGeneToContext(new ArcGene(s));
			}
			br.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	// output the net to the file
	public void outputToFile(String filename)
	{
		try
		{
			BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
			for(ArcGene gene : genes)
			{
				bw.write(gene + "\n");
			}
			bw.close();
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	// COMPARE
	
	// compare by fitness
	public int compareTo(NeuralNetBot net)
	{
		if(score < net.score)
		{
			return 1;
		}
		if(score > net.score)
		{
			return -1;
		}
		return 0;
	}

}
