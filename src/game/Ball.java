package game;

import haxball.Utils;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PVector;
import shiffman.box2d.Box2DProcessing;
import bot.GameState;

public class Ball extends AGameObject
{
	boolean touched;
	
	public Ball(PApplet parent, Box2DProcessing box2d, PVector pos)
	{
		super(parent, box2d, pos);

		bodyDef.linearDamping = 1 - GameState.friction;
		bodyDef.bullet = true;
		body = box2d.createBody(bodyDef);
		shape = new CircleShape();
		shape.setRadius( box2d.scalarPixelsToWorld(GameState.ballR) );
		fixtureDef.shape = shape;
		
		fixtureDef.density = GameState.ballDensity;
		fixtureDef.restitution = GameState.ballRestitution;

		fixtureDef.filter.categoryBits = Utils.ballCategory;
		fixtureDef.filter.maskBits = Utils.ballMask;
		body.createFixture(fixtureDef);
	}

	public void draw()
	{
		parent.stroke(Utils.strokeColor);
		parent.strokeWeight(Utils.ballStrokeWeight);
		parent.fill(Utils.ballColor);
		
		Vec2 pos = box2d.getBodyPixelCoord(body);
		parent.ellipse(Utils.fieldStartX + pos.x, Utils.fieldStartY + pos.y,
				2 * GameState.ballR, 2 * GameState.ballR);		
	}
	
	public boolean checkTouched()
	{
		return touched = touched || body.getLinearVelocity().length() != 0;
	}
	
	public void reset()
	{
		super.reset();
		touched = false;
	}
	
	public int checkGoal()
	{
		if(box2d.coordWorldToPixels(body.getPosition()).x
				> (GameState.realFieldW + GameState.fieldW) / 2)
		{
			return 0;
		}
		if(box2d.coordWorldToPixels(body.getPosition()).x
				< (GameState.realFieldW - GameState.fieldW) / 2)
		{
			return 1;
		}
		return -1;
	}
	
	public void kickFrom(Vec2 hitPlayerPos)
	{		
		float xDiff = getPosition().x - hitPlayerPos.x;
		float yDiff = hitPlayerPos.y - getPosition().y;
		float angle = PApplet.atan2(yDiff, xDiff) + parent.random(-GameState.shootAngDiff, 
				GameState.shootAngDiff);
		
		Vec2 force = (new Vec2(PApplet.cos(angle), PApplet.sin(angle))).mulLocal(GameState.maxShootAcceleration);		
		body.applyForceToCenter(force);
	}
}
