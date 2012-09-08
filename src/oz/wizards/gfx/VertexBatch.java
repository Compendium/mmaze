package oz.wizards.gfx;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import oz.wizards.Game;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * A vertex batch, that uses vertex arrays to draw.
 *
 * @author compendium
 */
public class VertexBatch {
	public Vector3f translation = new Vector3f();
	public Vector3f rotation = new Vector3f();

	class TextureInfo {
		int texId;
		FloatBuffer vertices;
		int vertexCount;
		int vertexCapacity;
	}

	private static final int FLOAT_SIZE_BYTES = 4;
	private static final int TRIANGLE_VERTICES_DATA_POS_OFFSET = 0;
	private static final int TRIANGLE_VERTICES_DATA_COLOR_OFFSET = 3;
	private static final int TRIANGLE_VERTICES_DATA_UV_OFFSET = 3 + 3;

	private Map<Integer /* texids */, TextureInfo /* vertices */> mVertexMap;

	private Shader shader;
	private int mPositionAttrib = 0;
	private int mTexcoordAttrib = 0;
	private int mColorAttrib = 0;

	public VertexBatch(Shader s) {
		this.shader = s;
		mVertexMap = new HashMap<Integer, VertexBatch.TextureInfo>();

		mPositionAttrib = s.getAttributeLocation("position");
		mTexcoordAttrib = s.getAttributeLocation("texcoord");
		mColorAttrib = s.getAttributeLocation("color");
	}

	/**
	 * Adds a quad (2 Triangles / 6 Vertices) to the rendering queue for later
	 * rendering with end()
	 * 
	 *2c-----d3
	 * |\    |
	 * |  \  |
	 * |    \|
	 *0a-----b1
	 * 
	 * @param tex
	 *            The texture to use for drawing, can be null(TODO)
	 * @param a
	 *            Lower Left
	 * @param b
	 *            Lower Right
	 * @param c
	 *            Upper Left
	 * @param d
	 *            Upper Right
	 * @param uvmin
	 *            The upper left texture coordinate in pixels
	 * @param uvmax
	 *            The lower right texture coordinate in pixels
	 * @param rgb
	 *            The tinting color
	 */
	public final void putQuad(Texture tex, Vector3f a, Vector3f b, Vector3f c,
			Vector3f d, Vector2f uvmin, Vector2f uvmax, Vector3f rgb) {
		if (mVertexMap.containsKey(tex.texId) == false) {
			TextureInfo ti = new TextureInfo();
			ti.texId = tex.texId;
			ti.vertices = ByteBuffer
					.allocateDirect(
							(3 * FLOAT_SIZE_BYTES + 3 * FLOAT_SIZE_BYTES + 2 * FLOAT_SIZE_BYTES) * 6 * 1)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			ti.vertexCount = 0;
			ti.vertexCapacity = 6;
			mVertexMap.put(tex.texId, ti);
			// System.out.print("Newly allocated to " +
			// mVertexMap.get(tex.texId).vertexCapacity +
			// " Vertices, which equates to " +
			// mVertexMap.get(tex.texId).vertices.capacity() + " floats\n");
		}

		if (mVertexMap.get(tex.texId).vertexCount >= mVertexMap.get(tex.texId).vertexCapacity)
		// if(mVertexMap.get(tex.texId).vertexCount >=
		// mVertexMap.get(tex.texId).vertexCapacity)
		{
			// System.out.printf("vertexCount: %d, vertexCapacity: %d\n",
			// mVertexMap.get(tex.texId).vertexCount,
			// mVertexMap.get(tex.texId).vertexCapacity);
			mVertexMap.get(tex.texId).vertexCapacity *= 2;
			FloatBuffer currentBuffer = mVertexMap.get(tex.texId).vertices;
			FloatBuffer newbuffer = ByteBuffer
					.allocateDirect(
							(3 * FLOAT_SIZE_BYTES + 3 * FLOAT_SIZE_BYTES + 2 * FLOAT_SIZE_BYTES)
									* mVertexMap.get(tex.texId).vertexCapacity)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			currentBuffer.flip();
			newbuffer.put(currentBuffer);
			mVertexMap.get(tex.texId).vertices = newbuffer;
			// System.out.print("Re-allocated to " +
			// mVertexMap.get(tex.texId).vertexCapacity +
			// " Vertices, which equates to " +
			// mVertexMap.get(tex.texId).vertices.capacity() + " floats\n");
		}

		Vector2f realuvmin = new Vector2f((float) (1. / tex.width * uvmin.x)
				+ (float) (0.1 / tex.width),
				(float) (1. / tex.height * uvmin.y)
						+ (float) (0.1 / tex.height));
		Vector2f realuvmax = new Vector2f((float) (1. / tex.width * uvmax.x)
				- (float) (0.1 / tex.width),
				(float) (1. / tex.height * uvmax.y)
						- (float) (0.1 / tex.height));
		// Vector2f realuvmin = new Vector2f((float)(1./tex.width*uvmin.x),
		// (float)(1./tex.height*uvmin.y));
		// Vector2f realuvmax = new Vector2f((float)(1./tex.width*uvmax.x),
		// (float)(1./tex.height*uvmax.y));

		// addTriangle(mVertexMap.get(texid), a, b, c, rgb);
		// addTriangle(mVertexMap.get(texid), c, b, d, rgb);
		TextureInfo ti = mVertexMap.get(tex.texId);

		addVertex(ti, b, rgb, new Vector2f(realuvmax.x, realuvmax.y));
		addVertex(ti, c, rgb, new Vector2f(realuvmin.x, realuvmin.y));
		addVertex(ti, a, rgb, new Vector2f(realuvmin.x, realuvmax.y));

		addVertex(ti, d, rgb, new Vector2f(realuvmax.x, realuvmin.y));
		addVertex(ti, c, rgb, new Vector2f(realuvmin.x, realuvmin.y));
		addVertex(ti, b, rgb, new Vector2f(realuvmax.x, realuvmax.y));
	}

