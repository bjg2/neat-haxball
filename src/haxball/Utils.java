package haxball;

import java.io.File;
import java.util.HashSet;

import org.jbox2d.common.Vec2;

import processing.core.PApplet;
import processing.core.PFont;

public final class Utils
{
	
	// physics filter scenery
	public static int[] playersCategory = new int[2];
	public static int ballCategory;
	public static int fieldEdgeCategory;
	public static int ballFieldEdgeCategory;
	public static int[] playersStartEdgeCategory = new int[2];
	
	public static int[] playersMask = new int[2];
	public static int ballMask;
	public static int fieldEdgeMask;
	public static int ballFieldEdgeMask;
	public static int[] playersStartEdgeMask = new int[2];
		
	// commands consts
	public static int[] leftKeyCode = new int[2];
	public static int[] rightKeyCode = new int[2];
	public static int[] upKeyCode = new int[2];
	public static int[] downKeyCode = new int[2];
	public static int[] shootKeyCode = new int[2];
	
	public static char[] leftKey = new char[2];
	public static char[] rightKey = new char[2];
	public static char[] upKey = new char[2];
	public static char[] downKey = new char[2];
	public static char[] shootKey = new char[2];
	
	// draw consts	
	public static int backgroundColor;
	public static int[] teamColor = new int[2];
	public static int ballColor;
	public static int strokeColor;
	public static int shootStrokeColor;
	public static int textColor;
	public static float ballStrokeWeight;
	public static float playerStrokeWeight;
	
	public static float fieldStartX;
	public static float fieldStartY;
	
	public static float scoreH;
	public static float scoreTextH;
	public static float scoreSquareA;
	public static float scoreSpace;
	public static PFont scoreFont;
	
	public static void initUtils(PApplet parent)
	{		
		// commands
		leftKeyCode[0] = rightKeyCode[0] = upKeyCode[0] = downKeyCode[0] = shootKeyCode[0] = -1;
		leftKeyCode[1] = rightKeyCode[1] = upKeyCode[1] = downKeyCode[1] = shootKeyCode[1] = -1;
		leftKey[0] = rightKey[0] = upKey[0] = downKey[0] = shootKey[0] = 'p';
		leftKey[1] = rightKey[1] = upKey[1] = downKey[1] = shootKey[1] = 'p';
		
		leftKeyCode[0] = PApplet.LEFT;
		rightKeyCode[0] = PApplet.RIGHT;
		upKeyCode[0] = PApplet.UP;
		downKeyCode[0] = PApplet.DOWN;
		shootKey[0] = ' ';
		
		leftKey[1] = 'd';
		rightKey[1] = 'g';
		upKey[1] = 'r';
		downKey[1] = 'f';
		shootKey[1] = 'a';
 
		// physics filter scenery
		playersCategory[0] = 1;
		playersCategory[1] = 2;
		ballCategory = 4;
		fieldEdgeCategory = 8;
		ballFieldEdgeCategory = 16;
		playersStartEdgeCategory[0] = 32;
		playersStartEdgeCategory[1] = 64;

		playersMask[0] = playersCategory[0]
				+ playersCategory[1]
				+ ballCategory
				+ fieldEdgeCategory
				+ playersStartEdgeCategory[0]
				+ playersStartEdgeCategory[1]
				;
		playersMask[1] = playersCategory[0]
				+ playersCategory[1]
				+ ballCategory
				+ fieldEdgeCategory
				+ playersStartEdgeCategory[0]
				+ playersStartEdgeCategory[1];
		ballMask = ballCategory
				+ playersCategory[0]
				+ playersCategory[1]
				+ ballFieldEdgeCategory
				+ fieldEdgeCategory;
		fieldEdgeMask = fieldEdgeCategory
						+ playersCategory[0]
						+ playersCategory[1]
						+ ballCategory;
		ballFieldEdgeMask = ballFieldEdgeCategory
				+ ballCategory
				;
		playersStartEdgeMask[0] = playersStartEdgeCategory[0]
				+ playersCategory[0]
				+ playersCategory[1];
		playersStartEdgeMask[1] = playersStartEdgeCategory[1]
				+ playersCategory[0]
				+ playersCategory[1];

		// draw		
		backgroundColor = parent.color(60, 49, 43);
		teamColor[0] = parent.color(229, 110, 86);
		teamColor[1] = parent.color(86, 137, 229);
		strokeColor = parent.color(0);
		ballColor = shootStrokeColor = textColor = parent.color(255);
		ballStrokeWeight = 2;
		playerStrokeWeight = 3;
		
		scoreH = 30;
		scoreSpace = 10;
		
		fieldStartX = 2 * scoreH;
		fieldStartY = 2 * scoreH;
		scoreSquareA = scoreH;
		
		scoreTextH = 25;
		scoreFont = parent.createFont("Comic Sans MS Bold", scoreTextH);
	}
	
	// making int array from hashset of ints
	public static int[] HashSetToArray(HashSet<Integer> set)
	{
		int[] ret = new int[set.size()];
		int i = 0;
		for(int el : set)
		{
			ret[i++] = el;
		}
		return ret;
	}

	// sigmoid function
	public static double sigmoid(double x)
	{
		return 1 / (1 + Math.exp(-x));
	}
	
	// dist point P from line segment AB
	public static double distFromLineSegment(Vec2 a, Vec2 b, Vec2 p)
	{
		double dist = Vec2.cross(b.sub(a), p.sub(a)) / (b.sub(a).length());
		
		if(Vec2.dot(b.sub(a), p.sub(b)) > 0)
		{
			return p.sub(b).length();
		}
		
		if(Vec2.dot(a.sub(b), p.sub(a)) > 0)
		{
			return p.sub(a).length();
		}
		
		return dist;
	}
	
	public static void createFolderIfNotExists(String folderPath)
	{
		File f = new File(folderPath);
		if(!f.exists())
		{
			f.mkdir();
		}
	}
}
