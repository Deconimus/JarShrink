package jarshrink;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import visionCore.util.Lists;

/**
 * Builds the class-dependcy-tree
 * @author Deconimus
 */
public class ClassTreeBuilder {

	/**
	 * Builds the class-dependcy-tree
	 * @author Deconimus
	 */
	public static Set<String> getClassTree(String mainClass, Map<String, String[]> dependencyMap, String[] keep, boolean printExtDeps) {
		
		return getClassTree(mainClass, dependencyMap, keep, printExtDeps, System.out);
	}
	
	/**
	 * Builds the class-dependcy-tree
	 * @author Deconimus
	 */
	public static Set<String> getClassTree(String mainClass, Map<String, String[]> dependencyMap, String[] keep, boolean printExtDeps, PrintStream printStream) {
		
		Set<String> classTree = new HashSet<String>();
		List<String> newlyAdded = new ArrayList<String>();
		
		boolean nullDeps = false;
		
		if (mainClass != null) {
		
			classTree.add(mainClass);
			newlyAdded.add(mainClass);
		}
		
		if (keep != null && keep.length > 0) {
		
			for (String cl : dependencyMap.keySet()) {
				
				for (String k : keep) {
					
					if (cl.startsWith(k)) {
						
						classTree.add(cl);
						newlyAdded.add(cl);
						
						break;
					}
				}
			}
		}
		
		List<String> buffer = new ArrayList<String>();
		
		while (!newlyAdded.isEmpty()) {
		
			for (String cl : newlyAdded) {
				
				String[] deps = dependencyMap.get(cl);
				
				if (deps == null && printExtDeps) {
					
					if (!nullDeps) {
						
						if (printStream != null) printStream.println("External Dependencies:\n"); 
						nullDeps = true;
					}
					
					if (printStream != null) printStream.println("]"+cl); 
					continue;
				}
				
				Lists.addAll(buffer, deps);
			}
			
			newlyAdded.clear();
			
			for (String cl : buffer) {
				
				if (!classTree.contains(cl)) {
					
					classTree.add(cl);
					newlyAdded.add(cl);
				}
			}
			
			buffer.clear();
		}
		
		return classTree;
	}
	
	
}
