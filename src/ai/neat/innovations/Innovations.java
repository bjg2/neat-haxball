package ai.neat.innovations;

import haxball.Logger;

import java.util.ArrayList;
import java.util.HashMap;

import ai.neat.genes.NeuronType;

public class Innovations
{
	// link innovations
	
	ArrayList<LinkInnovation> initialLinks = new ArrayList<LinkInnovation>();
	// because there is no get in HashSet i use HashMap...
	HashMap<LinkInnovation, LinkInnovation> linkInnovations =
			new HashMap<LinkInnovation, LinkInnovation>();
	int newLinkInnovationId = 0;
	
	ArrayList<NeuronInnovation> initialNeurons = new ArrayList<NeuronInnovation>();
	HashMap<LinkInnovation, ArrayList<NeuronInnovation>> neuronInnovations
		= new HashMap<LinkInnovation, ArrayList<NeuronInnovation>>();
	int newNeuronInnovationId = 0;
	
	int newGenomeId = 0;
	int newSpecieId = 0;
	
	public Innovations(int inputNum, int outputNum)
	{
		for(int inputI = 0; inputI < inputNum; inputI++)
		{
			for(int outputI = 0; outputI < outputNum; outputI++)
			{
				LinkInnovation li = new LinkInnovation(newLinkInnovationId++,
						inputI, inputNum + outputI);
				linkInnovations.put(li, li);
				initialLinks.add(li);
			}
		}
		
		initialNeurons.add(new NeuronInnovation(newNeuronInnovationId++, NeuronType.bias));
		for(int i = 1; i < inputNum; i++)
		{
			initialNeurons.add(new NeuronInnovation(newNeuronInnovationId++,
					NeuronType.input));
		}
		for(int i = 0; i < outputNum; i++)
		{
			initialNeurons.add(new NeuronInnovation(newNeuronInnovationId++,
					NeuronType.output));
		}
	}
	
	// is this innovation already made?
	public boolean linkInnovationExists(int fromNeuron, int neuronOut)
	{
		LinkInnovation li = new LinkInnovation(newLinkInnovationId,
				fromNeuron, neuronOut);
		
		if(linkInnovations.containsKey(li))
		{
			return true;
		}
		return false;
	}
	
	// get right link from neuronIn to neuronOut
	public LinkInnovation getLinkInnovation(int fromNeuron, int toNeuron)
	{
		LinkInnovation li = new LinkInnovation(newLinkInnovationId,
				fromNeuron, toNeuron);
		
		if(linkInnovations.containsKey(li))
		{
			// this innovation already exists
			return linkInnovations.get(li);
		}
		
		// this is new innovation
		newLinkInnovationId++;
		linkInnovations.put(li, li);
		return li;
	}
	
	// has someone made this innovation before?
	public boolean neuronInnovationExists(int fromNeuron, int toNeuron)
	{
		LinkInnovation li = new LinkInnovation(-1, fromNeuron, toNeuron);
		
		if(!neuronInnovations.containsKey(li) || neuronInnovations.get(li).isEmpty())
		{
			return false;
		}
		
		return true;
	}
	
	// get all the innovations make between these two neurons
	// in order they are invented
	public ArrayList<NeuronInnovation> getNeuronInnovations
										(int fromNeuron, int toNeuron)
	{
		LinkInnovation li = new LinkInnovation(-1, fromNeuron, toNeuron);
		
		if(!neuronInnovations.containsKey(li))
		{
			neuronInnovations.put(li, new ArrayList<NeuronInnovation>());
		}
		
		return neuronInnovations.get(li);
	}
	
	// make new neuron innovation
	// (this is the first innovation or all the old ones were already in the genome)
	public NeuronInnovation makeNewNeuronInnovation(int fromNeuron, int toNeuron)
	{
		NeuronInnovation ni = new NeuronInnovation(newNeuronInnovationId, NeuronType.hidden);
		LinkInnovation li = new LinkInnovation(-1, fromNeuron, toNeuron);
		
		if(!neuronInnovations.containsKey(li))
		{
			neuronInnovations.put(li, new ArrayList<NeuronInnovation>());
		}
		
		neuronInnovations.get(li).add(ni);
		newNeuronInnovationId++;
		return ni;
	}
	
	public void saveInnovations(String savePath)
	{
		String innovationsText = "newLinkInnovationId: " + newLinkInnovationId + "\r\n";
		innovationsText += "newNeuronInnovationId: " + newNeuronInnovationId + "\r\n";
		innovationsText += "newGenomeId: " + newGenomeId + "\r\n";
		innovationsText += "newSpecieId: " + newSpecieId + "\r\n";
		
		innovationsText += "neuronInnovations:\r\n";
		for(NeuronInnovation ni : initialNeurons)
		{
			innovationsText += ni + "\r\n";				
		}
		for(ArrayList<NeuronInnovation> innovations : neuronInnovations.values())
		{
			for(NeuronInnovation ni : innovations)
			{
				innovationsText += ni + "\r\n";				
			}
		}
		
		innovationsText += "linkInnovations:\r\n";
		for(LinkInnovation li : linkInnovations.values())
		{
			innovationsText += li + "\r\n";
		}
		
		Logger.logToFile(savePath + "/innovations.txt", innovationsText);
	}
	
	public int getNewGenomeId()
	{
		return newGenomeId++;
	}
	
	public int getNewSpecieId()
	{
		return newSpecieId++;
	}

	public ArrayList<LinkInnovation> getInitialLinks()
	{
		return initialLinks;
	}

	public ArrayList<NeuronInnovation> getInitialNeurons()
	{
		return initialNeurons;
	}
}

