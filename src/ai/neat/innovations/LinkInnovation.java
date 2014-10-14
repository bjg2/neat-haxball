package ai.neat.innovations;

public class LinkInnovation
{
	int innovationId;
	int fromNeuron;
	int toNeuron;
	
	public LinkInnovation(int innovationId, int fromNeuron, int toNeuron)
	{
		this.innovationId = innovationId;
		this.fromNeuron = fromNeuron;
		this.toNeuron = toNeuron;
	}
	
	public int getInnovationId()
	{
		return innovationId;
	}
	
	public int getFromNeuron() {
		return fromNeuron;
	}

	public int getToNeuron() {
		return toNeuron;
	}

	// equals on neuronIn and neuronOut
	public boolean equals(Object obj)
	{
		if(!(obj instanceof LinkInnovation))
		{
			return false;
		}
		
		LinkInnovation li = (LinkInnovation) obj;
		return li.fromNeuron == fromNeuron && li.toNeuron == toNeuron;
	}

	// hash on neuronIn and neuronOut
	public int hashCode()
	{
		return fromNeuron * 1000000000 + toNeuron;
	}
}
