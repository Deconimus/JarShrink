package main;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import visionCore.util.Lists;

public class ClassTreeBuilder {
	
	
	public static Set<String> getClassTree(String mainClass, Map<String, String[]> dependencyMap) {
		
		Set<String> classTree = new HashSet<String>();
		List<String> newlyAdded = new ArrayList<String>();
		
		classTree.add(mainClass);
		newlyAdded.add(mainClass);
		
		/*
		for (File cl : mainPackageDir.listFiles()) {
			
			String n = cl.getName();
			
			if (n.toLowerCase().endsWith(".class")) {
				
				String cn = mainPackage+"."+n.substring(0, n.lastIndexOf('.'));
				
				classTree.add(cn);
				newlyAdded.add(cn);
			}
		}
		*/
		
		List<String> buffer = new ArrayList<String>();
		
		while (!newlyAdded.isEmpty()) {
		
			for (String cl : newlyAdded) {
				
				String[] deps = dependencyMap.get(cl);
				if (deps == null) { System.out.println("]"+cl); continue; }
				
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
