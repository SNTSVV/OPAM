package lu.uni.svv.PriorityAssignment.priority;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.util.JMetalLogger;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.utils.Settings;


/**
 * Class Responsibility
 *  - Definition of the problem to solve
 *  - Basic environments (This is included the definition of the problem)
 *  - An interface to create a solution
 *  - A method to evaluate a solution
 * @author jaekwon.lee
 */
@SuppressWarnings("serial")
public class PriorityProblem implements Problem<PrioritySolution> {
	private int NumberOfVariables = 0;
	private int NumberOfObjectives = 0;
	private int NumberOfAperiodics = 0;
	
	private List<Double[]> ranges = null;
	
	protected TaskDescriptor[] Tasks;
	
	/**
	 * Constructor
	 * * Load input data and setting environment of experiment
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	public PriorityProblem(TaskDescriptor[] _tasks, long _simulationTime) throws NumberFormatException, IOException{

		// Set environment of this problem.
		this.Tasks = _tasks;
		this.NumberOfVariables = _tasks.length;
		this.NumberOfObjectives = 2;    //Multi Objective Problem
	
		this.calculateRange(_tasks, _simulationTime);
	}
	
	protected void calculateRange(TaskDescriptor[] _tasks, long _simulationTime){
		this.ranges = new ArrayList<>();
		
		// get log normalization
		double minFitness = TaskDescriptor.getMinFitness(_tasks, _simulationTime);
		double maxFitness = TaskDescriptor.getMaxFitness(_tasks, _simulationTime);
		
		this.ranges.add(new Double[]{minFitness, maxFitness});
		
		// get range of condition
		int nTasks = _tasks.length;
		int nAperiodics = TaskDescriptor.getNumberOfAperiodics(_tasks);
		int maxCondition = (nAperiodics * (nAperiodics+1))/2;
		
		int maxValue = (nTasks-1);
		int minValue = (nTasks-nAperiodics)-1;
		int minCondition = (minValue*(minValue+1))/2 - (maxValue*(maxValue+1))/2;
	
		this.ranges.add(new Double[]{(double)minCondition, (double)maxCondition});
		JMetalLogger.logger.info(String.format("[Fitness 1] min: %e, max: %e", ranges.get(0)[0], ranges.get(0)[1]));
		JMetalLogger.logger.info(String.format("[Fitness 2] min: %.0f, max: %.0f", ranges.get(1)[0], ranges.get(1)[1]));
	}
	
	public TaskDescriptor[] getTasks(){ return Tasks;}
	
	public Double[] getFitnessRange(int objectiveID){
		return this.ranges.get(objectiveID);
	}
	
	@Override
	public int getNumberOfVariables() {
		return NumberOfVariables;
	}
	
	@Override
	public int getNumberOfObjectives() {
		return NumberOfObjectives;
	}
	
	@Override
	public int getNumberOfConstraints() {
		return 0;
	}
	
	@Override
	public String getName() {
		return null;
	}
	
	@Override
	public void evaluate(PrioritySolution prioritySolution) {
	
	}
	
	/**
	 * Class Responsibility :create solution interface
	 * Delegate this responsibility to Solution Class.
	 */
	@Override
	public PrioritySolution createSolution() {
		return new PrioritySolution(this);
	}
	
	public int getNumberOfAperiodics(){
		return NumberOfAperiodics;
		
	}
}
