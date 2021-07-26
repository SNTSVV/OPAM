package lu.uni.svv.PriorityAssignment.priority.search;

import lu.uni.svv.PriorityAssignment.priority.*;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PriorityNSGAII extends AbstractPrioritySearch {
	public PriorityNSGAII(int _cycle, List<PrioritySolution> _initial, Map<String, Object> _best, PriorityProblem _problem, int _maxIterations, int _populationSize, int _matingPoolSize, int _offspringPopulationSize, CrossoverOperator<PrioritySolution> _crossoverOperator, MutationOperator<PrioritySolution> _mutationOperator, SelectionOperator<List<PrioritySolution>, PrioritySolution> _selectionOperator, SolutionListEvaluator<PrioritySolution> _evaluator) {
		super(_cycle, _initial, _best, _problem, _maxIterations, _populationSize, _matingPoolSize, _offspringPopulationSize, _crossoverOperator, _mutationOperator, _selectionOperator, _evaluator);
		if (this.cycle!=0) {
			externalFitness = new ExternalFitness(_problem, (PrioritySolutionEvaluator) _evaluator, _best,
														getMaxPopulationSize(), (DominanceComparator<PrioritySolution>) dominanceComparator);
		}
		init_fitness();
		init_maximum();
	}

	@Override
	public void run() {
		List<PrioritySolution> offspringPopulation;
		List<PrioritySolution> matingPopulation;
		population = createInitialPopulation();
		population = evaluatePopulation(population);
		population = replacement(population, new ArrayList<PrioritySolution>());
		// printPopulation(String.format("[%d] population", iterations), population);
		initProgress();
		externalProcess(population, cycle==1);
		this.initial = null;

		while (!isStoppingConditionReached()){
			// Breeding
			matingPopulation = selection(population);
			offspringPopulation = reproduction(matingPopulation);
			offspringPopulation = evaluatePopulation(offspringPopulation);
//			printPopulation(String.format("[%d] offspring", iterations), offspringPopulation);
			population = replacement(population, offspringPopulation);
//			printPopulation(String.format("[%d] population", iterations), population);
			updateProgress();
		}

		JMetalLogger.logger.info("\t\tcalculating external fitness (cycle "+cycle+")");
		externalProcess(population, false);
		close();
	}

	protected List<PrioritySolution> reproduction(List<PrioritySolution> matingPool) {
		int numberOfParents = this.crossoverOperator.getNumberOfRequiredParents();
		this.checkNumberOfParents(matingPool, numberOfParents);
		List<PrioritySolution> offspringPopulation = new ArrayList(this.offspringPopulationSize);

		boolean flagFULL = false;

		for(int i = 0; i < matingPool.size(); i += numberOfParents) {
			if (flagFULL==true) break;

			List<PrioritySolution> parents = new ArrayList(numberOfParents);

			for(int j = 0; j < numberOfParents; ++j) {
				parents.add(this.population.get(i + j));
			}

			List<PrioritySolution> offspring = (List)this.crossoverOperator.execute(parents);
			Iterator it = offspring.iterator();

			while(it.hasNext()) {
				PrioritySolution s = (PrioritySolution) it.next();
				do {
					this.mutationOperator.execute(s);
					if (searchedList.size() >= maxFact){
						flagFULL = true;
						break;
					}
				} while(!searchedList.add(s.toArray()));
				if (flagFULL==true) break;

				offspringPopulation.add(s);
				if (offspringPopulation.size() >= this.offspringPopulationSize) {
					break;
				}
			}
		}

		return offspringPopulation;
	}

	@Override
	public String getName() {
		return "NSGA-II";
	}

	@Override
	public String getDescription() {
		return "Nondominated Sorting Genetic Algorithm version II";
	}

}
