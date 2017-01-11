package visionCore.util;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

public class Files {
	
	public static void deleteFiles(String directory, boolean recursive) {
		
		deleteFiles(new File(directory), recursive, null);
	}
	
	public static void deleteFiles(File dir, boolean recursive) {
		
		deleteFiles(dir, recursive, null);
	}
	
	public static void deleteFiles(String directory, boolean recursive, Predicate<File> filter) {
		
		deleteFiles(new File(directory), recursive, filter);
	}
	
	public static void deleteFiles(File dir, boolean recursive, Predicate<File> filter) {
		
		if (dir != null && dir.exists()) {
		
			File[] files = dir.listFiles();
			
			if (files.length > 0) {
				
				for (File file : files) {
					
					if (file.isDirectory()) {
						
						if (recursive) {
							
							deleteFiles(file.getPath(), true, filter);
						}
						
					} else {
						
						if (filter == null || filter.test(file)) {
						
							file.delete();
						}
					}
					
				}
			}
			
		}
		
	}
	
	public static int getSize(String dir) {
		
		return getSize(new File(dir));
	}
	
	public static int getSize(File file) {
		
		int filesize = -1;
		boolean stream = false;
		
		try {
			FileInputStream fis = new FileInputStream(file);
			filesize = fis.available();
			filesize /= 1024;
			fis.close();
		} catch (Exception e) { e.printStackTrace(); }
		
		return filesize;
	}
	
	
	public static void cleanseDir(String directory) {
		
		cleanseDir(new File(directory));
	}
	
	public static void cleanseDir(File directory) {
		
		if (directory != null && directory.exists()) {
		
			File[] files = directory.listFiles();
			
			if (files.length > 0) {
				
				for (File file : files) {
					
					if (file.isDirectory()) {
						
						cleanseDir(file.getAbsolutePath());
						
					}
					
					file.delete();
				}
			}
		}
	}
	
	
	public static void deleteDir(String directory) {
		
		deleteDir(new File(directory));
	}
	
	public static void deleteDir(File directory) {
		
		if (!directory.isDirectory()) { return; }
		
		cleanseDir(directory);
		directory.delete();
	}
	
	
	public static void deleteEmptyDirs(File dir) {
		
		for (File f : dir.listFiles()) {
			
			if (f.isDirectory()) {
				
				if (f.list().length > 0) { deleteEmptyDirs(f); }
				if (f.list().length == 0) { f.delete(); }
			}
		}
	}
	
	
	public static List<File> getFiles(String directory, Predicate<File> filter) {
		
		return getFiles(new File(directory), filter);
	}
	
	public static List<File> getFiles(File directory, Predicate<File> filter) {
		
		List<File> files = new ArrayList<File>();
		
		if (!directory.exists() || directory.listFiles() == null || directory.listFiles().length <= 0) { return files; }
		
		for (File f : directory.listFiles()) {
			
			if (filter == null || filter.test(f)) {
				
				files.add(f);
			}
			
		}
		
		return files;
	}
	
	public static List<File> getFilesRecursive(String dir) {
		return getFilesRecursive(dir, null);
	}
	
	public static List<File> getFilesRecursive(File dir) {
		return getFilesRecursive(dir, null);
	}
	
	public static List<File> getFilesRecursive(String dir, Predicate<File> filter) {
		return getFilesRecursive(new File(dir), filter);
	}
	
	public static List<File> getFilesRecursive(File dir, Predicate<File> filter) {
		
		if (dir == null || !dir.exists()) { return null; }
		
		List<File> files = new ArrayList<File>();
		
		File[] dirFiles = dir.listFiles();
		
		if (dirFiles != null) {
			
			for (File file : dirFiles) {
				
				if (file.isDirectory()) {
					
					files.addAll(getFilesRecursive(file.getPath(), filter));
					
				} else {
					
					if (filter == null || filter.test(file)) {
					
						files.add(file);
					}
				}
					
			}
			
		}
		
		return files;
	}
	
	public static boolean copyFileUsingStream(File source, File dest) {
		
		InputStream is = null;
		OutputStream os = null;
		
		boolean copied = false;
		
		try {
			
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			
			os = new BufferedOutputStream(os);
			
			byte[] buffer = new byte[8192];
			
			for (int length = 0; (length = is.read(buffer)) > 0;) {
				
				os.write(buffer, 0, length);
				
			}
			
			copied = true;
			
		} catch (Exception e) { e.printStackTrace(); }
		
		if (os != null) {
			try { os.flush(); } catch (Exception e) { e.printStackTrace(); }
			try { os.close(); } catch (Exception e) { e.printStackTrace(); }
		}
		
		if (is != null) {
			try { is.close(); } catch (Exception e) { e.printStackTrace(); }
		}
		
		return copied;
		
	}
	
	public static boolean moveFileUsingStream(File source, File dest) {
		
		if (copyFileUsingStream(source, dest)) {
			
			source.delete();
			return true;
		}
		
		return false;
	}
	
	public static boolean copyFileUsingChannel(File source, File dest) {
		
		FileChannel sourceChannel = null;
	    FileChannel destChannel = null;
	    
	    boolean copied = false;
	    
	    try {
	    	
	    	sourceChannel = new FileInputStream(source).getChannel();
	    	destChannel = new FileOutputStream(dest).getChannel();
	    	destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
	    	
	    	copied = true;
	    	
	    } catch (Exception e) {
	    	
	    	e.printStackTrace();
	    	
	    } finally{
	    	
	    	if (sourceChannel != null) {
	    		try { sourceChannel.close(); } catch (IOException e) { e.printStackTrace(); }
	    	}
	    	
	    	if (destChannel != null) {
	    		try { destChannel.close(); } catch (IOException e) { e.printStackTrace(); }
	    	}
	    	
	    }
	    
	    return copied;
	}
	
