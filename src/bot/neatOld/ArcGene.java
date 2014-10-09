package bot.neatOld;

public class ArcGene
{
	int node1, node2;
	double w;
	boolean active;

	public ArcGene(String geneString)
	{
		String[] params = geneString.split(" ");
		this.node1 = Integer.parseInt(params[0]);
		this.node2 = Integer.parseInt(params[1]);
		this.w = Double.parseDouble(params[2]);
		this.active = Boolean.parseBoolean(params[3]);
	}
	
	public ArcGene(ArcGene gene)
	{
		this.node1 = gene.node1;
		this.node2 = gene.node2;
		this.w = gene.w;
		this.active = gene.active;
	}
	
	public ArcGene(int node1, int node2)
	{
		this.node1 = node1;
		this.node2 = node2;
		randomizeW();
		this.active = true;
	}
	
	public ArcGene(int node1, int node2,	double w, boolean active)
	{
		this.node1 = node1;
		this.node2 = node2;
		this.w = w;
		this.active = active;
	}
	
	// totally random value
	public void randomizeW()
	{
		w = (Math.random() - 0.5) * 2 * NeatTrainer.maxW;
	}
	
	// move the random value slightly
	public void smallRandomizeW(double smallD)
	{
		w += (Math.random() - 0.5) * smallD;
		
		if(w > NeatTrainer.maxW)
		{
			w = NeatTrainer.maxW;
		}
		if(w < -NeatTrainer.maxW)
		{
			w = -NeatTrainer.maxW;
		}
	}
	
	public int hashCode()
	{
		return node1 * 10000 + node2;
	}
	
	public boolean equals(Object obj)
	{
		if(!(obj instanceof ArcGene))
		{
			return false;
		}
		
		ArcGene gene = (ArcGene) obj;
		return gene.node1 == node1
				&& gene.node2 == node2;
	}
	
	@Override
	public String toString()
	{
		return node1 + " " + node2 + " " + w + " " + active;
	}
}
