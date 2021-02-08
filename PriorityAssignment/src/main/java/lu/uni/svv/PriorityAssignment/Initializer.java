package lu.uni.svv.PriorityAssignment;

import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskType;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class Initializer {
	
	/**
	 * log formatter for jMetal
	 */
	public static SimpleFormatter formatter = new SimpleFormatter(){
		private static final String format = "[%1$tF %1$tT] %2$s: %3$s %n";
		
		@Override
		public synchronized String format(LogRecord lr) {
			return String.format(format,
					new Date(lr.getMillis()),
					lr.getLevel().getLocalizedName(),
					lr.getMessage()
			);
		}
	};
	
	/**
	 *  initLogger: basic setting for jMetal
	 */
	public static void initLogger(){
		JMetalLogger.logger.setUseParentHandlers(false);
		ConsoleHandler handler = new ConsoleHandler();
		handler.setFormatter(formatter);
		JMetalLogger.logger.addHandler(handler);
	}
	
	
	/**
	 * Return simulation time based on TIME_QUANTA unit
	 * @param input
	 * @return
	 */
	public static int calculateSimulationTime(TaskDescriptor[] input){
		int simulationTime = 0;
		
		if (Settings.TIME_MAX == 0) {
			// calculate simulation time for all task period (no matter the task type)
			//			long[] array = new long[input.length];
			//			for(int x=0; x<input.length; x++){
			//				array[x] = input[x].Period;
			//			}
			//			simulationTime = (int) RTScheduler.lcm(array);
						
						// Too large simulation time :: calculate different way.
			//			if (simulationTime > 100000){  // by considering time unit
			
			// Those task time information appplied TIME_QUANTA to make int type
			// calculate LCM for periodic tasks only
			List<Long> periodArray = new ArrayList<>();
			for(int x=0; x<input.length; x++){
				if (input[x].Type != TaskType.Periodic) continue;
				periodArray.add((long)(input[x].Period));
			}
			long[] array = new long[periodArray.size()];
			for(int x=0; x<periodArray.size(); x++) {
				array[x] = periodArray.get(x);
			}
			int periodicLCM = (int) RTScheduler.lcm(array);
			
			// get maximum inter arrival time among non-periodic tasks
			int maxInterArrivalTime = 0;
			for(int x=0; x<input.length; x++){
				if (input[x].Type == TaskType.Periodic) continue;
				maxInterArrivalTime = Math.max(maxInterArrivalTime, input[x].MaxIA);
			}
			
			// select max value among two simulation time
			simulationTime = Math.max(periodicLCM, maxInterArrivalTime);
		}
		else{
			simulationTime = (int) (Settings.TIME_MAX * (1/Settings.TIME_QUANTA));
		}
		
		return simulationTime;
	}
	
	
	public static void updateSettings(TaskDescriptor[] input) throws Exception{
		
		// update specific options
		if (Settings.P1_MUTATION_PROB==0)
			Settings.P1_MUTATION_PROB = 1.0/input.length;
		
		if (Settings.P2_MUTATION_PROB==0)
			Settings.P2_MUTATION_PROB = 1.0/input.length;
		
		// Set SIMULATION_TIME
		int simulationTime = Initializer.calculateSimulationTime(input);
		if (simulationTime<0) {
			System.out.println("Cannot calculate simulation time");
			throw new Exception("Cannot calculate simulation time");
		}
		Settings.TIME_MAX = (simulationTime * Settings.TIME_QUANTA); // to change to ms unit
	}
	
	
	public static void convertTimeUnit(double _quanta){
		Settings.ADDITIONAL_SIMULATION_TIME = (int)(Settings.ADDITIONAL_SIMULATION_TIME * (1/_quanta));
		Settings.MAX_OVER_DEADLINE = (int)(Settings.MAX_OVER_DEADLINE * (1/_quanta));
		Settings.TIME_MAX = (int)(Settings.TIME_MAX * (1/_quanta));
	}
}
