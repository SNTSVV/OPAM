package lu.uni.svv.PriorityAssignment.analysis;

import lu.uni.svv.PriorityAssignment.Initializer;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.qualityindicator.impl.GeneralizedSpread;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.qualityindicator.impl.hypervolume.PISAHypervolume;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontNormalizer;
import org.uma.jmetal.util.point.PointSolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;


public class QIEvaluatorPaired extends QIEvaluator {

	public static void main(String[] args) throws Exception {
		// Environment Settings
		Initializer.initLogger();
		Settings.update(args);
		GAWriter.init(null, false);

		// define output paths
		String[] approaches = Settings.APPROACHES.split(",");

		String[] paths = new String[]{Settings.COMPARE_PATH1, Settings.COMPARE_PATH2};

		new QIEvaluatorPaired().run(approaches, paths, Settings.OUTPUT_PATH,
									Settings.RUN_CNT, Settings.NUM_TEST);
	}

	////////////////////////////////////////////////////////
	// non-static members
	////////////////////////////////////////////////////////
	public FrontNormalizer frontNormalizer = null;
	public List<GenericIndicator<PointSolution> > QIs = null;
	public double[] defaults;
	public String[] names;

	public QIEvaluatorPaired() {
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
			for (int runID=0; runID<runNum; runID++) {
				if (initializeQIs(pops1Run[runID], pops2Run[runID])) {
					writer.write(calculate(runID+1, approaches[0], subject, pops1Run[runID]));
					writer.write(calculate(runID+1, approaches[1], subject, pops2Run[runID]));
				} else {
					writer.write(calculateForError(runID+1, approaches[0], subject));
					writer.write(calculateForError(runID+1, approaches[1], subject));
				}
			}
		}
		writer.close();
		JMetalLogger.logger.info("Calculating quality indicators...Done.");
	}

	public boolean initializeQIs(List<PointSolution> _setList1, List<PointSolution> _setList2) throws Exception {

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

	public List<PointSolution> getCyclePoints(List<PointSolution> pops1, List<PointSolution> pops2) {
		List<PointSolution> jointPopulation = new ArrayList<>();
		jointPopulation.addAll(pops1);
		jointPopulation.addAll(pops2);
		return jointPopulation;
	}
}
