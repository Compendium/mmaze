package oz.wizards.gfx;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import de.matthiasmann.twl.utils.PNGDecoder;
import de.matthiasmann.twl.utils.PNGDecoder.Format;

public class Texture {
	public int texId;
	public int width;
	public int height;
	public boolean hasAlpha;
	ByteBuffer buf;

	public Texture(String path) throws IOException {
		InputStream in = new FileInputStream(path);
		try {
			PNGDecoder decoder = new PNGDecoder(in);
			width = decoder.getWidth();
			height = decoder.getHeight();
			hasAlpha = decoder.hasAlpha();

			if (decoder.hasAlpha()) {
				buf = ByteBuffer.allocateDirect(4 * decoder.getWidth()
						* decoder.getHeight());
				decoder.decode(buf, decoder.getWidth() * 4, Format.RGBA);
			} else {
				buf = ByteBuffer.allocateDirect(3 * decoder.getWidth()
						* decoder.getHeight());
				decoder.decode(buf, decoder.getWidth() * 3, Format.RGB);
			}

			buf.flip();
			in.close();
		} finally {
		}
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER,
				GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER,
				GL11.GL_LINEAR);

		texId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texId);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, (hasAlpha ? GL11.GL_RGBA
				: GL11.GL_RGB), width, height, 0, (hasAlpha ? GL11.GL_RGBA
				: GL11.GL_RGB), GL11.GL_UNSIGNED_BYTE, buf);

		System.out.println("Loaded texture " + texId + " (" + path + ") " + (hasAlpha ? "with alpha." : "without alpha."));
	}
	
	public int getPixel (int x, int y) {
		//System.out.printf("getPixel @ %d, %d\n", x, y);
		return getPixel(x + y * width);
	}
	
	public int getPixel(int pixelpos) {
		//System.out.printf("getPixel @ %d\n", pixelpos);
		byte b1 = buf.get(pixelpos * (hasAlpha ? 4 : 3));
		byte b2 = buf.get(pixelpos * (hasAlpha ? 4 : 3) + 1);
		byte b3 = buf.get(pixelpos * (hasAlpha ? 4 : 3) + 2);
		byte b4 = buf.get(pixelpos * (hasAlpha ? 4 : 3) + 3);
		//int pixel = ((0xff & a) << 24) | ((0xff & b) << 16) | ((0xff & c) << 8) | ((0xff & d));
		int pixel = ((0xFF & b1) << 24) | ((0xFF & b2) << 16) |
	            ((0xFF & b3) << 8) | (0xFF & b4);
		//System.out.println(String.format("RGBA: %x %x %x %x", b1, b2, b3, b4));
		//System.out.println("got pixel color val " + String.format("%x", pixel));
		return pixel;
	}
}
