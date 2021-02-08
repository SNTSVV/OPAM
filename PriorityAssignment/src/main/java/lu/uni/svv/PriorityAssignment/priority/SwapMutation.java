package lu.uni.svv.PriorityAssignment.priority;

import java.util.List;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.JMetalLogger;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;


@SuppressWarnings("serial")
public class SwapMutation implements MutationOperator<PrioritySolution>
{
	//This class changes only Aperiodic or Sporadic tasks that can be changeable
	private double mutationProbability;
	RandomGenerator randomGenerator = null;
	
	
	/**  Constructor */
	public SwapMutation(double probability) throws JMetalException {

		if (probability < 0) {
			throw new JMetalException("Mutation probability is negative: " + probability) ;
		}
		if (probability > 1) {
			throw new JMetalException("Mutation probability is over 1.0: " + probability) ;
		}

		this.mutationProbability = probability ;
		this.randomGenerator = new RandomGenerator() ;
	}
	
	/** Execute() method */
	@Override
	public PrioritySolution execute(PrioritySolution solution) throws JMetalException {
		if (null == solution) {
			throw new JMetalException("Executed SwapMutation with Null parameter");
		}
		
		List<Integer> variables = solution.getVariables();
		int mutated = 0;
		for(int x=0; x<solution.getNumberOfVariables(); x++){
			if (randomGenerator.nextDouble() >= mutationProbability) continue;
			
			int point1 = randomGenerator.nextInt(0, solution.getNumberOfVariables()-1);
			int point2 = randomGenerator.nextInt(0, solution.getNumberOfVariables()-1);
			int temp = solution.getVariableValue(point1);
			solution.setVariableValue(point1, solution.getVariableValue(point2));
			solution.setVariableValue(point2, temp);
			mutated += 1;
		}
		
		if (mutated>1){
			JMetalLogger.logger.fine("Mutated("+mutated+"): "+solution.toString());
		}
		
		return solution;
	}
	
	/** Implements the mutation operation */
	private void doMutation(int[] _list, int _position)	{
	}
}