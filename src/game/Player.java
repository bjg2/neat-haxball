package game;

import haxball.Utils;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PVector;
import shiffman.box2d.Box2DProcessing;
import ai.ABot;
import ai.GameMove;
import ai.GameState;

public class Player extends AGameObject
{
	int teamId;
	
	ABot bot = null;
	boolean shoot;
	
	public Player(PApplet parent, Box2DProcessing box2d, PVector pos, int teamId, ABot bot)
	{
		super(parent, box2d, pos);
		this.teamId = teamId;
		this.bot = bot;

		bodyDef.linearDamping = 1 - GameState.friction;
		body = box2d.createBody(bodyDef);
		shape = new CircleShape();
		shape.setRadius( box2d.scalarPixelsToWorld(GameState.playerR) );
		fixtureDef.shape = shape;
		
		fixtureDef.density = GameState.playerDensity;
		fixtureDef.restitution = GameState.playerRestitution;

		fixtureDef.filter.categoryBits = Utils.playersCategory[teamId];
		fixtureDef.filter.maskBits = Utils.playersMask[teamId];
		body.createFixture(fixtureDef);
	}

	public void draw()
	{
		parent.stroke(Utils.strokeColor);
		parent.strokeWeight(Utils.playerStrokeWeight);
		if(shoot)
		{
			parent.stroke(Utils.shootStrokeColor);
		}		
		parent.fill(Utils.teamColor[teamId]);
		
		Vec2 pos = box2d.getBodyPixelCoord(body);
		parent.ellipse(Utils.fieldStartX + pos.x, Utils.fieldStartY + pos.y,
				2 * GameState.playerR, 2 * GameState.playerR);
	}
	
	public void makeMove(GameMove keyboardMove, GameState state)
	{
		GameMove move = keyboardMove;
		if(bot != null)
		{
			move = bot.makeMove(state);			
			if(teamId == 1)
			{
				move.invertxMove();
			}
		}
		
		if(move.getxMove() != 0 || move.getyMove() != 0)
		{
			Vec2 moveVec = new Vec2(move.getxMove(), -move.getyMove());
			moveVec.mulLocal(1.0f / moveVec.length());
			moveVec.mulLocal(GameState.maxPlayerAcceleration);
			body.applyForceToCenter(moveVec);				
		}
		shoot = move.isShoot();
	}
	
	public void gameFinished(GameState state)
	{
		if(bot != null)
		{
			bot.gameFinished(state);
		}
	}
	
	public boolean isShooting()
	{
		return shoot;
	}
}
