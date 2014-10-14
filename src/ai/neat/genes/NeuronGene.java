package ai.neat.genes;

import ai.neat.NeatParams;
import ai.neat.innovations.NeuronInnovation;

public class NeuronGene implements Comparable<NeuronGene>
{
	int innovationId;
	NeuronType type;
	boolean recurrent; // has self loop
	double activationResponse = 1; // sigmoid curve factor - default 1
	double posX; // position in the network grid
	double posY; // useful for drawing, deciding link recurrency and network depth
	
	public NeuronGene()
	{
	}
	
	public NeuronGene(NeuronInnovation ni, double posX)
	{
		this(ni.getInnovationId(), ni.getType(), false, posX,
				(ni.getType() == NeuronType.output)? 1 : 0);
	}
	
	public NeuronGene(NeuronInnovation ni, double posX, double posY)
	{
		this(ni.getInnovationId(), ni.getType(), false, posX, posY);
	}

	public NeuronGene(int innovationid, NeuronType type,
			boolean recurrent,
			double posX, double posY)
	{
		this.innovationId = innovationid;
		this.type = type;
		this.recurrent = recurrent;
		this.posX = posX;
		this.posY = posY;
	}
	
	public NeuronGene(NeuronGene ng)
	{
		this(ng.innovationId, ng.type,
				ng.recurrent, ng.posX, ng.posY);
		
		activationResponse = ng.activationResponse;
	}

	// make a small random move to the activation response
	public void smallRandomizeActivationResponse()
	{
		activationResponse += (Math.random() - 0.5) * 2 * NeatParams.smallActivationResponseRandomMove;
	}

	public boolean isRecurrent() {
		return recurrent;
	}

	public void setRecurrent(boolean recurrent) {
		this.recurrent = recurrent;
	}

	public int getInnovationId() {
		return innovationId;
	}

	public NeuronType getType() {
		return type;
	}

	public double getActivationResponse() {
		return activationResponse;
	}

	public double getPosX() {
		return posX;
	}

	public double getPosY() {
		return posY;
	}
	
	public String toString() {
		return "innovationId: " + innovationId
				+ " type: " + type
				+ " recurrent: " + recurrent
				+ " activationResponse: " + activationResponse
				+ " posX: " + posX
				+ " posY: " + posY;				
	}

	// compare on innovationId
	public int compareTo(NeuronGene ng)
	{
		return ng.innovationId - innovationId;
	}
	
	// equals on innovationId
	public boolean equals(Object obj)
	{
		if(!(obj instanceof NeuronGene))
		{
			return false;
		}
		
		NeuronGene ng = (NeuronGene) obj;
		return ng.innovationId == innovationId;
	}

	// hash is innovationId
	public int hashCode()
	{
		return innovationId;
	}
}
