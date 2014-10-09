package bot.neat.innovations;

import bot.neat.genes.NeuronType;

public class NeuronInnovation
{
	int innovationId;
	NeuronType type;
	
	public NeuronInnovation(int innovationId, NeuronType type)
	{
		this.innovationId = innovationId;
		this.type = type;
	}

	public int getInnovationId()
	{
		return innovationId;
	}

	public NeuronType getType()
	{
		return type;
	}
}
