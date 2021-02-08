package lu.uni.svv.PriorityAssignment.scheduler;

import lu.uni.svv.PriorityAssignment.utils.Settings;


public class ScheduleCalculator {
	Schedule[][] schedules;
	int[] targets;

	public ScheduleCalculator(Schedule[][] _schedules, int[] _targets){
		schedules = _schedules;
		targets = _targets;
	}
	
	private boolean inTargets(int taskID){
		if (targets==null) return true;
		if (targets.length==0) return true;
		
		for (int k=0; k<targets.length; k++){
			if (taskID == targets[k]) return true;
		}
		return false;
	}
	
	/**
	 * fitness function
	 * return a sum of all distance of e-d margins in all tasks
	 * @return
	 */
	public double distanceMargin(boolean reverse){
		double result = 0.0;
		double direction = 1.0;
		if (reverse) direction = -1.0;
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			if (!inTargets(t+1)) continue;
			
			// executions
			for (int e = 0; e < schedules[t].length; e++) {
				Schedule schedule = schedules[t][e];
				if (schedule == null) {
					System.out.println(String.format("schedule is null on Task%d exec %d in distanceMargin", t, e));
					continue;
				}
				int diff = schedule.finishedTime - schedule.deadline;
				
				if (Settings.MAX_OVER_DEADLINE!=0 && diff > Settings.MAX_OVER_DEADLINE){
					diff = Settings.MAX_OVER_DEADLINE;
				}
				result += direction * Math.pow(Settings.FD_BASE, diff/Settings.FD_EXPONENT);
			}
		}

		if (Double.isInfinite(result))
			result = Double.MAX_VALUE;


