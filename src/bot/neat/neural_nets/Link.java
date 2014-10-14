package bot.neat.neural_nets;

public class Link
{
	Neuron fromNeuron;
	Neuron toNeuron;
	
	double weight;
	
	boolean recurrent;
	
	public Link(Neuron fromNeuron, Neuron toNeuron, double weight, boolean recurrent)
	{
		this.fromNeuron = fromNeuron;
		this.toNeuron = toNeuron;
		this.weight = weight;
		this.recurrent = recurrent;
	}
}
