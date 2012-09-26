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
import oz.wizards.gfx.Shader;
import oz.wizards.gfx.Texture;
import oz.wizards.gfx.VertexBatch;

public class TitleScreen extends Screen {
	Shader shaderBase;
	VertexBatch vb;
	Texture texture;
	
	float fact = 0.f;
	boolean forward = true;
	
	@Override
	public void create() {
		try{
			Display.setDisplayMode(new DisplayMode(600, 600));
			Display.create();
			Display.setTitle("テスト");
			
			GL11.glViewport(0,0, Display.getWidth(), Display.getHeight());
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glFrontFace(GL11.GL_CCW);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			GL11.glClearColor(0, 0, 0, 1.f);
			GL11.glClearDepth(1.f);
			
			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			float ratio = (float)Display.getWidth() / (float)Display.getHeight();
			GLU.gluPerspective(45, ratio, 1.f, 1000.f);
			
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glLoadIdentity();
			
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
		shaderBase = new Shader("res/shaders/titlescreen");
		vb = new VertexBatch(shaderBase);
		try {
			texture = new Texture("res/textures/titlescreen.png");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void update() {
		while(Keyboard.next()) {
			if(Keyboard.getEventKeyState() == true) {
				if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
					this.active = false;
				}
			}
		}
	}

	@Override
	public void draw() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		float ratio = (float)Display.getWidth() / (float)Display.getHeight();
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		//GL11.glFrustum(-ratio, ratio, -1, 1, 1.f, 1000.f);
		GL11.glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 1.f, 100.f);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();
		shaderBase.enable();
		
		//float m = (float) (Math.tan(Math.toRadians(45.f / 2)) * z);
		float z = -10.0f;
		float x = (float)Display.getWidth()/2 - 449.f/2;
		float y = (float)Display.getHeight()/2 - 480.f/2;
		
		vb.putQuad(texture,
				new Vector3f(x + 0, y + 480, z),//3
				new Vector3f(x + 449, y + 480, z),//2
				new Vector3f(x + 0, y + 0, z),//1
				new Vector3f(x + 449, y + 0, z),//0
				new Vector2f(45,150), new Vector2f(492, 630), new Vector3f(fact,fact,fact));
		
		vb.render();		
		shaderBase.disable();
		if (forward) {
			fact += 0.01f * ((double) Main.sm.getFrametime()  / 10000000.0) * 0.9f;
			if (fact >= 1.f) {
				forward = false;
				fact = 1.f;
			}
		} else {
			fact -= 0.01f * ((double) Main.sm.getFrametime()  / 10000000.0) * 0.9f;
			if (fact <= 0.f) {
				forward = true;
				fact = 0.f;
				this.active = false;
			}
		}

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
		Main.sm.setNextScreen(new MenuScreen());
	}
}
