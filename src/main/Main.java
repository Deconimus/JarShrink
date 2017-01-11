package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipFile;

import visionCore.util.Files;
import visionCore.util.Jars;
import visionCore.util.Zipper;

public class Main {
	
	
	public static String abspath, javaHome;
	
	
	public static String jar, out;
	
	public static boolean printStatus, printDependencyList;
	
	static {
		
		printStatus = false;
		printDependencyList = true;
	}
	
	
	public static void main(String[] args) {
		
		setAbspath();
		setJavaHome();
		
		parseArgs(args);
		
		if (jar == null) {
			
			System.out.println("Jar not specified.");
			return;
		}
		
		File jarFile = new File(jar);
		if (!jarFile.exists()) { System.out.println("Jar not found."); return; }
		
		File unpacked = new File(abspath+"/tmp");
		if (unpacked.exists()) { Files.deleteDir(unpacked); }
		if (!unpacked.mkdir()) { unpacked.mkdirs(); }
		
		String mainClass = Jars.getMainClass(jarFile);
		
		if (mainClass == null || mainClass.trim().isEmpty()) { System.out.println("No Main-Class found."); return; }
		
		if (printStatus) System.out.println("Unpacking .jar");
		
		Jars.extract(jarFile, unpacked);
		
		if (printStatus) System.out.println("Analyzing dependencies");
		
		Map<String, String[]> dependencyMap = Dependencies.buildDependencyMap(unpacked);
		
		if (printStatus) System.out.println("Constructing dependency-tree");
		
		Set<String> classTree = ClassTreeBuilder.getClassTree(mainClass, dependencyMap);
		
		if (printDependencyList) {
		
			System.out.println("\nDependencies:\n");
			
			for (String s : classTree) {
				
				System.out.println(s);
			}
			System.out.println();
		}
		
		if (printStatus) System.out.println("Scraping redundant classes");
		
		Dependencies.removeRedundantClasses(unpacked, classTree);
		Files.deleteEmptyDirs(unpacked);
		
		if (printStatus) System.out.println("Building new .jar");
		
		Jars.create(unpacked, new File(out));
		
		Files.deleteDir(unpacked);
		
		if (printStatus) System.out.println("Done");
	}
	
	
	private static void parseArgs(String[] args) {
		
		if (args.length > 0) {
			
			jar = cleanArg(args[0]);
		}
		
		for (int i = 1; i < args.length; i++) {
			
			String arg = cleanArg(args[i]);
			
			String nextArg = (i < args.length-1) ? cleanArg(args[i+1]) : null;
			if (nextArg != null && nextArg.startsWith("-")) { nextArg = null; }
			
			if (arg.equals("-s") || arg.equals("-status")) {
				
				printStatus = true;
				
			} else if (arg.equals("-n") || arg.equals("-nolist")) {
				
				printDependencyList = false;
				
			} else if (nextArg != null) {
			
				if (arg.equals("-out") || arg.equals("-o")) {
					
					out = nextArg;
					i++;
				}
				
			}
		}
		
		if (out == null || out.isEmpty()) {
			
			out = jar.substring(0, Math.max(0, jar.lastIndexOf('.')))+"_shrunken.jar";
		}
	}
	
	private static String cleanArg(String arg) {
		
		arg = arg.replace('\\', '/').trim();
		if (arg.startsWith("\"") && arg.endsWith("\"")) { arg = arg.substring(1, arg.length()-1).trim(); }
		
		return arg;
	}
	
	
	private static void setAbspath() {
		
		try {
			
			abspath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath().replace("\\", "/");
			
			if (abspath.endsWith("/bin")) {
				
				abspath = abspath.substring(0, abspath.indexOf("/bin"));
			}
			
			if (abspath.endsWith(".jar")) {
				
				abspath = new File(abspath).getParentFile().getAbsolutePath();
			}
			
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	private static void setJavaHome() {
		
		File home = new File(System.getProperty("java.home"));
		
		if (!home.getName().startsWith("jdk")) {
			
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
		
		javaHome = home.getAbsolutePath().replace("\\", "/");
	}
	
}
