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
public class FullReplaceGA extends AbstractArrivalGA {
	
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
	public FullReplaceGA(int _cycle, List<ArrivalsSolution> _initial, Problem<ArrivalsSolution> problem, int maxIterations, int populationSize, CrossoverOperator<ArrivalsSolution> crossoverOperator, MutationOperator<ArrivalsSolution> mutationOperator, SelectionOperator<List<ArrivalsSolution>, ArrivalsSolution> selectionOperator, Comparator<ArrivalsSolution> comparator) {
		super(_cycle, _initial, problem, maxIterations, populationSize, crossoverOperator, mutationOperator, selectionOperator, comparator);
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
	// Execution algorithm
	//////////////////////////////////////////////////////////////////////////////////////////

	@Override
	protected List<ArrivalsSolution> replacement(List<ArrivalsSolution> population, List<ArrivalsSolution> offspringPopulation) {
		// Full replacement
		return offspringPopulation;
	}
	
	@Override
	public String getName() {
		return "FullReplaceGA";
	}
	
	@Override
	public String getDescription() {
		return "Fully Replacement Genetic Algorithm (Algorithm 20)";
	}
	
}
