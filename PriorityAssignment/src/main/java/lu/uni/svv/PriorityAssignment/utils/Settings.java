package lu.uni.svv.PriorityAssignment.utils;

import java.io.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import lu.uni.svv.PriorityAssignment.utils.ArgumentParser.DataType;


public class Settings {
	public static String  SettingFile         = "settings.json";
	
	// common parameter
	public static String  INPUT_FILE          = "";
	public static String  BASE_PATH           = "results";
	public static String  WORKNAME            = "";
	public static int     RUN_NUM             = 0;
	public static int     RUN_START           = 1;
	public static int     RUN_CNT             = 0;
	public static int     CYCLE_NUM           = 1;
	public static int     N_CPUS              = 1;
	public static boolean RANDOM_PRIORITY     = false;
	
	// Scheduler
	public static String  SCHEDULER           = "";
	public static String  TARGET_TASKLIST     = "";
	public static int[]   TARGET_TASKS        = null;
	public static double  TIME_QUANTA         = 0.1;
	public static double  TIME_MAX            = 60000;
	public static boolean ALLOW_OFFSET_RANGE  = false;
	public static boolean EXTEND_SIMULATION_TIME = true;
	public static int     ADDITIONAL_SIMULATION_TIME = 0;
	public static boolean EXTEND_SCHEDULER    = true;
	public static double  FD_BASE             = 2; //1.005
	public static double  FD_EXPONENT         = 10000;
	public static int     MAX_OVER_DEADLINE   = 0;  // 60000, base unit ms
	public static boolean VERIFY_SCHEDULE     = false;

	// Phase1 GA
	public static int     P1_POPULATION       = 10;
	public static int     P1_ITERATION        = 1000;
	public static double  P1_CROSSOVER_PROB   = 0.9;
	public static double  P1_MUTATION_PROB    = 0.5;
	public static String  P1_GROUP_FITNESS    = "average";
	public static String  P1_ALGORITHM        = "SSGA";
	
	//printing
	public static boolean PRINT_FITNESS       = false;
	public static boolean PRINT_DETAILS       = false;
	public static boolean PRINT_FINAL_DETAIL  = true;
	public static boolean PRINT_INTERNAL_FITNESS  = false;
	
	//Second phase
	public static int     P2_POPULATION       = 10;
	public static int     P2_ITERATIONS       = 1000;
	public static double  P2_CROSSOVER_PROB   = 0.8;
	public static double  P2_MUTATION_PROB    = 0;
	public static String  P2_GROUP_FITNESS    = "average";
	public static boolean P2_SIMPLE_SEARCH    = false;
	
	public static String  TEST_GENERATION     = "";  // "", Random, Initial
	public static String  TEST_PATH           = "";
	public static int     NUM_TEST            = 10;
	public static boolean NOLIMIT_POPULATION  = false;
	
	//Results Evaluator options
	public static String COMPARE_PATH1        = "";
	public static String COMPARE_PATH2        = "";
	public static String OUTPUT_PATH        = "";
	
	
	public Settings()
	{
	}
	
