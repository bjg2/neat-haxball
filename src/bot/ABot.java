package bot;

public abstract class ABot
{	
	// return xMove yMove shoot
	public abstract GameMove makeMove(GameState state);
	// game finished
	public abstract void gameFinished(GameState state);
}
