package ai.neat.bot;

import haxball.Utils;

import org.jbox2d.common.Vec2;

import ai.GameState;

public class FitnessCalculator
{
	// testing on only being close to the ball that is close to the enemy goal
	public static double scoreW = 0;
	public static double ballPlayerDistW = 0.01;
	public static double ballGoalDistW = 0.01;
	public static double attackW = 0;
	public static double defenceW = 0;
	
	double scoreFitness;
	double ballPlayerDistFitness;
	double ballGoalDistFitness;
	double attackFitness;
	double defenceFitness;
	
	double fitness;
	double fitnessSum;
	double goalsGiven;
	double goalsReceived;
	
	public void gameStared()
	{
		scoreFitness = ballPlayerDistFitness = ballGoalDistFitness = attackFitness = defenceFitness = 0;
	}
	
	public void move(GameState state)
	{
		if(!state.isPlaying)
		{
			// goal has just been scored
			return;
		}
		
		Vec2 enemyPlayerPos = state.enemyPlayersPos[0];
		
		double myBallDist = state.myPlayerPos.sub(state.ballPos).length();
		double enemyBallDist = enemyPlayerPos.sub(state.ballPos).length();
		
		// me closer to the ball the better
		ballPlayerDistFitness -= ballPlayerDistW * myBallDist;
		
		// enemy further from the ball the better
		//ballPlayerDistFitness += ballPlayerDistW * enemyBallDist;
		
		Vec2 myGoalCenter = new Vec2((GameState.realFieldW - GameState.fieldW) / 2,
				GameState.realFieldH / 2);
		Vec2 enemyGoalCenter = new Vec2((GameState.realFieldW + GameState.fieldW) / 2,
				GameState.realFieldH / 2);
		double ballMyGoalDist = myGoalCenter.sub(state.ballPos).length();
		double ballEnemyGoalDist = enemyGoalCenter.sub(state.ballPos).length();

		// ball closer to the enemy goal the better
		ballGoalDistFitness -= ballGoalDistW * ballEnemyGoalDist;

		// ball further from my goal the better
		//ballGoalDistFitness += ballGoalDistW * ballMyGoalDist;
		
		// how good am i attacking
		attackFitness += attackW * attackFitness(state.myPlayerPos, enemyPlayerPos,
				state.ballPos, enemyGoalCenter);
		
		// how good am i defending
		defenceFitness -= defenceW * attackFitness(enemyPlayerPos, state.myPlayerPos,
				state.ballPos, myGoalCenter);
	}
	
	public void gameFinished(GameState state)
	{
		// better score better bot
		scoreFitness += (state.myScore - state.enemyScore) * scoreW;
		
		fitness = scoreFitness + ballPlayerDistFitness + ballGoalDistFitness + attackFitness + defenceFitness;
		
		fitnessSum += fitness;
		goalsGiven += state.myScore;
		goalsReceived += state.enemyScore;
	}
	
	// calculate attack fitness for resolved ball point
	// (original point or mirrored)
	public double attackFitnessForBall(Vec2 myPlayerPos, Vec2 enemyPlayerPos,
			Vec2 ballPos, Vec2 enemyGoalPos)
	{
		double playerBallDist = myPlayerPos.sub(ballPos).length();
		double goalBallDist = enemyGoalPos.sub(ballPos).length();
		
		// the enemy is further away from possible goal shot - the better
		// my player closer to the ball - the better
		// the ball is closer to the enemy goal - the better 
		return Utils.distFromLineSegment(ballPos, enemyGoalPos, enemyPlayerPos)
				/ playerBallDist
				/ goalBallDist;
	}
	
	// calculate attack fitness for ball 
	public double attackFitness(Vec2 myPlayerPos, Vec2 enemyPlayerPos,
			Vec2 ballPos, Vec2 goalPos)
	{
		// default pos
		Vec2 thisBallPos = ballPos;
		double ret = attackFitnessForBall(myPlayerPos, enemyPlayerPos,
				thisBallPos, goalPos);

		// ball mirrored up
		thisBallPos = new Vec2(ballPos.x,
				(GameState.realFieldH - GameState.fieldH) / 2
				- (ballPos.y - (GameState.realFieldH - GameState.fieldH) / 2));
		ret += attackFitnessForBall(myPlayerPos, enemyPlayerPos,
				thisBallPos, goalPos);

		// ball mirrored down
		thisBallPos = new Vec2(ballPos.x,
				(GameState.realFieldH + GameState.fieldH) / 2
				+ ((GameState.realFieldH + GameState.fieldH) / 2 - ballPos.y));
		ret += attackFitnessForBall(myPlayerPos, enemyPlayerPos,
				thisBallPos, goalPos);
		
		return ret;
	}
	
	public String toString()
	{
		return "fitness: " + fitness
				+ " scoreFitness: " + scoreFitness
				+ " ballPlayerDistFitness: " + ballPlayerDistFitness
				+ " ballGoalDistFitness: " + ballGoalDistFitness
				+ " attackFitness: " + attackFitness
				+ " defenceFitness: " + defenceFitness;
	}
}
