package lu.uni.svv.PriorityAssignment.priority;

import junit.framework.TestCase;
import java.util.ArrayList;
import java.util.List;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalProblem;
import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;



public class PrioritySolutionEvaluatorTest extends TestCase {
	
	TaskDescriptor[] input;
	int simulationTime = 0;
	
	public void init() throws Exception{
		// Environment Settings
		Settings.update(new String[] {});
		Settings.INPUT_FILE = "res/empirical/GAP.csv";
		Settings.WORKNAME = "TEST/GAP";
		Settings.TIME_MAX = 0;
		Settings.TIME_QUANTA = 1;
		Settings.CYCLE_NUM = 3;
		Settings.P1_ITERATION = 10;
		Settings.P2_GROUP_FITNESS = "average";
		
		
		// load input
		input = TaskDescriptor.loadFromCSV(Settings.INPUT_FILE, Settings.TIME_MAX, Settings.TIME_QUANTA);
		
		// Set SIMULATION_TIME
		simulationTime = 0;
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
		
		// initialze static objects
		ArrivalsSolution.initUUID();
		PrioritySolution.initUUID();
		
		
		JMetalLogger.logger.info("Initialized program");
		
	}
	
	public void testEvaluateSolution() throws Exception{
		init();
		
		PriorityProblem problemP = new PriorityProblem(input, simulationTime);
		List<PrioritySolution> P = createInitialPriorities(Settings.P2_POPULATION, null, problemP);
		
		List<Integer[]> priorities = PrioritySolution.toArrays(P);
		ArrivalProblem problemA = new ArrivalProblem(input, priorities, simulationTime, Settings.SCHEDULER);
		List<ArrivalsSolution> A = createInitialArrivals(problemA, Settings.P1_POPULATION);
		List<Arrivals[]> arrivals = ArrivalsSolution.toArrays(A);
		PrioritySolutionEvaluator evaluator = new PrioritySolutionEvaluator(input, arrivals, arrivals, simulationTime, Settings.SCHEDULER);
		
		evaluator.evaluateSolution(P.get(0), arrivals, false, false,0);
		
	}

	public List<PrioritySolution> createInitialPriorities(int maxPopulation, Integer[] priorityEngineer, PriorityProblem problem) {
		
		List<PrioritySolution> population = new ArrayList<>(maxPopulation);
		PrioritySolution individual;
		for (int i = 0; i < maxPopulation; i++) {
			if (i==0 && priorityEngineer!=null){
				individual = new PrioritySolution(problem, priorityEngineer);
			}
			else {
				individual = new PrioritySolution(problem);
			}
			population.add(individual);
		}
		return population;
	}
	
	public List<ArrivalsSolution> createInitialArrivals(ArrivalProblem _problem, int n){
		List<ArrivalsSolution> A = new ArrayList<>();
		for (int i=0; i<n; i++){
			A.add(new ArrivalsSolution(_problem));
		}
		return A;
	}
}