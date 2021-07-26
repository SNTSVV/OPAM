package lu.uni.svv.PriorityAssignment.generator;

import lu.uni.svv.PriorityAssignment.utils.ArgumentParser;

public class TaskSetParams {
	public long SIMULATION_TIME=0;
	public double TIMEUNIT;
	public double TARGET_UTILIZATION;
	public double DELTA_UTILIZATION;
	public int N_TASKSET;
	public int N_TASK;
	public int MAX_ARRIVAL_RANGE;
	public double RATIO_APERIODIC;
	public String BASE_PATH;
	public String PRIORITY;
	public boolean LIM_SIM;
	public int GRANULARITY;
	public String controlValue;
	public double MIN_ARRIVAL_RANGE;
	
	
	public void parse(String[] args) throws Exception{
		// parsing args
		ArgumentParser parser = parseArgs(args);
		
		// set params
		this.SIMULATION_TIME = (int)parser.getParam("SIMULATION_TIME");
		this.TIMEUNIT = (double)parser.getParam("TIMEUNIT");
		this.TARGET_UTILIZATION = (double)parser.getParam("TARGET_UTILIZATION");
		this.DELTA_UTILIZATION = (double)parser.getParam("DELTA_UTILIZATION");
		this.N_TASKSET = (int)parser.getParam("N_TASKSET");
		this.N_TASK = (int)parser.getParam("N_TASK");
		this.MAX_ARRIVAL_RANGE = (int)parser.getParam("MAX_ARRIVAL_RANGE");
		this.RATIO_APERIODIC = (double)parser.getParam("RATIO_APERIODIC");
		this.BASE_PATH = (String)parser.getParam("BASE_PATH");
		this.PRIORITY = (String)parser.getParam("PRIORITY");
		this.LIM_SIM = (boolean)parser.getParam("LIM_SIM");
		this.GRANULARITY = (int)parser.getParam("GRANULARITY");
		this.controlValue = (String)parser.getParam("CONTROL_VALUE");
		this.MIN_ARRIVAL_RANGE = (double)parser.getParam("MIN_ARRIVAL_RANGE");
		
		if(!this.LIM_SIM) this.SIMULATION_TIME = 0;
	}
	
	private ArgumentParser parseArgs(String[] args) throws Exception {
		// Setting arguments
		ArgumentParser parser = new ArgumentParser();
		parser.addOption(false,"Help", ArgumentParser.DataType.BOOLEAN, "h", "help", "Show how to use this program", false);
		parser.addOption(false,"SIMULATION_TIME", ArgumentParser.DataType.INTEGER, "s", null, "", 10000);
		parser.addOption(false,"TIMEUNIT", ArgumentParser.DataType.DOUBLE, "t", null, "", 0.1);
		parser.addOption(false,"TARGET_UTILIZATION", ArgumentParser.DataType.DOUBLE, "u", null, "", 0.7);
		parser.addOption(false,"N_TASKSET", ArgumentParser.DataType.INTEGER, "n", null, "", 10);
		parser.addOption(false,"DELTA_UTILIZATION", ArgumentParser.DataType.DOUBLE, "d", null, "", 0.01);
		parser.addOption(false,"MAX_ARRIVAL_RANGE", ArgumentParser.DataType.INTEGER, "a", null, "", 2);
		parser.addOption(false,"RATIO_APERIODIC", ArgumentParser.DataType.DOUBLE, "r", null, "", 0.4);  // average industrial subjects (rounded to -1 digits, 0.37 to 0.4)
		parser.addOption(false,"N_TASK", ArgumentParser.DataType.INTEGER, "m", null, "", 20);           // average industrial subjects (rounded to 2 digits, 18.3 to 20)
		parser.addOption(false,"BASE_PATH", ArgumentParser.DataType.STRING,"b", null, "", null);
		parser.addOption(false,"GRANULARITY", ArgumentParser.DataType.INTEGER,null, "granularity", "", 10);
		parser.addOption(false,"PRIORITY", ArgumentParser.DataType.STRING,null, "priority", "default rate monotonic", "RM");
		parser.addOption(false,"CONTROL_VALUE", ArgumentParser.DataType.STRING,"c", null, "", null);
		parser.addOption(false,"MIN_ARRIVAL_RANGE", ArgumentParser.DataType.DOUBLE,null, "minArrival", "", 1.0);
		// Task set
		parser.addOption(false,"LIM_SIM", ArgumentParser.DataType.BOOLEAN,"l", null, "", false);
		
		// parsing args
		try{
			parser.parseArgs(args);
		}
		catch(Exception e) {
			System.out.println("Error: " + e.getMessage());
			System.out.println("");
			System.out.println(parser.getHelpMsg());
			System.exit(0);
		}
		
		if((Boolean)parser.getParam("Help")){
			System.out.println(parser.getHelpMsg());
			System.exit(1);
		}
		
		return parser;
	}
}
