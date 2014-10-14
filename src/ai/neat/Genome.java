package ai.neat;

import java.util.ArrayList;
import java.util.TreeMap;

import ai.neat.bot.NeuralNetBot;
import ai.neat.genes.LinkGene;
import ai.neat.genes.NeuronGene;
import ai.neat.genes.NeuronType;
import ai.neat.innovations.Innovations;
import ai.neat.innovations.LinkInnovation;
import ai.neat.innovations.NeuronInnovation;

@SuppressWarnings("unchecked")
public class Genome implements Comparable<Genome>
{
	int genomeId;
	TreeMap<Integer, NeuronGene> neurons = new TreeMap<Integer, NeuronGene>();
	TreeMap<Integer, LinkGene> links = new TreeMap<Integer, LinkGene>();
	
	double fitness; // raw fitness
	double adjustedFitness; // specie adjusted fitness
	double amountToSpawn; // how much this individual adds to the specie spawn number
	
	int inputNum;
	int outputNum;
	
	int specieId;
	
	Innovations innovations;
	
	NeuralNetBot phenotype;
	
	public Genome()
	{
	}
	
	public Genome(Genome g)
	{
		this.genomeId = g.innovations.getNewGenomeId();
		this.inputNum = g.inputNum;
		this.outputNum = g.outputNum;
		this.innovations = g.innovations;
		this.neurons = (TreeMap<Integer, NeuronGene>) g.neurons.clone();
		this.links = (TreeMap<Integer, LinkGene>) g.links.clone();
	}
	
	public Genome(int genomeId, int inputNum, int outputNum, Innovations innovations,
			TreeMap<Integer, NeuronGene> neurons, TreeMap<Integer, LinkGene> links)
	{
		this.genomeId = genomeId;
		this.inputNum = inputNum;
		this.outputNum = outputNum;
		this.innovations = innovations;
		this.neurons = neurons;
		this.links = links;
	}
	
	public Genome(int genomeId, Innovations innovations)
	{
		this.genomeId = genomeId;
		this.inputNum = NeatParams.inputNodesN;
		this.outputNum = NeatParams.outputNodesN;
		this.innovations = innovations;
				
		ArrayList<NeuronInnovation> initialNeurons = innovations.getInitialNeurons();

		// initial input (+bias) neurons
		double posXMove = 1.0 / (inputNum + 1);
		double posX = posXMove;
		for(int i = 0; i < inputNum; i++, posX += posXMove)
		{
			NeuronInnovation ni = initialNeurons.get(i);
			NeuronGene ng = new NeuronGene(ni, posX);
			neurons.put(ni.getInnovationId(), ng);
		}
		
		// initial output neurons
		posXMove = 1.0 / (outputNum + 1);
		posX = posXMove;
		for(int i = 0; i < outputNum; i++, posX += posXMove)
		{
			NeuronInnovation ni = initialNeurons.get(inputNum + i);
			NeuronGene ng = new NeuronGene(ni, posX);
			neurons.put(ni.getInnovationId(), ng);
		}
		
		// initial links
		ArrayList<LinkInnovation> initialLinks = innovations.getInitialLinks();
		for(LinkInnovation li : initialLinks)
		{
			LinkGene lg = new LinkGene(li);
			links.put(li.getInnovationId(), lg);
		}
	}
	
	// do we have link between these two neurons
	public boolean linkExists(int fromNeuron, int toNeuron)
	{
		if(!innovations.linkInnovationExists(fromNeuron, toNeuron))
		{
			return false;
		}
		LinkInnovation li = innovations.getLinkInnovation(fromNeuron, toNeuron);
		return links.containsKey(li.getInnovationId());
	}
	
	// do we have link with this innovationId?
	public boolean linkExists(int innovationId)
	{
		return links.containsKey(innovationId);
	}
	
	// do we have a neuron with this innovationId?
	public boolean neuronExists(int neuronId)
	{
		return neurons.containsKey(neuronId);
	}
	
	// mutate to make a new link
	public void mutateNewLink()
	{
		if(Math.random() > NeatParams.newLinkMutationRate)
		{
			// it will not mutate
			return;
		}

		int fromNeuron;
		int toNeuron;
		boolean recurrent = false;
		if(Math.random() < NeatParams.newLoopedLinkMutationRate)
		{
			// trying to make a looped link - a link from neuron to itself
			
			ArrayList<NeuronGene> loopableNeurons = new ArrayList<NeuronGene>();
			for(NeuronGene ng : neurons.values())
			{
				if(ng.getType() != NeuronType.bias
					&& ng.getType() != NeuronType.input
					&& !ng.isRecurrent())
				{
					loopableNeurons.add(ng);
				}
			}
			
			if(loopableNeurons.isEmpty())
			{
				return;
			}
			
			NeuronGene ng = loopableNeurons.get((int)(loopableNeurons.size()
					* Math.random()));
			ng.setRecurrent(true);
			
			fromNeuron = toNeuron = ng.getInnovationId();
			recurrent = true;
		}
		else
		{
			// trying to add a two neurons link
			
			ArrayList<LinkInnovation> possibleLinks = new ArrayList<LinkInnovation>();
			for(NeuronGene possibleNg1 : neurons.values())
			{
				for(NeuronGene possibleNg2 : neurons.values())
				{
					if(possibleNg1 != possibleNg2
							&& possibleNg2.getType() != NeuronType.bias
							&& possibleNg2.getType() != NeuronType.input
							&& !linkExists(possibleNg1.getInnovationId(), possibleNg2.getInnovationId()))
					{
						LinkInnovation li = new LinkInnovation(-1,
								possibleNg1.getInnovationId(), possibleNg2.getInnovationId());
						possibleLinks.add(li);
					}
				}
			}
			
			if(possibleLinks.isEmpty())
			{
				return;
			}
			
			LinkInnovation li = possibleLinks.get((int) (Math.random() * possibleLinks.size()));

			fromNeuron = li.getFromNeuron();
			toNeuron = li.getToNeuron();
			
			NeuronGene fromNg = neurons.get(fromNeuron);
			NeuronGene toNg = neurons.get(toNeuron);
			
			recurrent = toNg.getPosY() < fromNg.getPosY();
		}
		
		LinkInnovation li = innovations.getLinkInnovation(fromNeuron, toNeuron);
		LinkGene lg = new LinkGene(fromNeuron, toNeuron, Double.NaN, true, recurrent, li.getInnovationId());
		links.put(li.getInnovationId(), lg);
	}	

