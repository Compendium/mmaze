package oz.wizards.gfx;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.Game;
import oz.wizards.Main;

public class VertexBuffer {
	private int mVboid = -1;
	private int mCamUniform = 0;
	private int mPositionAttrib = 0;
	private int mTexcoordAttrib = 0;
	private int mIdAttrib = 0;
	private int mTexUniform = 0;
	private int mVertexCount = 0;
	private int mMaxVertexCount = 10;
	//FloatBuffer buffer;
	ByteBuffer buffer;
	
	private Texture texture;
	
	public VertexBuffer (final Shader s, final Texture tex) {
		texture = tex;
		//buffer = ByteBuffer.allocateDirect((3*4) * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		buffer = ByteBuffer.allocateDirect(mMaxVertexCount * ((3*4)+(2*4)+4)).order(ByteOrder.nativeOrder());
		mPositionAttrib = s.getAttributeLocation("position");
		mTexcoordAttrib = s.getAttributeLocation("texcoord");
		mTexUniform = s.getUniformLocation("texture");
		mCamUniform = s.getUniformLocation("cam");
		glUniform1i(mTexUniform, 0);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture.texId);
	}

	/**
	 * Uses GL_QUADS, order is CCW (Counter Clockwise) example.
	 * Coordinates depend on the current projection-matrix.
	 *
	 * 0----3
	 * |    |
	 * |    |
	 * 1----2
	 *
	 * @param v The current vertex
	 */
	public void add(Vector3f v, Vector2f texc) {
		if(mVertexCount == mMaxVertexCount) {
			mMaxVertexCount *= 2;
			//FloatBuffer newbb = ByteBuffer.allocateDirect((mMaxVertexCount * (3*4))).order(ByteOrder.nativeOrder()).asFloatBuffer();
			//newbb.put(buffer);
			//buffer = newbb;
			ByteBuffer newbb = ByteBuffer.allocateDirect(mMaxVertexCount * ((3*4)+(2*4)+4)).order(ByteOrder.nativeOrder());
			buffer.flip();
			//buffer.rewind();
			newbb.put(buffer);
			buffer = newbb;
			//System.out.println("Buffer-resize");
		}
		mVertexCount++;

		buffer.putFloat(v.x);
		buffer.putFloat(v.y);
		buffer.putFloat(v.z);
		buffer.putFloat(texc.x);
		buffer.putFloat(texc.y);
	}
	
	public void putQuad (Vector3f a, Vector3f b, Vector3f c, Vector3f d, Vector2f uvmin, Vector2f uvmax, Vector3f rgb) {
		Vector2f realuvmin = new Vector2f((float) (1. / texture.width * uvmin.x)
				+ (float) (0.1 / texture.width),
				(float) (1. / texture.height * uvmin.y)
						+ (float) (0.1 / texture.height));
		Vector2f realuvmax = new Vector2f((float) (1. / texture.width * uvmax.x)
				- (float) (0.1 / texture.width),
				(float) (1. / texture.height * uvmax.y)
						- (float) (0.1 / texture.height));
		
		add(b, new Vector2f(realuvmax.x, realuvmax.y));
		add(c, new Vector2f(realuvmin.x, realuvmin.y));
		add(a, new Vector2f(realuvmin.x, realuvmax.y));

		add(d, new Vector2f(realuvmax.x, realuvmin.y));
		add(c, new Vector2f(realuvmin.x, realuvmin.y));
		add(b, new Vector2f(realuvmax.x, realuvmax.y));
	}

	public void upload() throws IOException{
		if(buffer.position() == 0) {
			System.err.println("Attempt to upload empty buffer!");
			return;
		}
		if(mVboid != -1){ //vbo already exists, delete it
			glDeleteBuffers(mVboid);
		}
		mVboid = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, mVboid);
		buffer.flip();
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		//fixed//glEnableClientState(GL_VERTEX_ARRAY);
		//fixed//glVertexPointer(3, GL_FLOAT, 0, 0);
		//glEnableVertexAttribArray(mPositionAttrib);
		//glVertexAttribPointer(mPositionAttrib, 3, GL_FLOAT, false, 0, 0);

		glBindBuffer(GL_ARRAY_BUFFER, 0);
		//glDisableVertexAttribArray(mPositionAttrib);

		System.out.println("Uploaded buffer " + mVboid + " with " + this.buffer.limit() / 1000.0 + "k bytes");
}

	public void render(){
		glUniform1i(mTexUniform, 0);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture.texId);

		glEnableVertexAttribArray(mPositionAttrib);
		glEnableVertexAttribArray(mTexcoordAttrib);
		glBindBuffer(GL_ARRAY_BUFFER, mVboid);
		glVertexAttribPointer(mPositionAttrib, 3, GL_FLOAT, false, (3*4)+(2*4), 0);
		glVertexAttribPointer(mTexcoordAttrib, 2, GL_FLOAT, false, (3*4)+(2*4), (3*4));
		glDrawArrays(GL_TRIANGLES, 0, mMaxVertexCount);
		Game.drawCalls++;
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}
