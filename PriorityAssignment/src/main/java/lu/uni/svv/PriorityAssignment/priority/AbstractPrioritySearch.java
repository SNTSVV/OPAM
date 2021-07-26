package lu.uni.svv.PriorityAssignment.priority;

import java.util.*;
import java.util.logging.Level;

import lu.uni.svv.PriorityAssignment.utils.Monitor;
import lu.uni.svv.PriorityAssignment.utils.UniqueList;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;


public class AbstractPrioritySearch extends NSGAII<PrioritySolution> {

	protected int cycle;
	protected int iterations;
	protected int maxIterations;
	protected List<PrioritySolution> initial;
	protected static UniqueList searchedList = null;

	protected GAWriter fwriter;
	protected GAWriter mwriter;
	protected ExternalFitness externalFitness = null;

	protected long maxFact = 0;
	protected long startTime = 0;

	public AbstractPrioritySearch(int _cycle,
						  List<PrioritySolution> _initial, Map<String, Object> _best, PriorityProblem _problem,
						  int _maxIterations, int _populationSize, int _matingPoolSize, int _offspringPopulationSize,
						  CrossoverOperator<PrioritySolution> _crossoverOperator,
						  MutationOperator<PrioritySolution> _mutationOperator,
						  SelectionOperator<List<PrioritySolution>, PrioritySolution> _selectionOperator,
						  SolutionListEvaluator<PrioritySolution> _evaluator)
	{
		super(_problem, _maxIterations*_populationSize, _populationSize, _matingPoolSize, _offspringPopulationSize,
				_crossoverOperator, _mutationOperator, _selectionOperator, _evaluator);

		this.cycle = _cycle;
		this.initial = _initial;
		this.iterations = 0;
		this.maxIterations = _maxIterations;
		this.evaluations = 0;
		// _best parameter is used for the external fitness so sub-class should set this parameter

		this.maxFact = factorial(_problem.Tasks.length);

		if (searchedList==null) {
			searchedList = new UniqueList();
		}

		this.startTime = System.currentTimeMillis();
	}

	public static void init(){
		searchedList = new UniqueList();
	}

	@Override
	public void run() {
		// Need to define in each algorithm
	}

	@Override
	protected List<PrioritySolution> evaluatePopulation(List<PrioritySolution> population) {
		JMetalLogger.logger.info("\t\tcalculating internal fitness (iteration "+iterations+")");
		Monitor.start("evaluateP2", true);
		population = this.evaluator.evaluate(population, this.getProblem());
		Monitor.end("evaluateP2", true);
		Monitor.updateMemory();
		return population;
	}

	public void externalProcess(List<PrioritySolution> _population, boolean _withOrigin){
		Monitor.start("external", true);
		if (externalFitness==null) return;
		if(_withOrigin){
			externalFitness.evaluateOrigin(_population, 0, 0);
		}
		else{
			externalFitness.run(_population, cycle, iterations);
		}
		Monitor.end("external", true);
		Monitor.updateMemory();
	}

	/**
	 * create offsprings randomly
	 * @return
	 */
	public List<PrioritySolution> createRandomOffspirings() {
		List<PrioritySolution> offspringPopulation = new ArrayList(this.offspringPopulationSize);
		for(int i = 0; i < this.offspringPopulationSize; i++) {
			PrioritySolution individual =  this.problem.createSolution();
			offspringPopulation.add(individual);
		}
		return offspringPopulation;
	}

	/**
	 * Create initial priority assignments
	 * @return
	 */
	@Override
	public List<PrioritySolution> createInitialPopulation() {
		List<PrioritySolution> population = new ArrayList<>(this.maxPopulationSize);
		PrioritySolution individual;
		for (int i = 0; i < this.maxPopulationSize; i++) {
			if (initial != null && i < initial.size()){
				individual = initial.get(i);
			}
			else {
				individual =  this.problem.createSolution();
			}
			population.add(individual);
		}
		return population;
	}

	@Override
	protected boolean isStoppingConditionReached() {
//		return this.evaluations >= this.maxEvaluations;
		if (Settings.TIME_LIMITATION != 0){
			long ts = System.currentTimeMillis();
			return (ts-this.startTime) > Settings.TIME_LIMITATION;
		}
		else{
			return this.iterations > this.maxIterations;
		}
	}


	@Override
	protected List<PrioritySolution> replacement(List<PrioritySolution> _population, List<PrioritySolution> _offspringPopulation) {
		List<PrioritySolution> jointPopulation = new ArrayList();
		jointPopulation.addAll(_population);
		jointPopulation.addAll(_offspringPopulation);
		RankingAndCrowdingSelection<PrioritySolution> rankingAndCrowdingSelection = new RankingAndCrowdingSelection(this.getMaxPopulationSize(), this.dominanceComparator);
		return rankingAndCrowdingSelection.execute(jointPopulation);
	}

	protected void printPopulation(String _title, List<PrioritySolution> _population){
		System.out.println(_title);
		for(PrioritySolution individual : _population){
			String plist = individual.getVariablesString();
			double fd = -individual.getObjective(0);
			double fc = -individual.getObjective(1);
			String output = String.format("\tID(%d): %s (F_C: %.0f, F_D: %e)", individual.ID , plist, fc, fd);
			System.out.println(output);
		}
	}


	@Override
	protected void initProgress() {
		super.initProgress();
//		JMetalLogger.logger.info("["+getName()+"] Iteration "+iterations+": created initial population: "+evaluations);
		if (Settings.PRINT_FITNESS) {
			print_fitness();
			print_maximum();
		}
		iterations = 1;

	}

