package ai.neat.neural_nets;

public enum NeuralNetRunType
{
	Snapshot,	// updates only one layer at the time
	Active		// updates every layer from inputs to outputs, like in other neural nets
}
