package oz.wizards.screen;

public abstract class Screen {
	protected boolean active = true;
	
	public abstract void create();

	public abstract void update();

	public abstract void draw();

	public abstract void destruct();
	
	public boolean isActive() {
		return active;
	}
}