	public static void update(String[] args) throws Exception {
		// Setting arguments
		ArgumentParser parser = new ArgumentParser();
		parser.addOption(false,"Help", DataType.BOOLEAN, "h", "help", "Show how to use this program", false);
		parser.addOption(false,"SettingFile", DataType.STRING, "f", null, "Base setting file.", SettingFile);
		parser.addOption(false,"INPUT_FILE", DataType.STRING, null, "data", "input data that including job information");
		parser.addOption(false,"BASE_PATH", DataType.STRING, "b", null, "Base path to save the result of experiments");
		parser.addOption(false,"WORKNAME", DataType.STRING, "w", "workName", "the path for saving workdata in second phase");
		parser.addOption(false,"RUN_NUM", DataType.INTEGER, null, "runID", "Specific run ID when you execute run separately");
		parser.addOption(false,"CYCLE_NUM", DataType.INTEGER, null, "cycle", "Specific run ID when you execute run separately");
		parser.addOption(false,"N_CPUS", DataType.INTEGER, null, "cpus", "the number of CPUs");
		parser.addOption(false,"RANDOM_PRIORITY", DataType.BOOLEAN, null, "random", "random priority");
		parser.addOption(false,"RUN_START", DataType.INTEGER, null, "runStart", "starting number of run ID");
		parser.addOption(false,"RUN_CNT", DataType.INTEGER, null, "runCnt", "number of runs");
		
		//scheduler
		parser.addOption(false,"SCHEDULER", DataType.STRING, "s", null, "Scheduler");
		parser.addOption(false,"TARGET_TASKLIST", DataType.STRING, "t", "targets","target tasks for search");
		parser.addOption(false,"TIME_QUANTA", DataType.DOUBLE, null, "quanta", "Scheduler time quanta");
		parser.addOption(false,"TIME_MAX", DataType.DOUBLE, null, "max", "scheduler time max");
		parser.addOption(false,"ALLOW_OFFSET_RANGE", DataType.BOOLEAN, null, "offsetRange", "Use offset value as a range from 0 to the Offset value");
		parser.addOption(false,"EXTEND_SIMULATION_TIME", DataType.BOOLEAN, null, "extendSimulTime", "Extend simulation time");
		parser.addOption(false,"ADDITIONAL_SIMULATION_TIME", DataType.INTEGER, null, "addMaxTime", "The length of simulation time");
		parser.addOption(false,"EXTEND_SCHEDULER", DataType.BOOLEAN, null, "extendScheduler", "Scheduler extend when they finished simulation time, but the queue remains");
		parser.addOption(false,"FD_BASE", DataType.DOUBLE, null, "base", "base for F_D calculation");
		parser.addOption(false,"FD_EXPONENT", DataType.DOUBLE, null, "exponent", "exponent for F_D calculation");
		parser.addOption(false,"MAX_OVER_DEADLINE", DataType.INTEGER, null, "maxMissed", "The maximum value for the one execution's deadline miss(e-d)");
		parser.addOption(false,"VERIFY_SCHEDULE", DataType.BOOLEAN, null, "verifySchedule", "Do verification process of schedule result when it set");
		
		// Phase 1 GA
		parser.addOption(false,"P1_POPULATION", DataType.INTEGER, null, "p1", "Population for GA");
		parser.addOption(false,"P1_ITERATION", DataType.INTEGER, null, "i1", "Maximum iterations for GA");
		parser.addOption(false,"P1_CROSSOVER_PROB", DataType.DOUBLE, null, "c1", "Crossover rate for GA");
		parser.addOption(false,"P1_MUTATION_PROB", DataType.DOUBLE, null, "m1", "Mutation rate for GA");
		parser.addOption(false,"P1_GROUP_FITNESS", DataType.STRING, null, "fg1", "one type of fitness among average, maximum or minimum");
		parser.addOption(false,"P1_ALGORITHM", DataType.STRING, null, "algo1", "Simple search mode, not using crossover and mutation just produce children randomly in phase 1");
		
		
		// print results
		parser.addOption(false,"PRINT_FITNESS", DataType.BOOLEAN, null, "printFitness", "If you set this parameter, The program will produce fitness detail information");
		parser.addOption(false,"PRINT_DETAILS", DataType.BOOLEAN, null, "printDetails", "If you set this parameter, The program will produce detail information");
		parser.addOption(false,"PRINT_FINAL_DETAIL", DataType.BOOLEAN, null, "printFinal", "If you set this parameter, The program will produce detail information for final cycle");
		parser.addOption(false,"PRINT_INTERNAL_FITNESS", DataType.BOOLEAN, null, "printInternal", "If you set this parameter, The program will produce internal arrival fitness for each cycle");
		
		// Second phase GA
		parser.addOption(false,"P2_POPULATION", DataType.INTEGER, null, "p2", "Population size for NSGA-II");
		parser.addOption(false,"P2_ITERATIONS", DataType.INTEGER, null, "i2", "Maximum iterations for NSGA-II");
		parser.addOption(false,"P2_CROSSOVER_PROB", DataType.DOUBLE, null, "c2", "Crossover rate for GA2");
		parser.addOption(false,"P2_MUTATION_PROB", DataType.DOUBLE, null, "m2", "Mutation rate for GA");
		parser.addOption(false,"P2_GROUP_FITNESS", DataType.STRING, null, "fg2", "one type of fitness among average, maximum or minimum");
		parser.addOption(false,"P2_SIMPLE_SEARCH", DataType.BOOLEAN, null, "simpleP2", "Simple search mode, not using crossover and mutation just produce children randomly in phase 2");
		
		parser.addOption(false,"TEST_GENERATION", DataType.STRING, null, "workType", "search mode: Normal, Random, Initial");
		parser.addOption(false,"TEST_PATH", DataType.STRING, null, "testPath", "path to load test data");
		parser.addOption(false,"NUM_TEST", DataType.INTEGER, null, "numTest", "The number of test arrivals");
		parser.addOption(false,"NOLIMIT_POPULATION", DataType.BOOLEAN, null, "nolimitPop", "Apply limit population for external fitness");
		
		//experiment options
		parser.addOption(false,"COMPARE_PATH1", DataType.STRING, null, "compare1", "target path to compare");
		parser.addOption(false,"COMPARE_PATH2", DataType.STRING, null, "compare2", "target path to compare");
		parser.addOption(false,"OUTPUT_PATH", DataType.STRING, null, "output", "output path for QI evaluators");
		
		
		// parsing args
		try{
			parser.parseArgs(args);
		}
		catch(Exception e)
		{
			System.out.println("Error: "+e.getMessage());
			System.out.println("");
			System.out.println(parser.getHelpMsg());
			System.exit(0);
		}
		
		if((Boolean)parser.getParam("Help")){
			System.out.println(parser.getHelpMsg());
			System.exit(1);
		}
		
		// Load settings from file
		String filename = (String)parser.getParam("SettingFile");
		Settings.updateSettings(filename);      //Update settings from the settings.json file.
		updateFromParser(parser);               //Update settings from the command parameters
		
		Settings.TARGET_TASKS = convertToIntArray(Settings.TARGET_TASKLIST);
		Arrays.sort(Settings.TARGET_TASKS);
	}
	
