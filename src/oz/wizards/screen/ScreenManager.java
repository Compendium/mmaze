package oz.wizards.screen;

public class ScreenManager implements Runnable {
	boolean keepRunning = true;
	private long frametime = 0;
	private long frametimeTimestamp = 0;
	
	private Screen currentScreen;
	private Screen nextScreen;
	
	public ScreenManager (Screen startingScreen) {
		currentScreen = startingScreen;
	}

	@Override
	public void run() {
		while(keepRunning) {
			currentScreen.create();
			while(currentScreen.isActive()) {
				frametimeTimestamp = System.nanoTime();
				currentScreen.update();
				currentScreen.draw();
				frametime = System.nanoTime() - frametimeTimestamp;
			}
			currentScreen.destruct();
			
			currentScreen = nextScreen;
			if(currentScreen == null)
				keepRunning = false;
		}
	}
	
	public long getFrametime () {
		return frametime;
	}

	public void setNextScreen(Screen screen) {
		nextScreen = screen;
	}
}
