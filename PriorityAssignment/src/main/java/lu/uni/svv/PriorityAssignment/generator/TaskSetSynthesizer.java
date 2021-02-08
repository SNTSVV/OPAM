package lu.uni.svv.PriorityAssignment.generator;

import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskType;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Time unit: millisecond
 */
public class TaskSetSynthesizer {

	public static void main(String[] args)throws Exception{
		// parsing args
		TaskSetParams params = new TaskSetParams();
		params.parse(args);
		
		TaskSetSynthesizer synthesizer = new TaskSetSynthesizer((int)(params.SIMULATION_TIME/params.TIMEUNIT),
																params.LIM_SIM,
																params.GRANULARITY,
																params.MIN_ARRIVAL_RANGE,
																params.PRIORITY);
		
		synthesizer.run(params.WORKNAME, params.controlValue, params.N_TASKSET,
				params.TIMEUNIT, params.TARGET_UTILIZATION, params.DELTA_UTILIZATION,
				params.N_TASK, params.RATIO_APERIODIC, params.MAX_ARRIVAL_RANGE);
	}
	
	//////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	
	private long MAX_SIM_TIME = 0;
	private int GRANULARITY = 0;
	private boolean LIM_SIM = true;
	private String PRIORITY = "RM";
	private double MINIMUM_RANGE = 1.0;
	
	private Random rand;
	private int nDiscarded = 0;
	
	public TaskSetSynthesizer(long _maxSimTime, boolean _limitSim, int _granularity){
		this(_maxSimTime, _limitSim, _granularity, 1,"RM");
	}
	
	public TaskSetSynthesizer(long _maxSimTime, boolean _limitSim, int _granularity, double _minRangeAperiodic){
		this(_maxSimTime, _limitSim, _granularity, _minRangeAperiodic,"RM");
	}
	
	public TaskSetSynthesizer(long _maxSimTime, boolean _limitSim, int _granularity, double _minRangeAperiodic, String _priority){
		rand = new Random();
		this.MAX_SIM_TIME = _maxSimTime;
		this.LIM_SIM = _limitSim;
		this.GRANULARITY = _granularity;
		this.PRIORITY = _priority;
		this.MINIMUM_RANGE = _minRangeAperiodic;
	}
	
	public void run(String workName, String control, int nTaskset,
	                 double TIMEUNIT, double targetU, double delta,
	                 int nTask, double ratioAperiodic, int maxArrivalRange) {
		
		// Generate result path
		String basePath = String.format("res/%s", workName);
		File path = new File(String.format("%s/%s",basePath, control));
		if (!path.exists()) path.mkdirs();
		
		for (int x=0; x<nTaskset; x++) {
			long ts = System.currentTimeMillis();
			nDiscarded = 0;
			OPAMTaskSet taskset = generateMultiTaskset(nTask, targetU, delta,
					(int)(10/TIMEUNIT), (int)(1000/TIMEUNIT), (int)(GRANULARITY/TIMEUNIT), true,
					ratioAperiodic, maxArrivalRange);
			ts = System.currentTimeMillis() - ts;
			
			// Print results
			double LCM = taskset.calculateLCM(false);   //taskset.calculateLCM();   // LCM of all
			double maxIA = taskset.getMaxInterArrivalTime();
			double sTime = Math.max(LCM, maxIA)*TIMEUNIT;
			System.out.print(String.format("Time: %.3fs, Discarded: %8d, LCMp: %25.2fms || ", ts / 1000.0, nDiscarded, sTime));
			System.out.println(taskset.toString());
			
			String filename = String.format("%s/taskset_%03d.csv", path.getAbsolutePath(), x);
			taskset.print(filename, TIMEUNIT);
		} // for number of tast set
	}

