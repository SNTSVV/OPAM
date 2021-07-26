package lu.uni.svv.PriorityAssignment.priority;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;

import lu.uni.svv.PriorityAssignment.scheduler.Schedule;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.scheduler.ScheduleCalculator;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import lu.uni.svv.PriorityAssignment.utils.ListUtil;


public class PrioritySolutionEvaluator implements SolutionListEvaluator<PrioritySolution> {
	
	// Internal Values
	private List<Arrivals[]> ArrivalsList;	    // Arrivals
	public List<Arrivals[]>  TestArrivalsList;	// Test Arrivals
	private Class            schedulerClass;    // scheduler will be used to evaluate chromosome
	
	
	public PrioritySolutionEvaluator(List<Arrivals[]> _arrivalsList, List<Arrivals[]> _testArrivalsList) throws NumberFormatException, IOException {
		
		// Set environment of this problem.
		this.ArrivalsList = _arrivalsList;
		this.TestArrivalsList = _testArrivalsList;


	}

	
	@Override
	public List<PrioritySolution> evaluate(List<PrioritySolution> population, Problem<PrioritySolution> problem) {
		// get scheduler object
		// RTScheduler.DETAIL = true;
		// RTScheduler.PROOF = true;
		try {
			int x=0;
			for(PrioritySolution solution:population){
				evaluateSolution( (PriorityProblem)problem, solution, ArrivalsList, false, false, 0);
				x++;
//				JMetalLogger.logger.info(String.format("\tEvaluated solution [%d/%d] with %s arrivals", x, population.size(), ArrivalsList.size()));
			}
			
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return population;
	}
	
	
	public List<PrioritySolution> evaluateForExternal(List<PrioritySolution> population, Problem<PrioritySolution> problem) {
		try {
			int x=0;
			for(PrioritySolution solution:population){
				evaluateSolution( (PriorityProblem)problem, solution, TestArrivalsList, true, x==0,0);
				x++;
//				JMetalLogger.logger.info(String.format("\tEvaluated solution for external [%d/%d] with %s arrivals", x, population.size(), TestArrivalsList.size()));
			}
			
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return population;
	}
	
	
	public void evaluateSolution(PriorityProblem _problem, PrioritySolution solution, List<Arrivals[]> arrivalsList, boolean isSimple, boolean ext, int showingProgress)
			throws IllegalAccessException, InvocationTargetException, InstantiationException {
		
		// convert solution to priorities array
		Integer[] priorities = solution.toArray();

		// prepare scheduler
		RTScheduler scheduler = _problem.getScheduler();;
		
		// calculate fitness values for each arrivals
		double[] fitnessList = new double[arrivalsList.size()];
		int[] deadlineList = new int[arrivalsList.size()];
		int[][] maximums = new int[arrivalsList.size()][];
		String[] deadlineStrings = null;
		if (!isSimple) {
			deadlineStrings = new String[arrivalsList.size()];
		}
		List<Schedule[][]> schedulesList = new ArrayList<>();
		
		for (int x=0; x< arrivalsList.size(); x++){
			// run scheduler
			scheduler.run(arrivalsList.get(x), priorities);
			
			// make result
			Schedule[][] schedules = scheduler.getResult();
			ScheduleCalculator calculator = new ScheduleCalculator(schedules, Settings.TARGET_TASKS);
			fitnessList[x] = calculator.distanceMargin(true);
			deadlineList[x] = calculator.checkDeadlineMiss();
			maximums[x] = calculator.getMaximumExecutions();
			if (Settings.PRINT_FINAL_DETAIL)
				schedulesList.add(schedules);
			// for Debug
			//calculator.checkBins();
			
			if (arrivalsList.size()>1 && showingProgress!=0){
				if((x+1)%showingProgress==0) {
					JMetalLogger.logger.info(String.format("\t\tScheduling test has done [%d/%d]", x + 1, arrivalsList.size()));
				}
			}
			if (!isSimple && deadlineList[x]>0){
				String header = String.format("%d,",x);
				deadlineStrings[x] = calculator.getDeadlineMiss(header);
			}
			
			// for debug
			if (Settings.PRINT_DETAILS && ((ext==true && x==0) || solution.ID == 1)) {
				storeForDebug(arrivalsList.get(x), schedules, priorities, solution.ID, x);
			}
		}
//		String filename = String.format("_solutions2/phase2_priorities_sol%d.json", solution.ID);
//		solution.store(filename);
		
		// save fitness information
		int idx = selectedFitnessValue(fitnessList);
		solution.setObjective(0, -mergeFitnessValues(fitnessList));
		solution.setObjective(1, -ConstrantsEvaluator.calculate(solution, _problem.Tasks));
		solution.setAttribute("DeadlineMiss", mergeDeadlineMiss(deadlineList));
		solution.setAttribute("FitnessIndex", idx);
		solution.setAttribute("Maximums", maximums[idx]);
		solution.setAttribute("FitnessList", fitnessList);
		solution.setAttribute("DeadlineList", deadlineList);
		if (Settings.PRINT_FINAL_DETAIL)
			solution.setAttribute("schedulesList", schedulesList);

		if (!isSimple) {
			solution.setAttribute("DMString", deadlineStrings[idx]);
		}
	}
	
	
	@Override
	public void shutdown() {
	
	}
	
	/**
	 * calculate fitness value among multiple fitness values
	 * @param list
	 * @return
	 */
	public double mergeFitnessValues(double[] list){
		double fitness = 0.0;
		if (Settings.P2_GROUP_FITNESS.compareTo("maximum")==0)
			fitness = list[ListUtil.maximumIdx(list)];
		else if (Settings.P2_GROUP_FITNESS.compareTo("minimum")==0)
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
	public int selectedFitnessValue(double[] list){
		int idx = 0;
		if (Settings.P2_GROUP_FITNESS.compareTo("maximum")==0)
			idx = ListUtil.maximumIdx(list);
		else if (Settings.P2_GROUP_FITNESS.compareTo("minimum")==0)
			idx = ListUtil.minimumIdx(list);
		else // average
			idx = ListUtil.averageIdx(list);
		return idx;
	}
	
	/**
	 * print out arrivals and priorities and schedule result
	 * @param
	 * @return
	 */
	public void storeForDebug(Arrivals[] arrivals, Schedule[][] schedules, Integer[] priorities, long sID, int aID){
		String filename = String.format("_arrivals2/sol%d_arr%d.json", sID, aID);
		printArrivals(arrivals, filename);
		
		filename = String.format("_schedules2/sol%d_arr%d.json", sID, aID);
		Schedule.printSchedules(filename, schedules);
		
		// convert priority
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int x=0; x < priorities.length; x++) {
			sb.append(priorities[x]);
			if (x!=(priorities.length-1))
				sb.append(", ");
		}
		sb.append(" ]");
		
		// store priority
		filename = String.format("_priorities2/sol%d_arr%d.json", sID, aID);
		GAWriter writer = new GAWriter(filename, Level.FINE, null);
		writer.info(sb.toString());
		writer.close();
	}
	
	public void printArrivals(Arrivals[] arrivals, String filename){
		GAWriter writer = new GAWriter(filename, Level.FINE, null);
		StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);
		
		sb.append("[\n");
		for (int x=0; x < arrivals.length; x++) {
			sb.append("\t");
			
			fmt.format("[");
			Arrivals item = arrivals[x];
			for(int i=0; i< item.size(); i++) {
				fmt.format("%d", item.get(i));
				if ( item.size() > (i+1) )
					sb.append(",");
			}
			fmt.format("]");
			if (x!=(arrivals.length-1))
				sb.append(",");
			sb.append("\n");
		}
		sb.append("]");
		
		writer.info(sb.toString());
		writer.close();
	}
	
}
