package jarshrink;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import visionCore.util.Files;
import visionCore.util.Jars;

/**
 * The JarShrinks processor-class.
 * @author Deconimus
 */
public class JarShrinker {
	
	
	private static final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
	private static final String ext = ((isWindows) ? ".exe" : "");
	
	
	private File tmpdir;
	
	private String jdepsLocation;
	
	private boolean printStatus, printDependencyList;
	
	private PrintStream printStream;
	
	
	public JarShrinker() {
		
		this(new File(findAbspath()+File.separator+"tmp"));
	}
	
	/**
	 * @param tmpdir	Specifies the directory in which the jar will be temporarily extracted
	 * @author Deconimus
	 */
	public JarShrinker(File tmpdir) {
		
		this(tmpdir, findJdepsLocation());
	}
	
	/**
	 * @param tmpdir	Specifies the directory in which the jar will be temporarily extracted
	 * @param tmpdir	Specifies the location of jdeps
	 * @author Deconimus
	 */
	public JarShrinker(File tmpdir, String jdepsLocation) {
		
		this.tmpdir = tmpdir;
		this.jdepsLocation = jdepsLocation;
		
		this.printStatus = false;
		this.printDependencyList = false;
		
		this.setPrintStream(System.out);
	}
	
	
	/**
	 * Shrinks the jar by removing redundant classes and subclasses. <br>
	 * Optionally it keeps specified packages and classes together with their dependencies. <br>
	 * 
	 * @param jarFile	The specified jar to shrink.
	 * @param out		The output-file of the new jar.
	 * @param keep		Keep the specified packages or classes and their dependencies.
	 * 
	 * @author Deconimus
	 */
	public void shrink(File jarFile, File out, String... keep) {
		
		String jarFileName = jarFile.getName();
		int ind = jarFileName.lastIndexOf('.');
		
		File unpacked = new File(tmpdir.getAbsolutePath()+File.separator+jarFileName.substring(0, (ind == -1) ? jarFileName.length() : ind));
		if (unpacked.exists()) { Files.deleteDir(unpacked); }
		if (!unpacked.mkdir()) { unpacked.mkdirs(); }
		
		String mainClass = Jars.getMainClass(jarFile);
		
		if ((mainClass == null || mainClass.trim().isEmpty()) && (keep == null || keep.length <= 0)) { 
			
			printStream.println("No Main-Class found and no packages to keep.");
			return;
		}
		
		if (printStatus && printStream != null) printStream.println("Unpacking .jar");
		
		Jars.extract(jarFile, unpacked);
		
		if (printStatus && printStream != null) printStream.println("Analyzing dependencies");
		
		Map<String, String[]> dependencyMap = Dependencies.buildDependencyMap(jdepsLocation, unpacked);
		
		if (printStatus && printStream != null) printStream.println("Constructing dependency-tree");
		
		Set<String> classTree = ClassTreeBuilder.getClassTree(mainClass, dependencyMap, keep);
		
		if (printDependencyList && printStream != null) {
		
			printStream.println("\nDependencies:\n");
			
			for (String s : classTree) {
				
				printStream.println(s);
			}
			printStream.println();
		}
		
		if (printStatus && printStream != null) printStream.println("Scraping redundant classes");
		
		Dependencies.removeRedundantClasses(unpacked, classTree);
		
		if (printStatus && printStream != null) printStream.println("Building new .jar");
		
		Jars.create(unpacked, out);
		
		Files.deleteDir(unpacked);
		
		if (printStatus && printStream != null) printStream.println("Done");
	}
	
	
	public File getTmpdir() { return tmpdir; }
	public void setTmpdir(File tmpdir) { this.tmpdir = tmpdir; }

	public String getJdepsLocation() { return jdepsLocation; }
	public void setJdepsLocation(String jdepsLocation) { this.jdepsLocation = jdepsLocation; }

	public boolean getPrintStatus() { return printStatus; }
	public void setPrintStatus(boolean printStatus) { this.printStatus = printStatus; }

	public boolean getPrintDependencyList() { return printDependencyList; }
	public void setPrintDependencyList(boolean printDependencyList) { this.printDependencyList = printDependencyList; }
	
	public PrintStream getPrintStream() { return printStream; }
	public void setPrintStream(PrintStream out) { this.printStream = out; }
	
	
	private static String findAbspath() {
		
		String abspath = null;
		
		try {
			
			abspath = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
			
			if (abspath.endsWith(File.separator+"bin")) {
				
				abspath = abspath.substring(0, abspath.indexOf(File.separator+"bin"));
			}
			
			if (abspath.endsWith(".jar")) {
				
				abspath = new File(abspath).getParentFile().getAbsolutePath();
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		
		return (abspath == null) ? "" : abspath;
	}
	
	private static String findJdepsLocation() {
		
		File home = new File(System.getProperty("java.home"));
		
		if (home.getName().toLowerCase().equals("jre") && home.getParentFile().getName().toLowerCase().startsWith("jdk")) {
			
			home = home.getParentFile();
			
		} else if (!home.getName().toLowerCase().startsWith("jdk")) {
			
			File d = home.getParentFile();
			
			long ver = -1L;
			
			for (File f : d.listFiles()) {
				
				if (f.isDirectory() && f.getName().toLowerCase().startsWith("jdk")) {
					
					if (!new File(f.getAbsolutePath()+File.separator+"bin"+File.separator+"jdeps"+ext).exists()) { continue; }
					
					long v = -1L;
					
					try {
						v = (long)Double.parseDouble(f.getName().replace(" ", "").substring(3).replace(".", "")
										.replace("x86", "").replace("x64", "").replace("u", "").replace("_", "")); 
					} catch (Exception e) {}
					
					if (v > ver) {
						
						ver = v;
						home = f;
					}
				}
			}
		}
		
		return home.getAbsolutePath()+File.separator+"bin"+File.separator+"jdeps"+ext;
	}

}
