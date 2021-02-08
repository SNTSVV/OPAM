package lu.uni.svv.PriorityAssignment;

import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import lu.uni.svv.PriorityAssignment.arrivals.SampleGenerator;
import lu.uni.svv.PriorityAssignment.priority.PriorityNSGAII;
import lu.uni.svv.PriorityAssignment.priority.PrioritySolution;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.FileManager;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;

import java.util.*;
import java.util.logging.Level;

public class CoEvolveStarter {
	/**
	 * Main code to start co-evolution
	 *      - initialize parameters
	 *      - initialize global objects
	 * @param args
	 * @throws Exception
	 */
	public static void main( String[] args ) throws Exception {
		// Environment Settings
		Initializer.initLogger();
		Settings.update(args);
		
		// load input
		TaskDescriptor[] input = TaskDescriptor.loadFromCSV(Settings.INPUT_FILE, Settings.TIME_MAX, Settings.TIME_QUANTA);
		
		// update dynamic settings
		Initializer.updateSettings(input);
		
		// convert settings' time unit
		Initializer.convertTimeUnit(Settings.TIME_QUANTA);
		
		// make simulation time
		int simulationTime = (int) Settings.TIME_MAX;
		if (Settings.ADDITIONAL_SIMULATION_TIME != 0) {
			simulationTime += Settings.ADDITIONAL_SIMULATION_TIME;
		}
		
		// create engineer's priority assignment
		Integer[] prioritiy = null;
		if (!Settings.RANDOM_PRIORITY) {
			prioritiy = getPrioritiesFromInput(input);
		}
		JMetalLogger.logger.info("Initialized program");
		
		// test mode check
		boolean testGenerationMode = stringIn(Settings.TEST_GENERATION, new String[]{"Random","Adaptive","AdaptiveFull","Heuristic", "Limit"}); //"Initial"
		if (testGenerationMode){
			GAWriter.init(null, true);
			printInput(TaskDescriptor.toString(input, Settings.TIME_QUANTA), null);
		}
		
		// work for test data
		List<Arrivals[]> testArrivals = null;
		testArrivals = generateTestData(input, simulationTime);
		if (testArrivals == null) {
			// Quit all process
				return;
		}
		
		if (Settings.RUN_CNT==0) {
			run(input, simulationTime, prioritiy, testArrivals);
		}
		else {
			for (int runID = 0; runID < Settings.RUN_CNT; runID++) {
				Settings.RUN_NUM = Settings.RUN_START + runID;
				run(input, simulationTime, prioritiy, testArrivals);
			}
		}
	}
	
	public static boolean stringIn(String str, String[] items){
		for (String item : items) {
			if (str.compareTo(item)==0) return true;
		}
		return false;
	}
	
	public static void run(TaskDescriptor[] input, int simulationTime, Integer[] prioritiy, List<Arrivals[]> testArrivals) throws Exception {
		GAWriter.init(null, true);
		printInput(TaskDescriptor.toString(input, Settings.TIME_QUANTA), null);
		
		// initialze static objects
		ArrivalsSolution.initUUID();
		PrioritySolution.initUUID();
		PriorityNSGAII.init();
		
		// Run Co-Evolution (run function will generate result files)
		CoEvolve system = new CoEvolve();
		system.run(input, Settings.CYCLE_NUM, simulationTime, prioritiy, testArrivals);
	}
	
	public static List<Arrivals[]> generateTestData(TaskDescriptor[] input, int simulationTime) throws Exception{
		List<Arrivals[]> testArrivals=null;
		SampleGenerator generagor = new SampleGenerator(input, simulationTime);
		if (Settings.TEST_GENERATION.compareTo("Random")==0) {
			generagor.generateRandom(Settings.NUM_TEST, false, false, false);
			return null;
		}
		else if (Settings.TEST_GENERATION.compareTo("Adaptive")==0) {
			generagor.generateAdaptive(Settings.NUM_TEST, 100, true);
			return null;
		}
		else if (Settings.TEST_GENERATION.compareTo("AdaptiveFull")==0) {
			generagor.generateAdaptive(Settings.NUM_TEST, 100, false);
			return null;
		}
		else if (Settings.TEST_GENERATION.length()==0){
			// normal execution
			testArrivals = FileManager.LoadTestArrivals(input, simulationTime);
		}
		else{
			throw new Exception("There is no options for " + Settings.TEST_GENERATION);
		}
		return testArrivals;
	}

	/**
	 * printing input and setting to the result folder to distinguish real
	 * @param inputs
	 * @param changed
	 */
	private static void printInput(String inputs, String changed){
		String settingStr = Settings.getString();
		System.out.print(settingStr);
		
		// print current settings
		GAWriter writer = new GAWriter("settings.txt", Level.FINE, null);
		writer.info(settingStr);
		writer.close();
		
		// print original input
		writer = new GAWriter("input.csv", Level.INFO, null);
		writer.info(inputs);
		writer.close();
		
		// print changed inputs
		if (changed != null) {
			writer = new GAWriter("changed.txt", Level.INFO, null);
			writer.print(changed);
			writer.close();
		}
	}
	
	/**
	 * generate initial priorities from input
	 * the order of priorities following task IDs
	 * @param input
	 * @return
	 */
	private static Integer[] getPrioritiesFromInput(TaskDescriptor[] input) {
		Integer[] priorities = new Integer[input.length];
		HashMap<Integer, Integer> map = new HashMap<>();
		
		
		// assign priority level
		for(int priority=0; priority<input.length;priority++) {
			int minTaskIdx = 0;
			int minPriority = Integer.MAX_VALUE;
			for (int x = 0; x < input.length; x++) {
				if (map.containsKey(x)) continue;
				
				if (minPriority < input[x].Priority) {
					minTaskIdx = x;
					minPriority = input[x].Priority;
				}
			}
			
			priorities[minTaskIdx] = priority;
			map.put(minTaskIdx, 1);
		}
		
		return priorities;
	}
}
