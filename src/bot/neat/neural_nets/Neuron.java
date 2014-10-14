package bot.neat.neural_nets;

import java.util.ArrayList;

import bot.neat.genes.NeuronGene;
import bot.neat.genes.NeuronType;

public class Neuron
{
	ArrayList<Link> inLinks = new ArrayList<Link>();
	ArrayList<Link> outLinks = new ArrayList<Link>();
	
	// sum of weighted inputs to this neuron
	double activationSum;
	// output from this neuron
	double output;
	// type of neuron
	NeuronType type;
	// id
	int innovationId;
	// activation response modifier
	double activationResponse;
	// for visualization purposes
	double posX;
	double posY;
	// is this neuron activated, has it got any inputs this turn?
	boolean activated;
	
	public Neuron(NeuronGene ng)
	{
		activationSum = output = 0;
		type = ng.getType();
		innovationId = ng.getInnovationId();
		activationResponse = ng.getActivationResponse();
		posX = ng.getPosX();
		posY = ng.getPosY();
	}
	
	public void addInLink(Link inLink)
	{
		inLinks.add(inLink);
	}
	
	public void addOutLink(Link outLink)
	{
		inLinks.add(outLink);
	}
}
