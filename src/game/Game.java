package game;

import haxball.Utils;

import java.util.HashMap;

import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;
import shiffman.box2d.Box2DProcessing;
import ai.ABot;
import ai.GameMove;
import ai.GameState;

public class Game
{
	PImage fieldImage;
	Team[] teams = new Team[2];
	int[] score = new int[2];
	Ball ball;
	PApplet parent;
	int ballFor;
	int timeLeft;
	int goalCooloutTimeLeft;
	boolean finished;
	
	Body[] playersStartEdgeBodies = new Body[2];
	
	Box2DProcessing box2d;
	
	public Game(PApplet parent, ABot bot1, ABot bot2)
	{
		box2d = new Box2DProcessing(parent);
		box2d.createWorld();
		box2d.setGravity(0, 0);

		this.parent = parent;
		fieldImage = parent.loadImage("field.png");

		addStaticObjects();
				
		// teams
		float playerCenterDist = parent.random(GameState.centerR,
				GameState.fieldW / 2 - GameState.centerR);
		
		teams[0] = new Team(parent, box2d, 0, bot1, playerCenterDist);
		teams[1] = new Team(parent, box2d, 1, bot2, playerCenterDist);
		
		// ball
		ball = new Ball(parent, box2d, new PVector(GameState.realFieldW / 2, GameState.realFieldH / 2));
		
		timeLeft = GameState.gameTime;
		finished = false;
		
		ballFor = 0;
		reset();
	}
	
	public void reset()
	{
		teams[0].reset();
		teams[1].reset();
		ball.reset();
		
		playersStartEdgeBodies[ballFor].setActive(true);
	}
	
	public void addStaticObjects()
	{
		// goalposts
		float leftGoalpostX = (GameState.realFieldW - GameState.fieldW) / 2;
		float rightGoalpostX = GameState.realFieldW - leftGoalpostX;
		float upGoalpostY = (GameState.realFieldH - GameState.goalW) / 2;
		float downGoalpostY = GameState.realFieldH - upGoalpostY;
		
		addGoalpost(new PVector(leftGoalpostX, upGoalpostY));
		addGoalpost(new PVector(leftGoalpostX, downGoalpostY));
		addGoalpost(new PVector(rightGoalpostX, upGoalpostY));
		addGoalpost(new PVector(rightGoalpostX, downGoalpostY));
		
		addFieldEdge();
		addBallEdge();
	
		addPlayerStartEdge(0);
		addPlayerStartEdge(1);
	}
	
	public void addPlayerStartEdge(int teamId)
	{
		int numOfCirclePoints = 10;
		Vec2[] edgeVertices = new Vec2[numOfCirclePoints + 3];
		edgeVertices[0] = new Vec2(box2d.coordPixelsToWorld(GameState.realFieldW / 2, 0));
		
		float ang = PApplet.PI / 2;
		float angMove = PApplet.PI / numOfCirclePoints;
		if(teamId == 0)
		{
			angMove = -angMove;
		}

		Vec2 center = new Vec2(GameState.realFieldW / 2, GameState.realFieldH / 2);
		for(int i = 0; i <= numOfCirclePoints; i++, ang += angMove)
		{
			edgeVertices[i + 1] = box2d.coordPixelsToWorld(center.add( (new Vec2(PApplet.cos(ang), PApplet.sin(ang))).mul(GameState.centerR) ));
		}
		edgeVertices[numOfCirclePoints + 2] = new Vec2(box2d.coordPixelsToWorld(GameState.realFieldW / 2, GameState.realFieldH));
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.fixedRotation = true;
		bodyDef.type = BodyType.KINEMATIC;
		playersStartEdgeBodies[teamId] = box2d.createBody(bodyDef);
		ChainShape edgeChain = new ChainShape();
		edgeChain.createChain(edgeVertices, edgeVertices.length);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = edgeChain;
		
		fixtureDef.filter.categoryBits = Utils.playersStartEdgeCategory[teamId];
		fixtureDef.filter.maskBits = Utils.playersStartEdgeMask[teamId];
		playersStartEdgeBodies[teamId].createFixture(fixtureDef);
		
		playersStartEdgeBodies[teamId].setActive(false);
	}
	
