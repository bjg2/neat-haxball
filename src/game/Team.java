package game;

import java.util.ArrayList;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PVector;
import shiffman.box2d.Box2DProcessing;
import ai.ABot;
import ai.GameMove;
import ai.GameState;

public class Team
{
	PApplet parent;
	ArrayList<Player> players = new ArrayList<Player>();
	int teamId;
	
	public Team(PApplet parent, Box2DProcessing box2d, int teamId, ABot bot,
			float playerCenterDist)
	{
		this.parent = parent;
		this.teamId = teamId;
		
		PVector center = new PVector(GameState.realFieldW / 2,
				GameState.realFieldH / 2);
	
		for(int i = 0; i < GameState.numOfPlayers; i++)
		{
			float ang = parent.random(PApplet.HALF_PI, 3 * PApplet.HALF_PI);
			PVector p = PVector.add(center, 
					PVector.mult(new PVector(PApplet.cos(ang), PApplet.sin(ang)), playerCenterDist));
			while(p.y < 0 || p.y >= GameState.realFieldH)
			{
				ang = parent.random(PApplet.HALF_PI, 3 * PApplet.HALF_PI);
				p = PVector.add(center, 
						PVector.mult(new PVector(PApplet.cos(ang), PApplet.sin(ang)), playerCenterDist));
			}
			
			if(teamId == 1)
			{
				p.x = GameState.realFieldW - p.x;
			}
			
			players.add( new Player(parent, box2d, p, teamId, bot) );
		}
	}
	
	public void reset()
	{
		for(Player pl : players)
		{
			pl.reset();
		}		
	}

	public void draw()
	{
		for(Player pl : players)
		{
			pl.draw();
		}
	}
	
	public void makeMove(GameMove keyboardMove, GameState state)
	{				
		for(int i = 0; i < players.size(); i++)
		{
			state.pickPlayer(i);
			players.get(i).makeMove(keyboardMove, state);
		}
	}
	
	public void gameFinished(GameState state)
	{
		for(Player pl : players)
		{
			pl.gameFinished(state);
		}
	}
	
	public Vec2[] getPlayersPos()
	{
		Vec2[] ret = new Vec2[players.size()];
		
		for(int i = 0; i < players.size(); i++)
		{
			ret[i] = players.get(i).getPosition();
		}
		
		return ret;
	}
	
	public Vec2[] getPlayersVelocity()
	{
		Vec2[] ret = new Vec2[players.size()];
		
		for(int i = 0; i < players.size(); i++)
		{
			ret[i] = players.get(i).getVelocity();
		}
		
		return ret;
	}
	
	public ArrayList<Player> getPlayers()
	{
		return players;
	}
	
}
