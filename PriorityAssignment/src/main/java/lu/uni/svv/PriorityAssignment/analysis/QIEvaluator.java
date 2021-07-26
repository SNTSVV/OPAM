package lu.uni.svv.PriorityAssignment.analysis;

import com.opencsv.CSVReader;
import lu.uni.svv.PriorityAssignment.Initializer;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.qualityindicator.impl.GeneralizedSpread;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;


public class QIEvaluator {
	
	public static void main(String[] args) throws Exception {
		// Environment Settings
		Initializer.initLogger();
		Settings.update(args);
		GAWriter.init(null, false);
		
		// define output paths
		String[] approaches = Settings.APPROACHES.split(",");
		
		String[] paths = new String[]{Settings.COMPARE_PATH1, Settings.COMPARE_PATH2};
		
		new QIEvaluator().run(approaches, paths, Settings.OUTPUT_PATH,
									Settings.RUN_CNT, Settings.CYCLE_NUM);
	}
	
	////////////////////////////////////////////////////////
	// non-static members
	////////////////////////////////////////////////////////
	public FrontNormalizer frontNormalizer = null;
	public List<GenericIndicator<PointSolution> > QIs = null;
	public double[] defaults;
	public String[] names;
	
	public QIEvaluator() {
		// CI result will be ranged [0, 0.5] because the reference front will weakly dominate the pareto front produced by one single run
		// default values for GD, GD+, HV, GS, CI, UNFR, SP and CS
		names = new String[]{ "GD", "GDP", "HV", "GS", "CI", "UNFR", "SP", "CS", "CSunique"};
		defaults = new double[]{0.0, 0.0, 1.0, 0.0, 0.5, 1.0, 0.0, 1.0, 1.0};
	}
	
	public void run(String[] approaches, String[] paths, String output, int runNum, int targetCycle) throws Exception {

		// create output file
		GAWriter writer = new GAWriter(output, Level.INFO, null, "./", false);
		StringBuilder title =  new StringBuilder("Approach,Subject,Run");
		for (String name:names){
			title.append(",");
			title.append(name);
		}
		title.append("\n");
		writer.write(title.toString());

		// load all populations
		HashMap<String, List<PointSolution>[]> pops1 = this.load_fitness(paths[0], targetCycle, runNum);
		HashMap<String, List<PointSolution>[]> pops2 = this.load_fitness(paths[1], targetCycle, runNum);

		// calculate QIs for each subjects
		Set<String> subjects =  pops1.keySet();
		for (String subject:subjects) {
			JMetalLogger.logger.info(String.format("Calculating quality indicators for %s...", subject));
			// Generate reference Pareto
			List<PointSolution>[] pops1Run = pops1.get(subject);
			List<PointSolution>[] pops2Run = pops2.get(subject);

			// calculate QI values
			if (initializeQIs(pops1Run, pops2Run)) {
				for(int runID=0; runID<runNum; runID++)
					writer.write(calculate(runID, approaches[0], subject, pops1Run[runID]));
				for(int runID=0; runID<runNum; runID++)
					writer.write(calculate(runID, approaches[1], subject, pops2Run[runID]));
			}
			else{
				for(int runID=0; runID<runNum; runID++)
					writer.write(calculateForError(runID, approaches[0], subject));
				for(int runID=0; runID<runNum; runID++)
					writer.write(calculateForError(runID, approaches[1], subject));
			}
		}
		writer.close();
		JMetalLogger.logger.info("Calculating quality indicators...Done.");
	}

	public boolean initializeQIs(List<PointSolution>[] _setList1, List<PointSolution>[] _setList2) throws Exception {

		// create reference Pareto
		List<PointSolution> jointPopulation = getCyclePoints(_setList1, _setList2);
		List<PointSolution> ref = this.getReferencePareto(jointPopulation);

		// Data normalizing
		this.frontNormalizer = new FrontNormalizer(jointPopulation) ;
		ArrayFront refFront = new ArrayFront(ref);
		Front normalizedReferenceFront;
		try{
			normalizedReferenceFront = this.frontNormalizer.normalize(refFront);
		}
		catch (Exception e){
			return false;
		}

		// define quality indicators
		QIs = new ArrayList<GenericIndicator<PointSolution> >();
		QIs.add(new GenerationalDistance<>(normalizedReferenceFront));
		QIs.add(new GenerationalDistancePlus<>(normalizedReferenceFront));
		QIs.add(new PISAHypervolume<>(normalizedReferenceFront));  // need to normalize values [0,1]
		QIs.add(new GeneralizedSpread<>(normalizedReferenceFront));
		QIs.add(new ContributionIndicator<>(normalizedReferenceFront));
		QIs.add(new UniqueNondominatedFrontRatio<>(normalizedReferenceFront));
		QIs.add(new Spacing<>());
		QIs.add(new CoverageIndicator<>(normalizedReferenceFront, false));
		QIs.add(new CoverageIndicator<>(normalizedReferenceFront, true));

		return true;
	}