	public void addFieldEdge()
	{
		Vec2[] edgeVertices = new Vec2[4];
		edgeVertices[0] = new Vec2(box2d.coordPixelsToWorld(0, 0));
		edgeVertices[1] = new Vec2(box2d.coordPixelsToWorld(GameState.realFieldW, 0));
		edgeVertices[2] = new Vec2(box2d.coordPixelsToWorld(GameState.realFieldW, GameState.realFieldH));
		edgeVertices[3] = new Vec2(box2d.coordPixelsToWorld(0, GameState.realFieldH));

		BodyDef bodyDef = new BodyDef();
		bodyDef.fixedRotation = true;
		bodyDef.type = BodyType.KINEMATIC;
		Body body = box2d.createBody(bodyDef);
		ChainShape edgeChain = new ChainShape();
		edgeChain.createLoop(edgeVertices, edgeVertices.length);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = edgeChain;
		
		fixtureDef.filter.categoryBits = Utils.fieldEdgeCategory;
		fixtureDef.filter.maskBits = Utils.fieldEdgeMask;
		body.createFixture(fixtureDef);
	}
	
	public void addBallEdge()
	{
		Vec2[] ballEdgeVertices = new Vec2[16];
		float innerFieldX = (GameState.realFieldW - GameState.fieldW) / 2;
		float innerFieldY = (GameState.realFieldH - GameState.fieldH) / 2;
		float innerEndFieldX = GameState.realFieldW - innerFieldX;
		float innerEndFieldY = GameState.realFieldH - innerFieldY;
		
		ballEdgeVertices[0] = new Vec2(box2d.coordPixelsToWorld(innerFieldX, innerFieldY));
		ballEdgeVertices[1] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX, innerFieldY));
		
		ballEdgeVertices[2] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX, 140));
		ballEdgeVertices[3] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX + 20, 140));
		ballEdgeVertices[4] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX + 30, 155));
		ballEdgeVertices[5] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX + 30, 250));
		ballEdgeVertices[6] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX + 20, 265));
		ballEdgeVertices[7] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX, 265));
				
		ballEdgeVertices[8] = new Vec2(box2d.coordPixelsToWorld(innerEndFieldX, innerEndFieldY));
		ballEdgeVertices[9] = new Vec2(box2d.coordPixelsToWorld(innerFieldX, innerEndFieldY));

		ballEdgeVertices[10] = new Vec2(box2d.coordPixelsToWorld(innerFieldX, 265));
		ballEdgeVertices[11] = new Vec2(box2d.coordPixelsToWorld(innerFieldX - 20, 265));
		ballEdgeVertices[12] = new Vec2(box2d.coordPixelsToWorld(innerFieldX - 30, 250));
		ballEdgeVertices[13] = new Vec2(box2d.coordPixelsToWorld(innerFieldX - 30, 155));
		ballEdgeVertices[14] = new Vec2(box2d.coordPixelsToWorld(innerFieldX - 20, 140));
		ballEdgeVertices[15] = new Vec2(box2d.coordPixelsToWorld(innerFieldX, 140));
		
		BodyDef bodyDef = new BodyDef();
		bodyDef.fixedRotation = true;
		bodyDef.type = BodyType.STATIC;
		Body body = box2d.createBody(bodyDef);
		ChainShape ballEdgeChain = new ChainShape();
		ballEdgeChain.createLoop(ballEdgeVertices, ballEdgeVertices.length);
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = ballEdgeChain;
		
		fixtureDef.filter.categoryBits = Utils.ballFieldEdgeCategory;
		fixtureDef.filter.maskBits = Utils.ballFieldEdgeMask;		
		body.createFixture(fixtureDef);		
	}
	
	public void addGoalpost(PVector pos)
	{
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(box2d.coordPixelsToWorld(pos));
		bodyDef.fixedRotation = true;
		bodyDef.type = BodyType.KINEMATIC;
		Body body = box2d.createBody(bodyDef);
		Shape shape = new CircleShape();
		shape.setRadius( box2d.scalarPixelsToWorld(GameState.goalpostR) );
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		
		fixtureDef.filter.categoryBits = Utils.fieldEdgeCategory;
		fixtureDef.filter.maskBits = Utils.fieldEdgeMask;
		body.createFixture(fixtureDef);
	}
	
	public void drawBackground()
	{
		parent.background(Utils.backgroundColor);
		parent.image(fieldImage, Utils.fieldStartX, Utils.fieldStartY);		
	}
	
	Vec2[] reverseX(Vec2[] in)
	{
		Vec2[] out = new Vec2[in.length];
		for(int i = 0; i < in.length; i++)
		{
			out[i] = new Vec2(GameState.realFieldW - 1 - in[i].x, in[i].y);
		}
		return out;
	}
	
	public void update(HashMap<Integer, Boolean> keyCodesPressed,
			HashMap<Character, Boolean> keysPressed)
	{
		if(timeLeft <= 0)
		{		
			return;
		}
		
		Vec2[][] playersPos = new Vec2[2][];
		Vec2[][] playersVelocity = new Vec2[2][];
		for(int i = 0; i < 2; i++)
		{
			playersPos[i] = teams[i].getPlayersPos();
			playersVelocity[i] = teams[i].getPlayersVelocity();
		}

		Vec2 ballPos = ball.getPosition();
		Vec2 ballVelocity = ball.getVelocity();
		
		for(int teamId = 0; teamId < teams.length; teamId++)
		{
			int keyboardXMove = 0;
			int keyboardYMove = 0;
			boolean keyboardShoot = false;
			
			if(keyCodesPressed != null)
			{
				if((Utils.leftKeyCode[teamId] != -1 && keyCodesPressed.get(Utils.leftKeyCode[teamId]))
						|| (Utils.leftKeyCode[teamId] == -1 && keysPressed.get(Utils.leftKey[teamId])))
						{
							keyboardXMove--;
						}
				
				if((Utils.rightKeyCode[teamId] != -1 && keyCodesPressed.get(Utils.rightKeyCode[teamId]))
						|| (Utils.rightKeyCode[teamId] == -1 && keysPressed.get(Utils.rightKey[teamId])))
						{
							keyboardXMove++;
						}
				
				if((Utils.upKeyCode[teamId] != -1 && keyCodesPressed.get(Utils.upKeyCode[teamId]))
						|| (Utils.upKeyCode[teamId] == -1 && keysPressed.get(Utils.upKey[teamId])))
						{
							keyboardYMove--;
						}
				
				if((Utils.downKeyCode[teamId] != -1 && keyCodesPressed.get(Utils.downKeyCode[teamId]))
						|| (Utils.downKeyCode[teamId] == -1 && keysPressed.get(Utils.downKey[teamId])))
						{
							keyboardYMove++;
						}
				
				if((Utils.shootKeyCode[teamId] != -1 && keyCodesPressed.get(Utils.shootKeyCode[teamId]))
						|| (Utils.shootKeyCode[teamId] == -1 && keysPressed.get(Utils.shootKey[teamId])))
						{
							keyboardShoot = true;
						}
			}			
			
			GameMove keyboardMove = new GameMove(keyboardXMove, keyboardYMove, keyboardShoot);

			Vec2[] myPlayersPos = playersPos[teamId];
			Vec2[] enemyPlayersPos = playersPos[(teamId == 0)? 1 : 0];
			Vec2[] myPlayersVelocity = playersVelocity[teamId];
			Vec2[] enemyPlayersVelocity = playersVelocity[(teamId == 0)? 1 : 0];
			Vec2 myBallPos = new Vec2(ballPos);
			Vec2 myBallVelocity = new Vec2(ballVelocity);
			
			if(teamId == 1)
			{
				myPlayersPos = reverseX(myPlayersPos);
				enemyPlayersPos = reverseX(enemyPlayersPos);
				myPlayersVelocity = reverseX(myPlayersVelocity);
				enemyPlayersVelocity = reverseX(enemyPlayersVelocity);
				myBallPos.x = GameState.realFieldW - 1 - myBallPos.x;
				myBallVelocity.x = GameState.realFieldW - 1 - myBallVelocity.x;
			}
			
			GameState state = new GameState(myPlayersPos, myPlayersVelocity,
					enemyPlayersPos, enemyPlayersVelocity,
					myBallPos, myBallVelocity,
					ballFor, score[teamId], score[(teamId == 0)? 1 : 0], timeLeft,
					goalCooloutTimeLeft <= 0);
			
			teams[teamId].makeMove(keyboardMove, state);
			
			for(Player pl : teams[teamId].getPlayers())
			{
				if(pl.isShooting()
						&& pl.getPosition().sub(ballPos).length()
							< GameState.ballR + GameState.playerShotR)
				{
					ball.kickFrom(pl.getPosition());
				}
			}
		}

		box2d.step();
		
		if(goalCooloutTimeLeft > 0)
		{
			goalCooloutTimeLeft--;
			return;
		}
		if(goalCooloutTimeLeft == 0)
		{
			reset();
			goalCooloutTimeLeft--;
			return;
		}
		
		if(!ball.checkTouched())
		{
			//return;
		}
		else
		{
			playersStartEdgeBodies[0].setActive(false);
			playersStartEdgeBodies[1].setActive(false);
			ballFor = -1;
		}
		timeLeft--;
		
		int goal = ball.checkGoal();
		if(goal != -1)
		{
			score[goal]++;
			goalCooloutTimeLeft = GameState.goalCooloutTime;
			ballFor = (goal == 0)? 1 : 0;
			return;
		}

		// send the bots that game is finished
		if(timeLeft == 0)
		{
			for(int teamId = 0; teamId < 2; teamId++)
			{
				GameState state = new GameState(null, null,
						null, null,
						null, null,
						ballFor, score[teamId], score[(teamId == 0)? 1 : 0], timeLeft,
						false);
				teams[teamId].gameFinished(state);
			}
		}
	}
	
	public String scoreString()
	{
		return score[0] + ":" + score[1];
	}
	
	public void drawScore()
	{
		float space = Utils.scoreSpace;
		float startX = Utils.fieldStartX + space;
		float startY = Utils.fieldStartY - space;
		
		parent.noStroke();
		if(ballFor == 0 && goalCooloutTimeLeft <= 0)
		{
			parent.stroke(255);
			parent.strokeWeight(3);
		}
		parent.fill(Utils.teamColor[0]);
		parent.rect(startX, startY - Utils.scoreSquareA,
				Utils.scoreSquareA, Utils.scoreSquareA, space);
		
		startX += Utils.scoreSquareA + space;
		
		float textY = startY - (Utils.scoreH - Utils.scoreTextH);
		
		String s = score[0] + " " + score[1];
		parent.fill(255);
		parent.textFont(Utils.scoreFont);
		parent.text(s, startX, textY);
		
		startX += parent.textWidth(s) + space;
		parent.noStroke();
		if(ballFor == 1 && goalCooloutTimeLeft <= 0)
		{
			parent.stroke(255);
			parent.strokeWeight(3);
		}
		parent.fill(Utils.teamColor[1]);
		parent.rect(startX, startY - Utils.scoreSquareA,
				Utils.scoreSquareA, Utils.scoreSquareA, space);
		
		startX += parent.textWidth(s) + 5 * space;
		
		if(timeLeft <= 0)
		{
			parent.fill(255);
			s = "GAME FINISHED!";
			parent.text(s, startX, textY);
		}
		
		int timeSeconds = (int)(PApplet.ceil(timeLeft / 60.0f));
		int timeMinutes = timeSeconds / 60;
		timeSeconds = timeSeconds % 60;
		
		s = timeMinutes + ":";
		if(timeSeconds < 10)
		{
			s += 0;
		}
		s += timeSeconds;
		startX = Utils.fieldStartX + GameState.realFieldW - parent.textWidth(s) - space;
		parent.fill(255);
		parent.text(s, startX, textY);
		
		if(goalCooloutTimeLeft > 0)
		{
			s = "GOAAAAL!";
			startX -= parent.textWidth(s) + 5 * space;
			parent.text(s, startX, textY);			
		}
	}
	
	public boolean isGameFinished()
	{
		return timeLeft <= 0;
	}
	
	public void draw()
	{
		drawBackground();
		for(Team t : teams)
		{
			t.draw();
		}
		ball.draw();
		drawScore();
	}
}