	public static int[] convertToIntArray(String commaSeparatedStr) {
		if (commaSeparatedStr.startsWith("["))
			commaSeparatedStr = commaSeparatedStr.substring(1);
		if (commaSeparatedStr.endsWith("]"))
			commaSeparatedStr = commaSeparatedStr.substring(0,commaSeparatedStr.length()-1);
		
		int[] result = null;
		if (commaSeparatedStr.trim().length()==0){
			result = new int[0];
		}
		else {
			String[] commaSeparatedArr = commaSeparatedStr.split("\\s*,\\s*");
			result = new int[commaSeparatedArr.length];
			for (int x = 0; x < commaSeparatedArr.length; x++) {
				result[x] = Integer.parseInt(commaSeparatedArr[x]);
			}
		}
		return result;
	}
	
	
	
	/**
	 * update setting information from json file
	 * @param filename
	 * @throws Exception
	 */
	public static void updateSettings(String filename) throws Exception {
		
		// Parse Json
		String jsontext = readPureJsonText(filename);
		JSONParser parser = new JSONParser();
		JSONObject json = (JSONObject)parser.parse(jsontext);
		
		for (Object key:json.keySet()) {  // for in setting file keys
			// find key in the Class fields
			Field field = findKeyField(key.toString());
			if (field == null) {
				throw new Exception("Cannot find variable \"" + key + "\" in setting Class.");
			}
			
			// set value from setting file to class
			field.setAccessible(true);
			Object type = field.getType();
			Object value = json.get(key);
			
			if (type == int.class || type == long.class) {
				field.set(Settings.class, Integer.parseInt(value.toString()));
			} else if (type == float.class || type == double.class) {
				field.set(Settings.class, Double.parseDouble(value.toString()));
			} else if (type == boolean.class) {
				field.set(Settings.class, value);
			} else {
				field.set(Settings.class, value.toString());
			}
			field.setAccessible(false);
		}
	}
	