	/**
	 * Generate a set of tasks including aperiodic and periodic tasks
	 * @param nTask number of tasks
	 * @param targetU target total utilization
	 * @param delta acceptable actual total utilization delta
	 * @param minPeriod minimum task period
	 * @param maxPeriod maximum task period
	 * @param granularity a granularity of task period (task period would be multiples of granularity)
	 * @param allowDuplicate allow duplicated task period to a set of periodic tasks
	 * @param ratioAperiodic the ratio of aperiodic tasks in the set of tasks
	 * @param maxArrivalRange the maximum range of inter-arrival time for aperiodic tasks
	 * @return
	 */
	public OPAMTaskSet generateMultiTaskset(int nTask, double targetU, double delta,
	                                         int minPeriod, int maxPeriod, int granularity, boolean allowDuplicate,
	                                         double ratioAperiodic, int maxArrivalRange) {
		OPAMTaskSet taskset = null;
		while (taskset == null) {
			taskset = generatePeriodicTaskSet(nTask, targetU, minPeriod, maxPeriod, granularity, allowDuplicate);
			if(!taskset.isValidWCET() || !taskset.isValidUtilization(targetU, delta)) {
				nDiscarded++;
				taskset = null;
				continue;
			}
			taskset =  selectAperiodicTasks(taskset, ratioAperiodic, maxArrivalRange, 0, 0, granularity);
			if (LIM_SIM && !taskset.isValidSimulationTime((int)(MAX_SIM_TIME))){
				nDiscarded++;
				taskset=null;
				continue;
			}
			if (this.PRIORITY.compareTo("RM")==0) {
				taskset = assignPriority(taskset);
			}
			else{
				taskset = assignPriorityEngineer(taskset);
			}
		}
		return taskset;
	}
	
	/**
	 * Generate a set of periodic tasks
	 * @param nTask number of tasks
	 * @param targetU target total utilization
	 * @param minPeriod minimum task period
	 * @param maxPeriod maximum task period
	 * @param granularity a granularity of task period (task period would be multiples of granularity)
	 * @param allowDuplicate allow duplicated task period to a set of periodic tasks
	 * @return a set of periodic tasks
	 */
	public OPAMTaskSet generatePeriodicTaskSet(int nTask, double targetU, int minPeriod, int maxPeriod, int granularity, boolean allowDuplicate){
		List<TaskDescriptor> tasks  = new ArrayList<TaskDescriptor>();
		
		double[] utilizations = UUniFastDiscard(nTask, targetU);
		int[] periods = generatePeriods(nTask, minPeriod, maxPeriod, granularity,allowDuplicate);
		int[] WCETs = generateWCETs(periods, utilizations);
		
		TaskDescriptor.UNIQUE_ID = 1;
		for (int t=0; t<nTask; t++){
			TaskDescriptor task = new TaskDescriptor();
			task.Name       = String.format("t%02d", t+1);
			task.Type 		= TaskType.Periodic;
			task.WCET 	    = WCETs[t];
			task.MaxWCET    = WCETs[t];
			task.Period  	= periods[t];
			task.Deadline 	= periods[t];
			task.Priority 	= 0;
			tasks.add(task);
		}
		return new OPAMTaskSet(tasks);
	}
	
	/**
	 * Select some tasks among a set of periodic tasks as the ratio _ratioAperiodic and change them to aperiodic tasks
	 * @param _taskset
	 * @param _ratioAperiodic
	 * @param maxArrivalRange
	 * @param selectRangeMin
	 * @param selectRangeMax
	 * @param _granularity
	 * @return
	 */
	public OPAMTaskSet selectAperiodicTasks(OPAMTaskSet _taskset, double _ratioAperiodic, int maxArrivalRange, int selectRangeMin, int selectRangeMax, int _granularity) {
		int nTasks = _taskset.tasks.length;
		int nAperiodicTasks =  (int)Math.round(nTasks *_ratioAperiodic);
		int cnt = 0;
		
		while(cnt<nAperiodicTasks){
			// select a task at random
			int ID = (int)Math.floor(rand.nextDouble()*(nTasks)); // at random in range [0, nTasks)
			if (_taskset.tasks[ID].Type!=TaskType.Periodic) continue;
			
			// change a task to aperiodic task
			int min = (int)(_taskset.tasks[ID].Period * MINIMUM_RANGE) + _granularity;
			int max = _taskset.tasks[ID].Period * maxArrivalRange + _granularity;
			double randVal = rand.nextDouble()*(max-min) + min;
			_taskset.tasks[ID].MinIA = _taskset.tasks[ID].Period;
			_taskset.tasks[ID].MaxIA = (int)(Math.floor(randVal/_granularity) * _granularity);
			_taskset.tasks[ID].Type = TaskType.Aperiodic;
			cnt++;
		}
		return _taskset;
	}
	