	public static boolean moveFileUsingChannel(File source, File dest) {
		
		if (copyFileUsingChannel(source, dest)) {
			
			source.delete();
			return true;
		}
		
		return false;
	}
	
	public static void copyFileUsingOS(File source, File dest) {
		
		try { java.nio.file.Files.copy(source.toPath(), dest.toPath(), REPLACE_EXISTING); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void moveFileUsingOS(File source, File dest) {
		
		try { java.nio.file.Files.move(source.toPath(), dest.toPath(), REPLACE_EXISTING); }
		catch (Exception e) { e.printStackTrace(); }
	}
	
	/** shorthand for copyFileUsingOS */
	public static void copy(File source, File dest) {
		
		copyFileUsingOS(source, dest);
	}
	
	/** shorthand for moveFileUsingOS */
	public static void move(File source, File dest) {
		
		moveFileUsingOS(source, dest);
	}
	
	public static void copyDir(File source, File dest) {
		
		if (!dest.exists() || !dest.isDirectory()) {
			
			dest.mkdirs();
		}
		
		List<File> files = getFilesRecursive(source.getAbsolutePath());
		
		for (File file : files) {
			
			String npath = dest.getAbsolutePath() + file.getAbsolutePath().substring(source.getAbsolutePath().length());
			
			File nfile = new File(npath);
			if (!nfile.getParentFile().exists()) {
				
				nfile.getParentFile().mkdirs();
			}
			
			copy(file, nfile);
		}
	}
	
	public static void moveDir(File source, File dest) {
		
		if (!dest.exists() || !dest.isDirectory()) {
			
			dest.mkdirs();
		}
		
		List<File> files = getFilesRecursive(source.getAbsolutePath());
		
		for (File file : files) {
			
			String npath = dest.getAbsolutePath() + file.getAbsolutePath().substring(source.getAbsolutePath().length());
			
			File nfile = new File(npath);
			if (!nfile.getParentFile().exists()) {
				
				nfile.getParentFile().mkdirs();
			}
			
			move(file, nfile);
		}
		
		source.delete();
	}
	
	
	public static String getExtension(File f) {
		
		String name = f.getName().toLowerCase();
		
		String x = "";
		try { x = name.substring(name.lastIndexOf(".")+1, name.length()); } catch (Exception e) {}
		
		return x;
	}
	
	
	public static boolean writeText(File file, String text) {
		
		PrintWriter out = null;
		
		try {
			
			out = new PrintWriter(file);
			out.write(text);
			
			if (out != null) { out.close(); }
			
		} catch (Exception e) {
			
			e.printStackTrace();
			if (out != null) { out.close(); }
			
			return false;
		}
		
		if (out != null) { out.close(); }
		
		return true;
	}
	
	/*
	public static String readText(File file) {
		
		String s = "";
		BufferedReader br = null;
		
		try { 
			
			br = new BufferedReader(new FileReader(file));
			
			for (String line = ""; (line = br.readLine()) != null;) {
				
				s += line.trim();
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		
		return s;
	}*/
	
	public static String readText(File file) {
		
		String s = "";
		
		if (!file.exists()) { return s; }
		
		Scanner scan = null;
		
		try {
			
			scan = new Scanner(file);
			
			for (String line = ""; scan.hasNextLine();) {
				line = scan.nextLine();
				
				s += line+"\n";
			}
			
			scan.close();
			
		} catch (Exception e) { e.printStackTrace(); if (scan != null) { scan.close(); } }
		
		return s;
	}
	
	public static List<String> readLines(File file) {
		
		return readLines(file, true);
	}
	
	public static List<String> readLines(File file, boolean printErr) {
		
		List<String> list = new ArrayList<String>();
		BufferedReader br = null;
		
		try {
			
			br = new BufferedReader(new FileReader(file));
			
			for (String line = ""; (line = br.readLine()) != null;) {
				
				list.add(line.trim());
			}
			
		} catch (Exception e) { if (printErr) { e.printStackTrace(); } }
		
		return list;
	}
	
	
	public static byte[] readFirstBytes(int bytes, File f) {
		if (!f.exists() || f.isDirectory()) { return null; }
		
		byte[] buffer = new byte[bytes];
		
		try {
			
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			dis.readFully(buffer);
			dis.close();
			
		} catch (Exception e) {}
		
		return buffer;
	}
	
	
	public static boolean canModify(File file) {
		
		String oldp = file.getAbsolutePath();
		String tmppath = oldp+"_kekbruhxdddd";
		File tmp = new File(tmppath);
		
		if (file.renameTo(tmp)) {
			
			tmp.renameTo(new File(oldp));
			return true;
		}
		
		return false;
	}
	
	public static void waitOnFile(File file, int maxSeconds) {
		
		waitOnFile(file, maxSeconds, true);
	}
	
	public static void waitOnFile(File file, int maxSeconds, boolean write) {
		
		long maxMillis = maxSeconds * 1000L;
		
		if (!file.exists()) { return; }
		//for (long m = 0, w = 50; m < 100 && !file.exists(); m += w) { try { Thread.sleep(w); } catch (Exception e) {} }
		
		for (long m = 0, w = 50; m < maxMillis && ((write) ? !canModify(file) : !file.canRead()); m += w) {
			
			try { Thread.sleep(w); } catch (Exception e) {}
		}
		
	}
	
	
	/** Strips a fileName of illegal characters */
	public static String cleanName(String fileName) {
		
		fileName = fileName.replaceAll("[?\\\\/|\"<>*:]", "");
		fileName = fileName.trim();
		
		return fileName;
	}

}
