package lu.uni.svv.PriorityAssignment.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;

public class Monitor {
	public static HashMap<String, Long> times;
	private static String workname = "";
	private static long ts = 0;
	
	public static long heapInit = 0;
	public static long heapUsed = 0;
	public static long heapCommit = 0;
	public static long heapMax = 0;
	public static long nonheapUsed = 0;
	
	public static double kb = 1024.0;
	public static double MB = 1024.0*1024.0;
	public static double GB = 1024.0*1024.0*1024.0;
	
	/**
	 * Initialize monitor class
	 *    Use this function to measure total execution time and memory usages
	 *    This function will save the time this function called with a key "all"
	 */
	public static void init(){
		updateMemory();
		Monitor.times = new HashMap<String, Long>();
		Monitor.times.put("all", System.currentTimeMillis());
		Monitor.workname = "";
	}
	
	/**
	 * Finish the monitor
	 *     Use this function to measure total execution time and memory usages
	 *     This function save the time difference between now and the previous value with a key "all"
	 */
	public static void finish() {
		long ts = System.currentTimeMillis();
		long prev = Monitor.times.get("all");
		Monitor.times.put("all", ts - prev);
		updateMemory();
	}
	
	/**
	 * This function starts to measure the time of a sub-work
	 *   if you want to record time for different sub-work after called start(),
	 *   you should call end() first to prevent a collision with the previous sub-work
	 * @param name
	 * @param cumulate this parameter lets you measure cumulative time for a particular-work
	 */
	public static void start(String name, boolean cumulate){
		if (!cumulate) {
			Monitor.times.put(name, System.currentTimeMillis());
		}
		else{
			Monitor.ts =  System.currentTimeMillis();
			Monitor.workname =  name;
		}
	}
	
	/**
	 * This function saves time elapsed of a sub-work
	 * @param name a name of sub-work
	 * @param cumulate if you set cumulate when you use start(), you should set this parameter
	 */
	public static void end(String name, boolean cumulate){
		long ts = System.currentTimeMillis();
		if (!cumulate) {
			long prev = Monitor.times.get(name);
			Monitor.times.put(name, ts - prev);
		}
		else{
			long prev = Monitor.times.containsKey(workname)?Monitor.times.get(workname):0;
			Monitor.times.put(workname, prev + (ts - Monitor.ts));
		}
	}
	
	/**
	 * Get a time elapsed
	 */
	public static long getTime(){
		return Monitor.times.get("all");
	}
	
	/**
	 * Get a time elapsed for a particular sub-work
	 */
	public static long getTime(String name){
		if (!Monitor.times.containsKey(name)) return 0;
		return Monitor.times.get(name);
	}
	
	
	/**
	 * Update memory usages
	 */
	public static void updateMemory(){
		MemoryMXBean membean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = membean.getHeapMemoryUsage();
		MemoryUsage nonheap = membean.getNonHeapMemoryUsage();
		
		Monitor.heapInit = (Monitor.heapInit < heap.getInit() ) ? heap.getInit() : Monitor.heapInit;
		Monitor.heapUsed = (Monitor.heapUsed < heap.getUsed() ) ? heap.getUsed() : Monitor.heapUsed;
		Monitor.heapCommit = (Monitor.heapCommit < heap.getCommitted() ) ? heap.getCommitted() : Monitor.heapCommit;
		Monitor.heapMax = (Monitor.heapMax < heap.getMax() ) ? heap.getMax() : Monitor.heapMax;
		Monitor.nonheapUsed = (Monitor.nonheapUsed < nonheap.getUsed() ) ? nonheap.getUsed() : Monitor.nonheapUsed;
	}
	
	/**
	 * get Memory usuage examples
	 */
	public static long[] getMemory() {
		MemoryMXBean membean = (MemoryMXBean) ManagementFactory.getMemoryMXBean();
		MemoryUsage heap = membean.getHeapMemoryUsage();
		MemoryUsage nonheap = membean.getNonHeapMemoryUsage();
		long heapInit = heap.getInit();
		long heapUsed = heap.getUsed();
		long heapCommit = heap.getCommitted();
		long heapMax = heap.getMax();
		long nonheapUsed = nonheap.getUsed();
		
		long[] list = new long[5];
		list[0] = heapInit;
		list[0] = heapUsed;
		list[1] = nonheapUsed;
		list[2] = heapCommit;
		list[3] = heapMax;
		return list;
	}
	
}
