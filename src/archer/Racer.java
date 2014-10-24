package archer;

import game.Game;

import java.util.HashMap;

import processing.core.PApplet;

public class Racer extends PApplet
{
	private static final long serialVersionUID = 3790355573456668298L;
	Game game;
	
	
	// basic initialization
	public void setup()
	{
		size(displayWidth, displayHeight);

		game = new Game(this);
	}
	
	public void draw()
	{
		game.update();
		game.draw();
	}
	
	public boolean sketchFullScreen()
	{
		return true;
	}
	
	public static void main(String _args[])
	{
		PApplet.main(new String[] { archer.Racer.class.getName() });
	}
}
