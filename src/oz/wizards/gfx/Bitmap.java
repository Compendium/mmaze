package oz.wizards.gfx;
import java.io.File;
import java.io.FileOutputStream;


public class Bitmap {
	int rowsize, padding, pdata, bmp_size;
	byte[] pixel_array;
	int width, height;

	/**
	 * Reads a bitmap file from the give path, and fills the {@link #pixel_array} with the pixel data.
	 * @param path The path of the file to load
	 */
	public Bitmap(String path) {

	}

	/**
	 * Creates a new bitmap with the specified width and height
	 * @param width
	 * @param height
	 */
	public Bitmap(int width, int height) {
		this.width = width;
		this.height = height;
		rowsize = (int) (((float) (8 * width) / (float) 32) * 4);
		padding = (rowsize*3)%4;
		pixel_array = new byte[rowsize * height * 3 + (padding * height)];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++) {
				pixel_array[(x+y*rowsize)*3+0+(padding*y)] = (byte) 0x00;//b
				pixel_array[(x+y*rowsize)*3+1+(padding*y)] = (byte) 0x00;//g
				pixel_array[(x+y*rowsize)*3+2+(padding*y)] = (byte) 0x00;//r
			}
		}
	}
	
	/**
	 * Write the pixel data together with an bmp header to the specified path.
	 * @param path
	 */
	public void write (String path) {
		pdata = pixel_array.length;
		byte [] dib_header = {
				0x28, 0x00, 0x00, 0x00, // 40 bytes, number of bytes in the dib header (from this point)
				(byte)((width) & 0xff), (byte)((width >> 8) & 0xff), (byte)((width >> 16) & 0xff), (byte)((width >> 24) & 0xff), //width
				(byte)((height) & 0xff), (byte)((height >> 8) & 0xff), (byte)((height >> 16) & 0xff), (byte)((height >> 24) & 0xff), //height
				0x01, 0x00, //number of color planes being used, has to be 1
				0x18, 0x00, //number of bits per pixel, 24 bits here (RGB888)
				0x00, 0x00, 0x00, 0x00, //no compression
				(byte)((pdata) & 0xff), (byte)((pdata >> 8) & 0xff), (byte)((pdata >> 16) & 0xff), (byte)((pdata >> 24) & 0xff), // size of the raw data in the pixel array (+padding)
				0x13, 0x0B, 0x00, 0x00, //dpi horizontal
				0x13, 0x0B, 0x00, 0x00, //dpi vertical
				0x00, 0x00, 0x00, 0x00, //number of colors in the palette
				0x00, 0x00, 0x00, 0x00, //important colors, 0 = all
		};
		bmp_size = 14 /*header*/ + dib_header.length + pixel_array.length;
		byte[] bmp_header = {
				0x42, 0x4d, //magic number, file indentification "BM" for BitMap
				(byte)((bmp_size) & 0xff), (byte)((bmp_size >> 8) & 0xff), (byte)((bmp_size >> 16) & 0xff), (byte)((bmp_size >> 24) & 0xff), //size of the bmp file
				0x00, 0x00, 0x00, 0x00, //unused
				0x36, 0x00, 0x00, 0x00, //54 bytes, offset for pixel data/array
		};
		
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(new File(path));
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			fos.write(bmp_header);
			fos.write(dib_header);
			fos.write(pixel_array);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads {@link #pixel_array} with the pixel data of the specified file.
	 * @param path
	 */
	public void read (String path) {
		
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
