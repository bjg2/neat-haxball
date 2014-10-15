package game;

import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;

import processing.core.PApplet;
import processing.core.PVector;
import shiffman.box2d.Box2DProcessing;

public abstract class AGameObject
{	
	PApplet parent;	
	Box2DProcessing box2d;
	Shape shape;
	BodyDef bodyDef;
	FixtureDef fixtureDef;
	Body body;
	PVector initPos;
	
	Vec2 lastPos = null;
	
	protected AGameObject(PApplet parent, Box2DProcessing box2d, PVector pos)
	{
		this.parent = parent;
		this.box2d = box2d;
		this.initPos = pos;
		
		bodyDef = new BodyDef();
		bodyDef.position.set(box2d.coordPixelsToWorld(pos));
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.fixedRotation = true;
		
		fixtureDef = new FixtureDef();
	}
	
	public abstract void draw();
	
	public void reset()
	{
		body.setTransform(box2d.coordPixelsToWorld(initPos), 0);
		body.setLinearVelocity(new Vec2(0, 0));
	}
	
	public Vec2 getPosition()
	{
		return box2d.coordWorldToPixels(body.getPosition());
	}
 
	public Vec2 getVelocity()
	{
		return body.getLinearVelocity();
	}
}
