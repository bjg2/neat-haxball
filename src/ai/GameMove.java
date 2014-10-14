package ai;

public class GameMove
{
	int xMove;
	int yMove;
	boolean shoot;
	
	public GameMove(int xMove, int yMove, boolean shoot)
	{
		this.xMove = xMove;
		this.yMove = yMove;
		this.shoot = shoot;
	}
	
	// cause second team is looking in opposite direction
	public void invertxMove()
	{
		xMove = -xMove;
	}

	public int getxMove() {
		return xMove;
	}

	public int getyMove() {
		return yMove;
	}

	public boolean isShoot() {
		return shoot;
	}
}
