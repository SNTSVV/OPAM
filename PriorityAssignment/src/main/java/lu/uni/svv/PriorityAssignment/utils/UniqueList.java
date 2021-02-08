package lu.uni.svv.PriorityAssignment.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class UniqueList {
	private HashMap<Integer, ArrayList<Integer[]>> map;
	private int size;
	
	public UniqueList(){
		 this.map = new HashMap<>();
		 size = 0;
	}
	
	public boolean add(Integer[] item){
		int key = hashCode(item);
		
		ArrayList<Integer[]> list = map.get(key);
		if (list != null){
			int size = list.size();
			for(int x=0; x<size; x++){
				if (compareItem(list.get(x), item)) {
					return false;
				}
			}
			list.add(item);
		}
		else{
			list = new ArrayList<>();
			list.add(item);
			map.put(key, list);
		}
		size++;
		return true;
	}
	
	public boolean contains(Integer[] item){
		int key = hashCode(item);
		
		ArrayList<Integer[]> list = map.get(key);
		if (list != null){
			for(int x=0; x<list.size(); x++){
				if (compareItem(list.get(x), item)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean compareItem(Integer[] a, Integer[] b){
		for(int x=0; x<a.length; x++){
			if (!a[x].equals(b[x])) return false;
		}
		return true;
	}
	
	public int hashCode(Integer[] item){
		int h = 0;
		int size = item.length;
		for (int i = 0; i < size; i++) {
			h =  109*h + item[i];
		}
		return h;
	}
	
	public int size(){ return size; }
	
	public Iterator<Integer> getIteratorKeys(){
		return map.keySet().iterator();
	}
	
	public ArrayList<Integer[]> getSlot(Integer key){
		return map.get(key);
	}
}
