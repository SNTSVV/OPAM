package lu.uni.svv.PriorityAssignment.priority.search;

import java.util.*;
import java.util.logging.Level;

import lu.uni.svv.PriorityAssignment.priority.*;
import lu.uni.svv.PriorityAssignment.utils.Monitor;
import lu.uni.svv.PriorityAssignment.utils.UniqueList;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;


public class PrioritySearch extends NSGAII<PrioritySolution> {

	protected int cycle;
	protected int iterations;
	protected int maxIterations;
	protected List<PrioritySolution> initial;
	protected Map<String, Object> best;
	protected static UniqueList searchedList = null;

	protected GAWriter fwriter;
	protected GAWriter mwriter;
	protected ExternalFitness externalFitness = null;

	private long maxFact = 0;

	public PrioritySearch(int _cycle,
						  List<PrioritySolution> _initial, Map<String, Object> _best, PriorityProblem problem,
						  int maxIterations, int populationSize, int matingPoolSize, int offspringPopulationSize,
						  CrossoverOperator<PrioritySolution> crossoverOperator,
						  MutationOperator<PrioritySolution> mutationOperator,
						  SelectionOperator<List<PrioritySolution>, PrioritySolution> selectionOperator,
						  SolutionListEvaluator<PrioritySolution> evaluator)
	{
		super(problem, maxIterations*populationSize, populationSize, matingPoolSize, offspringPopulationSize,
				crossoverOperator, mutationOperator, selectionOperator, evaluator);

		this.cycle = _cycle;
		this.initial = _initial;
		this.iterations = 0;
		this.maxIterations = maxIterations;
		this.evaluations = 0;

		if (this.cycle!=0) {
			externalFitness = new ExternalFitness(problem, (PrioritySolutionEvaluator) evaluator, _best,
					getMaxPopulationSize(), (DominanceComparator<PrioritySolution>) dominanceComparator);
		}

		init_fitness();
		init_maximum();

		if (searchedList==null) {
			searchedList = new UniqueList();
		}
		this.maxFact = factorial(problem.Tasks.length);
	}

	public static void init(){
		searchedList = new UniqueList();
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
		externalProcess(cycle==1);
		this.initial = null;

		while (!isStoppingConditionReached()){
			// Breeding
			if (Settings.P2_SIMPLE_SEARCH){
				offspringPopulation = createRandomOffspirings();
			}
			else{
				matingPopulation = selection(population);
				offspringPopulation = reproduction(matingPopulation);
			}
			offspringPopulation = evaluatePopulation(offspringPopulation);
//			printPopulation(String.format("[%d] offspring", iterations), offspringPopulation);
			population = replacement(population, offspringPopulation);
//			printPopulation(String.format("[%d] population", iterations), population);
			updateProgress();
		}

		JMetalLogger.logger.info("\t\tcalculating external fitness (cycle "+cycle+")");
		externalProcess(false);
		close();
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

	public void externalProcess(boolean withOrigin){
		Monitor.start("external", true);
		if (externalFitness==null) return;
		if(withOrigin){
			externalFitness.evaluateOrigin(population, 0, 0);
		}
		else{
			externalFitness.run(population, cycle, iterations);
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
		return this.iterations > this.maxIterations;
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
	protected List<PrioritySolution> replacement(List<PrioritySolution> population, List<PrioritySolution> offspringPopulation) {
		List<PrioritySolution> jointPopulation = new ArrayList();
		jointPopulation.addAll(population);
		jointPopulation.addAll(offspringPopulation);
		RankingAndCrowdingSelection<PrioritySolution> rankingAndCrowdingSelection = new RankingAndCrowdingSelection(this.getMaxPopulationSize(), this.dominanceComparator);
		return rankingAndCrowdingSelection.execute(jointPopulation);
	}

	protected void printPopulation(String title, List<PrioritySolution> population){
		System.out.println(title);
		for(PrioritySolution individual : population){
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
	private int factorial(int F){
		int ret = 1;
		for(int x=2; x<=F; x++){
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
	private void init_fitness(){
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

	private List<PrioritySolution> getBestSolutions(){
		// minimum is the best
		List<PrioritySolution> pop = new ArrayList<>();

		PrioritySolution selected = population.get(0);
		double minV = selected.getObjective(0);
		for (PrioritySolution solution: population){
			double value = solution.getObjective(0);
			if (minV > value){
				selected = solution;
				minV = value;
			}
		}
		pop.add(selected);
		return pop;
	}

	private void print_fitness(){
		StringBuilder sb = new StringBuilder();

		int idx =0;
		for (PrioritySolution solution: population){
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
		int[] maximums = (int[])population.get(0).getAttribute("Maximums");

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

	private void printDeadlines(PrioritySolution _solution, int _iter, int _idx){
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