	/**
	 * Assign priority to the task set according to rate-monotonic scheduling
	 *      The lower priority the task has, the higher priority it has
	 * @param _taskset
	 * @return
	 */
	public OPAMTaskSet assignPriority(OPAMTaskSet _taskset) {
		for(int p=0; p<_taskset.tasks.length; p++){
			int period = _taskset.tasks[p].Period;
			
			int priority = 0;
			for(int t=0; t<_taskset.tasks.length; t++){
				if (p==t) continue;
				int cPeriod = _taskset.tasks[t].Period;
				
				if (cPeriod<period) priority++;
				if (cPeriod==period && p>t) priority++;
			}
			_taskset.tasks[p].Priority = _taskset.tasks.length - priority -1;
		}
		return _taskset;
	}
	
	/**
	 * Assign priority to the task set according to engineer's desire
	 *      Basically it follows rate-monotonic only for Periodic tasks
	 *      For aperiodic tasks, this function makes their priorities lower.
	 * @param _taskset
	 * @return
	 */
	public OPAMTaskSet assignPriorityEngineer(OPAMTaskSet _taskset) {
		// assign priorities to the non aperiodic tasks
		int cntPeriod = 0;
		for(int p=0; p<_taskset.tasks.length; p++) {
			if (_taskset.tasks[p].Type == TaskType.Aperiodic) continue;
			int period = _taskset.tasks[p].Period;
			
			int priority = 0;
			for (int t = 0; t < _taskset.tasks.length; t++) {
				if (p==t) continue;
				if (_taskset.tasks[t].Type == TaskType.Aperiodic) continue;
				int cPeriod = _taskset.tasks[t].Period;
				
				if (cPeriod<period) priority++;
				if (cPeriod==period && p>t) priority++;
			}
			_taskset.tasks[p].Priority = _taskset.tasks.length - priority -1;
			cntPeriod++;
		}
		
		// assign priorities to the aperiodic tasks
		for(int p=0; p<_taskset.tasks.length; p++){
			if (_taskset.tasks[p].Type != TaskType.Aperiodic) continue;
			int period = _taskset.tasks[p].Period;
			
			int priority = cntPeriod;
			for(int t=0; t<_taskset.tasks.length; t++){
				if (p==t) continue;
				if (_taskset.tasks[t].Type != TaskType.Aperiodic) continue;
				int cPeriod = _taskset.tasks[t].Period;
				
				if (cPeriod<period) priority++;
				if (cPeriod==period && p>t) priority++;
			}
			_taskset.tasks[p].Priority = _taskset.tasks.length - priority -1;
		}
		return _taskset;
	}
	
	//////////////////////////////////////////////////////////////////
	// Basic task generation functions
	//////////////////////////////////////////////////////////////////
	/**
	 * UUniFast algorithm to generate utilizations (It allows to generate total utilization < 1)
	 *      E. Bini and G. C. Buttazzo,
	 *      “Measuring the performance of schedulability tests,”
	 *      Real-Time Systems, vol. 30, no. 1–2, pp. 129–154, 2005.
	 * @param nTasks
	 * @param utilization
	 * @return
	 */
	public double[] UUniFast(int nTasks, double utilization){
		double sumU = utilization;
		double nextSumU = 0;
		double[] vecU = new double[nTasks];
		
		for (int x = 1; x < nTasks; x++) {
			nextSumU = sumU * Math.pow(rand.nextDouble(), 1.0 / (nTasks - x));
			vecU[x-1] = sumU - nextSumU;
			sumU = nextSumU;
		}
		vecU[nTasks-1] = sumU;
		return vecU;
	}
	
	/**
	 * Revised UUniFast algorithm to the multi-processor
	 * When a utilization of a task is over 1.0, the set of utilization is discarded and re-generates them again
	 *      R. I. Davis and A. Burns,
	 *      “Improved priority assignment for global fixed priority pre-emptive scheduling in multiprocessor
	 *      real-time systems,” Real-Time Systems, vol. 47, no. 1, pp. 1–40, 2011.
	 * @param nTasks
	 * @param utilization
	 * @return
	 */
	public double[] UUniFastDiscard(int nTasks, double utilization){
		double[] vecU = null;
		
		while(vecU==null){
			vecU = UUniFast(nTasks, utilization);
			boolean flag = true;
			for (int x=0; x<vecU.length; x++){
				if(vecU[x] >= 1.0) {flag=false; break;}
			}
			if(!flag) vecU = null;
		}
		return vecU;
	}
	