	/**
	 * update setting information from json file
	 * @param _parser
	 * @throws Exception
	 */
	public static void updateFromParser(ArgumentParser _parser) throws Exception {

		for (String key:_parser.keySet()){
			if (key.compareTo("Help")==0) continue;
			if (key.compareTo("SettingFile")==0) continue;
			
			// find key in the Class fields
			Field field = findKeyField(key);
			if (field == null) {
				throw new Exception("Cannot find variable \"" + key + "\" in setting Class.");
			}
			
			// Set value
			DataType paramType = _parser.getDataType(key);
			Object paramValue = _parser.getParam(key);
			try {
				field.setAccessible(true);
				
				if (paramType==DataType.STRING)
					field.set(null, (String)paramValue);
				
				else if (paramType==DataType.INTEGER)
					field.setInt(null, (Integer)paramValue);
				
				else if (paramType==DataType.BOOLEAN)
					field.setBoolean(null, (Boolean)paramValue);
				
				else if (paramType==DataType.DOUBLE)
					field.setDouble(null, (Double)paramValue);
				
				else {
					throw new Exception("Undefined data type for " + field.getName());
				}
				
				field.setAccessible(false);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static Field findKeyField(String key){
		Field[] fields = Settings.class.getFields();
		
		Field field = null;
		for (Field item : fields) {
			if (key.compareTo(item.getName()) == 0) {
				field = item;
				break;
			}
		}
		return field;
	}
	
	
	/**
	 * Setting text to Pure json text
	 * @param filename
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static String readPureJsonText(String filename) throws IOException, Exception{
		StringBuilder content = new StringBuilder();
		
		BufferedReader br = new BufferedReader(new FileReader(filename));
		while (true) {
			String line = br.readLine();
			if (line == null) break;
			
			// remove comment
			int idx = getCommentIdx(line);
			if (idx >= 0){
				line = line.substring(0, idx);
			}
			
			// append them into content
			content.append(line);
			content.append(System.lineSeparator());
		}
		
		return content.toString();
	}
	private static int getCommentIdx(String s) {
		int idx = -1;
		
		if (s == null && s.length() <=1) return idx;
		
		boolean string = false;
		for(int x=0; x<s.length(); x++) {
			if (!string && s.charAt(x) == '\"'){string = true;	continue;}      // string start
			if (string)
			{
				if (s.charAt(x) == '\\') {x++; continue;}                 // escape
				if (s.charAt(x) == '\"') {string = false;	continue;}      // string end
				continue;
			}
			
			if (s.charAt(x) == '/' && s.charAt(x+1) == '/'){
				idx = x;
				break;
			}
		}
		
		return idx;
	}
	
	/**
	 * convert Class Properties to string
	 */
	public static String getString(){
		Field[] fields = Settings.class.getFields();
		
		StringBuilder sb = new StringBuilder();
		sb.append("---------------------Settings----------------------\n");
		for (Field field:fields){
			sb.append(String.format("%-20s: ",field.getName()));
			
			field.setAccessible(true);
			Object value;
			try {
				value = field.get(Settings.class);
			}catch(IllegalAccessException e){
				value = "";
			}
			if (value instanceof Integer) sb.append((Integer)value);
			if (value instanceof Double) sb.append((Double)value);
			if (value instanceof Boolean) sb.append((Boolean)value);
			if (value instanceof String){
				sb.append("\"");
				sb.append((String)value);
				sb.append("\"");
			}
			if (value instanceof int[]){
				sb.append("[");
				for (int x=0; x<((int[]) value).length; x++){
					if (x!=0) sb.append(", ");
					sb.append(((int[])value)[x]);
				}
				sb.append("]");
			}
			
			sb.append("\n");
		}
		sb.append("---------------------------------------------------\n\n");
		
		return sb.toString();
	}
	
}
