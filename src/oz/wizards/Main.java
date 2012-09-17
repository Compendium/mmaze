package oz.wizards;

import java.io.File;

import oz.wizards.net.NetworkManager;
import oz.wizards.screen.GameScreen;
import oz.wizards.screen.MenuScreen;
import oz.wizards.screen.Screen;
import oz.wizards.screen.ScreenManager;
import oz.wizards.screen.TitleScreen;

//client
public class Main {
	public static ScreenManager sm;
	public static GameScreen game;
	public static Thread smThread;
	
	public static NetworkManager networkManager;
	public static Thread networkManagerThread;
	
	public static void main(String[] args) {
		String os = new String();
		String osString = System.getProperty("os.name").toLowerCase();
		if(osString.contains("windows")) os = "windows";
		else if (osString.contains("linux")) os = "linux";
		else if (osString.contains("mac")) os = "macosx";
		else if (osString.contains("solaris")) os = "solaris";
		
		System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir").toLowerCase() + File.separator + "lib" + File.separator + "lwjgl-2.8.3" + File.separator + "native" + File.separator + os);
		
		sm = new ScreenManager(new TitleScreen());
		smThread = new Thread(sm);
		smThread.start();
	}

}