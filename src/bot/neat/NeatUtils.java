package bot.neat;

public class NeatUtils
{
	
	public static double sigmoid(double x, double activationResponse)
	{
		return 1 / (1 + Math.pow(Math.E, -x/activationResponse));
	}

}
