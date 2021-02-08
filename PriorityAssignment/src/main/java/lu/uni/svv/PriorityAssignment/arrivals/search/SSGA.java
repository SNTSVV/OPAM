package lu.uni.svv.PriorityAssignment.arrivals.search;

import java.util.ArrayList;
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
public class SSGA extends AbstractArrivalGA {
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
	public SSGA(int _cycle, List<ArrivalsSolution> _initial, Problem<ArrivalsSolution> problem, int maxIterations, int populationSize, CrossoverOperator<ArrivalsSolution> crossoverOperator, MutationOperator<ArrivalsSolution> mutationOperator, SelectionOperator<List<ArrivalsSolution>, ArrivalsSolution> selectionOperator, Comparator<ArrivalsSolution> comparator) {
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
	protected List<ArrivalsSolution> reproduction(List<ArrivalsSolution> matingPopulation) {
		// Same code with library SSGA code
		List<ArrivalsSolution> offspringPopulation = new ArrayList(1);
		List<ArrivalsSolution> parents = new ArrayList(2);
		parents.add(matingPopulation.get(0));
		parents.add(matingPopulation.get(1));
		List<ArrivalsSolution> offspring = (List)this.crossoverOperator.execute(parents);
		this.mutationOperator.execute(offspring.get(0));
		offspringPopulation.add(offspring.get(0));
		return offspringPopulation;
	}
	
	@Override
	protected List<ArrivalsSolution> selection(List<ArrivalsSolution> population) {
		// modified not to select the same solutions
		List<ArrivalsSolution> matingPopulation = new ArrayList<>(2);
		int i=0;
		long prev_id = -1;
		while (i<2){
			ArrivalsSolution solution = selectionOperator.execute(population);
			long id = solution.ID;
			if (prev_id==id) continue;
			prev_id = id;
			matingPopulation.add(solution);
			i += 1;
		}
		
		return matingPopulation;
	}
	
	@Override
	public String getName() {
		return "ssGA";
	}
	
	@Override
	public String getDescription() {
		return "Steady-State Genetic Algorithm";
	}
	
}
