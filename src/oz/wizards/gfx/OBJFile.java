package oz.wizards.gfx;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

/**
 * A class to parse and use .obj files. NOTE: the faces *must* be defined like
 * this: f v1/vt1 v2/vt2 v3/vt3 ... or like this: f v1/vt1/vn1 v2/vt2/vn2
 * v3/vt3/vn3 ...
 * 
 * @author compendium
 */
public class OBJFile {
	boolean isValid = false;
	public String name;
	public List<Vector4f> v = new Vector<Vector4f>(); //vertex
	public List<Vector3f> vt = new Vector<Vector3f>(); //texcoord
	public List<Vector3f> vn = new Vector<Vector3f>(); //normal

	class Face { // type to make face parsing easier
		public Vector4f v[] = new Vector4f[3]; // vertex coordinates
		public Vector3f vt[] = new Vector3f[3];// texture coordinates
		public Vector3f vn[] = new Vector3f[3];// normals
	};

	public List<Face> faces = new Vector<Face>();

	public OBJFile(String path) {
		try {
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;
			while ((strLine = br.readLine()) != null) {
				if(strLine.startsWith("#")){
					//skipping comment
					continue;
				} else if(strLine.startsWith("mtllib ")) {
					//skipping material inclusion
					continue;
				} else if(strLine.startsWith("o ")) {
					name = strLine.substring(2);
					System.out.println(name);
				} else if(strLine.startsWith("v ")) {
					String data = strLine.substring(2);
					v.add(parseVector4f(data));
				} else if(strLine.startsWith("vt ")) {
					String data = strLine.substring(3);
					vt.add(parseVector3f(data));
				} else if(strLine.startsWith("vn ")) {
					String data = strLine.substring(3);
					vn.add(parseVector3f(data));
				} else if(strLine.startsWith("usemtl")) {
					//skipping command to use material
					continue;
				} else if(strLine.startsWith("s ")) {
					//TODO enable/disable smooth shading
				} else if(strLine.startsWith("f ")) {
					String data = strLine.substring(2);
					faces.add(parseFace(data));
				}
			}

			in.close();
			isValid = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// f v1/[vt1]/vn1
	private Face parseFace(String data) {
		Face nface = new Face();
		Pattern groupPattern = Pattern.compile("\\S+");
		Matcher groupMatcher = groupPattern.matcher(data);
		int group = 0;
		while(groupMatcher.find()) {
			Pattern digitPattern = Pattern.compile("[0-9]+");
			Matcher digitMatcher = digitPattern.matcher(groupMatcher.group());
			
			digitMatcher.find();//should match at least 1 group, or else the file is corrupt/invalid
			nface.v[group] = v.get(Integer.parseInt(digitMatcher.group())-1);
			if(groupMatcher.group().contains("//")) { //obj doesn't define texture coordinates
				digitMatcher.find();
				nface.vn[group] = vn.get(Integer.parseInt(digitMatcher.group())-1);
				nface.vt[group] = new Vector3f(0,0,0);
				group++;
				continue;
			}
			
			digitMatcher.find();
			nface.vt[group] = vt.get(Integer.parseInt(digitMatcher.group())-1);
			digitMatcher.find();
			nface.vn[group] = vn.get(Integer.parseInt(digitMatcher.group())-1);
			
			group++;
		}
		return nface;
	}

	private Vector4f parseVector4f (String data) {
		Vector3f a = parseVector3f(data);
		return new Vector4f(a.x, a.y, a.z, 1.0f);
	}
	
	private Vector3f parseVector3f (String data) {
		Vector3f d = new Vector3f();
		
		String d1 = "", d2 = "", d3 = "";
		Pattern pattern = Pattern.compile("-?[0-9]+\\.[0-9]+");
		Matcher matcher = pattern.matcher(data);
		matcher.find();
		d1 = matcher.group();
		matcher.find();
		d2 = matcher.group();
		if(matcher.find()) {
			d3 = matcher.group();
		} else d3 = "0";
		d.x = Float.parseFloat(d1);
		d.y = Float.parseFloat(d2);
		d.z = Float.parseFloat(d3);
		return d;
	}
}
