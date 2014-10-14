package bot.neat;

public class NeatParams
{
	// link mutations
	public static double newLinkMutationRate = 0.07;
	public static double newLoopedLinkMutationRate = 0.05;
	
	// neuron mutations
	public static double newNeuronMutationRate = 0.03;
	
	// link weight mutations
	public static double linkWeightMutationRate = 0.2;
	public static double maxWeight = 5;
	public static double replaceWeightMutationRate = 0.1;
	public static double smallWeightRandomMove = 0.5;
	
	// neuron activation response mutation
	public static double activationResponseMutationRate = 0.05;
	public static double smallActivationResponseRandomMove = 0.1;
	
	// compatibility distance calculation parameters
	public static double disjointCoef = 1;
	public static double excessCoef = 1;
	public static double matchedCoef = 0.4;
	public static double specieCompatibilityTreshold = 0.26;
	
	// specie fitness adjustment parameters
	public static double youngAgeThreshold = 10;
	public static double youngAgeBonus = 1.3;
	public static double oldAgeThreshold = 50;
	public static double oldAgePenalty = 0.7;
	
	public static int generationWithoutImprovementMaxNum = 15; 
	
	// num of inputs and outputs
	public static int inputNodesN = 13;
	public static int outputNodesN = 3;
	
	// genetic algo params
	public static int populationSize = 100;
	public static int numberOfRounds = 20;
	public static double populationPercentMating = 0.2;
	public static double crossoverRate = 0.7;
}
