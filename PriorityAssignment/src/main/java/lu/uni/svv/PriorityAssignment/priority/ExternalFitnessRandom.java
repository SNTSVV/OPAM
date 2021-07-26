package lu.uni.svv.PriorityAssignment.priority;

import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;

import java.util.*;
import java.util.logging.Level;


public class ExternalFitnessRandom extends ExternalFitness {

	public ExternalFitnessRandom(PriorityProblem _problem, PrioritySolutionEvaluator _evaluator, Map<String, Object> _best, int _maxPopulation){
		this(_problem, _evaluator, _best, _maxPopulation, new DominanceComparator());
	}

	public ExternalFitnessRandom(PriorityProblem _problem, PrioritySolutionEvaluator _evaluator, Map<String, Object> _best, int _maxPopulation, DominanceComparator<PrioritySolution> _dominanceComparator){
		super(_problem, _evaluator, _best, _maxPopulation, _dominanceComparator);
	}
	/**
	 * Evaluate a population
	 * @param _population
	 * @param _cycle
	 * @param _iterations
	 * @return
	 */
	public double evaluateOrigin(List<PrioritySolution> _population, int _cycle, int _iterations){
		if( _population == null) return 0;

		// copy population
		List<PrioritySolution> empty = new ArrayList<>();
		List<PrioritySolution> jointP = difference(_population, empty, true);
		((PrioritySolutionEvaluator)this.evaluator).evaluateForExternal(jointP, this.problem);

		//TODO:: Do we need Size filter..??
		int popSize = (Settings.NOLIMIT_POPULATION)?jointP.size():this.maxPopulation;
		if (jointP.size()<popSize){
			popSize = jointP.size();
		}

		NonDominatedSolutionListArchive<PrioritySolution> nonDominatedArchive =
				new NonDominatedSolutionListArchive<PrioritySolution>(this.dominanceComparator);
		nonDominatedArchive.addAll(jointP);
		List<PrioritySolution> pareto = nonDominatedArchive.getSolutionList();

		// find new best pareto
		double distance = calculateDistance(pareto);

		print_external_fitness(_cycle, _iterations, pareto, distance);
		print_external_fitness_ex(_cycle, _iterations, pareto, distance);
		return distance;
	}


	//////////////////////////////////////////////////////////////////////////////
	// External fitness functions
	//////////////////////////////////////////////////////////////////////////////
	public double run(List<PrioritySolution> _population, int _cycle, int _iterations){
		if( _population == null) return 0;

		// copy population and recalculate fitness values
		List<PrioritySolution> lastPopulation = (List<PrioritySolution>)best.get("Population");

		// select individuals which are not evaluated before
		List<PrioritySolution> jointP = difference(_population, lastPopulation, true);
		((PrioritySolutionEvaluator)this.evaluator).evaluateForExternal(jointP, this.problem);

		// Add evaluated individuals into joint population
		jointP.addAll( lastPopulation );

		// Calculate ranking for external fitness
		NonDominatedSolutionListArchive<PrioritySolution> nonDominatedArchive = new NonDominatedSolutionListArchive<>(dominanceComparator);
		nonDominatedArchive.addAll(jointP);
		List<PrioritySolution> pareto = nonDominatedArchive.getSolutionList();

		// find new best pareto
		double distance = calculateDistance(pareto);
		best.put("Distance", distance);
		best.put("Pareto", pareto);
		best.put("Population", pareto);
		best.put("Cycle", _cycle);
		best.put("Iteration", _iterations);

		print_external_fitness(_cycle, _iterations, pareto, distance);
		print_external_fitness_ex(_cycle, _iterations, pareto, distance);

//		printPopulation("Full Population", selected);
//		printPopulation("Pareto", pareto);
//		summaryPop("FullPopulation", jointP);
//		summaryPop("Pareto", pareto);
//		summaryPop("External", selected);
		return distance;
	}

}
