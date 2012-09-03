package oz.wizards.net;

public class Package {
	byte [] packet;
	public int pointer;
	int length;
	
	public Package () {
		pointer = 0;
		packet = new byte[1024];
		length = 0;
	}
	
	public void fillHeader () {
		Packer.packString(this, "MAZE");
		Packer.packLong(this, System.currentTimeMillis());
	}
	
	public byte[] getPacket () {
		return packet;
	}
}