		return result;
	}
	
	/**
	 * print each bin of e-d values for a solution
	 */
	public void checkBins(){

		int size = 100;
		int[] cnt = new int[size+1];
		int[] cntM = new int[size+1];
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			if (!inTargets(t+1)) continue;
			
			// executions
			for (int e = 0; e < schedules[t].length; e++) {
				Schedule schedule = schedules[t][e];
				int diff = schedule.finishedTime - schedule.deadline;
				
				int bin = diff/1000;
				if (bin<0){
					 if (bin<-size) bin = -size;
					 cntM[-bin] += 1;
				}
				else{
					if (bin>size) bin = size;
					cnt[bin] += 1;
				}
				if (bin==100){
					System.out.println("Task" + (t+1));
				}
			}
		}
		
		StringBuilder sb = new StringBuilder(5000);
		sb.append("CNT_M:");
		for(int i=0; i<=size; i++){
			sb.append(cntM[i]);
			sb.append(",");
		}
		sb.append("\n CNT :");
		for(int i=0; i<=size; i++){
			sb.append(cnt[i]);
			sb.append(",");
		}
		System.out.println(sb.toString());
	}
	
	/**
	 * get a number of deadline miss for all tasks
	 * @return
	 */
	public int checkDeadlineMiss(){
		int count = 0;
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			if (!inTargets(t+1)) continue;
			if (schedules[t]==null) continue;
			
			// executions
			for (int e = 0; e < schedules[t].length; e++) {
				Schedule schedule = schedules[t][e];
				if (schedule == null) {
					System.out.println(String.format("schedule is null on Task%d exec %d in checkDeadlineMiss", t, e));
					continue;
				}
				int diff = schedule.finishedTime - schedule.deadline;
				
				// Deadline missed
				if (diff > 0) {
					count += 1;
				}
			}
		}
		return count;
	}
	
	/**
	 * get string about information of deadline miss
	 * It returns a list of taskID, executionID, e-d as String
	 * @param head
	 * @return
	 */
	public String getDeadlineMiss(String head){
		StringBuilder sb = new StringBuilder();
		int count = 0;
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			if (!inTargets(t+1)) continue;
			if (schedules[t]==null) continue;
			
			// executions
			for (int e = 0; e < schedules[t].length; e++) {
				Schedule schedule = schedules[t][e];
				int diff = schedule.finishedTime - schedule.deadline;
				
				// Deadline missed
				if (diff > 0) {
					sb.append(head);
					sb.append(t+1);
					sb.append(",");
					sb.append(e);
					sb.append(",");
					sb.append(diff);
					sb.append("\n");
				}
			}
		}
		return sb.toString();
	}
	
	/**
	 * print schedules
	 */
	public void printSchedule(){
		// for each task
		for(int t=0; t<schedules.length; t++) {
			StringBuilder sb = new StringBuilder();
			if (schedules[t]==null) {
				sb.append("null");
			}
			else {
				Schedule[] schedule = schedules[t];
				
				sb.append("{ ");
				// for each execution
				for (int x = 0; x < schedule.length; x++) {
					if (x >= 5) {
						sb.append("... ");
						sb.append(schedule.length - 2);
						sb.append(" more");
						break;
					}
					sb.append(x);
					sb.append(":[");
					sb.append(schedule[x].arrivalTime);
					sb.append(", ");
					for (int a = 0; a < schedule[x].activatedNum; a++) {
						sb.append(schedule[x].startTime.get(a));
						sb.append("(");
						sb.append(schedule[x].endTime.get(a) - schedule[x].startTime.get(a));
						sb.append("), ");
					}
					sb.append("], ");
				}
				sb.append(" }");
			}
//			sb.append("\n");
			System.out.println("Task" + t + ": " + sb.toString());
		}
	}
	
	/**
	 * maximum e-d each task
	 * @return list of maximum e-d
	 */
	public int[] getMaximumExecutions(){
		int[] maximumMissed = new int[schedules.length];
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			int maxValue = Integer.MIN_VALUE;
			
			if (schedules[t]!=null) {
				for (int e = 0; e < schedules[t].length; e++) {
					Schedule schedule = schedules[t][e];
					int diff = schedule.finishedTime - schedule.deadline;
					
					if (maxValue < diff) {
						maxValue = diff;
					}
				}
			}
			maximumMissed[t] = maxValue;
		}
		return maximumMissed;
	}
	
	/**
	 * minimum e-d each task
	 * @return list of maximum e-d
	 */
	public int[] getMinimumExecutions(){
		int[] minimumMissed = new int[schedules.length];
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			int minValue = Integer.MAX_VALUE;
			// executions
			if (schedules[t]!=null) {
				for (int e = 0; e < schedules[t].length; e++) {
					Schedule schedule = schedules[t][e];
					int diff = schedule.finishedTime - schedule.deadline;
					
					if (minValue > diff) {
						minValue = diff;
					}
				}
			}
			minimumMissed[t] = minValue;
		}
		return minimumMissed;
	}
	
	/**
	 * return list of the number of deadline miss for each task
	 * @return list of the number of deadline miss
	 */
	public int[] getDeadlineMissList(){
		int[] counts = new int[schedules.length];
		
		int count = 0;
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			if (!inTargets(t+1)) continue;
			if (schedules[t] == null) continue;
			
			// executions
			for (int e = 0; e < schedules[t].length; e++) {
				Schedule schedule = schedules[t][e];
				int diff = schedule.finishedTime - schedule.deadline;
				
				// Deadline missed
				if (diff > 0) {
					counts[t] += 1;
				}
			}
		}
		return counts;
	}
	
	/**
	 * get distribution of E-D for each task
	 * @return list of deadline miss counts
	 */
	public CountMap[] getExecutionDistList(){
		
		CountMap[] counts = new CountMap[schedules.length];
		
		// tasks
		for (int t=0; t<schedules.length; t++) {
			if (!inTargets(t+1)) continue;
			if (schedules[t] == null) continue;
			
			counts[t] = new CountMap();
			
			// executions
			for (int e = 0; e < schedules[t].length; e++) {
				Schedule schedule = schedules[t][e];
				int diff = schedule.finishedTime - schedule.deadline;
				
				if (counts[t].containsKey(diff)){
					counts[t].put(diff, counts[t].get(diff)+1);
				}
				else{
					counts[t].put(diff, 1);
				}
			}
		}
		return counts;
	}
}


