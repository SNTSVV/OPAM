package lu.uni.svv.PriorityAssignment.utils;

public class Utils {
	
	public static void printMemory(String _msg){
		Runtime runtime = Runtime.getRuntime();
		double max = runtime.maxMemory()/ 1000000000.0;
		double total = runtime.totalMemory()/1000000000.0;
		double free = runtime.freeMemory()/1000000000.0;
		
		System.out.println(String.format("\tMemory(%s): max(%.2fG), total(%.2fG), free(%.2fG), used(%.2fG))", _msg, max, total, free, total-free));
	}
}
