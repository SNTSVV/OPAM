package lu.uni.svv.PriorityAssignment.priority.search;

import lu.uni.svv.PriorityAssignment.priority.*;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


public class PriorityRandom extends AbstractPrioritySearch {

	NonDominatedSolutionListArchive<PrioritySolution> nonDominatedArchive ;

	public PriorityRandom(int _cycle, List<PrioritySolution> _initial, Map<String, Object> _best, PriorityProblem _problem, int _maxIterations, int _populationSize, int _matingPoolSize, int _offspringPopulationSize, CrossoverOperator<PrioritySolution> _crossoverOperator, MutationOperator<PrioritySolution> _mutationOperator, SelectionOperator<List<PrioritySolution>, PrioritySolution> _selectionOperator, SolutionListEvaluator<PrioritySolution> _evaluator) {
		super(_cycle, _initial, _best, _problem, _maxIterations, _populationSize, _matingPoolSize, _offspringPopulationSize, _crossoverOperator, _mutationOperator, _selectionOperator, _evaluator);

//		nonDominatedArchive = new NonDominatedSolutionListArchive<PrioritySolution>((DominanceComparator<PrioritySolution>) dominanceComparator);
		externalFitness = new ExternalFitnessRandom(_problem, (PrioritySolutionEvaluator) _evaluator, _best,
								getMaxPopulationSize(), (DominanceComparator<PrioritySolution>) dominanceComparator);
		init_fitness();
		init_maximum();
	}

	@Override
	public void run() {
		List<PrioritySolution> offspringPopulation;
		population = createInitialPopulation();
		population = evaluatePopulation(population);
//		population = replacement(population, new ArrayList<>());
		// printPopulation(String.format("[%d] population", iterations), population);
		initProgress();
		externalProcess(population, cycle==1);
		this.initial = null;

		while (!isStoppingConditionReached()){
			// Breeding
			offspringPopulation = createRandomOffspirings();
			offspringPopulation = evaluatePopulation(offspringPopulation);
			population = replacement(population, offspringPopulation);
			updateProgress();
		}

		JMetalLogger.logger.info("\t\tcalculating external fitness (cycle "+cycle+") // pop size: "+population.size());
		externalProcess(population, false);
		close();
	}

	/**
	 * Create initial priority assignments
	 * @return
	 */
	@Override
	public List<PrioritySolution> createInitialPopulation() {
		List<PrioritySolution> population = new ArrayList<>();
		if (initial != null && initial.size()>0){
			for(PrioritySolution item:initial) {
				population.add(item);
			}
		}
		else {
			for (int i = 0; i < this.maxPopulationSize; i++) {
				population.add(this.problem.createSolution());
			}
		}
		return population;
	}

	@Override
	protected List<PrioritySolution> replacement(List<PrioritySolution> population, List<PrioritySolution> offspringPopulation) {
		NonDominatedSolutionListArchive<PrioritySolution> nonDominatedArchive =
				new NonDominatedSolutionListArchive<>((DominanceComparator<PrioritySolution>) dominanceComparator);

		// add to the non-dominated archive
		for (PrioritySolution individual:population){
			nonDominatedArchive.add(individual);
		}
		for (PrioritySolution offspring:offspringPopulation){
			nonDominatedArchive.add(offspring);
		}
		return nonDominatedArchive.getSolutionList();
	}

	@Override
	public String getName() {
		return "RS";
	}

	@Override
	public String getDescription() { return "Multi-objective random search algorithm" ; }
}
