package visionCore.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.zip.ZipOutputStream;

import visionCore.math.FastMath;

public class Jars {
	
	
	public static String getManifestText(File jar) {
		
		if (jar.isDirectory()) {
			
			return Files.readText(new File(jar.getAbsolutePath().replace("\\", "/")+"/META-INF/MANIFEST.MF"));
		}
		
		byte[] data = Zipper.getEntry(jar, "META-INF/MANIFEST.MF");
		if (data == null) { return null; }
		
		return new String(data, Charset.forName("UTF8"));
	}
	
	
	public static String getMainClass(File jar) {
		
		return getMainClass(getManifestText(jar));
	}
	
	private static String getMainClass(String manifestText) {
		
		if (manifestText == null) { return null; }
		
		String f = "Main-Class: ";
		int ind = manifestText.indexOf(f);
		
		if (ind < 0) { return null; }
		
		int start = -1, end = ind;
		
		for (int i = ind+f.length(), len = manifestText.length(); i < len; i++, end=i) {
			char c = manifestText.charAt(i);
			
			if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
				
				if (start < 0) { start = i; }
				
			} else if (start > -1) { break; }
		}
		
		return manifestText.substring(FastMath.clampToRange(start, 0, manifestText.length()), 
									  FastMath.clampToRange(end, 0, manifestText.length()));
	}
	
	public static String getMainPackage(File jar) {
		
		String mc = getMainClass(jar);
		
		return mc.substring(0, Math.max(mc.lastIndexOf('.'), 0));
	}
	
	
	public static void extract(File jar, File outdir) {
		
		Zipper.unzip(jar, outdir);
	}
	
	
	public static byte[] getEntry(File jar, String entry) {
		
		return Zipper.getEntry(jar, entry);
	}
	
	public static String[] listEntries(File jar) {
		
		return Zipper.listEntries(jar);
	}
	
	
	public static void create(File in, File out) {
		
		create(in, out, new File(in.getAbsolutePath().replace("\\", "/")+"/META-INF/MANIFEST.MF"));
	}
	
	public static void create(File in, File out, File manifest) {
		
		if (out.exists()) { out.delete(); }
		
		ZipOutputStream zout = null;
		
		try {
			
			zout = new ZipOutputStream(new FileOutputStream(out));
			
			byte[] buffer = new byte[4096];
			
			if (manifest != null && manifest.exists() && !manifest.isDirectory()) {
				
				Zipper.addEntry(zout, "META-INF/MANIFEST.MF", manifest, buffer);
			}
			
			addEntries(zout, in, in, manifest, buffer);
			
			zout.flush();
			zout.close();
			
		} catch (Exception e) { e.printStackTrace(); }
		
	}
	
	private static void addEntries(ZipOutputStream zout, File dir, File root, File manifest, byte[] buffer) throws IOException {
		
		for (File f : dir.listFiles()) {
			
			if (f == manifest || f.equals(manifest)) { continue; }
			
			Zipper.addEntry(zout, f, root, buffer);
			
			if (f.isDirectory()) {
				
				addEntries(zout, f, root, manifest, buffer);
			}
		}
	}
	
	
}
