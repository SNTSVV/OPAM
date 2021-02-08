package lu.uni.svv.PriorityAssignment.arrivals;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;

import lu.uni.svv.PriorityAssignment.scheduler.Schedule;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import org.uma.jmetal.problem.impl.AbstractGenericProblem;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.scheduler.ScheduleCalculator;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import lu.uni.svv.PriorityAssignment.utils.ListUtil;
import org.uma.jmetal.util.JMetalLogger;


/**
 * Class Responsibility
 *  - Definition of the problem to solve
 *  - Basic environments (This is included the definition of the problem)
 *  - An interface to create a solution
 *  - A method to evaluate a solution
 */
@SuppressWarnings("serial")
public class ArrivalProblem extends AbstractGenericProblem<ArrivalsSolution> {
	
	// Internal Values
	public int               SIMULATION_TIME;	// Total simulation time // to treat all time values as integer
	public TaskDescriptor[]  Tasks;	            // Task information
	public List<Integer[]>   Priorities;	    // Priority list
	private Class            schedulerClass;    // scheduler will be used to evaluate chromosome
	
	/**
	 * Constructor
	 * Load input data and setting environment of experiment	
	 * @param _tasks
	 * @param _schedulerName
	 */
	public ArrivalProblem(TaskDescriptor[] _tasks, List<Integer[]> _priorities, int _simulationTime, String _schedulerName) throws NumberFormatException, IOException{
		
		// Set environment of this problem.
		this.SIMULATION_TIME = _simulationTime;
		this.Tasks = _tasks;
		this.Priorities = _priorities;
		this.setName("");
		this.setNumberOfVariables(this.Tasks.length);
		this.setNumberOfObjectives(1);	//Single Objective Problem
		
		// Create Scheduler instance
		try {
			String packageName = this.getClass().getPackage().getName();
			packageName = packageName.substring(0, packageName.lastIndexOf("."));
			this.schedulerClass = Class.forName(packageName + ".scheduler." + _schedulerName);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * set a list of priorities to evaluate a solution
	 * @param _priorities
	 */
	public void setPriorities(List<Integer[]> _priorities) {
		this.Priorities = _priorities;
	}
	
	/**
	 * create solution interface
	 * Delegate this responsibility to Solution Class.
	 */
	@Override
	public ArrivalsSolution createSolution() {
		return new ArrivalsSolution(this);
	}
	
	/**
	 * evaluate solution interface
	 */
	@Override
	public void evaluate(ArrivalsSolution _solution) {
		//Prepare scheduler
		RTScheduler scheduler = null;
		Constructor constructor = schedulerClass.getConstructors()[0];
		Object[] parameters = {this.Tasks, SIMULATION_TIME};
		try {
			
			Arrivals[] arrivals = _solution.toArray();
			
			double[] fitnessList = new double[Priorities.size()];
			int[] deadlineList = new int[Priorities.size()];
			int[][] maximums = new int[Priorities.size()][];
			String[] deadlineStrings = new String[Priorities.size()];
			
			for(int x=0; x<this.Priorities.size(); x++){
				
				// run Scheduler
				scheduler = (RTScheduler)constructor.newInstance(parameters);
				scheduler.run(arrivals, Priorities.get(x));
				
				// make result
				ScheduleCalculator calculator = new ScheduleCalculator(scheduler.getResult(), Settings.TARGET_TASKS);
				fitnessList[x] = calculator.distanceMargin(false);
				deadlineList[x] = calculator.checkDeadlineMiss();
				maximums[x] = calculator.getMaximumExecutions();
				
//				if (Priorities.size()>1) {
//					JMetalLogger.logger.info(String.format("\t\tScheduling test has done [%d/%d]", x + 1, Priorities.size()));
//				}
				if (deadlineList[x]>0){
					String header = String.format("%d,",x);
					deadlineStrings[x] = calculator.getDeadlineMiss(header);
				}
				
				// for debug
				if (Settings.PRINT_DETAILS) {
					storeForDebug(_solution, scheduler.getResult(), _solution.ID, x);
				}
			}
			
			// save fitness information
			int idx = selectedFitnessValue(fitnessList);
			_solution.setObjective(0, mergeFitnessValues(fitnessList));
			_solution.setAttribute("DeadlineMiss", mergeDeadlineMiss(deadlineList));
			_solution.setAttribute("FitnessIndex", idx);
			_solution.setAttribute("Maximums", maximums[idx]);
			_solution.setAttribute("DMString", deadlineStrings[idx]);
			
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	

	/**
	 * calculate fitness value among multiple fitness values
	 * @param list
	 * @return
	 */
	private double mergeFitnessValues(double[] list){
		double fitness = 0.0;
		if (Settings.P1_GROUP_FITNESS.compareTo("maximum")==0)
			fitness = list[ListUtil.maximumIdx(list)];
		else if (Settings.P1_GROUP_FITNESS.compareTo("minimum")==0)
			fitness = list[ListUtil.minimumIdx(list)];
		else // average
			fitness = ListUtil.average(list);
		return fitness;
	}

	/**
	 * check how many deadline miss are occurred among multiple fitness values
	 * @param deadlineList
	 * @return
	 */
	public int mergeDeadlineMiss(int[] deadlineList){
		int sumDM = 0;
		for (int x=0; x<deadlineList.length; x++){
			sumDM += deadlineList[x];
		}
		return sumDM;
	}
	
	/**
	 * select index calculated fitness value among multiple fitness values
	 * @param list
	 * @return
	 */
	private int selectedFitnessValue(double[] list){
		int idx = 0;
		if (Settings.P1_GROUP_FITNESS.compareTo("maximum")==0)
			idx = ListUtil.maximumIdx(list);
		else if (Settings.P1_GROUP_FITNESS.compareTo("minimum")==0)
			idx = ListUtil.minimumIdx(list);
		else // average
			idx = ListUtil.averageIdx(list);
		return idx;
	}
	
	/**
	 * print function for debugging
	 * @param solution
	 * @param schedules
	 * @param sID
	 * @param pID
	 */
	public void storeForDebug(ArrivalsSolution solution, Schedule[][] schedules, long sID, int pID){
		String filename = String.format("_arrivals1/sol%d_prio%d.json", sID, pID);
		solution.store(filename);
		
		filename = String.format("_schedules1/sol%d_prio%d.json", sID, pID);
		Schedule.printSchedules(filename, schedules);
		
		// convert priority
		Integer[] prios = Priorities.get(pID);
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int x=0; x < prios.length; x++) {
			sb.append(prios[x]);
			if (x!=(prios.length-1))
				sb.append(", ");
		}
		sb.append(" ]");
		
		// store priority
		filename = String.format("_priorities1/sol%d_prio%d.json", sID, pID);
		GAWriter writer = new GAWriter(filename, Level.FINE, null);
		writer.info(sb.toString());
		writer.close();
	}
}
