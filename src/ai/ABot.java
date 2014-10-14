package ai;

public abstract class ABot
{	
	// return xMove yMove shoot
	public abstract GameMove makeMove(GameState state);
	// game started
	public abstract void gameStarted();
	// game finished
	public abstract void gameFinished(GameState state);	
}