	public void render() {
		// glEnableClientState(GL_VERTEX_ARRAY);
		// glEnableClientState(GL_COLOR_ARRAY);
		// glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glEnableVertexAttribArray(mPositionAttrib);
		glEnableVertexAttribArray(mTexcoordAttrib);
		glEnableVertexAttribArray(mColorAttrib);
		glActiveTexture(GL_TEXTURE0);

		for (Entry<Integer, TextureInfo> cursor : mVertexMap.entrySet()) {
			// prevent unnecessary glDraw* calls, that wouldn't draw any
			// vertices anyway
			// (this is because the memory for the vertices isn't actually
			// cleared to prevent deallocating and reallocating memory all the
			// time because it's highly likely we want the same amount of
			// vertex-memory the next frame)
			if (cursor.getValue().vertexCount == 0)
				continue;

			glBindTexture(GL_TEXTURE_2D, cursor.getKey());

			// cursor.getValue().vertices.flip();
			cursor.getValue().vertices
					.position(TRIANGLE_VERTICES_DATA_POS_OFFSET);
			// glVertexPointer(3, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
			// cursor.getValue().vertices);
			glVertexAttribPointer(mPositionAttrib, 3, false, (3 * 4) + (3 * 4)
					+ (2 * 4), cursor.getValue().vertices);

			cursor.getValue().vertices
					.position(TRIANGLE_VERTICES_DATA_COLOR_OFFSET);
			// glColorPointer(3, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
			// cursor.getValue().vertices);
			if (mColorAttrib != -1)
				glVertexAttribPointer(mColorAttrib, 3, false, (3 * 4) + (3 * 4)
						+ (2 * 4), cursor.getValue().vertices);

			cursor.getValue().vertices
					.position(TRIANGLE_VERTICES_DATA_UV_OFFSET);
			// glTexCoordPointer(2, TRIANGLE_VERTICES_DATA_STRIDE_BYTES,
			// cursor.getValue().vertices);
			if (mTexcoordAttrib != -1)
				glVertexAttribPointer(mTexcoordAttrib, 2, false, (3 * 4)
						+ (3 * 4) + (2 * 4), cursor.getValue().vertices);

			Game.drawCalls++;
			glDrawArrays(GL_TRIANGLES, 0, cursor.getValue().vertexCount + 1);
			cursor.getValue().vertexCount = 0;
			cursor.getValue().vertices.clear();
		}
		// glDisableClientState(GL_VERTEX_ARRAY);
		// glDisableClientState(GL_COLOR_ARRAY);
		// glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glDisableVertexAttribArray(mPositionAttrib);
		glDisableVertexAttribArray(mTexcoordAttrib);
		glDisableVertexAttribArray(mColorAttrib);

	}

