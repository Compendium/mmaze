package oz.wizards;

public class Main {
	public static Game game;
	public static Thread gameThread;
	
	public static void main(String[] args) {
		game = new Game();
		gameThread = new Thread(game);
		gameThread.start();
	}

}
