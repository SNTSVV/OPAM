package lu.uni.svv.PriorityAssignment.utils;

import junit.framework.TestCase;

public class MonitorTest extends TestCase {
	public void testClass() throws Exception{
		Monitor.init();
		Monitor.updateMemory();
		System.out.println(String.format("InitHeap: %.1fM (%.1fG)", Monitor.heapInit/Monitor.MB, Monitor.heapInit/Monitor.GB));
		System.out.println(String.format("usedHeap: %.1fM (%.1fG)", Monitor.heapUsed/Monitor.MB, Monitor.heapUsed/Monitor.GB));
		System.out.println(String.format("commitHeap: %.1fM (%.1fG)", Monitor.heapCommit/Monitor.MB, Monitor.heapCommit/Monitor.GB));
		System.out.println(String.format("MaxHeap: %.1fM (%.1fG)", Monitor.heapMax/Monitor.MB, Monitor.heapMax/Monitor.GB));
		System.out.println(String.format("MaxNonHeap: %.1fM (%.1fG)", Monitor.nonheapUsed/Monitor.MB, Monitor.nonheapUsed/Monitor.GB));
		for (int i=0; i<10; i++){
			System.out.println("working...");
			Monitor.start("test", true);
			Thread.sleep(500);
			Monitor.end("test", true);
		}
		Monitor.updateMemory();
		Monitor.finish();
		System.out.println(String.format("test: %.3f", Monitor.times.get("test")/1000.0));
		System.out.println(String.format("all: %.3f", Monitor.times.get("all")/1000.0));
		System.out.println(String.format("InitHeap: %.1fM (%.1fG)", Monitor.heapInit/Monitor.MB, Monitor.heapInit/Monitor.GB));
		System.out.println(String.format("usedHeap: %.1fM (%.1fG)", Monitor.heapUsed/Monitor.MB, Monitor.heapUsed/Monitor.GB));
		System.out.println(String.format("commitHeap: %.1fM (%.1fG)", Monitor.heapCommit/Monitor.MB, Monitor.heapCommit/Monitor.GB));
		System.out.println(String.format("MaxHeap: %.1fM (%.1fG)", Monitor.heapMax/Monitor.MB, Monitor.heapMax/Monitor.GB));
		System.out.println(String.format("MaxNonHeap: %.1fM (%.1fG)", Monitor.nonheapUsed/Monitor.MB, Monitor.nonheapUsed/Monitor.GB));
	}
	
}