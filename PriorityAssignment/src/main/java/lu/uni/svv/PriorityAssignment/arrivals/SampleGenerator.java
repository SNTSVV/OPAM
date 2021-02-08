package lu.uni.svv.PriorityAssignment.arrivals;

import lu.uni.svv.PriorityAssignment.scheduler.RTScheduler;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskType;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class SampleGenerator {
	public static String TESTFILENAME = "test.list";
	
	public TaskDescriptor[] Tasks;
	public ArrivalProblem problem;
	private GAWriter writer = null;
	
	public SampleGenerator(TaskDescriptor[] input, int simulationTime) throws Exception
	{
		this(input, simulationTime, null);
	}
	
	public SampleGenerator(ArrivalProblem problem) throws Exception
	{
		Tasks = problem.Tasks;
		this.problem = problem;
	}
	
	public SampleGenerator(TaskDescriptor[] input, int simulationTime, String path) throws Exception
	{
		Tasks = input;
		problem = new ArrivalProblem(input, null, simulationTime, Settings.SCHEDULER);
		
		if (path!=null) SampleGenerator.TESTFILENAME = path;
	}
	
	/**
	 * Generate sample test set
	 */
	public List<ArrivalsSolution> generateRandom(int maxNum, boolean addMin, boolean addMax, boolean addMiddle)  throws Exception {
		initWriter();
		List<ArrivalsSolution> solutions = new ArrayList<>();
		
		// generate random solutions
		JMetalLogger.logger.info("Creating samples...");
		int count = 0;
		while (count<maxNum) {
			count++;
			ArrivalsSolution.ARRIVAL_OPTION option = ArrivalsSolution.ARRIVAL_OPTION.RANDOM;
			if (addMin && count==1) option = ArrivalsSolution.ARRIVAL_OPTION.MIN;
			if (addMax && count==2) option = ArrivalsSolution.ARRIVAL_OPTION.MAX;
			if (addMiddle && count==3) option = ArrivalsSolution.ARRIVAL_OPTION.MIDDLE;

			// Create solution and save to the file
			ArrivalsSolution solution = new ArrivalsSolution(problem, option);
			solutions.add(solution);
			appendLine(solution, count);
			
			JMetalLogger.logger.info(String.format("Created sample [%d/%d]", count, maxNum));
		}
		JMetalLogger.logger.info("Creating samples...Done.");
		closeWriter();
		return solutions;
	}
	
	public List<ArrivalsSolution> generateAdaptive(int maxNum, int nCandidate, boolean extremePoints) {
		initWriter();
		
		JMetalLogger.logger.info("Creating samples...");
		List<ArrivalsSolution> population = new ArrayList<>();
		if (extremePoints && maxNum > 2) {
			ArrivalsSolution solution = new ArrivalsSolution(problem, ArrivalsSolution.ARRIVAL_OPTION.MIN);
			population.add(solution);
			appendLine(solution, 1);
			JMetalLogger.logger.info(String.format("Created sample [%d/%d] -- minimum arrivals", population.size(), maxNum));
			
			solution = new ArrivalsSolution(problem, ArrivalsSolution.ARRIVAL_OPTION.MAX);
			population.add(solution);
			appendLine(solution, 2);
			JMetalLogger.logger.info(String.format("Created sample [%d/%d] -- maximum arrivals", population.size(), maxNum));
		}
		
		// generate remaining solutions
		while(population.size()<maxNum){
			// create candidates
			List<ArrivalsSolution> candidates = new ArrayList<>();
			for(int x=0; x<nCandidate; x++){
				candidates.add(new ArrivalsSolution(problem, ArrivalsSolution.ARRIVAL_OPTION.TASK));
			}
			
			// calculate minimum distance (select maximum)
			double dist = 0.0;
			int index = -1;
			if (population.size()==0){
				// if there is no comparable individuals, select randomly
				RandomGenerator rand = new RandomGenerator();
				index = rand.nextInt(0, candidates.size()-1);
			}
			else {
				for (int x = 0; x < nCandidate; x++) {
					double d = this.getMinDistance(candidates.get(x), population);
					if (d > dist) {
						dist = d;
						index = x;
					}
				}
			}
			
			// select maximum distance
			ArrivalsSolution solution = candidates.get(index);
			population.add(solution);
			appendLine(solution, population.size());
			JMetalLogger.logger.info(String.format("Created sample [%d/%d]", population.size(), maxNum));
		}
		JMetalLogger.logger.info("Creating samples...Done.");
		closeWriter();
		return null;
	}
	
	public double getMinDistance(ArrivalsSolution candidate, List<ArrivalsSolution> pop) {
		int[] vec1 = candidate.getReprVariables();
		
		double min = Double.MAX_VALUE;
		for (ArrivalsSolution s:pop){
			double dist = this.euclidean(vec1, s.getReprVariables());
			if (dist<min){
				min = dist;
			}
		}
		return min;
	}
	
	
	public double euclidean(int[] vec1, int[] vec2) {
		double dist = 0;
		for (int x=0; x<vec1.length; x++){
			int t = vec2[x] - vec1[x];
			dist += t*t;
		}
		dist = Math.sqrt(dist);
		return dist;
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// Writing utility
	////////////////////////////////////////////////////////////////////////////////
	/**
	 * Print out to file for arrival times
	 */
	public void initWriter()
	{
		writer = new GAWriter(SampleGenerator.TESTFILENAME, Level.FINE, null);
		writer.info("Index\tArrivalJSON");
	}
	public void appendLine(ArrivalsSolution solution, int idx) {
		String line = solution.getVariablesStringInline();
		if (idx==-1) idx = (int)solution.ID;
		writer.write(String.format("%d\t", idx));
		writer.write(line);
		writer.write("\n");
	}
	public void closeWriter(){
		writer.close();
	}
	
	////////////////////////////////////////////////////////////////////////////////
	// test function
	////////////////////////////////////////////////////////////////////////////////
	
	public double valid(List<ArrivalsSolution> pop) {
		double[] dists = new double[pop.size()];
		for (int x=0; x<pop.size(); x++){
			int[] vec1 = pop.get(x).getReprVariables();
			
			double min = Double.MAX_VALUE;
			for (int y=0; y<pop.size(); y++){
				if (x==y) continue;
				
				double dist = this.euclidean(vec1, pop.get(y).getReprVariables());
				if (dist<min){
					min = dist;
				}
			}
			dists[x] = min;
		}
		
		double avg = mean(dists);
		double std = calculateSD(avg, dists);
		System.out.println(String.format("Valid dist = avg: %.2f, std: %.2f", avg, std));
		return 0;
	}
	
	public double mean(double arrays[]) {
		double sum = 0.0;
		
		for(double num : arrays) {
			sum += num;
		}
		return sum/arrays.length;
	}
	
	public double calculateSD(double mean, double arrays[]) {
		double standardDeviation = 0.0;
		
		for(double num: arrays) {
			standardDeviation += Math.pow(num - mean, 2);
		}
		
		return Math.sqrt(standardDeviation/arrays.length);
	}
	
	
	public double calculateAvg(ArrivalsSolution solution){
		int[] vars = solution.getReprVariables();
		
		int avg =0;
		int cnt = 0;
		for(int x=0; x<vars.length; x++){
			if (Tasks[x].Type == TaskType.Periodic) continue;
			avg += vars[x];
			cnt ++;
		}
		avg = avg / cnt;
		return avg;
	}
	
	/**
	 * Test code for simple generator
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception{
		
		// Environment Settings
		Settings.update(args);
		GAWriter.init(null, true);
		
		// load input
		TaskDescriptor[] input = TaskDescriptor.loadFromCSV(Settings.INPUT_FILE, Settings.TIME_MAX, Settings.TIME_QUANTA);
		
		// Set SIMULATION_TIME
		int simulationTime = 0;
		if (Settings.TIME_MAX == 0) {
			long[] periodArray = new long[input.length];
			for(int x=0; x<input.length; x++){
				periodArray[x] = input[x].Period;
			}
			simulationTime = (int) RTScheduler.lcm(periodArray);
			if (simulationTime<0) {
				System.out.println("Cannot calculate simulation time");
				return;
			}
		}
		else{
			simulationTime = (int) (Settings.TIME_MAX * (1/Settings.TIME_QUANTA));
			if (Settings.EXTEND_SIMULATION_TIME) {
				long max_deadline = TaskDescriptor.findMaximumDeadline(input);
				simulationTime += max_deadline;
			}
		}
		Settings.TIME_MAX = simulationTime;
		
		// initialze static objects
		ArrivalsSolution.initUUID();
		
		SampleGenerator generator = new SampleGenerator(input, simulationTime);

//		List<ArrivalsSolution> pop = generator.generateRandom(10, true, true, true);
		List<ArrivalsSolution> pop = generator.generateAdaptive(10, 10, false);
		
		
		for (ArrivalsSolution i:pop){
			int[] vars = i.getReprVariables();
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("%6d : ",i.ID));
			sb.append("[");
			for (int x=0; x<vars.length; x++){
				if (input[x].Type == TaskType.Periodic) continue;
				sb.append(String.format("%3d, ",vars[x]));
			}
			sb.append(String.format("], avg: %.2f", generator.calculateAvg(i)));
			System.out.println(sb.toString());
		}
		generator.valid(pop);
	}
	
}
