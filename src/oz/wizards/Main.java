package oz.wizards;

import oz.wizards.net.NetworkManager;

//client
public class Main {
	public static Game game;
	public static Thread gameThread;
	public static NetworkManager networkManager;
	public static Thread networkManagerThread;
	
	public static void main(String[] args) {
		game = new Game();
		gameThread = new Thread(game);
		networkManager = new NetworkManager();
		networkManagerThread = new Thread(networkManager);
		
		gameThread.start();
		networkManagerThread.start();
	}

}
