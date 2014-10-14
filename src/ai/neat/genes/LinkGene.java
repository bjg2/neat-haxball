package ai.neat.genes;

import ai.neat.NeatParams;
import ai.neat.innovations.LinkInnovation;

public class LinkGene implements Comparable<LinkGene>
{
	int fromNeuron;
	int toNeuron;
	double weight;
	boolean enabled;
	int innovationId;
	boolean recurrent; // going to the node closer to the input nodes
	
	public LinkGene()
	{
	}
	
	public LinkGene(LinkInnovation li)
	{
		this(li.getFromNeuron(), li.getToNeuron(),
				Double.NaN, true,
				false, li.getInnovationId());
	}
	
	public LinkGene(int fromNeuron, int toNeuron,
			double weight, boolean enabled,
			boolean recurrent, int inovationId)
	{
		this.fromNeuron = fromNeuron;
		this.toNeuron = toNeuron;
		this.weight = weight;
		this.enabled = enabled;
		this.recurrent = recurrent;
		this.innovationId = inovationId;
		
		if(Double.isNaN(weight))
		{
			totallyRandomizeWeight();
		}
	}
	
	public LinkGene(LinkGene lg)
	{
		this(lg.fromNeuron, lg.toNeuron,
				lg.weight, lg.enabled,
				lg.recurrent, lg.innovationId);
	}
	
	// make a totally new random weight
	public void totallyRandomizeWeight()
	{
		weight = (Math.random() - 0.5) * 2 * NeatParams.maxWeight;
	}
	
	// make a small random move to the weight
	public void smallRandomizeWeight()
	{
		weight += (Math.random() - 0.5) * 2 * NeatParams.smallWeightRandomMove;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public int getFromNeuron() {
		return fromNeuron;
	}

	public int getToNeuron() {
		return toNeuron;
	}

	public double getWeight() {
		return weight;
	}

	public int getInnovationId() {
		return innovationId;
	}

	public boolean isRecurrent() {
		return recurrent;
	}

	public String toString()
	{
		return "innovationId: " + innovationId
				+ " fromNeuron: " + fromNeuron
				+ " toNeuron: " + toNeuron
				+ " weight: " + weight
				+ " enabled: " + enabled
				+ " recurrent: " + recurrent;
	}

	// compare by innovationId
	public int compareTo(LinkGene lg)
	{
		return lg.innovationId - innovationId;
	}
	
	// equals on innovationId
	public boolean equals(Object obj)
	{
		if(!(obj instanceof LinkGene))
		{
			return false;
		}
		
		LinkGene lg = (LinkGene) obj;
		return lg.innovationId == innovationId;
	}

	// hash is innovationId
	public int hashCode()
	{
		return innovationId;
	}
}
