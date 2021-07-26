package lu.uni.svv.PriorityAssignment.utils;

import lu.uni.svv.PriorityAssignment.arrivals.ArrivalProblem;
import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.arrivals.ArrivalsSolution;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import org.uma.jmetal.util.JMetalLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
	/**
	 * Load Test Arrivals
	 * @return
	 */
	public static List<Arrivals[]> LoadTestArrivals(String _path, TaskDescriptor[] _input, int _simulationTime, int _numCnt) throws Exception{
//		JMetalLogger.logger.info("Loading test arrivals....");
		ArrivalProblem problemA = new ArrivalProblem(_input, null, _simulationTime, Settings.SCHEDULER);
		List<Arrivals[]> arrivals = new ArrayList<>();
		
		// load test file names
		BufferedReader reader = new BufferedReader(new FileReader(_path));
		String line = reader.readLine();
		while(line!=null){
			line = reader.readLine();
			if (line==null || line.trim().compareTo("")==0) break;
			String[] items = line.split("\t");
			ArrivalsSolution sol = ArrivalsSolution.loadFromJSONString(problemA, items[items.length-1]);
			arrivals.add(sol.toArray());
		}
		
		// selecting test files
		if (_numCnt!=0) {
			RandomGenerator random = new RandomGenerator();
			while (arrivals.size() > _numCnt) {
				int idx = random.nextInt(1, arrivals.size()) - 1;
				arrivals.remove(idx);
			}
		}
		
		if (arrivals.size()==0){
			throw new Exception("No file to load arrivals");
		}
//		JMetalLogger.logger.info("Loading test arrivals....Done");
		return arrivals;
	}
	
	public static List<String> listFilesUsingDirectoryStream(String dir) {
		List<String> fileList = new ArrayList<>();
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(dir));
			
			for (Path path : stream) {
				if (!Files.isDirectory(path)) {
					fileList.add(path.getFileName().toString());
				}
			}
		}
		catch (IOException e){
		
		}
		return fileList;
	}
	
}
