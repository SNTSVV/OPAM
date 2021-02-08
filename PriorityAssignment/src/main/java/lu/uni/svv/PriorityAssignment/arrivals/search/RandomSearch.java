package lu.uni.svv.PriorityAssignment.arrivals.search;

import lu.uni.svv.PriorityAssignment.arrivals.AbstractArrivalGA;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.problem.Problem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


@SuppressWarnings("serial")
public class RandomSearch extends AbstractArrivalGA {
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
	public RandomSearch(int _cycle, List<ArrivalsSolution> _initial, Problem<ArrivalsSolution> problem, int maxIterations, int populationSize, CrossoverOperator<ArrivalsSolution> crossoverOperator, MutationOperator<ArrivalsSolution> mutationOperator, SelectionOperator<List<ArrivalsSolution>, ArrivalsSolution> selectionOperator, Comparator<ArrivalsSolution> comparator) {
		super(_cycle, _initial, problem, maxIterations, populationSize, crossoverOperator, mutationOperator, selectionOperator, comparator);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Execution algorithm
	//////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected List<ArrivalsSolution> replacement(List<ArrivalsSolution> population, List<ArrivalsSolution> offspringPopulation) {
		// Same code with library SSGA code
		Collections.sort(population, comparator);
		int worstSolutionIndex = population.size() - 1;
		if (comparator.compare(population.get(worstSolutionIndex), offspringPopulation.get(0)) > 0) {
			population.remove(worstSolutionIndex);
			population.add(offspringPopulation.get(0));
		}
		
		return population;
	}
	
	@Override
	public List<ArrivalsSolution> reproduction(List<ArrivalsSolution> matingPopulation) {
		// Random Search
		List<ArrivalsSolution> offsprings = new ArrayList<>(1);
		ArrivalsSolution solution = problem.createSolution();
		offsprings.add(solution);
		return offsprings;
	}
	
	@Override
	protected List<ArrivalsSolution> selection(List<ArrivalsSolution> population) {
		// do not selection
		return null;
	}
	
	@Override
	public String getName() {
		return "RS";
	}
	
	@Override
	public String getDescription() {
		return "Random Search";
	}
	
}
