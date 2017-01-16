package jarshrink;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import visionCore.io.MultiOutputStream;
import visionCore.io.MultiPrintStream;
import visionCore.util.Files;
import visionCore.util.Jars;
import visionCore.util.Zipper;

/**
 * JarShrink's Main class.
 * @author Deconimus
 */
public class Main {
	
	
	public static String abspath, javaHome;
	
	
	public static String jar, out, keep[], tmpdir;
	
	public static boolean printStatus, printDependencyList;
	
	static {
		
		printStatus = false;
		printDependencyList = true;
	}
	
	
	public static void main(String[] args) {
		
		if (args.length == 0 || (args[0].trim().equals("-h") || args[0].trim().endsWith("-help"))) { help(); return; }
		
		setAbspath();
		setJavaHome();
		
		parseArgs(args);
		
		if (jar == null) {
			
			System.out.println("Jar not specified. Run \"-help\" for more information.");
			return;
		}
		
		File jarFile = new File(jar);
		if (!jarFile.exists()) { System.out.println("Jar not found."); return; }
		
		
		JarShrinker shrinker = new JarShrinker(new File(tmpdir));
		shrinker.setPrintStatus(printStatus);
		shrinker.setPrintDependencyList(printDependencyList);
		
		try {
			
			PrintStream logOut = new PrintStream(new FileOutputStream(abspath+File.separator+"log.txt"));
			PrintStream multiOut = new MultiPrintStream(System.out, logOut);
			
			shrinker.setPrintStream(multiOut);
		
		} catch (FileNotFoundException e) { }
		
		shrinker.shrink(jarFile, new File(out), keep);
		
	}
	
	
	private static void parseArgs(String[] args) {
		
		if (args.length <= 0) { return; }
			
		jar = cleanArg(args[0]);
		
		List<String> keep = new ArrayList<String>();
		
		for (int i = 1; i < args.length; i++) {
			
			String arg = cleanArg(args[i]);
			
			String nextArg = (i < args.length-1) ? cleanArg(args[i+1]) : null;
			if (nextArg != null && nextArg.startsWith("-")) { nextArg = null; }
			
			if (arg.equals("-s") || arg.equals("-status")) {
				
				printStatus = true;
				
			} else if (arg.equals("-n") || arg.equals("-nolist")) {
				
				printDependencyList = false;
				
			} else if (nextArg != null) {
			
				if (arg.equals("-o") || arg.equals("-out")) {
					
					out = nextArg;
					i++;
					
				} else if (arg.equals("-k") || arg.equals("-keep")) {
					
					while (nextArg.endsWith("*")) { nextArg = nextArg.substring(0, nextArg.length()-1); }
					while (nextArg.endsWith("..")) { nextArg = nextArg.substring(0, nextArg.length()-1); }
					
					keep.add(nextArg);
					i++;
					
				} else if (arg.equals("-t") || arg.equals("-tmp")) {
					
					tmpdir = nextArg;
					i++;
				}
				
			}
		}
		
		if (out == null || out.isEmpty()) {
			
			out = jar.substring(0, Math.max(0, jar.lastIndexOf('.')))+"_shrunken.jar";
		}
		
		if (tmpdir == null || tmpdir.isEmpty()) {
			
			tmpdir = abspath;
		}
		
		Main.keep = keep.toArray(new String[keep.size()]);
	}
	
	private static String cleanArg(String arg) {
		
		arg = arg.trim();
		if (arg.startsWith("\"") && arg.endsWith("\"")) { arg = arg.substring(1, arg.length()-1).trim(); }
		
		return arg;
	}
	
	
	private static void help() {
		
		System.out.println("\nJarShrink by Deconimus\n");
		
		System.out.println("Grammar:\n");
		System.out.println("\tjarShrink <jarFile> [<argumentName> <argumentValue>]");
		
		System.out.println("\nArguments:\n");
		
		System.out.println("\t-o | -out\tSpecifies the output-file for the newly created jar.");
		System.out.println("\t-k | -keep\tSpecifies a package or class that will be retained together with it's");
		System.out.println("\t\t\tdependencies. Can be called multiple times.");
		System.out.println("\t-s | -status\tPrint status information.");
		System.out.println("\t-n | -nolist\tDon't print the dependency list.");
		
		System.out.println();
		
	}
	
	
	private static void setAbspath() {
		
		try {
			
			abspath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
			
			if (abspath.endsWith(File.separator+"bin")) {
				
				abspath = abspath.substring(0, abspath.indexOf(File.separator+"bin"));
			}
			
			if (abspath.endsWith(".jar")) {
				
				abspath = new File(abspath).getParentFile().getAbsolutePath();
			}
			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private static void setJavaHome() {
		
		File home = new File(System.getProperty("java.home"));
		
		if (home.getName().equals("jre") && home.getParentFile().getName().startsWith("jdk")) {
			
			home = home.getParentFile();
			
		} else if (!home.getName().startsWith("jdk")) {
			
			File d = home.getParentFile();
			
			long ver = -1L;
			
			for (File f : d.listFiles()) {
				
				if (f.isDirectory() && f.getName().startsWith("jdk")) {
					
					long v = -1L;
					try { v = (long)Double.parseDouble(f.getName().substring(3).replace(".", "").replace("_", "")); }
					catch (Exception e) {}
					
					if (v > ver) {
						
						ver = v;
						home = f;
					}
				}
			}
		}
		
		javaHome = home.getAbsolutePath();
	}
	
}
