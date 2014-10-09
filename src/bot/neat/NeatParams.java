package bot.neat;

public class NeatParams
{
	// link mutations
	public static double newLinkMutationRate = 0.3;
	public static double newLoopedLinkMutationRate = 0.05;
	
	// neuron mutations
	public static double newNeuronMutationRate = 0.3;
	
	// link weight mutations
	public static double linkWeightMutationRate = 0.05;
	public static double maxWeight = 5;
	public static double replaceWeightMutationRate = 0.1;
	public static double smallWeightRandomMove = 0.3;
	
	// neuron activation response mutation
	public static double activationResponseMutationRate = 0.05;
	public static double smallActivationResponseRandomMove = 0.1;
}
