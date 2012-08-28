package oz.wizards.gfx;

import java.io.BufferedReader;
import java.io.FileReader;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader {
	private String path;
	private int shader = 0;
	private int vertShader = 0;
	private int fragShader = 0;

	public Shader(String path) {
		this.path = path;
		vertShader = createShader(GL20.GL_VERTEX_SHADER, path + ".vert");
		fragShader = createShader(GL20.GL_FRAGMENT_SHADER, path + ".frag");

		shader = GL20.glCreateProgram();
		GL20.glAttachShader(shader, vertShader);
		GL20.glAttachShader(shader, fragShader);
		
		GL20.glLinkProgram(shader);
		if(GL20.glGetProgram(shader, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
			System.err.println("Error while linking");
			printProgramLogInfo(shader);
		}
		//GL20.glValidateProgram(shader);
		//if(GL20.glGetProgram(shader, GL20.GL_VALIDATE_STATUS) == GL11.GL_FALSE) {
			//System.out.println("Error while validationg");
			//printLogInfo(shader);
		//}
		//enable();
		System.out.println("Created shader " + shader + ", with frag:" + fragShader + " and vert:" + vertShader);
	}

	public void enable() {
		GL20.glUseProgram(shader);
	}

	public void disable() {
		GL20.glUseProgram(0);
	}
	
	public static void disableAll () {
		GL20.glUseProgram(0);
	}

	public int getUniformLocation(String u) {
		int r =  GL20.glGetUniformLocation(shader, u);
		//printLogInfo(shader);
		if(r == -1)
			System.err.println("[" + path + "] Error while getting uniform : " + u);
		return r;
	}

	public int getAttributeLocation(String a) {
		int r = GL20.glGetAttribLocation(shader, a);
		if(r == -1)
			System.err.println("[" + path + "] Error while getting attrib : " + a);
		return r;
	}

	private int createShader(int type, String path) {
		int s = GL20.glCreateShader(type);
		String code = "";
		String line;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(path));
			while ((line = reader.readLine()) != null) {
				code += line + "\n";
				//System.out.println(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		GL20.glShaderSource(s, code);
		GL20.glCompileShader(s);
		if(GL20.glGetShader(s, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE)
		{
			System.err.println("Error while compiling " + path);
			printShaderLogInfo(s);
		}
		return s;
	}

	private static boolean printProgramLogInfo(int obj) {
		int length = GL20.glGetProgram(obj, GL20.GL_INFO_LOG_LENGTH);
		if (length > 1) {
			String out = GL20.glGetProgramInfoLog(obj, 256);
			System.err.print(out);
		} else
			return true;
		return false;
	}
	
	private static boolean printShaderLogInfo(int obj) {
		int length = GL20.glGetShader(obj, GL20.GL_INFO_LOG_LENGTH);
		if (length > 1) {
			String out = GL20.glGetShaderInfoLog(obj, 256);
			System.err.print(out);
		} else
			return true;
		return false;
	}
}
