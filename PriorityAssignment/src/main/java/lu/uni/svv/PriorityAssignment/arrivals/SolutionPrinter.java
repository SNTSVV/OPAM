package lu.uni.svv.PriorityAssignment.arrivals;

import java.util.List;
import java.util.logging.Level;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;


public class SolutionPrinter {
	private GAWriter wFitness;
	private GAWriter wMaximum;
	private int cycle;
	
	public SolutionPrinter(int nTask, int cycle){
		this.cycle = cycle;
		init_fitness();
		init_maximum(nTask);
		
	}
	
	public void print(List<ArrivalsSolution> population,  int iter){
		print_fitness(population, iter);
		print_maximum(population, iter);
	}
	
	public void close() {
		if (wFitness != null)
			wFitness.close();
		
		if (wMaximum != null)
			wMaximum.close();
	}
	
	private void init_fitness(){
		// Title
		String title = "Cycle,Iteration,Fitness,DeadlineMiss";
		wFitness = new GAWriter("_fitness/fitness_phase1.csv", Level.INFO, title, null, true);
		
	}
	
	private void print_fitness(List<ArrivalsSolution> population, int iter){
		ArrivalsSolution bestSolution = population.get(0);
		
		double fitness = bestSolution.getObjective(0);
		StringBuilder sb = new StringBuilder();
		
		sb.append(cycle);
		sb.append(",");
		sb.append(iter);
		sb.append(",");
		sb.append(String.format("%e", fitness));
		sb.append(",");
		sb.append(bestSolution.getAttribute("DeadlineMiss"));
		
		wFitness.info(sb.toString());
	}
	
	private void init_maximum(int nTasks){
		// title
		StringBuilder sb = new StringBuilder();
		sb.append("Cycle,Iteration,");
		for(int num=0; num<nTasks; num++){
			sb.append(String.format("Task%d",num+1));
			if (num+1 < nTasks)
				sb.append(",");
		}
		
		wMaximum = new GAWriter("_maximums/maximums_phase1.csv", Level.FINE, sb.toString(), null, true);
	}
	
	private void print_maximum(List<ArrivalsSolution> population, int iter) {
		
		// get maximums from best individual
		int[] maximums = (int[])population.get(0).getAttribute("Maximums");
		
		// generate text
		StringBuilder sb = new StringBuilder();
		sb.append(cycle);
		sb.append(",");
		sb.append(iter);
		sb.append(",");
		int x=0;
		for (; x < maximums.length - 1; x++) {
			sb.append(maximums[x]);
			sb.append(',');
		}
		sb.append(maximums[x]);
		
		wMaximum.info(sb.toString());
	}
	
	public void saveExpendedInfo(ArrivalsSolution _solution)
	{
		// Save Results -->
		//     _details/solution/{solutionID}.txt
		//     _details/Narrivals/{solutionID}.csv
		// print out a solution info.
		String filename = String.format("/_details/solution/%d.json", _solution.ID);
		_solution.store(filename);
		
		// sampleing information
		filename = String.format("/_details/Narrivals/%d.csv", _solution.ID);
		GAWriter writer = new GAWriter(filename, Level.INFO, null);
		writer.info(makeArrivals(_solution));
		writer.close();
	}
	
	private String makeArrivals(ArrivalsSolution _solution){
		StringBuilder sb = new StringBuilder();
		sb.append("TaskID,Arrivals\n");
		List<Arrivals> arrivalsList = _solution.getVariables();
		int tid = 1;
		for (Arrivals item : arrivalsList){
			sb.append(tid);
			sb.append(",");
			sb.append(item.size());
			sb.append("\n");
			tid+=1;
		}
		return sb.toString();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////
	// Print out intermediate results
	////////////////////////////////////////////////////////////////////////////////
	public void printPopulation(String title, List<ArrivalsSolution> population){
		System.out.println(title);
		for(ArrivalsSolution individual : population){
			System.out.println("\t" + individual.toString());
		}
	}
}
