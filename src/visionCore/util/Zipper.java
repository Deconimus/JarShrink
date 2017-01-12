package visionCore.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class Zipper {

	public static void zip(String dir, String target) {
		zip(new File(dir), new File(target));
	}
	
	public static void zip(String dir, String target, int level) {
		zip(new File(dir), new File(target), level);
	}
	
	public static void zip(File input, File output) {
		
		zip(input, output, Deflater.DEFAULT_COMPRESSION);
	}
	
	/**
	 * Compresses a File and saves it to the given target file using the standart deflate algorithm
	 * @param dir The input file.
	 * @param target The output file.
	 * @param level The level of compression from 1 (fastest) to 9 (best).
	 */
	public static void zip(File input, File output, int level) {
		
		ZipOutputStream zos = null;
		
		try {
			
			zos = new ZipOutputStream(new FileOutputStream(output));
			byte[] buffer = new byte[1024];
			
			if (input.isDirectory()) {
				
				List<File> files = Files.getFilesRecursive(input.getAbsolutePath());
				
				for (File file : files) {
					
					ZipEntry ze = new ZipEntry(file.getCanonicalPath().substring(input.getCanonicalPath().length() + 1,
												file.getCanonicalPath().length()));
					zos.putNextEntry(ze);
					zos.setLevel(level);
					
					FileInputStream in = new FileInputStream(file);
					
					for (int len; (len = in.read(buffer)) > 0;) {
						
						zos.write(buffer, 0, len);
					}
					
					in.close();
				}
				
			} else {
				
				ZipEntry ze = new ZipEntry(input.getName());
				zos.putNextEntry(ze);
				zos.setLevel(level);
				
				FileInputStream in = new FileInputStream(input);
				
				for (int len; (len = in.read(buffer)) > 0;) {
					
					zos.write(buffer, 0, len);
				}
				
				in.close();
			}
			
			zos.flush();
			zos.close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		} finally {
			
			if (zos != null) {
				
				try {
					zos.flush();
					zos.close();
				} catch (Exception f) { f.printStackTrace(); }
				
			}
			
		}
		
	}
	
	
	public static void unzip(File zipFile, File outdir) {
		
		if (outdir.exists() && !outdir.isDirectory()) { System.err.println("visionCore.util.Zipper.unzip(zip, outdir): 'outdir' is not a directory!"); return; }
		
		if (!outdir.exists()) {
			
			if (!outdir.mkdirs()) { System.err.println("visionCore.util.Zipper.unzip(zip, outdir): 'outdir' couldn't be created."); return; }
		}
		
		String outpath = outdir.getAbsolutePath().replace("\\", "/");
		
		String zfnl = zipFile.getName().toLowerCase();
		boolean isJar = zfnl.endsWith(".jar") || zfnl.endsWith(".jzip");
		
		ZipFile zf = null;
		
		try {
			
			zf = new ZipFile(zipFile);
			
			if (isJar) { new File(outpath+"/META-INF").mkdirs(); }
			
			byte[] buffer = new byte[8192];
			
			for (Enumeration<? extends ZipEntry> it = zf.entries(); it.hasMoreElements();) {
				ZipEntry ze = it.nextElement();
				
				File f = new File(outpath+"/"+ze.getName());
				if (f.exists()) { f.delete(); }
				
				//if (isJar && !f.getParentFile().exists()) { f.getParentFile().mkdirs(); }
				
				if (!ze.isDirectory()) {
					
					FileOutputStream out = new FileOutputStream(f);
					InputStream in = zf.getInputStream(ze);
					
					for (int len; (len = in.read(buffer)) > 0;) {
						
						out.write(buffer, 0, len);
					}
					
					in.close();
					
					out.flush();
					out.close();
					
				} else { f.mkdirs(); }
			}
			
			zf.close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.err.println("\nvisionCore.util.Zipper.unzip(zip, outdir): Error while extracting .jar file.");
			try { if (zf != null) { zf.close(); } } catch (Exception e1) {}
		}
	}
	
	
	public static InputStream getZipStream(String dir) {
		return getZipStream(new File(dir));
	}
	
	public static InputStream getZipStream(File input) {
		
		try {
			
			ZipInputStream zin = new ZipInputStream(new FileInputStream(input));
			ZipEntry ze = zin.getNextEntry();
			ZipFile zf = new ZipFile(input);
			
			return zf.getInputStream(ze);
			
		} catch (Exception e) { e.printStackTrace(); }
		
		return null;
		
	}
	
	public static byte[] getEntry(File zipFile, String entry) {
		
		ZipFile zf = null;
		
		try {
			
			zf = new ZipFile(zipFile);
			
			InputStream in = zf.getInputStream(zf.getEntry(entry));
			
			byte[] buffer = new byte[4096];
			
			ByteArrayOutputStream out = new ByteArrayOutputStream(8192);
			
			for (int len; (len = in.read(buffer)) > 0;) {
				
				out.write(buffer, 0, len);
			}
			
			in.close();
			out.flush();
			out.close();
			zf.close();
			
			return out.toByteArray();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.err.println("\nvisionCore.util.Jars: Error while reading .jar file.");
			
			try { if (zf != null) { zf.close(); } } catch (Exception e1) {}
		}
		
		return null;
	}
	
	public static String[] listEntries(File zipFile) {
		
		String[] entries = null;
		
		ZipFile zf = null;
		
		try {
			
			zf = new ZipFile(zipFile);
			entries = new String[zf.size()];
			
			int i = 0;
			for (Enumeration<? extends ZipEntry> it = zf.entries(); it.hasMoreElements(); i++) {
				ZipEntry ze = it.nextElement();
				
				entries[i] = ze.getName();
			}
			
			zf.close();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			System.err.println("\nvisionCore.util.Jars: Error while reading .jar file.");
			
			try { if (zf != null) { zf.close(); } } catch (Exception e1) {}
		}
		
		return entries;
	}
	
	
	public static void addEntry(ZipOutputStream zout, File file, File root, byte[] buffer) throws IOException {
		
		String entryName = file.getAbsolutePath().substring(root.getAbsolutePath().length()+1).replace('\\', '/');
		
		addEntry(zout, entryName, file, buffer);
	}
	
	public static void addEntry(ZipOutputStream zout, String entryName, File file, byte[] buffer) throws IOException {
		
		if (buffer == null) { buffer = new byte[4096]; }
		
		ZipEntry ze = new ZipEntry(entryName+((file.isDirectory()) ? "/" : ""));
		
		zout.putNextEntry(ze);
		
		if (!file.isDirectory()) {
			
			InputStream in = new FileInputStream(file);
			
			for (int len; (len = in.read(buffer)) > 0;) {
				
				zout.write(buffer, 0, len);
			}
			
			in.close();
		}
		
		zout.closeEntry();
	}
	
	
}
