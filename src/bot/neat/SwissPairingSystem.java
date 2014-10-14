package bot.neat;


public class SwissPairingSystem
{
	int teamsN;
	double[] scores;
	int[] whites;

	boolean[][] played;
	double[][] gameFitness;
	
	// score penalty is much bigger than whites penalty
	double scorePenaltyCoef = 1;
	double whitesPenaltyCoef = 0.1;
	// calculated in constructor as  opposite to worst penalty times coef
	double feasableFitness;
	double feasableFitnessCoef = 1;
	
	public SwissPairingSystem(int teamsN, int expectedK)
	{
		this.teamsN = teamsN;
		feasableFitness = (scorePenaltyCoef * expectedK
				+ whitesPenaltyCoef * 2 * expectedK) * feasableFitnessCoef;
		
		scores = new double[teamsN];
		whites = new int[teamsN];
		gameFitness = new double[teamsN][teamsN];
		played = new boolean[teamsN][teamsN];
		
		for(int i = 0; i < teamsN; i++)
		{
			scores[i] = 0;
			whites[i] = 0;
			for(int j = 0; j < teamsN; j++)
			{
				played[i][j] = false;
			}
			played[i][i] = true;
		}
	}
	
	// difference in their whites and black games
	private double getWhitesDiff(int player1, int player2)
	{
		return Math.abs(whites[player1] + whites[player2]);		
	}
	
	// difference with these two scores
	private double getScoresDiff(int player1, int player2)
	{
		return Math.abs(scores[player1] - scores[player2]);
	}
	
	// initialize how fitness change with chosen game
	private void initGameFitness()
	{
		for(int i = 0; i < teamsN; i++)
		{
			for(int j = 0; j < teamsN; j++)
			{
				if(played[i][j])
				{
					// it's impossible for them to play each other
					gameFitness[i][j] = 0;
				}
				else
				{
					// it's possible for them to play each other
					gameFitness[i][j] = feasableFitness -
							(getScoresDiff(i, j) * scorePenaltyCoef
							+ getWhitesDiff(i, j) * whitesPenaltyCoef);
				}
			}
		}
	}
	
	// are all pairings feasible? could they play each other?
	public boolean isPairingFeasable(int[][] pairings)
	{
		for(int i = 0; i < teamsN / 2; i++)
		{
			if(played[pairings[i][0]][pairings[i][1]])
			{
				return false;
			}
		}
		return true;
	}
	
	// how fit is this pairing?
	public double calcFitness(int[][] pairings)
	{
		double fitness = 0;
		for(int i = 0; i < teamsN / 2; i++)
		{
			fitness += gameFitness[ pairings[i][0] ][ pairings[i][1] ];
		}
		return fitness;
	}
	
	// for each pair, take an random team and find opponent with the biggest fit
	private int[][] makeGreedyPairings(int[] teamsLeft)
	{
		int pairsN = teamsLeft.length / 2;
		int[][] pairings = new int[pairsN][2];
		
		for(int i = 0; i < pairsN; i++)
		{
			int teamsN = (pairsN - i) * 2;
			
			int rand = (int) (Math.random() * teamsN);
			int team1 = teamsLeft[rand];
			teamsN--;
			teamsLeft[rand] = teamsLeft[teamsN];
			teamsLeft[teamsN] = team1;
			
			int bestTeam2Idx = -1;
			int bestTeam2 = -1;
			for(int team2Idx = 0; team2Idx < teamsN; team2Idx++)
			{
				int team2 = teamsLeft[team2Idx];
				if(bestTeam2Idx == -1
						|| gameFitness[team1][team2] > gameFitness[team1][bestTeam2])
				{
					bestTeam2Idx = team2Idx;
					bestTeam2 = team2;
				}
			}
			
			teamsN--;
			teamsLeft[bestTeam2Idx] = teamsLeft[teamsN];
			teamsLeft[teamsN] = bestTeam2;
			
			pairings[i][0] = team1;
			pairings[i][1] = bestTeam2;
		}
		
		// try to improve this solution by exchanging some two pairs
		// COULD BE OPTIMIZED BY REMEMBERING WHO WAS CHANGED AND TRYING THAT ONE INSTEAD OF ALL! 		
		boolean improved;
		do
		{
			improved = false;
			
			for(int pairI = 0; pairI < pairsN; pairI++)
			{
				for(int pairJ = pairI + 1; pairJ < pairsN; pairJ++)
				{
					int[] firstPair = pairings[pairI];
					int[] secondPair = pairings[pairJ];
							
					double currentFitness = gameFitness[firstPair[0]][firstPair[1]]
							+ gameFitness[secondPair[0]][secondPair[1]];
					
					double firstChangeFitness = gameFitness[firstPair[0]][secondPair[0]]
							+ gameFitness[firstPair[1]][secondPair[1]];
					double secondChangeFitness = gameFitness[firstPair[0]][secondPair[1]]
							+ gameFitness[secondPair[0]][firstPair[1]];
					
					if(currentFitness < firstChangeFitness
							|| currentFitness < secondChangeFitness)
					{
						if(firstChangeFitness > secondChangeFitness)
						{
							int help = firstPair[1];
							firstPair[1] = secondPair[0];
							secondPair[0] = help;
						}
						else
						{
							int help = firstPair[1];
							firstPair[1] = secondPair[1];
							secondPair[1] = help;
						}
						improved = true;
					}
				}
			}
		} while(improved);
				
		return pairings;
	}
	
	// to normalize the whites
	public int[][] sortPairsForPlay(int[][] pairings)
	{
		if(pairings == null)
		{
			return null;
		}
		
		for(int i = 0; i < pairings.length; i++)
		{
			if(whites[pairings[i][0]] > whites[pairings[i][1]])
			{
				int help = pairings[i][0];
				pairings[i][0] = pairings[i][1];
				pairings[i][1] = help;
			}
		}
		return pairings;
	}
		
	// make pairings for this round
	public int[][] makeNewPairings()
	{
		initGameFitness();
		
		// make a pairings via randomized greedy
		int randomizeN = 100;		
		
		// all teams
		int[] teams = new int[teamsN];
		for(int i = 0; i < teamsN; i++)
		{
			teams[i] = i;
		}		

		int[][] bestPairing = null;
		double bestFitness = Double.MIN_VALUE;
		for(int i = 0; i < randomizeN; i++)
		{
			int[][] thisPairing = makeGreedyPairings(teams);
			if(isPairingFeasable(thisPairing))
			{
				double thisFitness = calcFitness(thisPairing);
				if(bestPairing == null || thisFitness > bestFitness)
				{
					bestPairing = thisPairing;
					bestFitness = thisFitness;
				}				
			}
		}
		
		// it's possible we haven't found any feasible solution
		// this will rarely happen
		return sortPairsForPlay(bestPairing);
	}
	
	// update score for finished game
	public void gameFinished(int player1, int player2, double score1, double score2)
	{		
		// update they played with each other
		played[player1][player2] = played[player2][player1] = true;
		
		// update their scores
		if(score1 >= score2)
		{
			scores[player1] += (score1 > score2)? 1 : 0.5;
		}
		if(score2 >= score1)
		{
			scores[player2] += (score2 > score1)? 1 : 0.5;
		}
		
		// update their white positions
		whites[player1]++;
		whites[player2]--;
	}
}
