package lu.uni.svv.PriorityAssignment.arrivals;

import java.util.List;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.util.JMetalException;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;


@SuppressWarnings("serial")
public class RandomTLMutation implements MutationOperator<ArrivalsSolution>
{
	//This class changes only Aperiodic or Sporadic tasks that can be changeable
	private double mutationProbability;
	List<Integer> PossibleTasks = null;
	ArrivalProblem problem = null;
	RandomGenerator randomGenerator = null;

	public long newValue = 0;
	public int taskID = 0;
	public long position = 0;
	
	/**  Constructor */
	public RandomTLMutation(List<Integer> _possibleTasks, ArrivalProblem _problem, double _probability) throws JMetalException {

		if (_probability < 0) {
			throw new JMetalException("Mutation probability is negative: " + _probability) ;
		}
		if (_probability > 1) {
			throw new JMetalException("Mutation probability is over 1.0: " + _probability) ;
		}

		this.mutationProbability = _probability ;
		this.randomGenerator = new RandomGenerator() ;
		PossibleTasks = _possibleTasks;
		problem = _problem;
	}
	  

	/* Getters and Setters */
	public double getMutationProbability() {
		return mutationProbability;
	}
	public void setMutationProbability(double mutationProbability) {
		this.mutationProbability = mutationProbability;
	}

	/** Execute() method */
	@Override
	public ArrivalsSolution execute(ArrivalsSolution solution) throws JMetalException {
		if (null == solution) {
			throw new JMetalException("Executed RandomTLMutation with Null parameter");
		}
		
		this.newValue = -1;
		this.position = -1;
		this.taskID = -1;
		
		for (int t:PossibleTasks)
		{
			Arrivals variable = solution.getVariableValue(t-1);
			for (int a=0; a<variable.size(); a++) {
				if (randomGenerator.nextDouble() >= mutationProbability) continue;
				doMutation(variable, t-1, a);
			}
		}

		return solution;
	}
	
	/** Implements the mutation operation */
	private void doMutation(Arrivals arrivals, int _taskIdx, int _position)
	{
		// execute mutation
		long curValue = arrivals.get(_position);
		long lastValue = 0;
		if ( _position>=1 ) lastValue = arrivals.get(_position-1);
		
		TaskDescriptor T = problem.Tasks[_taskIdx];
		
		// make a new value
		// only non-periodic tasks changes because we filtered in early stage
		long newValue = lastValue + randomGenerator.nextLong(T.MinIA, T.MaxIA);;

		// propagate changed values
		long delta = newValue - curValue;
		curValue += delta;
		arrivals.set(_position, curValue);
		
		// modify nextValues following range constraint			
		for (int x = _position+1; x< arrivals.size(); x++) {
			long nextValue = arrivals.get(x);
			if ( (nextValue >= T.MinIA + curValue) && (nextValue <= T.MaxIA + curValue) )	break;
			arrivals.set(x, nextValue+delta);
			curValue = nextValue+delta;
		}
		
		// Maximum Constraint
		// if the current value is over Maximum time quanta, remove the value
		// otherwise, save the result at the same place
		for (int x = arrivals.size()-1; x>=0; x--) {
			if (arrivals.get(x) <= problem.SIMULATION_TIME)
				break;
			arrivals.remove(x);
		}
		
		// Maximum Constraint
		lastValue = arrivals.get(arrivals.size()-1);
		if (lastValue + T.MaxIA < problem.SIMULATION_TIME)
		{
			arrivals.add(lastValue + randomGenerator.nextLong(T.MinIA, T.MaxIA));
		}
		
		return;
	}
}