	@Override
	protected void updateProgress() {
		super.updateProgress();
//		JMetalLogger.logger.info("["+getName()+"] Iteration "+iterations+" (evaluated solutions: "+evaluations+")");
		if (Settings.PRINT_FITNESS) {
			print_fitness();
			print_maximum();
		}
		iterations += 1;
		System.gc();
	}

	////////////////////////////////////////////////////////////////////////////////
	// Properties
	////////////////////////////////////////////////////////////////////////////////

	public Map<String, Object> getBest(){
		return externalFitness.best;
	}

	public List<PrioritySolution> getPopulation(){
		return population;
	}

	// get Best Pareto, parent NSGAII algorithm have this method
	//	public List<PrioritySolution> getResult(){
	//		return population;
	//	}

	////////////////////////////////////////////////////////////////////////////////
	// Utils
	////////////////////////////////////////////////////////////////////////////////
	protected int factorial(int _num){
		int ret = 1;
		for(int x=2; x<=_num; x++){
			ret = ret * x;
			if (ret < 0) {
				ret = Integer.MAX_VALUE;
				break;
			}
		}
		return ret;
	}

	////////////////////////////////////////////////////////////////////////////////
	// Print out intermediate results
	////////////////////////////////////////////////////////////////////////////////
	protected void init_fitness(){
		if (externalFitness==null){
			// When this class works for only NSGA mode, it produce the results like the external fitness results
			// iteration value will be located at Cycle and the Iteration value will be set 2.
			String title = "Cycle,Iteration,Distance,SolutionIndex,SolutionID,Schedulability,Satisfaction,DeadlineMiss,Rank,CrowdingDistance";
			fwriter = new GAWriter("_fitness/fitness_external.csv", Level.INFO, title, null, true);
		}
		else{
			String title = "Cycle,Iteration,SolutionIndex,SolutionID,Schedulability,Satisfaction,DeadlineMiss,Rank,CrowdingDistance"; //
			fwriter = new GAWriter("_fitness/fitness_phase2.csv", Level.INFO, title, null, true);
		}


	}

	protected void print_fitness(){
		StringBuilder sb = new StringBuilder();

		int idx =0;
		List<PrioritySolution> pop = getPopulation();
		for (PrioritySolution solution: pop){
			// if (!(rank==null || (int)rank==0)) continue;
			int rank = 0;
			Object rankClass = org.uma.jmetal.util.solutionattribute.impl.DominanceRanking.class;
			if (solution.hasAttribute(rankClass))
				rank = (Integer)solution.getAttribute(rankClass);

			if (externalFitness==null && rank!=0) continue;

			double cDistance = 0.0;
			Object distClass = org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance.class;
			if (solution.hasAttribute(distClass))
				cDistance = (Double)solution.getAttribute(distClass);

			int dms = (int)solution.getAttribute("DeadlineMiss");

			if (externalFitness==null) {
				sb.append(iterations);		//actual iteration value is located at Cycle
				sb.append(",");
				sb.append((iterations==0)?0:2);  //result Iteration value set 2 (for the analysis purpose)
				sb.append(",");
				sb.append(0);		        // distance
				sb.append(",");
			}
			else{
				sb.append(cycle);
				sb.append(",");
				sb.append(iterations);
				sb.append(",");
			}
			sb.append(idx);
			sb.append(",");
			sb.append(solution.ID);
			sb.append(",");
			sb.append(String.format("%e",-solution.getObjective(0)));
			sb.append(",");
			sb.append(-solution.getObjective(1));
			sb.append(",");
			sb.append(dms);
			sb.append(",");
			sb.append(rank);
			sb.append(",");
			sb.append(cDistance);
			sb.append("\n");
			idx ++;

//			if (dms[fIndex] > 0){
//				printDeadlines(solution, iterations, idx);
//			}
		}

		fwriter.write(sb.toString());
	}

	public void init_maximum(){
		// title
		StringBuilder sb = new StringBuilder();
		sb.append("Cycle,Iteration,");
		for(int num=0; num<problem.getNumberOfVariables(); num++){
			sb.append(String.format("Task%d",num+1));
			if (num+1 < problem.getNumberOfVariables())
				sb.append(",");
		}

		mwriter = new GAWriter(String.format("_maximums/maximums_phase2.csv"), Level.FINE, sb.toString(), null, true);
	}

	public void print_maximum() {

		// get maximums from best individual
		int[] maximums = (int[])getPopulation().get(0).getAttribute("Maximums");

		// generate text
		StringBuilder sb = new StringBuilder();
		sb.append(cycle);
		sb.append(",");
		sb.append(iterations);
		sb.append(",");
		int x=0;
		for (; x < maximums.length - 1; x++) {
			sb.append(maximums[x]);
			sb.append(',');
		}
		sb.append(maximums[x]);

		mwriter.info(sb.toString());
	}

	protected void close() {
		if (mwriter != null)
			mwriter.close();

		if (fwriter != null)
			fwriter.close();

		if (externalFitness != null)
			externalFitness.close();
	}

	protected void printDeadlines(PrioritySolution _solution, int _iter, int _idx){
		if (_solution==null) return ;
		String text = (String)_solution.getAttribute("DMString");

		String title = "ArrivalID,Task,Execution,DeadlineMiss";
		GAWriter writer = new GAWriter(String.format("_deadlines/deadlines_phase2_iter%d_sol%d_rankIdx%d.csv", _iter, _solution.ID, _idx), Level.INFO, title, null);
		writer.info(text);
		writer.close();

		text = _solution.getVariablesString();
		writer = new GAWriter(String.format("_priorities_rank/priorities_phase2_iter%d_sol%d_rankIdx%d.csv", _iter,  _solution.ID, _idx), Level.INFO, "", null);
		writer.info(text);
		writer.close();
	}
}
