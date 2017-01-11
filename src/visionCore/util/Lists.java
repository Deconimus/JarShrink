package visionCore.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Lists {
	
	
	public static <T> void addAll(List<T> list, T[] array) {
		
		if (list instanceof ArrayList) {
			
			ArrayList<T> a = (ArrayList<T>)list;
			a.ensureCapacity(a.size()+array.length);
		}
		
		for (T t : array) {
			
			list.add(t);
		}
	}
	
	public static <T> void addAll(List<T> list, int index, T[] array) {
		
		if (list instanceof ArrayList) {
			
			// way less array copying due to element shifts
			
			List<T> buf = asArrayList(array);
			list.addAll(index, buf);
			
		} else {
		
			for (int i = 0; i < array.length; i++) {
				
				list.add(index+i, array[i]);
			}
		}
	}
	

	public static <T> T getLast(List<T> list) {
		
		if (!list.isEmpty()) {
			
			return list.get(list.size()-1);
		}
		
		return null;
	}
	
	public static <T> T removeLast(List<T> list) {
		
		if (!list.isEmpty()) {
			
			return list.remove(list.size()-1);
		}
		
		return null;
	}
	
	
	public static <T> List<T> asList(T[] array) {
		
		return asArrayList(array);
	}
	
	public static <T> ArrayList<T> asArrayList(T[] array) {
		
		ArrayList<T> list = new ArrayList<T>(array.length);
		addAll(list, array);
		
		return list;
	}
	
	public static <T> LinkedList<T> asLinkedList(T[] array) {
		
		LinkedList<T> list = new LinkedList<T>();
		addAll(list, array);
		
		return list;
	}
	
	
}