	/**
	 * generate Periods by log-uniform
	 * according to paper below
	 *      P. Emberson, R. Stafford, and R. I. Davis,
	 *      “Techniques for the synthesis of multiprocessor tasksets,”
	 *      Proceedings of the 1st International Workshop on Analysis Tools and Methodologies for Embedded and Real-time Systems (WATERS),
	 *      vol. 1, no. Waters, pp. 6–11, 2010.
	 * @param nTasks
	 * @param tMin: the order of magnitude
	 * @param tMax: the order of magnitude
	 * @return
	 */
	public int[] generatePeriods(int nTasks, int tMin, int tMax, int granularity, boolean allowDuplicate){
		//generate periods
		double min = Math.log(tMin);
		double max = Math.log(tMax + granularity); //Math.log(tMax);
		int[] periods = new int[nTasks];
		
		int x = 0;
		while(x<nTasks){
			// log distribution in range [log(min), log(max))  <- log(max) exclusive
			double randVal = rand.nextDouble() * (max-min) + min; // generate random value in [log(tMin), log(tMax+granularity))
			int val = (int)Math.floor(Math.exp(randVal) / granularity) * granularity;
			// Check periods contains the value;
			if (!allowDuplicate && checkDuplicate(periods, x, val)) continue;
			periods[x] = val;
			x++;
		}
		return periods;
	}
	
	public boolean checkDuplicate(int[] periods, int x, int val){
		boolean flag = false;
		for (int t = 0; t < x; t++) {
			if (periods[t] == val) flag = true;
		}
		return flag;
	}
	
	/**
	 * Generate periods among divisors between tMin and tMax according to log-uniform
	 * @param nTasks
	 * @param tMin
	 * @param tMax
	 * @param granularity
	 * @param allowDuplicate currently always allow duplicate periods
	 * @return
	 */
	public int[] generatePeriodsDivisors(int nTasks, int tMin, int tMax, int granularity, boolean allowDuplicate){
		int[] divisors = this.generateDivisors(MAX_SIM_TIME, tMin, tMax);
		
		//generate periods
		int[] periods = new int[nTasks];
		
		int x = 0;
		while(x<nTasks){
			// get random value of log-uniform in range of indexes of divisors
			double min = Math.log(1);
			double max = Math.log(divisors.length + 1); //Math.log(tMax);
			
			// log distribution in range [log(min), log(max))  <- log(max) exclusive
			double randVal = rand.nextDouble() * (max - min) + min; // generate random value in [log(tMin), log(tMax+granularity))
			int val = (int) Math.floor(Math.exp(randVal));
			periods[x] = divisors[val-1];
			x++;
		}
		return periods;
	}
	/**
	 * generate Periods by CDF based log-uniform
	 *     Select only divisors of the MAX_SIM_TIME in range [tMin, tMax]
	 *     the probability of maximum divisor is CDF from tMax to tMax + granularity
	 * @param nTasks
	 * @param tMin
	 * @param tMax
	 * @param granularity
	 * @param allowDuplicate
	 * @return
	 */
	public int[] generatePeriodsDivisorsCDF(int nTasks, int tMin, int tMax, int granularity, boolean allowDuplicate){
		int[] divisors = generateDivisors(MAX_SIM_TIME, tMin, tMax);
		double[] CDFs = this.generateCDFDivisors(divisors, tMin, tMax+granularity);
		
		//generate periods
		int[] periods = new int[nTasks];
		
		int x = 0;
		while(x<nTasks){
			double r = rand.nextDouble();
			int idx=0;
			for(int c=0; c<CDFs.length; c++){
				if ( CDFs[c] <= r) idx = c;
			}
			periods[x] = divisors[idx];
			x++;
		}
		return periods;
	}
	
	public double[] generateCDFDivisors(int[] divisors, int tMin, int tMax){
		double[] CDFs = new double[divisors.length];
		for (int x=0; x<divisors.length; x++){
			CDFs[x] = this.cdf(divisors[x], 1, tMax);
		}
		return CDFs;
	}
	
	/**
	 * get CDF value of x from CDF of log-uniform function
	 * @param x the value what you want to know its CDF from a
	 * @param a the minimum range of CDF of log-uniform
	 * @param b the maximum range of CDF of log-uniform
	 * @return
	 */
	public double cdf(int x, int a, int b){
		double value = x/(double)a;
		double base = b/(double)a;
		return Math.log(value)/Math.log(base);
		
	}

