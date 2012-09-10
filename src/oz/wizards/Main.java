package oz.wizards;

import oz.wizards.net.NetworkManager;
import oz.wizards.screen.Game;
import oz.wizards.screen.Menu;
import oz.wizards.screen.Title;

//client
public class Main {
	public static Game game;
	public static Title title;
	public static Menu menu;
	
	public static Thread gameThread;
	public static NetworkManager networkManager;
	public static Thread networkManagerThread;
	
	public static void main(String[] args) {
		//game = new Game();
		//title = new Title();
		//gameThread = new Thread(title);
		//networkManager = new NetworkManager();
		//networkManagerThread = new Thread(networkManager);
		
		//gameThread.start();
		//networkManagerThread.start();
		
		title = new Title();
		
		gameThread = new Thread(title);
		gameThread.start();
	}

}
