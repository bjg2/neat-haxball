package ai.neat.neural_nets;

import java.util.Map;
import java.util.TreeMap;

import ai.neat.NeatUtils;
import ai.neat.genes.LinkGene;
import ai.neat.genes.NeuronGene;
import ai.neat.genes.NeuronType;

public class NeuralNet 
{
	TreeMap<Integer, Neuron> neurons = new TreeMap<Integer, Neuron>();
	
	NeuralNetRunType runType;
	
	public NeuralNet(TreeMap<Integer, NeuronGene> neuronGenes, TreeMap<Integer, LinkGene> linkGenes,
			NeuralNetRunType runType)
	{
		// make neurons from neuron genes
		for(NeuronGene ng : neuronGenes.values())
		{
			neurons.put(ng.getInnovationId(), new Neuron(ng));
		}
		
		// make links from link genes
		for(LinkGene lg : linkGenes.values())
		{
			if(lg.isEnabled())
			{
				Neuron fromNeuron = neurons.get(lg.getFromNeuron());
				Neuron toNeuron = neurons.get(lg.getToNeuron());
				Link l = new Link(fromNeuron, toNeuron, lg.getWeight(), lg.isRecurrent());
				
				fromNeuron.addOutLink(l);
				toNeuron.addInLink(l);
			}
		}
		
		this.runType = runType;
	}
	
	// get the outputs from the inputs
	public double[] update(double[] inputs)
	{
		if(runType == NeuralNetRunType.Active)
		{
			// every time network is running activation needs to pass whole network
			// let's delete everything from the last pass
			
			for(Neuron n : neurons.values())
			{
				n.output = 0;
				n.activated = false;
			}
		}
		
		// set outputs and activation for input neurons
		int inputI = 0;
		for(Map.Entry<Integer, Neuron> neuronEntry : neurons.entrySet())
		{
			Neuron n = neuronEntry.getValue();
			if(n.type == NeuronType.input)
			{
				n.output = inputs[inputI++];
				n.activated = true;
			}
			if(n.type == NeuronType.bias)
			{
				n.output = 1;
				n.activated = true;
			}
		}
		
		boolean outputNodesActivated = false;
		while(!outputNodesActivated)
		{
			// repeat this code while output is not activated from every input neuron
			// it will happen network depth times if run type is active
			// or if doing snapshot the first time
			// one time otherwise
			
			outputNodesActivated = true;

			for(Neuron n : neurons.values())
			{
				if(n.type != NeuronType.bias && n.type != NeuronType.input)
				{
					// calculating activation sum for this neuron
					n.activationSum = 0;
					
					for(Link inLink : n.inLinks)
					{
						if(inLink.fromNeuron.activated)
						{
							// add input activation sum and set this neuron activated
							n.activationSum += inLink.fromNeuron.output * inLink.weight;
							n.activated = true;
						}
					}
				}
			}
			
			for(Neuron n : neurons.values())
			{
				if(n.type == NeuronType.output && !n.activated)
				{
					// this output node is not activated
					outputNodesActivated = false;					
				}
				
				if(n.type != NeuronType.bias && n.type != NeuronType.input && n.activated)
				{
					// calculate new outputs for neurons
					n.output = NeatUtils.sigmoid(n.activationSum, n.activationResponse);
				}
			}
		}
		
		// get how many outputs is there
		int outputN = 0;
		for(Map.Entry<Integer, Neuron> neuronEntry : neurons.entrySet())
		{
			Neuron n = neuronEntry.getValue();
			if(n.type == NeuronType.output)
			{
				outputN++;
			}
		}
		double[] outputs = new double[outputN];
		
		// get all the outputs
		int outputI = 0;
		for(Map.Entry<Integer, Neuron> neuronEntry : neurons.entrySet())
		{
			Neuron n = neuronEntry.getValue();
			if(n.type == NeuronType.output)
			{
				outputs[outputI++] = n.output;
			}
		}
		
		return outputs;
	}
}
