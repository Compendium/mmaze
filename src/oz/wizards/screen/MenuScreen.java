package oz.wizards.screen;

import java.io.IOException;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.Main;
import oz.wizards.gfx.Font;
import oz.wizards.gfx.Shader;
import oz.wizards.gfx.Texture;
import oz.wizards.gfx.VertexBatch;
import oz.wizards.gfx.VertexBuffer;
import oz.wizards.net.NetworkManager;

import static org.lwjgl.opengl.GL11.*;

public class MenuScreen extends Screen implements Runnable {
	boolean isCloseRequested = false;
	long frametime, frametimeTimestamp;

	Shader shaderBase;
	VertexBatch vb;
	Texture fontTexture;
	Font font;

	static final int PLAY_MULTIPLAYER = 0;
	static final int PLAY_SINGLEPLAYER = 1;
	static final int SET_OPTIONS = 2;
	static final int QUIT = 3;
	int currentlyHighlighted = PLAY_MULTIPLAYER;

	@Override
	public void run() {
		create();
		while (isCloseRequested == false) {
			if (Display.isCloseRequested()) {
				isCloseRequested = true;
				System.out.println("closing");
			}

			frametimeTimestamp = System.nanoTime();
			draw();
			update();
			frametime = System.nanoTime() - frametimeTimestamp;
		}
		destruct();
	}

	@Override
	public void create() {
		Thread.currentThread().setName("MenuThread");
		try {
			Display.makeCurrent();
		} catch (LWJGLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		glClearColor(0, 0, 0, 1);

		shaderBase = new Shader("res/shaders/titlescreen");
		vb = new VertexBatch(shaderBase);
		try {
			fontTexture = new Texture("res/textures/font.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		font = new Font(fontTexture, vb);
		font.init();
	}

	@Override
	public void update() {
		if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
			this.active = false;
			Main.sm.setNextScreen(null);
		}
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState() == true) {
				if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
					if (currentlyHighlighted > 0)
						currentlyHighlighted--;
					else if (currentlyHighlighted == 0)
						currentlyHighlighted = 3;
				} else if (Keyboard.getEventKey() == Keyboard.KEY_DOWN) {
					if (currentlyHighlighted < 3)
						currentlyHighlighted++;
					else if (currentlyHighlighted == 3)
						currentlyHighlighted = 0;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_RETURN) {
					if (currentlyHighlighted == QUIT) {
						System.exit(+1);
					} else if (currentlyHighlighted == PLAY_MULTIPLAYER) {
						Main.networkManager = new NetworkManager();
						Main.networkManagerThread = new Thread(
								Main.networkManager);
						Main.networkManagerThread.start();

						Main.game = new GameScreen();
						Main.sm.setNextScreen(Main.game);
						this.active = false;
					}
				}
			}
		}
	}

	@Override
	public void draw() {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		float ratio = (float) Display.getWidth() / (float) Display.getHeight();
		GLU.gluOrtho2D(-ratio, ratio, -1, +1);

		shaderBase.enable();
		font.draw(new Vector2f(-ratio / 2.f, 0), 0.005f, new Vector3f(0.8f,
				0.8f, 0.8f), "mmaze");
		Vector3f n = new Vector3f(0.5f, 0.5f, 0.5f);
		Vector3f h = new Vector3f(1.f, 1.f, 1.f);
		font.draw(new Vector2f(-ratio / 2.f, -0.1f), 0.005f,
				(currentlyHighlighted == PLAY_MULTIPLAYER ? h : n),
				"multiplayer");
		font.draw(new Vector2f(-ratio / 2.f, -0.15f), 0.005f,
				(currentlyHighlighted == PLAY_SINGLEPLAYER ? h : n),
				"singleplayer");
		font.draw(new Vector2f(-ratio / 2.f, -0.20f), 0.005f,
				(currentlyHighlighted == SET_OPTIONS ? h : n), "options");
		font.draw(new Vector2f(-ratio / 2.f, -0.25f), 0.005f,
				(currentlyHighlighted == QUIT ? h : n), "quit");
		vb.render();
		shaderBase.disable();

		Display.update();
	}

	@Override
	public void destruct() {
		try {
			Display.releaseContext();
		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
