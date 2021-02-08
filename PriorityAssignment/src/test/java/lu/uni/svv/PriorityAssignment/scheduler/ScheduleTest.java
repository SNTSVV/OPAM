package lu.uni.svv.PriorityAssignment.scheduler;

import junit.framework.TestCase;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalProblem;
import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class ScheduleTest extends TestCase {
	/**
	 * Test with Periodic tasks
	 * No deadline misses
	 */
	public void testScheduleLoad() throws Exception
	{
		// Environment Settings
		GAWriter.setBasePath("../results/TEST_T");
		
		
		Schedule[][] schedules = Schedule.loadSchedules("../results/TEST_T/_schedules/phase2_sol1_arr0.json");
		Schedule.printSchedules("_schedules/phase2_sol1_arr0_copy.json", schedules);
	}
	
	public void testScheduleVerify() throws Exception
	{
		// Environment Settings
		String path = "../results/TestScheduler7";
		GAWriter.setBasePath(path);
		
		// variables
		int solID = 11;
		int priorityID = 3;
		
		// load task descriptions
		TaskDescriptor[] Tasks = TaskDescriptor.loadFromCSV(path+"/input.csv", 3600000,1);
		
		// load schedules
		Schedule[][] schedules = Schedule.loadSchedules(String.format("%s/_schedules2/phase1_sol%d_prio%d.json", path, solID, priorityID));
		
		// load task priorities
		Integer[] priorities = loadPriority(String.format("%s/_priorities2/phase1_sol%d_prio%d.json", path, solID, priorityID));
		
		new ScheduleVerify(schedules, Tasks, priorities).verify();
	}

	public void testScheduler() throws Exception
	{
		// Environment Settings
		String path = "../results/TestSchedulerTest";
//		GAWriter.setBasePath(path);

		// variables
		int solID = 18;
		int priorityID = 0;

		// load task descriptions
		TaskDescriptor[] Tasks = TaskDescriptor.loadFromCSV(path+"/input.csv", 3600000,1);
		int simulationTime = getSimulationTime(Tasks);
		
		// load task priorities
		String filename = String.format("%s/_arrivals2/phase1_sol%d_prio%d.json", path, solID, priorityID);
		ArrivalProblem problemA = new ArrivalProblem(Tasks, null, simulationTime, "SQMScheduler");
		ArrivalsSolution solution = ArrivalsSolution.loadFromJSON(problemA, filename);
		Arrivals[] arrivals = solution.toArray();
		
		// load task priorities
		Integer[] priorities = loadPriority(String.format("%s/_priorities2/phase1_sol%d_prio%d.json", path, solID, priorityID));
		
		// scheduling
		SQMScheduler scheduler = new SQMScheduler(Tasks, simulationTime);
		scheduler.run(arrivals, priorities);
		
		// load arrivals
		Schedule[][] schedules = scheduler.getResult();
		filename = String.format("%s/_schedules3/phase1_sol%d_prio%d.json", path, solID, priorityID);
		Schedule.printSchedules(filename, schedules);

		new ScheduleVerify(schedules, Tasks, priorities, arrivals).verify();
	}
	
	/**
	 *
	 * @param _filename
	 * @return
	 * @throws Exception
	 */
	public Integer[] loadPriority(String _filename) throws Exception {
		Integer[] priorities = null;
		// load task priorities
		FileReader reader = new FileReader(_filename);
		JSONParser parser = new JSONParser();
		JSONArray json = (JSONArray) parser.parse(reader);
		priorities = new Integer[json.size()];
		for (int t = 0; t < json.size(); t++) {
			priorities[t] = ((Long) json.get(t)).intValue();
		}
		return priorities;
	}
	
	/**
	 *
	 * @param input
	 * @return
	 */
	public int getSimulationTime(TaskDescriptor[] input){
		long[] periodArray = new long[input.length];
		for(int x=0; x<input.length; x++){
			periodArray[x] = input[x].Period;
		}
		int time = (int) RTScheduler.lcm(periodArray);
		return time;
	}

}