	private void addVertex(TextureInfo ti, Vector3f p, Vector3f rgb, Vector2f uv) {
		ti.vertices.put(ti.vertexCount * 8 + 0, p.x);
		ti.vertices.put(ti.vertexCount * 8 + 1, p.y);
		ti.vertices.put(ti.vertexCount * 8 + 2, p.z);
		ti.vertices.put(ti.vertexCount * 8 + 3, rgb.x);
		ti.vertices.put(ti.vertexCount * 8 + 4, rgb.y);
		ti.vertices.put(ti.vertexCount * 8 + 5, rgb.z);
		ti.vertices.put(ti.vertexCount * 8 + 6, uv.x);
		ti.vertices.put(ti.vertexCount * 8 + 7, uv.y);
		ti.vertexCount++;
	}

	private void addVertex(TextureInfo ti, Vector4f p, Vector3f rgb, Vector3f uv) {
		ti.vertices.put(ti.vertexCount * 8 + 0, p.x);
		ti.vertices.put(ti.vertexCount * 8 + 1, p.y);
		ti.vertices.put(ti.vertexCount * 8 + 2, p.z);
		ti.vertices.put(ti.vertexCount * 8 + 3, rgb.x);
		ti.vertices.put(ti.vertexCount * 8 + 4, rgb.y);
		ti.vertices.put(ti.vertexCount * 8 + 5, rgb.z);
		ti.vertices.put(ti.vertexCount * 8 + 6, uv.x);
		ti.vertices.put(ti.vertexCount * 8 + 7, 1 - uv.y);
		ti.vertexCount++;
	}

	public Shader getShader() {
		return shader;
	}

	public void putOBJ(Texture tex, OBJFile obj) {
		if (mVertexMap.containsKey(tex.texId) == false) {
			TextureInfo ti = new TextureInfo();
			ti.texId = tex.texId;
			ti.vertices = ByteBuffer
					.allocateDirect(
							(3 * 4 + 3 * 4 + 2 * 4) * (obj.faces.size() * 3))
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			ti.vertexCount = 0;
			ti.vertexCapacity = obj.faces.size() * 3;
			mVertexMap.put(tex.texId, ti);
		}

		if (mVertexMap.get(tex.texId).vertexCount + obj.faces.size() * 3 >= mVertexMap.get(tex.texId).vertexCapacity){
			mVertexMap.get(tex.texId).vertexCapacity += obj.faces.size() * 3;
			FloatBuffer currentBuffer = mVertexMap.get(tex.texId).vertices;
			FloatBuffer newbuffer = ByteBuffer
					.allocateDirect(
							(3 * FLOAT_SIZE_BYTES + 3 * FLOAT_SIZE_BYTES + 2 * FLOAT_SIZE_BYTES)
									* mVertexMap.get(tex.texId).vertexCapacity)
					.order(ByteOrder.nativeOrder()).asFloatBuffer();
			currentBuffer.flip();
			newbuffer.put(currentBuffer);
			mVertexMap.get(tex.texId).vertices = newbuffer;
		}

		TextureInfo ti = mVertexMap.get(tex.texId);
		for (int i = 0; i < obj.faces.size(); i++) {
			addVertex(ti, obj.faces.get(i).v[0], new Vector3f(1, 1, 1),
					obj.faces.get(i).vt[0]);
			addVertex(ti, obj.faces.get(i).v[1], new Vector3f(1, 1, 1),
					obj.faces.get(i).vt[1]);
			addVertex(ti, obj.faces.get(i).v[2], new Vector3f(1, 1, 1),
					obj.faces.get(i).vt[2]);
		}
	}
}
