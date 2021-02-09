package lu.uni.svv.PriorityAssignment.scheduler;

import junit.framework.TestCase;
import lu.uni.svv.PriorityAssignment.CoEvolveStarter;
import lu.uni.svv.PriorityAssignment.Initializer;
import lu.uni.svv.PriorityAssignment.task.Task;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskState;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;

import java.util.PriorityQueue;
import java.util.Random;

public class RTSchedulerTest extends TestCase {
	
	/**
	 * Test with Periodic tasks
	 * No deadline misses
	 */
	public void testSchedulePriority() throws Exception
	{
		Random rand = new Random();

		// Environment Settings
		Initializer.initLogger();
		TaskDescriptor[] tasks = TaskDescriptor.loadFromCSV("../res/industrial/CCS.csv", 0, 1);
		Integer[] priorities = CoEvolveStarter.getPrioritiesFromInput(tasks);
		
		// prepare ready queue
		RTScheduler scheduler = new RTScheduler(tasks, 250);
		PriorityQueue<Task> readyQueue = new  PriorityQueue<Task>(60000, scheduler.queueComparator);
		int[] executionIndex = new int[tasks.length];
		
		int currentTime=0;
		for (int x=0; x<10; x++){
			int taskIdx = rand.nextInt(tasks.length);
			
			TaskDescriptor taskDesc = tasks[taskIdx];
			int priority = priorities[taskIdx];
			Task t = new Task(taskDesc.ID, executionIndex[taskIdx], taskDesc.WCET, currentTime, currentTime + taskDesc.Deadline, priority, taskDesc.Severity);
			t.updateTaskState(TaskState.Ready, currentTime);
			
			readyQueue.add(t);
			executionIndex[taskIdx]++;
		}
		
		while (!readyQueue.isEmpty())
		{
			Task t = readyQueue.poll();
			System.out.println(String.format("ID: %d, priority: %d, exID: %d", t.ID, t.Priority, t.ExecutionID));
		}
		
	}
	
	
}