	// mutate to make a new neuron
	public void mutateNewNeuron()
	{
		if(Math.random() > NeatParams.newNeuronMutationRate)
		{
			// it will not mutate
			return;
		}
		
		// get all the links that could be used
		ArrayList<LinkGene> possibleLinks = new ArrayList<LinkGene>();
		for(LinkGene lg : links.values())
		{
			if(lg.isEnabled() && !lg.isRecurrent()
				&& neurons.get(lg.getFromNeuron()).getType() != NeuronType.bias)
			{
				possibleLinks.add(lg);
			}
		}
		
		// choose a link
		LinkGene lg = possibleLinks.get((int)(Math.random() * possibleLinks.size()));
		lg.setEnabled(false);
		
		int fromNeuron = lg.getFromNeuron();
		int toNeuron = lg.getToNeuron();
		double oldWeight = lg.getWeight();
		
		double newPosX = (neurons.get(fromNeuron).getPosX()
				+ neurons.get(toNeuron).getPosX()) / 2;
		double newPosY = (neurons.get(fromNeuron).getPosY()
				+ neurons.get(toNeuron).getPosY()) / 2;
		
		ArrayList<NeuronInnovation> neuronInnovations = 
				innovations.getNeuronInnovations(fromNeuron, toNeuron);
		
		// check if this neuron innovation already exists 
		NeuronGene newNeuron = null;
		for(NeuronInnovation ni : neuronInnovations)
		{
			if(!neuronExists(ni.getInnovationId()))
			{
				// this innovation is ok
				newNeuron = new NeuronGene(ni, newPosX, newPosY);
				break;
			}
		}
		
		if(newNeuron == null)
		{
			// we must make new innovation
			NeuronInnovation ni = innovations.makeNewNeuronInnovation(fromNeuron, toNeuron);
			newNeuron = new NeuronGene(ni, newPosX, newPosY);
		}
		
		neurons.put(newNeuron.getInnovationId(), newNeuron);
		
		// make links to new neuron
		int newNeuronId = newNeuron.getInnovationId();
		LinkInnovation li1 = innovations.getLinkInnovation(fromNeuron, newNeuronId);
		LinkInnovation li2 = innovations.getLinkInnovation(newNeuronId, toNeuron);
		
		LinkGene lg1 = new LinkGene(li1.getFromNeuron(), li1.getToNeuron(),
				1, true, false, li1.getInnovationId());
		LinkGene lg2 = new LinkGene(li2.getFromNeuron(), li2.getToNeuron(),
				oldWeight, true, false, li2.getInnovationId());
		
		links.put(lg1.getInnovationId(), lg1);
		links.put(lg2.getInnovationId(), lg2);
	}
	
	// mutate link weights
	public void mutateWeights()
	{
		for(LinkGene lg : links.values())
		{
			if(Math.random() < NeatParams.linkWeightMutationRate)
			{
				// we'll mutate this link weight
				if(Math.random() < NeatParams.replaceWeightMutationRate)
				{
					// we'll totally change this weight
					lg.totallyRandomizeWeight();
				}
				else
				{
					// we'll randomize this weight by small amount
					lg.smallRandomizeWeight();
				}
			}
		}
	}

	// mutate neuron activation response 
	public void mutateActivationResponse()
	{
		for(NeuronGene ng : neurons.values())
		{
			if(Math.random() < NeatParams.activationResponseMutationRate)
			{
				// we'll mutate this link weight
				ng.smallRandomizeActivationResponse();
			}
		}
	}
	
	// create the neural net phenotype
	public void createPhenotype()
	{
		phenotype = new NeuralNetBot(neurons, links);
	}

	public double getFitness() {
		return fitness;
	}

	public void setAdjustedFitness(double adjustedFitness) {
		this.adjustedFitness = adjustedFitness;
	}

	public double getAmountToSpawn() {
		return amountToSpawn;
	}

	public void setAmountToSpawn(double amountToSpawn) {
		this.amountToSpawn = amountToSpawn;
	}

	public void setSpecieId(int specieId) {
		this.specieId = specieId;
	}

	public int compareTo(Genome g)
	{
		if(fitness - g.fitness > 0) return 1;
		if(fitness - g.fitness < 0) return -1;
		return 0;
	}
}