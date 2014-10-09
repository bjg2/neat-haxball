package bot;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;

public class GameState {

	// gameplay consts
	public static float fieldW = 740;
	public static float fieldH = 340;
	public static float realFieldW = 840;
	public static float realFieldH = 406;
	
	public static float ballR = 10;
	public static float playerR = 15;
	public static float playerShotR = 20;
	public static float goalpostR = 8;
	public static float goalW = 130;
	public static float centerR = 75;
	
	public static float maxPlayerAcceleration = 100;
	public static float maxPlayerVelocity = 958.95465f;
	public static float maxShootAcceleration = 2000;
	public static float maxShootVelocity = 1339.6011f;
	
	public static float friction = 0.15f;
	public static float ballRestitution = 0.5f;
	public static float ballDensity = 0.2f;
	public static float playerRestitution = 0.05f;
	public static float playerDensity = 0.75f;

	public static int numOfPlayers = 1;
	
	public static int gameTime = 60 * 60 * 5; // 5 mins, 60 fps
	public static int goalCooloutTime = 60 * 5; // 5 secs, 60 fps
	
	public static float shootAngDiff = PApplet.PI / 10;
	
	// changing state
	public Vec2 myPlayerPos, myPlayerVelocity;
	public Vec2[] myCoPlayersPos, myCoPlayersVelocity;
	public Vec2[] enemyPlayersPos, enemyPlayersVelocity;
	public Vec2 ballPos, ballVelocity;
	public int ballFor, myScore, enemyScore, movesLeft;
	public boolean isPlaying;
	
	// help
	Vec2[] myPlayersPos, myPlayersVelocity;
	
	public GameState(Vec2[] myPlayersPos, Vec2[] myPlayersVelocity,
			Vec2[] enemyPlayersPos, Vec2[] enemyPlayersVelocity,
			Vec2 ballPos, Vec2 ballVelocity,
			int ballFor, int myScore, int enemyScore, int movesLeft, boolean isPlaying)
	{
		this.myPlayersPos = myPlayersPos;
		this.myPlayersVelocity = myPlayersVelocity;
		this.enemyPlayersPos = enemyPlayersPos;
		this.enemyPlayersVelocity = enemyPlayersVelocity;
		this.ballPos = ballPos;
		this.ballVelocity = ballVelocity;
		this.ballFor = ballFor;
		this.myScore = myScore;
		this.enemyScore = enemyScore;
		this.movesLeft = movesLeft;
		this.isPlaying = isPlaying;
		
		myCoPlayersPos = new Vec2[numOfPlayers - 1];
		myCoPlayersVelocity = new Vec2[numOfPlayers - 1];
		
		for(int i = 0; i < numOfPlayers - 1; i++)
		{
			myCoPlayersPos[i] = myPlayersPos[i];
			myCoPlayersVelocity[i] = myPlayersVelocity[i];
		}
	}
	
	// pick a player among players in the team as myPlayer 
	public void pickPlayer(int i)
	{
		myPlayerPos = myPlayersPos[i];
		myPlayerVelocity = myPlayersVelocity[i];
		
		int lastI = i - 1;
		if(lastI < 0)
		{
			lastI += numOfPlayers;
		}
		
		if(numOfPlayers > 1)
		{
			myCoPlayersPos[i % (numOfPlayers - 1)] = myPlayersPos[lastI];
			myCoPlayersVelocity[i % (numOfPlayers - 1)] = myPlayersVelocity[lastI];
		}
	}
	
}
