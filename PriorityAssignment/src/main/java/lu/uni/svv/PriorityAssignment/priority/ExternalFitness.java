package lu.uni.svv.PriorityAssignment.priority;

import java.util.*;
import java.util.logging.Level;

import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.scheduler.Schedule;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import lu.uni.svv.PriorityAssignment.utils.StoreManger;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;


public class ExternalFitness {
	public int maxPopulation = 0;
	public Map<String, Object> best;
	
	protected GAWriter writer;
	protected PriorityProblem problem;
	protected PrioritySolutionEvaluator evaluator;
	protected Comparator<PrioritySolution> dominanceComparator;
	
	public ExternalFitness(PriorityProblem _problem, PrioritySolutionEvaluator _evaluator, Map<String, Object> _best, int _maxPopulation){
		this(_problem, _evaluator, _best, _maxPopulation, new DominanceComparator());
	}
	
	public ExternalFitness(PriorityProblem _problem, PrioritySolutionEvaluator _evaluator, Map<String, Object> _best, int _maxPopulation, Comparator<PrioritySolution> _dominanceComparator){
		problem = _problem;
		maxPopulation = _maxPopulation;
		dominanceComparator = _dominanceComparator;
		evaluator = _evaluator;
		this.best = _best;
		if (this.best == null){
			this.best = new HashMap<>();
			this.best.put("Distance", (Double)0.0);
			this.best.put("Pareto", new ArrayList<PrioritySolution>());
			this.best.put("Population", new ArrayList<PrioritySolution>());
		}
		
		init_external_fitness();
	}
	
	
	public void summaryPop(String name, List<PrioritySolution> _pop){
		int[] ids = new int[_pop.size()];
		
		for (int x=0; x< _pop.size(); x++){
			ids[x] = (int)_pop.get(x).ID;
		}
		Arrays.sort(ids);
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%15s(%2d): [",name, _pop.size()));
		for(int x=0; x<_pop.size(); x++){
			sb.append(String.format("%4d, ", ids[x]));
		}
		sb.append("]");
		System.out.println(sb.toString());
	}
	public double evaluateOrigin(List<PrioritySolution> _population, int _cycle, int _iterations){
		if( _population == null) return 0;
		
		// copy population
		List<PrioritySolution> empty = new ArrayList<>();
		List<PrioritySolution> jointP = difference(_population, empty, true);
		((PrioritySolutionEvaluator)this.evaluator).evaluateForExternal(jointP, this.problem);
		
		// Calculate ranking for external fitness
		RankingAndCrowdingSelection<PrioritySolution> rncSelection = null;
		int popSize = (Settings.NOLIMIT_POPULATION)?jointP.size():this.maxPopulation;
		if (jointP.size()<popSize){
			popSize = jointP.size();
		}
		rncSelection = new RankingAndCrowdingSelection(popSize, this.dominanceComparator); //this.getMaxPopulationSize()
		List<PrioritySolution> selected = rncSelection.execute(jointP);
		
		// find new best pareto
		double distance = calculateDistance(selected);
		
		print_external_fitness(_cycle, _iterations, selected, distance);
		return distance;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	// External fitness functions
	//////////////////////////////////////////////////////////////////////////////
	public double run(List<PrioritySolution> _population, int _cycle, int _iterations){
		if( _population == null) return 0;
		
		// copy population and recalculate fitness values
		List<PrioritySolution> lastPopulation = (List<PrioritySolution>)best.get("Population");
		
		// select individuals which are not evaluated before
		List<PrioritySolution> jointP = difference(_population, lastPopulation, true);
		((PrioritySolutionEvaluator)this.evaluator).evaluateForExternal(jointP, this.problem);
//		summaryPop("Internal", _population);
//		summaryPop("lastPopulation", lastPopulation);
//		summaryPop("New Evol", jointP);
		
		// Add evaluated individuals into joint population
		jointP.addAll( lastPopulation );
		
		// Calculate ranking for external fitness
		RankingAndCrowdingSelection<PrioritySolution> rncSelection = null;
		int popSize = (Settings.NOLIMIT_POPULATION)?jointP.size():this.maxPopulation;
		if (jointP.size()<popSize){
			popSize = jointP.size();
		}
		rncSelection = new RankingAndCrowdingSelection(popSize, this.dominanceComparator); //this.getMaxPopulationSize()
		List<PrioritySolution> selected = rncSelection.execute(jointP);
		List<PrioritySolution> pareto = SolutionListUtils.getNondominatedSolutions(selected);
		
		// find new best pareto
		double distance = calculateDistance(pareto);
		best.put("Distance", distance);
		best.put("Pareto", pareto);
		best.put("Population", selected);
		best.put("Cycle", _cycle);
		best.put("Iteration", _iterations);
		
		print_external_fitness(_cycle, _iterations, pareto, distance);
		
//		printPopulation("Full Population", selected);
//		printPopulation("Pareto", pareto);
//		summaryPop("FullPopulation", jointP);
//		summaryPop("Pareto", pareto);
//		summaryPop("External", selected);
		return distance;
	}
	
	public void close(){
		if (writer != null)
			writer.close();
	}
	
	protected List<PrioritySolution> intersection(List<PrioritySolution> A, List<PrioritySolution> B){
		List<PrioritySolution> result = new ArrayList<>();
		for (PrioritySolution a:A) {
			boolean flag = false;
			for(PrioritySolution b:B){
				if (a.ID==b.ID){
					flag=true;
					break;
				}
			}
			if (!flag) continue;
			
			result.add(a);
		}
		return result;
	}
	
	protected List<PrioritySolution> difference(List<PrioritySolution> A, List<PrioritySolution> B, boolean isClone){
		List<PrioritySolution> result = new ArrayList<>();
		for (PrioritySolution a:A) {
			boolean flag = false;
			for(PrioritySolution b:B){
				if (a.ID==b.ID){
					flag=true;
					break;
				}
			}
			if (flag) continue;
			
			result.add( (isClone)?a.clone():a );
		}
		return result;
	}
	
	protected double calculateDistance(List<PrioritySolution> _pareto){
		// set ideal point  (if not apply normailzed, ideal for F_C is the maximum value of F_C)
		double[] ideal = new double[]{0, 0};
		Double[] range = this.problem.getFitnessRange(1);
		ideal[1] = range[1];

		
		// calculate euclidian distance with ideal point
		EuclideanDistance ed = new EuclideanDistance();
		double[] fitnesses = null;
		double minDistance = Double.MAX_VALUE;
		for (int i=0; i<_pareto.size(); i++){
			PrioritySolution individual = _pareto.get(i);
			fitnesses = individual.getObjectives();
			double distance = ed.compute(fitnesses, ideal);
			
			if (minDistance > distance){
				minDistance = distance;
			}
		}
		return minDistance;
	}
	
	
	/**
	 * print out titles for external fitness values
	 */
	private void init_external_fitness(){
		// Title
		String title = "Cycle,Iteration,Distance,SolutionIndex,SolutionID,Schedulability,Satisfaction,DeadlineMiss,Rank,CrowdingDistance"; //
		writer = new GAWriter("_fitness/fitness_external.csv", Level.INFO, title, null, true);
	}
	
	/**
	 * print out external fitness values for each cycle
	 */
	public void print_external_fitness(int cycle, int iterations, List<PrioritySolution> _pareto, double _dist){
	
		String baseItems = String.format("%d,%d,%e",cycle, iterations, _dist);
		
		StringBuilder sb = new StringBuilder();
		int idx =0;
		for (PrioritySolution solution: _pareto){

//			if (!(rank==null || (int)rank==0)) continue;
			
			int DM = (int)solution.getAttribute("DeadlineMiss");

			int rank = 0;
			if (solution.hasAttribute(org.uma.jmetal.util.solutionattribute.impl.DominanceRanking.class))
				rank = (Integer)solution.getAttribute(org.uma.jmetal.util.solutionattribute.impl.DominanceRanking.class);
			
			double cDistance = 0.0;
			if (solution.hasAttribute(org.uma.jmetal.util.solutionattribute.impl.DominanceRanking.class))
				cDistance = (Double)solution.getAttribute(org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance.class);
			
			sb.append(baseItems);
			sb.append(",");
			sb.append(idx);
			sb.append(",");
			sb.append(solution.ID);
			sb.append(",");
			sb.append(String.format("%e", -solution.getObjective(0)));
			sb.append(",");
			sb.append(-solution.getObjective(1));
			sb.append(",");
			sb.append(DM);
			sb.append(",");
			sb.append(rank);
			sb.append(",");
			sb.append(cDistance);
			sb.append("\n");
			idx ++;
			
			// Print out the final cycle results
			if(Settings.PRINT_FINAL_DETAIL && cycle==0 && solution.ID==1) {
				List<Arrivals[]> arrivals = ((PrioritySolutionEvaluator)evaluator).TestArrivalsList;
				
				List<Schedule[][]> schedulesList = (ArrayList)solution.getAttribute("schedulesList");
				for (int k=0; k< schedulesList.size(); k++){
					storeForDebug(arrivals.get(k), schedulesList.get(k), solution.toArray(), solution.ID, k);
				}
			}
			
			// Print out the final cycle results
			if(Settings.PRINT_FINAL_DETAIL && cycle==Settings.CYCLE_NUM) {
				List<Arrivals[]> arrivals = ((PrioritySolutionEvaluator)evaluator).TestArrivalsList;

				List<Schedule[][]> schedulesList = (ArrayList)solution.getAttribute("schedulesList");
				for (int k=0; k< schedulesList.size(); k++){
					storeForDebug(arrivals.get(k), schedulesList.get(k), solution.toArray(), solution.ID, k);
				}
			}
		}
		
		writer.write(sb.toString());
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
	
	public void storeForDebug(Arrivals[] arrivals, Schedule[][] schedules, Integer[] priorities, long sID, int aID){
		String filename = String.format("_arrivalsEx/arr%d.json", aID);
		StoreManger.storeArrivals(arrivals, filename);
		
		filename = String.format("_schedulesEx/sol%d_arr%d.json", sID, aID);
		Schedule.printSchedules(filename, schedules);
		
		filename = String.format("_prioritiesEx/sol%d.json", sID);
		StoreManger.storePriority(priorities, filename);
	}
	

	
}