	/**
	 * generate WCET values
	 *      Note that when applying equation C=T*U,
	 *      the worst-case execution time is usually rounded to the nearest integer
	 *      which will affect the distribution of actual utilisations in the generated taskset.
	 *      Changing the unit of time to use larger numeric values will decrease this loss of accuracy.
	 * @param periods time periods for each task with regard to a time unit
	 * @param utilizations utilizations for each task (the length should be the same with periods
	 * @return
	 */
	public int[] generateWCETs(int[] periods, double[] utilizations){
		int[] wcets = new int[periods.length];
		for (int x = 0; x < periods.length; x++) {
			wcets[x] =(int) Math.round(periods[x] * utilizations[x]);
		}
		return wcets;
	}
	
	/**
	 * generate WCET values
	 *      Note that when applying equation C=T*U,
	 *      the worst-case execution time is usually rounded to the nearest integer
	 *      which will affect the distribution of actual utilisations in the generated taskset.
	 *      Changing the unit of time to use larger numeric values will decrease this loss of accuracy.
	 * @param periods time periods for each task with regard to a time unit
	 * @param utilizations utilizations for each task (the length should be the same with periods
	 * @return
	 */
	public double[] generateWCETsDouble(int[] periods, double[] utilizations){
		double[] wcets = new double[periods.length];
		for (int x = 0; x < periods.length; x++) {
			double WCET = Math.round(periods[x] * utilizations[x]);
			wcets[x]= WCET;
		}
		return wcets;
	}
	
	/**
	 * generate divisors of given value within given range
	 * @param num the number you want to get divisors
	 * @param min the min value of the given range
	 * @param max the max value of the given range
	 * @return
	 */
	public int[] generateDivisors(long num, int min, int max) {
		List<Integer> divisors = new ArrayList<>();
		for(int i = min; i<=max; i++){
			if(num % i ==0){
				divisors.add(i);
			}
		}
		int[] values = new int[divisors.size()];
		for (int x=0; x<divisors.size(); x++){
			values[x] = divisors.get(x);
		}
		return values;
	}
	
	//////////////////////////////////////////////////////////////////
	// Utilities
	//////////////////////////////////////////////////////////////////
	public String ListtoString(int[] list, String header){
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		sb.append(": [");
		for(int i=0; i<list.length; i++){
			sb.append(list[i]);
			sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public String ListtoString(double[] list, String header){
		StringBuilder sb = new StringBuilder();
		sb.append(header);
		sb.append(": [");
		for(int i=0; i<list.length; i++){
			sb.append(String.format("%.4f, ", list[i]));
		}
		sb.append("]");
		return sb.toString();
	}
	
	public double getUtilization(double[] Us) {
		double util = 0;
		for (double Ui : Us) {
			util += Ui;
		}
		return util;
	}
	public double getUtilization(int[] periods, int[] wcets) {
		double util = 0;
		for (int x=0; x<periods.length; x++) {
			util += (double)wcets[x]/periods[x];
		}
		return util;
	}
	public double getUtilization(int[] periods, double[] wcets) {
		double util = 0;
		for (int x=0; x<periods.length; x++) {
			util += wcets[x]/periods[x];
		}
		return util;
	}
	
	//////////////////////////////////////////////////////////////////
	// calculate lcm
	//////////////////////////////////////////////////////////////////

	/**
	 * Greatest Common Divisor for two int values
	 * @param _result
	 * @param _periodArray
	 * @return
	 */
	public static long gcd(long _result, long _periodArray) {
		while (_periodArray > 0) {
			long temp = _periodArray;
			_periodArray = _result % _periodArray; // % is remainder
			_result = temp;
		}
		return _result;
	}
	
	/**
	 * Least Common Multiple for two int numbers
	 * @param _result
	 * @param _periodArray
	 * @return
	 */
	public static long lcm(long _result, long _periodArray) {
		return _result * (_periodArray / gcd(_result, _periodArray));
	}
	
	/**
	 * Least Common Multiple for int arrays
	 *
	 * @param _periodArray
	 * @return
	 */
	public static long lcm(Long[] _periodArray) {
		long result = _periodArray[0];
		for (int i = 1; i < _periodArray.length; i++) result = lcm(result, _periodArray[i]);
		return result;
	}
	
}
