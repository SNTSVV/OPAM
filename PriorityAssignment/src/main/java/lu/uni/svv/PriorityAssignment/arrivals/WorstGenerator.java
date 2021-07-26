package lu.uni.svv.PriorityAssignment.arrivals;

import lu.uni.svv.PriorityAssignment.priority.PriorityProblem;
import lu.uni.svv.PriorityAssignment.priority.PrioritySolution;
import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskType;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Monitor;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.ObjectiveComparator;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;


public class WorstGenerator {
	public static String TESTFILENAME = "test.list";

	public TaskDescriptor[] Tasks;
	public ArrivalProblem problem;
	private GAWriter writer = null;
	public List<Integer[]> priorities;

	public WorstGenerator(TaskDescriptor[] input, int simulationTime, Integer[] _priorityEngineer) throws Exception
	{
		this(input, simulationTime, _priorityEngineer,null);
	}

	public WorstGenerator(TaskDescriptor[] input, int simulationTime, Integer[] _priorityEngineer, String path) throws Exception
	{
		// generate population
		PriorityProblem problemP = new PriorityProblem(input, simulationTime, Settings.SCHEDULER);
		List<PrioritySolution> P = createInitialPriorities(Settings.P2_POPULATION, _priorityEngineer, problemP);
		List<Integer[]> priorities = PrioritySolution.toArrays(P);

		Tasks = input;
		problem = new ArrivalProblem(input, priorities, simulationTime, Settings.SCHEDULER);
		if (path!=null) WorstGenerator.TESTFILENAME = path;
	}
	
	/**
	 * Generate sample test set
	 */
	public List<ArrivalsSolution> generate(int _maxNum)  throws Exception {
		Monitor.init();

		JMetalLogger.logger.info("Creating samples...");
		List<ArrivalsSolution> solutions = searchArriavls(_maxNum);
		JMetalLogger.logger.info("Creating samples...Done.");

		initWriter();
		for (int i=0; i<solutions.size(); i++){
			appendLine(solutions.get(i), i+1);
		}
		closeWriter();
		Monitor.finish();
		return solutions;
	}


	public List<ArrivalsSolution> searchArriavls(int _maxNum){
		// Define operators
		CrossoverOperator<ArrivalsSolution> crossoverOperator;
		MutationOperator<ArrivalsSolution> mutationOperator;
		SelectionOperator<List<ArrivalsSolution>, ArrivalsSolution> selectionOperator;
		Comparator<ArrivalsSolution> comparator;

		// Generate operators
		List<Integer> possibleTasks = TaskDescriptor.getVaryingTasks(problem.Tasks);
		crossoverOperator = new OnePointCrossover(possibleTasks, Settings.P1_CROSSOVER_PROB);
		mutationOperator = new RandomTLMutation(possibleTasks, problem, Settings.P1_MUTATION_PROB);
		selectionOperator = new BinaryTournamentSelection<>();
		comparator = new ObjectiveComparator<>(0, ObjectiveComparator.Ordering.DESCENDING);

		AbstractArrivalGA algorithm = null;
		try {
			// Find algorithm class
			String packageName = this.getClass().getPackage().getName();
			Class algorithmClass = Class.forName(packageName + ".search." + Settings.P1_ALGORITHM);

			// make algorithm instance
			Constructor constructor = algorithmClass.getConstructors()[0];
			Object[] parameters = {0, null, problem,
					Settings.P1_ITERATION, _maxNum,
					crossoverOperator, mutationOperator, selectionOperator, comparator};
			algorithm = (AbstractArrivalGA)constructor.newInstance(parameters);

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

		// execute algorithm in thread
		AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();

		// logging some information
		System.gc();

		return algorithm.getPopulation();
	}

	////////////////////////////////////////////////////////////////////////////////
	// Initial priorities
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create initial priority assignments
	 * @param maxPopulation
	 * @param priorityEngineer
	 * @param problem
	 * @return
	 */
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


	////////////////////////////////////////////////////////////////////////////////
	// Writing utility
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Print out to file for arrival times
	 */
	public void initWriter()
	{
		writer = new GAWriter(WorstGenerator.TESTFILENAME, Level.FINE, null);
		writer.info("Index\tArrivalJSON");
	}
	public void appendLine(ArrivalsSolution solution, int idx) {
		String line = solution.getVariablesStringInline();
		if (idx==-1) idx = (int)solution.ID;
		writer.write(String.format("%d\t", idx));
		writer.write(line);
		writer.write("\n");
	}
	public void closeWriter(){
		writer.close();
	}

}
