package lu.uni.svv.PriorityAssignment;

import junit.framework.TestCase;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;

public class CoEvolveStarterTest extends TestCase {
	
	public void testGetPrioritiesFromInput() throws Exception {
		// Environment Settings
		Initializer.initLogger();
		String[] args = new String[0];
		Settings.update(args);
		
		// load input
		TaskDescriptor[] input = TaskDescriptor.loadFromCSV(Settings.INPUT_FILE, Settings.TIME_MAX, Settings.TIME_QUANTA);
		
		// update dynamic settings
		Initializer.updateSettings(input);
		
		// convert settings' time unit
		Initializer.convertTimeUnit(Settings.TIME_QUANTA);
		
		// create engineer's priority assignment
		Integer[] prioritiy = null;
		if (!Settings.RANDOM_PRIORITY) {
			prioritiy = CoEvolveStarter.getPrioritiesFromInput(input);
		}
		JMetalLogger.logger.info("Initialized program");

	}
}