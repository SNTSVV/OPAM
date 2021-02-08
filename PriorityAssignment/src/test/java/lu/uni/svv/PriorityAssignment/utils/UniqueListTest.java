package lu.uni.svv.PriorityAssignment.utils;

import junit.framework.TestCase;

import java.util.Iterator;
import java.util.List;

public class UniqueListTest extends TestCase {
	
	
	public void testClass() throws Exception{
		
		UniqueList list = new UniqueList();
		int added = 0;
		int exists = 0;
		
		long now = System.currentTimeMillis();
		for(int i=0; i<100000; i++){
			Integer[] priorities = generateList(33);
			if (list.add(priorities))
				added++;
			else
				exists++;
		}
		long fin = System.currentTimeMillis();
		System.out.println("size:"+list.size());
		System.out.println("added:"+added);
		System.out.println("exists:"+exists);
		System.out.println("workingTime:"+((fin-now)/1000.0));
		print(list);
	}
	
	
	public static String toString(Integer[] list){
		StringBuilder sb  = new StringBuilder(list.length*3);
		sb.append("[");
		for(int i=0; i<list.length; i++){
			sb.append(list[i]);
			sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}
	
	
	public static Integer[] generateList(int size){
		RandomGenerator rand = new RandomGenerator();
		
		Integer[] item = new Integer[size];
		for(int i=0; i<size; i++){
			item[i] = i;
		}
		
		for(int i=0; i<size; i++){
			int idx1 = rand.nextInt(0, size-1);
			int idx2 = rand.nextInt(0, size-1);
			int temp = item[idx1];
			item[idx1] = item[idx2];
			item[idx2] = temp;
		}
		
		return item;
	}
	
	
	
	public void print(UniqueList unique){
		Iterator<Integer> it = unique.getIteratorKeys();
		
		int cnt = 0;
		while (it.hasNext()) {
			int key = it.next();
			List<Integer[]> list = unique.getSlot(key);
			
			if (list.size()>1){
				cnt++;
				System.out.println(String.format("%15d (%d): ", key, list.size()));
//				for(int x=0; x<list.size(); x++){
//					System.out.print(toString(list.get(x))+", ");
//				}
//				System.out.println("");
			}
		}
		System.out.println("Collision: "+cnt);
	}
	
}