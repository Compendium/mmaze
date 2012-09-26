package oz.wizards.net;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Unpacker {
	public static String unpackString(Package p) {
		String s = "";
		int length = unpackInt(p);
		for (int i = 0; i < length; i++) {
			s += unpackChar(p);
		}
		return s;
	}

	public static byte unpackByte(Package p) {
		if(p.pointer >= p.length) {
			System.err.println("error: trying to read beyond packet length");
			Thread.dumpStack();
			p.pointer++;
			return 0;
		}
		return p.packet[p.pointer++];
	}

	public static char unpackChar(Package p) {
		char c = 0;
		ByteBuffer b = ByteBuffer.allocate(2).put(unpackByte(p)).put(unpackByte(p));
		b.rewind();
		c = b.getChar();
		return c;
	}

	public static int unpackInt(Package p) {
		int i = 0;
		i = (int) ((unpackChar(p) << 16) | (unpackChar(p)));
		return i;
	}
	
	public static long unpackLong (Package p) {
		long l = 0;
		l = (long)((unpackInt(p)) | (unpackInt(p) << 32));
		return l;
	}
	
	public static float unpackFloat (Package p) {
		float f = 0;
		f = Float.intBitsToFloat(unpackInt(p));
		return f;
	}

	public static byte[] unpackByteArray(Package p, int total) {
		byte[] b = Arrays.copyOfRange(p.packet, p.pointer, p.pointer+total);
		p.pointer += total;
		return b;
	}
}
