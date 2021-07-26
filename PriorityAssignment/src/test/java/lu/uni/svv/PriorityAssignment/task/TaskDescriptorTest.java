package lu.uni.svv.PriorityAssignment.task;

import junit.framework.TestCase;
import lu.uni.svv.PriorityAssignment.priority.PriorityProblem;
import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;

public class TaskDescriptorTest extends TestCase {
	/**
	 * Test with Periodic tasks
	 * No deadline misses
	 */
	public void testMinMaxFitness() throws Exception
	{
		System.out.println("Working Directory = " + System.getProperty("user.dir"));
		// Environment Settings
		Settings.update(new String[]{});
		
		// load input
		TaskDescriptor[] input = TaskDescriptor.loadFromCSV(Settings.INPUT_FILE, Settings.TIME_MAX, Settings.TIME_QUANTA);
		
		// Set SIMULATION_TIME
		int simulationTime = 0;
		if (Settings.TIME_MAX == 0) {
			long[] periodArray = new long[input.length];
			for(int x=0; x<input.length; x++){
				periodArray[x] = input[x].Period;
			}
			simulationTime = (int) RTScheduler.lcm(periodArray);
			if (simulationTime<0) {
				System.out.println("Cannot calculate simulation time");
				return;
			}
		}
		else{
			simulationTime = (int) (Settings.TIME_MAX * (1/Settings.TIME_QUANTA));
			if (Settings.EXTEND_SIMULATION_TIME) {
				long max_deadline = TaskDescriptor.findMaximumDeadline(input);
				simulationTime += max_deadline;
			}
		}
		Settings.TIME_MAX = simulationTime;
		
		// update specifit options
		if (Settings.P2_MUTATION_PROB==0)
			Settings.P2_MUTATION_PROB = 1.0/input.length;
		
		PriorityProblem p = new PriorityProblem(input, simulationTime, Settings.SCHEDULER);
		Double[] range1 = p.getFitnessRange(0);
		Double[] range2 = p.getFitnessRange(1);
		System.out.println(String.format("[1] min fitness: %e, max fitness: %e", range1[0], range1[1]));
		System.out.println(String.format("[2] min fitness: %f, max fitness: %f", range2[0], range2[1]));
	}
	
}