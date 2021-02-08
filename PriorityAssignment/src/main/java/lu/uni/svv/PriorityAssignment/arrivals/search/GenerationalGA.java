package lu.uni.svv.PriorityAssignment.arrivals.search;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import lu.uni.svv.PriorityAssignment.arrivals.AbstractArrivalGA;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;


@SuppressWarnings("serial")
public class GenerationalGA extends AbstractArrivalGA {
	
	/**
	 * Constructor
	 *
	 * @param _cycle
	 * @param _initial
	 * @param problem
	 * @param maxIterations
	 * @param populationSize
	 * @param crossoverOperator
	 * @param mutationOperator
	 * @param selectionOperator
	 * @param comparator
	 */
	public GenerationalGA(int _cycle, List<ArrivalsSolution> _initial, Problem<ArrivalsSolution> problem, int maxIterations, int populationSize, CrossoverOperator<ArrivalsSolution> crossoverOperator, MutationOperator<ArrivalsSolution> mutationOperator, SelectionOperator<List<ArrivalsSolution>, ArrivalsSolution> selectionOperator, Comparator<ArrivalsSolution> comparator) {
		super(_cycle, _initial, problem, maxIterations, populationSize, crossoverOperator, mutationOperator, selectionOperator, comparator);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Execution algorithm
	//////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected List<ArrivalsSolution> replacement(List<ArrivalsSolution> population, List<ArrivalsSolution> offspringPopulation) {
		// Same code with library GenerationalGeneticAlgorithm code
		Collections.sort(population, this.comparator);
		offspringPopulation.add(population.get(0));
		offspringPopulation.add(population.get(1));
		Collections.sort(offspringPopulation, this.comparator);
		offspringPopulation.remove(offspringPopulation.size() - 1);
		offspringPopulation.remove(offspringPopulation.size() - 1);
		return offspringPopulation;
	}
	
	
	@Override
	public String getName() {
		return "GenerationalGA";
	}
	
	@Override
	public String getDescription() {
		return "Generational Genetic Algorithm";
	}
	
}
