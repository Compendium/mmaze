package oz.wizards;
import java.util.Random;
import java.util.Collections;
import java.util.Arrays;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import oz.wizards.gfx.Texture;
import oz.wizards.gfx.VertexBatch;

/*
 * recursive backtracking algorithm
 * shamelessly borrowed from the ruby at
 * http://weblog.jamisbuck.org/2010/12/27/maze-generation-recursive-backtracking
 */
public class MazeGenerator {
	private final int x;
	private final int y;
	private final int[][] maze;
	private static final Random rand = new Random();
	public byte[][] bytemap;
	public float scale = 10.f; //scale, important for collision detection

	public MazeGenerator(int x, int y) {
		this.x = x;
		this.y = y;
		maze = new int[this.x][this.y];
		bytemap = new byte[this.x * 2+1][this.y * 2+1];
		generateMaze(0, 0);
		parse(false);
	}
	
	public void print () {
		for(int x = 0; x < bytemap[0].length; x++) {
			for(int y = 0; y < bytemap.length; y++) {
				System.out.print((bytemap[x][y] == 1 ? '█' : ' '));
			}
			System.out.print('\n');
		}
	}

	public void parse(boolean dbgOutput) {
		for (int i = 0; i < y; i++) {
			int xptr = 0;
			// draw the north edge
			for (int j = 0; j < x; j++) {
				if(dbgOutput) System.out.print((maze[j][i] & 1) == 0 ? "e-" : "f ");
				if ((maze[j][i] & 1) == 0) {
					bytemap[xptr][i * 2] = 1;
					bytemap[xptr + 1][i * 2] = 1;
				} else {
					bytemap[xptr][i * 2] = 1;
					bytemap[xptr + 1][i * 2] = 0;
				}
				xptr += 2;
			}
			if(dbgOutput) System.out.println("c");
			bytemap[xptr][i * 2] = 1;
			xptr = 0;
			// draw the west edge
			for (int j = 0; j < x; j++) {
				if(dbgOutput) System.out.print((maze[j][i] & 8) == 0 ? "g " : "  ");
				if ((maze[j][i] & 8) == 0) {
					bytemap[xptr][i * 2 + 1] = 1;
					bytemap[xptr + 1][i * 2 + 1] = 0;
				} else {
					bytemap[xptr][i * 2 + 1] = 0;
					bytemap[xptr + 1][i * 2 + 1] = 0;
				}
				xptr += 2;
			}
			if(dbgOutput) System.out.println("d");
			bytemap[xptr][i * 2+1] = 1;
		}
		// draw the bottom line
		for (int j = 0; j < x; j++) {
			if(dbgOutput) System.out.print("--");
			bytemap[j*2][y * 2] = 1;
			bytemap[j*2+1][y * 2] = 1;
		}
		if(dbgOutput) System.out.println("b");
	}
	
	/**
	 * Puts a mesh of the maze into the specified {@link VertexBatch}.
	 * @param v
	 */
	public void meshify (VertexBatch v, Texture t) {
		float f = scale;
		for(int y = 0; y < bytemap[0].length; y++){
			for(int x = 0; x < bytemap.length; x++) {
				if(x == 0 || y == 0 || x == bytemap[0].length-1 || y == bytemap.length-1) continue;
				if(bytemap[x][y] == 1) continue;
				
				//floor+ceiling
				if(bytemap[x][y] == 0) {
					v.putQuad(t,
							new Vector3f(x*f+0,0,y*f+0),	//0
							new Vector3f(x*f+1*f,0,y*f+0),	//1
							new Vector3f(x*f+0,0,y*f+-1*f),	//2
							new Vector3f(x*f+1*f,0,y*f+-1*f),	//3
							new Vector2f(0,0), new Vector2f(128, 128), new Vector3f(1,1,1));
					v.putQuad(t,
							new Vector3f(x*f+0, 1*f, y*f+-1*f),	//0
							new Vector3f(x*f+1*f, 1*f, y*f+-1*f),	//1
							new Vector3f(x*f, 1*f, y*f),	//2
							new Vector3f(x*f+1*f, 1*f, y*f),	//3
							new Vector2f(0,0), new Vector2f(128, 128), new Vector3f(1,1,1));
				}
				
				//left wall
				if(bytemap[x-1][y] == 1) {
					v.putQuad(t,
							new Vector3f(x*f+0,0,y*f+0),	//0
							new Vector3f(x*f+0,0,y*f+-1*f),	//1
							new Vector3f(x*f+0,1*f,y*f+0),	//2
							new Vector3f(x*f+0,1*f,y*f+-1*f),	//3
							new Vector2f(0,0), new Vector2f(128, 128), new Vector3f(1,1,1));
				}
				//right wall
				if(bytemap[x+1][y] == 1) {
					v.putQuad(t,
							new Vector3f(x*f+1*f,0,y*f+-1*f),	//0
							new Vector3f(x*f+1*f,0,y*f+0),	//1
							new Vector3f(x*f+1*f,1*f,y*f+-1*f),	//2
							new Vector3f(x*f+1*f,1*f,y*f+0),	//3
							new Vector2f(0,0), new Vector2f(128, 128), new Vector3f(1,1,1));
				}
				//front wall
				if(bytemap[x][y+1] == 1) {
					v.putQuad(t,
							new Vector3f(x*f+1*f,0,y*f),	//0
							new Vector3f(x*f+0,0,y*f),	//1
							new Vector3f(x*f+1*f,1*f,y*f),	//2
							new Vector3f(x*f+0,1*f,y*f),	//3
							new Vector2f(0,0), new Vector2f(128, 128), new Vector3f(1,1,1));
				}
				//back wall
				if(bytemap[x][y-1] == 1) {
					v.putQuad(t,
							new Vector3f(x*f+0,0,y*f+-1*f),	//0
							new Vector3f(x*f+1*f,0,y*f+-1*f),	//1
							new Vector3f(x*f+0,1*f,y*f+-1*f),	//2
							new Vector3f(x*f+1*f,1*f,y*f+-1*f),	//3
							new Vector2f(0,0), new Vector2f(128, 128), new Vector3f(1,1,1));
				}
			}
		}
	}

	private void generateMaze(int cx, int cy) {
		DIR[] dirs = DIR.values();
		Collections.shuffle(Arrays.asList(dirs));
		for (DIR dir : dirs) {
			int nx = cx + dir.dx;
			int ny = cy + dir.dy;
			if (between(nx, x) && between(ny, y) && (maze[nx][ny] == 0)) {
				maze[cx][cy] |= dir.bit;
				maze[nx][ny] |= dir.opposite.bit;
				generateMaze(nx, ny);
			}
		}
	}

	private static boolean between(int v, int upper) {
		return (v >= 0) && (v < upper);
	}

	private enum DIR {
		N(1, 0, -1), S(2, 0, 1), E(4, 1, 0), W(8, -1, 0);
		private final int bit;
		private final int dx;
		private final int dy;
		private DIR opposite;

		// use the static initializer to resolve forward references
		static {
			N.opposite = S;
			S.opposite = N;
			E.opposite = W;
			W.opposite = E;
		}

		private DIR(int bit, int dx, int dy) {
			this.bit = bit;
			this.dx = dx;
			this.dy = dy;
		}
	};

	/*
	 * public static void main(String[] args) { int x = args.length >= 1 ?
	 * (Integer.parseInt(args[0])) : 32; int y = args.length == 2 ?
	 * (Integer.parseInt(args[1])) : 32; MazeGenerator maze = new
	 * MazeGenerator(x, y); maze.display(); }
	 */
}