	public String calculate(int runID, String approach, String subject, List<PointSolution> pops){
		StringBuilder sb = new StringBuilder();

		Front normalizedFront = this.frontNormalizer.normalize(new ArrayFront(pops)) ;
		List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront) ;

		sb.append(String.format("%s,%s,%d", approach, subject, runID+1));
		for (int q=0; q<QIs.size(); q++){
			double value = QIs.get(q).evaluate(normalizedPopulation);
			sb.append(String.format(",%f",value));
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public String calculateForError(int runID, String approach, String subject){
		StringBuilder sb = new StringBuilder();
		// if higher value is better, put 1.0, otherwise, put 0.0
		//if two pareto are identical, CI values is equal to 0.5
		sb.append(String.format("%s,%s,%d", approach, subject, runID+1));
		for (int q=0; q<defaults.length; q++){
			sb.append(",");
			sb.append(defaults[q]);
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public List<PointSolution> getCyclePoints(List<PointSolution>[] pops1, List<PointSolution>[] pops2) {
		List<PointSolution> jointPopulation = new ArrayList<>();
		
		for (int k = 0; k < pops1.length; k++) {
			jointPopulation.addAll(pops1[k]);
		}
		for (int k = 0; k < pops2.length; k++) {
			jointPopulation.addAll(pops2[k]);
		}
		return jointPopulation;
	}

	public List<PointSolution> getReferencePareto(List<PointSolution> pops) throws Exception {
		RankingAndCrowdingSelection<PointSolution> selector = null;
		selector = new RankingAndCrowdingSelection<>(pops.size(), new DominanceComparator<>());
//		int size = Math.min(pops.size(), Settings.P2_POPULATION);
//		selector = new RankingAndCrowdingSelection<>(size, new DominanceComparator<>());
		List<PointSolution> selected = selector.execute(pops);
		List<PointSolution> pareto = SolutionListUtils.getNondominatedSolutions(selected);
		JMetalLogger.logger.info(String.format("\tCalculating reference pareto....Done (Size: %d).", pareto.size()));
		return pareto;
	}

	/**
	 * Load fitness values from the result file
	 *    To treat the fitness values as the same way as we do in the search (minimize the objectives),
	 *    we multiply -1 to the fitness values
	 * @param filename
	 * @param cycleTarget
	 * @param runNum
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, List<PointSolution>[]> load_fitness(String filename, int cycleTarget, int runNum) throws IOException {
		HashMap<String, List<PointSolution>[]> dset = new HashMap<>();
		
		List<List<PointSolution>[]> list = new ArrayList<List<PointSolution>[]>();
		
		// read csv file
		CSVReader csvReader = new CSVReader(new FileReader(filename));
		csvReader.readNext();  // throw header
		String[] values = null;
		while ((values = csvReader.readNext()) != null) {
			String subject = values[0];
			int run = Integer.parseInt(values[2])-1;
			int cycle = Integer.parseInt(values[3]);
			if (cycle!=cycleTarget) continue;
			
			List<PointSolution>[] variable = null;
			if (dset.containsKey(subject)==false){
				variable = new ArrayList[runNum];
				dset.put(subject, variable);
			}
			else{
				variable = dset.get(subject);
			}
			
			if (variable[run]==null){
				variable[run] = new ArrayList<PointSolution>();
			}
			
			// put point info
			PointSolution sol = new PointSolution(2);
			sol.setObjective(0, -Double.parseDouble(values[8]));
			sol.setObjective(1, -Double.parseDouble(values[9]));
			variable[run].add(sol);
		}

		return dset;
	}

}
