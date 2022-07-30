package lu.uni.svv.PriorityAssignment.analysis;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import lu.uni.svv.PriorityAssignment.Initializer;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;
import org.uma.jmetal.util.JMetalLogger;
import org.uma.jmetal.util.point.PointSolution;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;


public class QIEvaluatorExt extends QIEvaluator {

	public static void main(String[] args) throws Exception {
		// Environment Settings
		Initializer.initLogger();
		Settings.update(args);
		GAWriter.init(null, false);

		// define output paths
		String[] approaches = Settings.APPROACHES.split(",");

		String[] paths = new String[]{Settings.COMPARE_PATH1, Settings.COMPARE_PATH2};

		new QIEvaluatorExt().run(approaches, paths, Settings.OUTPUT_PATH,
										Settings.RUN_CNT, Settings.NUM_TEST);
	}

	////////////////////////////////////////////////////////
	// non-static members
	////////////////////////////////////////////////////////
	public QIEvaluatorExt() {}

	public void run(String[] approaches, String[] paths, String output,  int runNum, int testNum) throws Exception {

		// create output file
		GAWriter writer = new GAWriter(output, Level.INFO, null, "./", false);
		StringBuilder title =  new StringBuilder("TestID,Approach,Subject,Run");
		for (String name:names){
			title.append(",");
			title.append(name);
		}
		title.append("\n");
		writer.write(title.toString());

		// load fitness
		for(int testID=1; testID<=testNum; testID++){
			String testStr = String.format("%d,", testID);

			// load all populations
			HashMap<String, List<PointSolution>[]> pops1 = this.load_fitness(paths[0], testID, runNum);
			HashMap<String, List<PointSolution>[]> pops2 = this.load_fitness(paths[1], testID, runNum);

			// calculate QIs for each subjects
			Set<String> subjects =  pops1.keySet();
			for (String subject:subjects) {
//				if (subject.compareTo("ESAIL")==0) continue;
				JMetalLogger.logger.info(String.format("[Test%02d] Calculating quality indicators for %s...", testID,subject));
				// Generate reference Pareto
				List<PointSolution>[] pops1Run = pops1.get(subject);
				List<PointSolution>[] pops2Run = pops2.get(subject);

				// calculate QI values
				if (initializeQIs(pops1Run, pops2Run)) {
					for(int runID=0; runID<runNum; runID++)
						writer.write(testStr + calculate(runID, approaches[0], subject, pops1Run[runID]));
					for(int runID=0; runID<runNum; runID++)
						writer.write(testStr + calculate(runID, approaches[1], subject, pops2Run[runID]));
				}
				else{
					for(int runID=0; runID<runNum; runID++)
						writer.write(testStr + calculateForError(runID, approaches[0], subject));
					for(int runID=0; runID<runNum; runID++)
						writer.write(testStr + calculateForError(runID, approaches[1], subject));
				}
			}
			JMetalLogger.logger.info("Calculating quality indicators...Done.");
		}
		writer.close();
	}

	/**
	 * Load fitness values from the result file
	 *    To treat the fitness values as the same way as we do in the search (minimize the objectives),
	 *    we multiply -1 to the fitness values
	 * @param filename
	 * @param runNum
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, List<PointSolution>[]> load_fitness(String filename, int numTest, int runNum) throws IOException, CsvValidationException {

		HashMap<String, List<PointSolution>[]> dset = new HashMap<>();
		
		List<List<PointSolution>[]> list = new ArrayList<List<PointSolution>[]>();
		
		// read csv file
		CSVReader csvReader = new CSVReader(new FileReader(filename));
		csvReader.readNext();  // throw header
		String[] values = null;
		while ((values = csvReader.readNext()) != null) {
			String subject = values[0];
			int run = Integer.parseInt(values[1])-1;  // -1 for using as a index
			int testID = Integer.parseInt(values[2]);
			if (testID!=numTest) continue;

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
			sol.setObjective(0, -Double.parseDouble(values[5]));
			sol.setObjective(1, -Double.parseDouble(values[6]));
			variable[run].add(sol);
		}

		return dset;
	}
	
}
