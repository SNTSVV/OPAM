package lu.uni.svv.PriorityAssignment;

import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.logging.Level;

import lu.uni.svv.PriorityAssignment.utils.Monitor;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.util.AlgorithmRunner;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.comparator.ObjectiveComparator.Ordering;
import org.uma.jmetal.util.comparator.RankingAndCrowdingDistanceComparator;
import lu.uni.svv.PriorityAssignment.arrivals.*;
import lu.uni.svv.PriorityAssignment.priority.*;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;


public class CoEvolve {
	
	// exchange variables
	Map<String, Object> best = null;
	
	public CoEvolve(){
	}
	
	public void run(TaskDescriptor[] input, int maxCycle, int simulationTime, Integer[] priorityEngineer, List<Arrivals[]> testArrivals) throws Exception {
		Monitor.init();
		JMetalLogger.logger.info("Start co-evolution");
		
		// Execute iterative approach (1 cycle == phase1 + phase2)
		int cycle = 0;
		
		//Step 0. Generate initial populations
		List<ArrivalsSolution> A = null;      // populationA will be generated in a SSGA
		PriorityProblem problemP = null;
		List<PrioritySolution> P = null;
		
		problemP = new PriorityProblem(input, simulationTime);
		P = createInitialPriorities(Settings.P2_POPULATION, priorityEngineer, problemP);
		
		GAWriter internalWriter = null;
		if(Settings.PRINT_INTERNAL_FITNESS) {
			internalWriter = new GAWriter("_arrivalsInternal/arrivals.list", Level.FINE, null);
			internalWriter.info("Cycle\tIndex\tArrivalJSON");
		}
		
		do{
			cycle++;
			
			// Step1. [Phase 1] Find a set of worst case arrival times
			List<Integer[]> priorities = PrioritySolution.toArrays(P);
			ArrivalProblem problemA = new ArrivalProblem(input, priorities, simulationTime, Settings.SCHEDULER);
			A = this.searchArrivals(problemA, cycle, A);
			if(Settings.PRINT_INTERNAL_FITNESS)	saveArrivals(internalWriter, A, cycle);
			
			// Step2. [Phase 2] find the best pareto front of priority assignment
			List<Arrivals[]> arrivals = ArrivalsSolution.toArrays(A);
			PrioritySolutionEvaluator evaluator = new PrioritySolutionEvaluator(input, arrivals, testArrivals, simulationTime, Settings.SCHEDULER);
			P = this.searchPriorities(problemP, evaluator, cycle, P);  // update bestSolutions inside

			// test
//			for (int x=0; x<P.size(); x++)
//				System.out.println("Priorities"+(x+1)+": " + P.get(x).getVariablesString());
				
		}while(cycle < maxCycle);
		if(Settings.PRINT_INTERNAL_FITNESS) internalWriter.close();
		
		Monitor.finish();
		saveResults();
		JMetalLogger.logger.info("Finished all search process.");
	}
	
	
	/**
	 *
	 * @param _problem
	 * @param _cycle
	 * @return
	 */
	private List<ArrivalsSolution> searchArrivals(ArrivalProblem _problem, int _cycle, List<ArrivalsSolution> _initial)
	{
		
		String runMsg = (Settings.RUN_NUM==0)?"": "[Run"+ Settings.RUN_NUM + "] ";
		JMetalLogger.logger.info(runMsg+ "Search worst-case arrival times (Phase1) cycle "+_cycle);
		
		// Define operators
		CrossoverOperator<ArrivalsSolution> crossoverOperator;
		MutationOperator<ArrivalsSolution> mutationOperator;
		SelectionOperator<List<ArrivalsSolution>, ArrivalsSolution> selectionOperator;
		Comparator<ArrivalsSolution> comparator;
		
		// Generate operators
		List<Integer> possibleTasks = TaskDescriptor.getVaryingTasks(_problem.Tasks);
		crossoverOperator = new OnePointCrossover(possibleTasks, Settings.P1_CROSSOVER_PROB);
		mutationOperator = new RandomTLMutation(possibleTasks, _problem, Settings.P1_MUTATION_PROB);
		selectionOperator = new BinaryTournamentSelection<>();
		comparator = new ObjectiveComparator<>(0, Ordering.DESCENDING);
		
		AbstractArrivalGA algorithm = null;
		try {
			// Find algorithm class
			String packageName = this.getClass().getPackage().getName();
			Class algorithmClass = Class.forName(packageName + ".arrivals.search." + Settings.P1_ALGORITHM);
			
			// make algorithm instance
			Constructor constructor = algorithmClass.getConstructors()[0];
			Object[] parameters = {_cycle, _initial, _problem,
					Settings.P1_ITERATION, Settings.P1_POPULATION,
					crossoverOperator, mutationOperator, selectionOperator, comparator};
			algorithm = (AbstractArrivalGA)constructor.newInstance(parameters);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		// execute algorithm in thread
		AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
		// Get result and print
//		saveArrivals(algorithm.getPopulation(), _cycle);
		
		// logging some information
		System.gc();
		
		return algorithm.getPopulation();
	}
	
	/**
	 *
	 * @param _problem
	 * @param _evaluator
	 * @param _cycle
	 * @return
	 * @throws JMetalException
	 * @throws FileNotFoundException
	 */
	public List<PrioritySolution> searchPriorities(PriorityProblem _problem, PrioritySolutionEvaluator _evaluator, int _cycle, List<PrioritySolution> _initial) throws JMetalException, FileNotFoundException {
		
		String runMsg = (Settings.RUN_NUM==0)?"": "[Run"+ Settings.RUN_NUM + "] ";
		JMetalLogger.logger.info(runMsg+ "Search Optimal Priority (Phase2) cycle "+_cycle);
		
		// define operators
		CrossoverOperator<PrioritySolution> crossover;
		MutationOperator<PrioritySolution> mutation;
		SelectionOperator<List<PrioritySolution>, PrioritySolution> selection;
		
		crossover = new PMXCrossover(Settings.P2_CROSSOVER_PROB);
		mutation = new SwapMutation(Settings.P2_MUTATION_PROB);
		selection = new BinaryTournamentSelection<>(new RankingAndCrowdingDistanceComparator<>());
		
		// Define algorithm
		PriorityNSGAII algorithm =	new PriorityNSGAII(
						_cycle, _initial, best, _problem,
						Settings.P2_ITERATIONS, Settings.P2_POPULATION, Settings.P2_POPULATION, Settings.P2_POPULATION,
						crossover, mutation, selection, _evaluator);
		
		// execute algorithm in thread
		AlgorithmRunner algorithmRunner = new AlgorithmRunner.Executor(algorithm).execute();
		// print results
//		List<PrioritySolution> population = algorithm.getResult();
		
//		savePriorities(population, _cycle);
	
		System.gc();
		
		best = algorithm.getBest();
		return algorithm.getPopulation();
	}
	
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
	
	
	/**
	 * Print out to file for arrival times
	 * @param _solutions
	 * @param _cycle
	 */
	private void saveArrivals(GAWriter writer, List<ArrivalsSolution> _solutions, int _cycle)
	{
//		if (_cycle!=Settings.CYCLE_NUM) return ;
		JMetalLogger.logger.info("Saving arrival times in cycle "+(_cycle)+"...");
		for(int idx=0; idx<_solutions.size(); idx++) {
			String line = _solutions.get(idx).getVariablesStringInline();
			writer.write(String.format("%d\t%d\t", _cycle, idx));
			writer.write(line);
			writer.write("\n");
		}
		JMetalLogger.logger.info("Saving population...Done");
	}

	
	/**
	 * Print out to file for arrival times
	 * @param _solutions
	 */
	private void saveTestArrivals(ArrivalProblem problem, List<ArrivalsSolution> _solutions) throws Exception {
		JMetalLogger.logger.info("Saving samples...");
		SampleGenerator gen = new SampleGenerator(problem);
		gen.initWriter();
		for(int idx=0; idx<_solutions.size(); idx++) {
			gen.appendLine(_solutions.get(idx), idx);
		}
		gen.closeWriter();
		JMetalLogger.logger.info("Saved arrival times for test data");
	}
	
	/**
	 * Print out to file for priorities
	 * @param _population
	 * @param _cycle
	 */
	private void savePriorities(List<PrioritySolution> _population, int _cycle)
	{
		JMetalLogger.logger.info("Saving priority assignments in cycle "+(_cycle)+"...");
		// print results
		for (int idx=0; idx < _population.size(); idx++) {
			_population.get(idx).store(String.format("_priorities/priorities_cycle%2d_%02d.json", _cycle, idx));
		}
		JMetalLogger.logger.info("Saving population...Done");
	}
	
	/**
	 * Print out all results (best pareto, last population, time information, memory information..)
	 */
	private void saveResults()
	{
		List<PrioritySolution> pareto = (List<PrioritySolution>)best.get("Pareto");
		if (pareto.size()==0) return;
		
		double dist = (Double)best.get("Distance");
		int iter = (Integer)this.best.get("Iteration");
		int cycle = (Integer)best.get("Cycle");
		
		JMetalLogger.logger.info("Saving best pareto priority assignments...");
		// print results
		GAWriter writer = new GAWriter("_best_pareto.list", Level.FINE, null);
		writer.info("Index\tPriorityJSON");
		for (int idx=0; idx < pareto.size(); idx++) {
			writer.write(String.format("%d\t", idx));
			writer.write(pareto.get(idx).getVariablesString());
			writer.write("\n");
		}
		writer.close();
		
		// print population results
		List<PrioritySolution> population = (List<PrioritySolution>)best.get("Population");
		JMetalLogger.logger.info("Saving population of priority assignments...");
		writer = new GAWriter("_last_population.list", Level.FINE, null);
		writer.info("Index\tPriorityJSON");
		for (int idx=0; idx < population.size(); idx++) {
			writer.write(String.format("%d\t", idx));
			writer.write(population.get(idx).getVariablesString());
			writer.write("\n");
		}
		
		writer = new GAWriter(String.format("_result.txt"), Level.FINE, null);
		writer.info("Cycle: "+ cycle);
		writer.info("bestDistance: "+ dist);
		writer.info("bestIteration: "+ iter);
		
		long all = Monitor.getTime();
		long evP1 = Monitor.getTime("evaluateP1");
		long evP2 = Monitor.getTime("evaluateP2");
		long evP2Ex = Monitor.getTime("external");
		long searchTime = all-evP1-evP2-evP2Ex;
		writer.info(String.format("TotalExecutionTime( s): %.3f",all/1000.0));
		writer.info(String.format("SearchTime(s): %.3f",(searchTime)/1000.0));
		writer.info(String.format("EvaluationTimeP1(s): %.3f",evP1/1000.0));
		writer.info(String.format("EvaluationTimeP2(s): %.3f",evP2/1000.0));
		writer.info(String.format("EvaluationTimeEx(s): %.3f",evP2Ex/1000.0));
		writer.info(String.format("InitHeap: %.1fM (%.1fG)", Monitor.heapInit/Monitor.MB, Monitor.heapInit/Monitor.GB));
		writer.info(String.format("usedHeap: %.1fM (%.1fG)", Monitor.heapUsed/Monitor.MB, Monitor.heapUsed/Monitor.GB));
		writer.info(String.format("commitHeap: %.1fM (%.1fG)", Monitor.heapCommit/Monitor.MB, Monitor.heapCommit/Monitor.GB));
		writer.info(String.format("MaxHeap: %.1fM (%.1fG)", Monitor.heapMax/Monitor.MB, Monitor.heapMax/Monitor.GB));
		writer.info(String.format("MaxNonHeap: %.1fM (%.1fG)", Monitor.nonheapUsed/Monitor.MB, Monitor.nonheapUsed/Monitor.GB));
		writer.close();
		
		JMetalLogger.logger.info("Saving population...Done");
	}
}
