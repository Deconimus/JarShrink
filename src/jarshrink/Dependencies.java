package jarshrink;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import visionCore.util.Lists;
import visionCore.util.Zipper;

/**
 * @author Deconimus
 */
public class Dependencies {
	
	
	/**
	 * Gathers information on dependencies of all present classes in the specified jar.
	 * @author Deconimus
	 */
	public static Map<String, String[]> buildDependencyMap(String jdeps, File jar) {
		
		List<String> lines = new ArrayList<String>(32);
		
		try {
		
			ProcessBuilder pb = new ProcessBuilder(jdeps, "-verbose:class", "-filter:none", "\""+jar.getAbsolutePath()+"\"");
			Process p = pb.start();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			for (String line = "", cur = ""; (line = reader.readLine()) != null;) {
				
				lines.add(line.trim());
			}
			
		} catch (Exception e) { e.printStackTrace(); }
		
		Map<String, String[]> map = new HashMap<String, String[]>();
		
		String lastKey = null;
		List<String> buffer = new ArrayList<String>();
		
		for (String line : lines) {
			
			if (line.contains(" java.") || line.contains(" javax.")) { continue; }
			
			int ind = line.indexOf("->");
			int parInd = line.indexOf(" (");
			
			if (ind < 0 && parInd >= 0) {
				
				if (lastKey != null && buffer != null) {
					
					map.put(lastKey, buffer.toArray(new String[buffer.size()]));
				}
				
				lastKey = line.substring(0, parInd).trim();
				buffer.clear();
				
			} else if (ind == 0) {
				
				int start = -1, end = -1;
				
				for (int i = ind+3, len = line.length(); i < len; i++) {
					char c = line.charAt(i);
					
					if (c != ' ' && c != '\t' && c != '\n') {
						
						if (start < 0) { start = i; }
						
					} else if (start > -1) { end = i; break; }
				}
				
				if (end == -1) { end = line.length(); }
				
				buffer.add(line.substring(start, end));
			}
		}
		
		if (lastKey != null && !buffer.isEmpty()) {
			
			map.put(lastKey, buffer.toArray(new String[buffer.size()]));
		}
		
		return map;
	}
	
	
	public static void removeRedundantClasses(File dir, Set<String> dependencies) {
		
		removeRedundantClasses(dir, dir, dependencies, "");
	}
	
	public static void removeRedundantClasses(File dir, File root, Set<String> dependencies, String packageName) {
		
		if (packageName.equalsIgnoreCase("org.eclipse.jdt.internal")) { return; }
		
		for (File f : dir.listFiles()) {
			
			if (!f.isDirectory() && f.getName().toLowerCase().endsWith(".class")) {
				
				String className = packageName+f.getName();
				className = className.substring(0, className.lastIndexOf('.')).trim();
				
				if (!dependencies.contains(className)) {
					
					f.delete();
				}
				
			} else if (f.isDirectory()) {
				
				removeRedundantClasses(f, root, dependencies, packageName+f.getName()+".");
				if (f.list().length <= 0) { f.delete(); }
			}
		}
	}
	
}
