package ai.neat.bot;

import java.util.TreeMap;

import org.jbox2d.common.Vec2;

import ai.ABot;
import ai.GameMove;
import ai.GameState;
import ai.neat.NeatParams;
import ai.neat.genes.LinkGene;
import ai.neat.genes.NeuronGene;
import ai.neat.neural_nets.NeuralNet;

public class NeuralNetBot extends ABot
{
	NeuralNet net;
	FitnessCalculator fitnessCalculator;
	
	public NeuralNetBot(TreeMap<Integer, NeuronGene> neuronGenes, TreeMap<Integer, LinkGene> linkGenes)
	{
		net = new NeuralNet(neuronGenes, linkGenes, NeatParams.neuralNetRunType);
		fitnessCalculator = new FitnessCalculator();
	}

	public GameMove makeMove(GameState state)
	{
		// calculate this move fitness
		fitnessCalculator.move(state);
		
		// prepare inputs for the net
		Vec2 enemyPlayerPos = state.enemyPlayersPos[0];
		Vec2 enemyPlayerVelocity = state.enemyPlayersPos[0];
		
		double[] inputs = new double[12];
		
		inputs[0] = (double) state.myPlayerPos.x / GameState.realFieldW;
		inputs[1] = (double) state.myPlayerPos.y / GameState.realFieldH;
		inputs[2] = (double) state.myPlayerVelocity.x / GameState.maxPlayerVelocity;
		inputs[3] = (double) state.myPlayerVelocity.y / GameState.maxPlayerVelocity;
		inputs[4] = (double) enemyPlayerPos.x / GameState.realFieldW;
		inputs[5] = (double) enemyPlayerPos.y / GameState.realFieldH;
		inputs[6] = (double) enemyPlayerVelocity.x / GameState.maxPlayerVelocity;
		inputs[7] = (double) enemyPlayerVelocity.y / GameState.maxPlayerVelocity;
		inputs[8] = (double) state.ballPos.x / GameState.realFieldW;
		inputs[9] = (double) state.ballPos.y / GameState.realFieldH;
		inputs[10] = (double) state.ballVelocity.x / GameState.maxShootVelocity;
		inputs[11] = (double) state.ballVelocity.y / GameState.maxShootVelocity;
		
		// do the net stuff
		double[] outputs = net.update(inputs);
		
		// make move from outputs
		int xMove = 0;
		if(outputs[0] < 0.33)
		{
			xMove = -1;
		}
		if(outputs[0] > 0.66)
		{
			xMove = 1;
		}
		
		int yMove = 0;
		if(outputs[1] < 0.33)
		{
			yMove = -1;
		}
		if(outputs[1] > 0.66)
		{
			yMove = 1;
		}
		
		boolean shoot = false;
		if(outputs[1] > 0.5)
		{
			shoot = true;
		}
		
		return new GameMove(xMove, yMove, shoot);
	}
	
	public void gameStarted()
	{
		fitnessCalculator.gameStared();
	}

	public void gameFinished(GameState state)
	{
		fitnessCalculator.gameFinished(state);
	}
	
	public double getGameFitness()
	{
		return fitnessCalculator.fitness;
	}
}
