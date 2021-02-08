package lu.uni.svv.PriorityAssignment.arrivals;

import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.util.JMetalException;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;


@SuppressWarnings("serial")
public class OnePointCrossover implements CrossoverOperator<ArrivalsSolution>
{
	private double crossoverProbability;
	private RandomGenerator randomGenerator;
	private List<Integer> PossibleTasks = null;

	/** Constructor */
	public OnePointCrossover(List<Integer> _possibleTasks, double crossoverProbability) {
		if (crossoverProbability < 0) {
			throw new JMetalException("Crossover probability is negative: " + crossoverProbability) ;
		}
		
		this.crossoverProbability = crossoverProbability;
		this.randomGenerator = new RandomGenerator();
		
		PossibleTasks = _possibleTasks;
	}
	
	/* Getter and Setter */
	public double getCrossoverProbability() {
		return crossoverProbability;
	}
	public void setCrossoverProbability(double crossoverProbability) {
		this.crossoverProbability = crossoverProbability;
	}
	@Override
	public int getNumberOfRequiredParents() {
		return 2;
	}
	@Override
	public int getNumberOfGeneratedChildren() {
		return 2;
	}

	
	/* Executing */
	@Override
	public List<ArrivalsSolution> execute(List<ArrivalsSolution> solutions)
	{
		if (solutions == null) {
			throw new JMetalException("Null parameter") ;
		} else if (solutions.size() != 2) {
			throw new JMetalException("There must be two parents instead of " + solutions.size()) ;
		}
		
		return doCrossover(crossoverProbability, solutions.get(0), solutions.get(1)) ;
	}


	/** doCrossover method */
	public List<ArrivalsSolution> doCrossover(double probability, ArrivalsSolution parent1, ArrivalsSolution parent2)
	{
		List<ArrivalsSolution> offspring = new ArrayList<ArrivalsSolution>(2);

		offspring.add((ArrivalsSolution) parent1.copy()) ;
		offspring.add((ArrivalsSolution) parent2.copy()) ;

		if (randomGenerator.nextDouble() < probability) {
			//System.out.println("[Debug] Executed crossover");
			
			// 1. Get the total number of bits
			int totalNumberOfVariables = parent1.getNumberOfVariables();
			  
			// 2. Get crossover point
			int crossoverPoint = randomGenerator.nextInt(1, PossibleTasks.size() - 1);
			crossoverPoint = PossibleTasks.get(crossoverPoint);
			
			//System.out.println(String.format("Crossover Point: Task %d", crossoverPoint));
			
			// 3. Exchange values
			for (int x = crossoverPoint-1; x < totalNumberOfVariables; x++) {
				offspring.get(0).setVariableValue(x, (Arrivals)parent2.getVariableValue(x).clone());
				offspring.get(1).setVariableValue(x, (Arrivals)parent1.getVariableValue(x).clone());
			}
		}
		
		return offspring;
	}

}
