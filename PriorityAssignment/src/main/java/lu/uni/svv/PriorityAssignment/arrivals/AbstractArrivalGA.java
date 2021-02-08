package lu.uni.svv.PriorityAssignment.arrivals;

import lu.uni.svv.PriorityAssignment.utils.Monitor;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.algorithm.impl.AbstractGeneticAlgorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalLogger;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@SuppressWarnings("serial")
public abstract class AbstractArrivalGA extends AbstractGeneticAlgorithm<ArrivalsSolution, ArrivalsSolution> {
	protected int maxIterations;
	protected int iterations;
	protected Comparator<ArrivalsSolution> comparator;
	protected SolutionPrinter printer;
	protected List<ArrivalsSolution> initials;
	
	/**
	 * Constructor
	 */
	public AbstractArrivalGA(int _cycle,
	                         List<ArrivalsSolution> _initial,
	                         Problem<ArrivalsSolution> problem,
	                         int maxIterations, int populationSize,
	                         CrossoverOperator<ArrivalsSolution> crossoverOperator,
	                         MutationOperator<ArrivalsSolution> mutationOperator,
	                         SelectionOperator<List<ArrivalsSolution>, ArrivalsSolution> selectionOperator,
	                         Comparator<ArrivalsSolution> comparator)
	{
		super(problem);
		setMaxPopulationSize(populationSize);
		this.maxIterations = maxIterations;
		this.crossoverOperator = crossoverOperator;
		this.mutationOperator = mutationOperator;
		this.selectionOperator = selectionOperator;
		this.comparator = comparator;
		
		this.initials = _initial;
		this.printer = new SolutionPrinter(problem.getNumberOfVariables(), _cycle);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Execution algorithm
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void run() {
		List<ArrivalsSolution> offspringPopulation;
		List<ArrivalsSolution> matingPopulation;
		
		population = (initials==null)?createInitialPopulation():initials;
		population = evaluatePopulation(population);
		initProgress();
		
		while (!isStoppingConditionReached()) {
			matingPopulation = selection(population);
			offspringPopulation = reproduction(matingPopulation);
			offspringPopulation = evaluatePopulation(offspringPopulation);
			population = replacement(population, offspringPopulation);
			updateProgress();
		}
		
		printer.close();
	}
	
	@Override
	protected boolean isStoppingConditionReached() {
		// Same code with library SSGA code
		return (iterations >= maxIterations);
	}
	
	
	@Override
	protected List<ArrivalsSolution> evaluatePopulation(List<ArrivalsSolution> population) {
		Monitor.start("evaluateP1", true);
		// Add logging message
		ArrivalProblem problem = (ArrivalProblem)this.problem;
		int count =0;
		for (ArrivalsSolution solution : population) {
			count += 1;
			problem.evaluate(solution);
//			if (Settings.PRINT_DETAILS)
//				printer.saveExpendedInfo(solution);

//			String msg = String.format("[%s] Evaluated solution [%d/%d] with %d priorities", getName(), count, population.size(), problem.Priorities.size());
//			JMetalLogger.logger.info(msg);
		}
		
		Monitor.end("evaluateP1", true);
		Monitor.updateMemory();
		return population;
	}
	
	@Override
	public void initProgress() {
		// Add logging message and start iteration from 0
		iterations = 0;
//		JMetalLogger.logger.info("["+ getName() +"] initialized population");
		if (Settings.PRINT_FITNESS) {
			Collections.sort(getPopulation(), comparator);
			printer.print(getPopulation(), iterations);
		}
	}
	
	@Override
	public void updateProgress() {
		// Add logging message and garbage collection
		iterations++;
//		JMetalLogger.logger.info(String.format("[%s] iteration: %d", getName(), iterations));
		if (Settings.PRINT_FITNESS) {
			Collections.sort(getPopulation(), comparator);
			printer.print(getPopulation(), iterations);
		}
		System.gc();
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Properties
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ArrivalsSolution getResult() {
		// Same code with library SSGA code
		Collections.sort(getPopulation(), comparator);
		return getPopulation().get(0);
	}
	
}
