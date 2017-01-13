package jarshrink;

import java.io.File;
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
	
	
	private File tmpdir;
	
	private String jdepsLocation;
	
	private boolean printStatus, printDependencyList;
	
	
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
			
			System.out.println("No Main-Class found and no packages to keep.");
			return;
		}
		
		if (printStatus) System.out.println("Unpacking .jar");
		
		Jars.extract(jarFile, unpacked);
		
		if (printStatus) System.out.println("Analyzing dependencies");
		
		Map<String, String[]> dependencyMap = Dependencies.buildDependencyMap(jdepsLocation, unpacked);
		
		if (printStatus) System.out.println("Constructing dependency-tree");
		
		Set<String> classTree = ClassTreeBuilder.getClassTree(mainClass, dependencyMap, keep);
		
		if (printDependencyList) {
		
			System.out.println("\nDependencies:\n");
			
			for (String s : classTree) {
				
				System.out.println(s);
			}
			System.out.println();
		}
		
		if (printStatus) System.out.println("Scraping redundant classes");
		
		Dependencies.removeRedundantClasses(unpacked, classTree);
		
		if (printStatus) System.out.println("Building new .jar");
		
		Jars.create(unpacked, out);
		
		Files.deleteDir(unpacked);
		
		if (printStatus) System.out.println("Done");
	}
	
	
	public File getTmpdir() { return tmpdir; }
	public void setTmpdir(File tmpdir) { this.tmpdir = tmpdir; }

	public String getJdepsLocation() { return jdepsLocation; }
	public void setJdepsLocation(String jdepsLocation) { this.jdepsLocation = jdepsLocation; }

	public boolean getPrintStatus() { return printStatus; }
	public void setPrintStatus(boolean printStatus) { this.printStatus = printStatus; }

	public boolean getPrintDependencyList() { return printDependencyList; }
	public void setPrintDependencyList(boolean printDependencyList) { this.printDependencyList = printDependencyList; }
	
	
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
		
		return home.getAbsolutePath()+File.separator+"bin"+File.separator+"jdeps" + ((isWindows) ? ".exe" : "");
	}
}
