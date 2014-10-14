package haxball;

import game.Game;

import java.util.HashMap;

import ai.neat.Neat;
import processing.core.PApplet;

public class Haxball extends PApplet
{
	private static final long serialVersionUID = 3790355573456668298L;
	Game game;
	
	// keys for human players
	HashMap<Integer, Boolean> keyCodesPressed = new HashMap<Integer, Boolean>();
	HashMap<Character, Boolean> keysPressed = new HashMap<Character, Boolean>();
	
	// basic initialization
	public void setup()
	{
		size(displayWidth, displayHeight);
		Utils.initUtils(this);
				
		initKeyCodePressed(Utils.leftKeyCode);
		initKeyCodePressed(Utils.upKeyCode);
		initKeyCodePressed(Utils.downKeyCode);
		initKeyCodePressed(Utils.rightKeyCode);
		initKeyCodePressed(Utils.shootKeyCode);
		
		initKeyPressed(Utils.leftKey);
		initKeyPressed(Utils.upKey);
		initKeyPressed(Utils.downKey);
		initKeyPressed(Utils.rightKey);
		initKeyPressed(Utils.shootKey);
		
		//game = new Game(this, null, null);
		
		Neat neat = new Neat(this);
		neat.train();
	}
	
	// init all important key codes to not pressed
	public void initKeyCodePressed(int[] keyCodes)
	{
		for(int i : keyCodes)
		{
			keyCodesPressed.put(i, false);
		}
	}
	
	// init all important keys to not pressed
	public void initKeyPressed(char[] keys)
	{
		for(char c : keys)
		{
			keysPressed.put(c, false);
		}
	}
	
	public void keyPressed()
	{
		keyCodesPressed.put(keyCode, true);
		keysPressed.put(key, true);
	}
	
	public void keyReleased()
	{
		keyCodesPressed.put(keyCode, false);
		keysPressed.put(key, false);		
	}

	public void draw()
	{
		//game.update(keyCodesPressed, keysPressed);
		//game.draw();
	}
	
	public boolean sketchFullScreen()
	{
		return true;
	}
	
	public static void main(String _args[])
	{
		PApplet.main(new String[] { haxball.Haxball.class.getName() });
	}
}
