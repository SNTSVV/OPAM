package lu.uni.svv.PriorityAssignment.analysis;

import com.opencsv.CSVReader;
import lu.uni.svv.PriorityAssignment.Initializer;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.operator.impl.selection.RankingAndCrowdingSelection;
import org.uma.jmetal.qualityindicator.impl.GeneralizedSpread;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.PointSolution;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;


public class ResultEvaluator {
	
	public static void main(String[] args) throws Exception {
		// Environment Settings
		Initializer.initLogger();
		Settings.update(args);
		GAWriter.init(null, false);
		
		// define output paths
		String[] approaches = Settings.WORKNAME.split(",");
		
		String comp1Path = String.format("%s/%s",Settings.BASE_PATH, Settings.COMPARE_PATH1);
		String comp2Path = String.format("%s/%s",Settings.BASE_PATH, Settings.COMPARE_PATH2);
		String outputPath = String.format("%s/%s",Settings.BASE_PATH, Settings.OUTPUT_PATH);
		String[] paths = new String[]{comp1Path, comp2Path};
		
		new ResultEvaluator().calculateQualityIndicators(
									approaches, paths, outputPath,
									Settings.RUN_CNT, Settings.CYCLE_NUM);
	}
	
	////////////////////////////////////////////////////////
	// non-static members
	////////////////////////////////////////////////////////
	GenerationalDistance GD = null;
	PISAHypervolume HV = null;
	GeneralizedSpread SP = null;
	
	public ResultEvaluator() {}
	
	public void calculateQualityIndicators(String[] approaches, String[] paths, String output, int runNum, int targetCycle) throws Exception {
		
		// load all populations
		HashMap<String, List<PointSolution>[]> pops1 = this.load_fitness(paths[0], targetCycle, runNum);
		HashMap<String, List<PointSolution>[]> pops2 = this.load_fitness(paths[1], targetCycle, runNum);
		
		// create output file if it exists delete first
		File f = new File(output);
		if (f.exists()) f.delete();
		String title =  "Approach,Subject,Run,GD,HV,Spread";
		GAWriter writer = new GAWriter(output, Level.INFO, title, "./", false);
		
		// work for each subject
		JMetalLogger.logger.info("Calculating quality indicators...");
		Set<String> subjects =  pops1.keySet();
		for (String subject:subjects){
			JMetalLogger.logger.info("Calculating quality indicators for "+subject+"...");
			List<PointSolution>[] pops1Run = pops1.get(subject);
			List<PointSolution>[] pops2Run = pops2.get(subject);
			
			// Generate reference Pareto
			List<PointSolution> jointPopulation = getCyclePoints(pops1Run, pops2Run);
			List<PointSolution> ref = this.getReferencePareto(jointPopulation);
			
			// Data normalizing
			FrontNormalizer frontNormalizer = new FrontNormalizer(jointPopulation) ;
			ArrayFront refFront = new ArrayFront(ref);
			Front normalizedReferenceFront;
			try{
				normalizedReferenceFront = frontNormalizer.normalize(refFront);
			}
			catch (Exception e){
				// calculate QI values
				writer.write(calculateForError(runNum, approaches[0], subject));
				writer.write(calculateForError(runNum, approaches[1], subject));
				continue;
			}
			
			// define quility indicators
			GD = new GenerationalDistance(normalizedReferenceFront);
			HV = new PISAHypervolume(normalizedReferenceFront);  // need to normalize values [0,1]
			SP = new GeneralizedSpread(normalizedReferenceFront);
			//Spread SP = new Spread(refFront);  // no exception handling when a pareto has one point
			
			// calculate QI values
			String resText = calculate(runNum, approaches[0], subject, pops1Run, frontNormalizer);
			writer.write(resText);
			resText = calculate(runNum, approaches[1], subject, pops1Run, frontNormalizer);
			writer.write(resText);
		}
		writer.close();
		JMetalLogger.logger.info("Calculating quality indicators...Done.");
	}
	
	public String calculate(int runNum, String approach, String subject, List<PointSolution>[] pops, FrontNormalizer normalizer){
		StringBuilder sb = new StringBuilder();
		for (int run = 0; run < runNum; run++) {
			Front normalizedFront = normalizer.normalize(new ArrayFront(pops[run])) ;
			List<PointSolution> normalizedPopulation = FrontUtils.convertFrontToSolutionList(normalizedFront) ;
			
			double gdValue = GD.evaluate(normalizedPopulation);
			double hvValue = HV.evaluate(normalizedPopulation);
			double spValue = SP.evaluate(normalizedPopulation);
			
			sb.append(String.format("%s,%s,%d,%f,%f,%f\n", approach, subject, run+1, gdValue, hvValue, spValue));
		}
		return sb.toString();
	}
	
	public String calculateForError(int runNum, String approach, String subject){
		StringBuilder sb = new StringBuilder();
		for (int run = 0; run < runNum; run++) {
			sb.append(String.format("%s,%s,%d,%f,%f,%f\n", approach, subject, run+1, 0.0, 1.0, 0.0));
		}
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

	public List<PointSolution> getReferencePareto(List<PointSolution> pops) throws Exception{
		
		JMetalLogger.logger.info("Calculating reference pareto....");
		// select reference pareto
		if (pops.size()<Settings.P2_POPULATION){
			JMetalLogger.logger.info("Calculating reference pareto....Done.");
			return pops;
		}
		else{
			RankingAndCrowdingSelection<PointSolution> rankingAndCrowdingSelection =
					new RankingAndCrowdingSelection(Settings.P2_POPULATION, new DominanceComparator());
			List<PointSolution> selected = rankingAndCrowdingSelection.execute(pops);
			List<PointSolution> pareto = SolutionListUtils.getNondominatedSolutions(selected);
			JMetalLogger.logger.info("Calculating reference pareto....Done.");
			return pareto;
		}
		
	}
	
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
