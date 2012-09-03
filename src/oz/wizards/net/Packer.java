package oz.wizards.net;

import java.nio.ByteBuffer;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class Packer {
	public static void packString(Package p, String s) {
		packInt(p, s.length());
		for (int i = 0; i < s.length(); i++) {
			packChar(p, s.charAt(i));
		}
	}

	public static void packByte(Package p, byte b) {
		p.packet[p.pointer++] = b;
	}
	
	public static void packByteArray(Package p, byte [] b) {
		for(int i = 0; i < b.length; i++)
			packByte(p, b[i]);
	}
	
	public static void packByteArray(Package p, byte [] b, int from, int to) {
		for(int i = from; i < to; i++)
			packByte(p, b[i]);
	}

	//char is 16 bit/2 byte !!
	public static void packChar(Package p, char c) {
		byte[] bytes = ByteBuffer.allocate(2).putChar(c).array();
		for(byte b : bytes) {
			packByte(p, b);
		}
	}

	public static void packInt(Package p, int i) {
		byte[] bytes = ByteBuffer.allocate(4).putInt(i).array();
		for(byte b : bytes) {
			packByte(p, b);
		}
	}

	public static void packLong(Package p, long l) {
		packInt(p, (int)((0x00000000ffffffffL & l)));
		packInt(p, (int)((0xffffffff00000000L & l) >> 32));
	}

	public static void packFloat(Package p, float f) {
		packInt(p, Float.floatToIntBits(f));
	}
}
