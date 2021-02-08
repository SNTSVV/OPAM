package lu.uni.svv.PriorityAssignment.scheduler;

import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;
import lu.uni.svv.PriorityAssignment.task.Task;
import lu.uni.svv.PriorityAssignment.task.TaskDescriptor;
import lu.uni.svv.PriorityAssignment.task.TaskState;
import lu.uni.svv.PriorityAssignment.utils.RandomGenerator;
import lu.uni.svv.PriorityAssignment.utils.Settings;

import java.awt.color.ICC_Profile;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Scheduling Policy
 * 	- We use Fixed Priority to schedule tasks.
 * 	- The two task has same priority, first arrived execution in the ready queue has higher priority to use CPU.
 *  -   In the other words, the execution that is arrived lately couldn't preempt the other executions already arrived in the ready queue.
 *
 * Deadline misses detection
 * 	- We count the number of deadline misses while the scheduling(by SIMULATION_TIME)
 * 	- And, if the ready queue has tasks after being done scheduling(after SIMULATION_TIME),
 * 			we will execute this scheduler by finishQuing all tasks in the ready queue.
 * @author jaekwon.lee
 *
 */
public class ScheduleVerify{
	
	protected Schedule[][]          schedules;
	protected TaskDescriptor[]      Tasks;
	protected int                   MaxTime;
	protected Integer[]             priorities;
	protected int[][]               timelines;
	protected Arrivals[]            arrivals;
	
	public ScheduleVerify(Schedule[][] _schedules, TaskDescriptor[] _tasks, Integer[] _priorities)
	{
		this(_schedules, _tasks, _priorities, null);
	}
	
	public ScheduleVerify(Schedule[][] _schedules, TaskDescriptor[] _tasks, Integer[] _priorities, Arrivals[] _arrivals)
	{
		schedules = _schedules;
		Tasks = _tasks;
		MaxTime = getMaximumExecutionTime();
		priorities = _priorities;
		arrivals = _arrivals;
		
		generateTimelines();
	}

	protected int getMaximumExecutionTime(){
		int max = 0;
		for(int x=0; x<schedules.length; x++){
			int idx = schedules[x].length-1;
			int val = schedules[x][idx].finishedTime;
			if(max<val) max = val;
		}
		return max;
	}
	
	/**
	 * generate timelines  (-2: ready state, 0: Idle, N: Running (cpu number) )
	 */
	protected void generateTimelines(){
		// generate timelines
		timelines = new int[schedules.length][];
		for (int x=0; x<schedules.length; x++){
			timelines[x] = new int[MaxTime];
		}
		
		// set ready state of task
		for (int x=0; x<schedules.length; x++) {
			for (int i = 0; i < schedules[x].length; i++) {
				Schedule schedule = schedules[x][i];
				for (int t = schedule.arrivalTime; t < schedule.finishedTime; t++) {
					timelines[x][t] = -2;
				}
			}
		}
		
		// set activated time of a task execution
		for (int x=0; x<schedules.length; x++) {
			for (int i = 0; i < schedules[x].length; i++) {
				Schedule schedule = schedules[x][i];
				
				for(int a=0; a<schedule.activatedNum; a++){
					int start = schedule.startTime.get(a);
					int end = schedule.endTime.get(a);
					int CPU = schedule.CPUs.get(a);
					
					for(int t=start; t<end; t++) {
						timelines[x][t] = (CPU==-1)? CPU: CPU+1;
					}
					
				}
			}
		}
	}
	

	/////////////////////////////////////////////////////////////////
	// Verification 6 steps
	/////////////////////////////////////////////////////////////////
	/**
	 * initialize scheduler
	 */
	public boolean verify(){
		try {
			verifyArrivalTime();
			verifyStartTime();
			verifyExecutionTime();
			verifyBlocking();
			verifyCPUExecution();
			// verifyTriggers();
		}
		catch(AssertionError | Exception e){
			System.out.println(e.getMessage());
		}
		return true;
	}
	
	/**
	 * Task arrivals verification
	 * @return
	 */
	public boolean verifyArrivalTime() {
		if (arrivals==null) return true;
		
		// to except for triggered tasks.
		boolean[] exceptions = TaskDescriptor.getArrivalExceptionTasks(Tasks);
		
		for(int tIDX=0; tIDX<schedules.length; tIDX++){
			if(exceptions[tIDX+1]) continue;
			
			if (schedules[tIDX].length!=arrivals[tIDX].size()){
				throw new AssertionError("Error to verify the number of executions on task " + (tIDX+1));
			}
			for(int e=0; e< schedules[tIDX].length; e++){
				if (schedules[tIDX][e].arrivalTime != arrivals[tIDX].get(e)){
					throw new AssertionError("Error to verify arrival time on " + (e+1) + " execution of task " + (tIDX+1));
				}
			}
		}
		return true;
	}
	
	/**
	 * Tasks which are higher priority than running tasks should be the blocked or running state
	 * When a task is in the ready state, only higher priority tasks then the task should be running.
	 * [Deprecated]
	 * @return
	 */
	public boolean verifyStartTime2() {
		
		for (int x = 0; x < timelines.length; x++) {
			ArrayList<Integer> list = getHigherPriorityTaskIdxs(Tasks[x].ID - 1);
			if (list.size() == 0) {
				for (int t = 0; t < timelines[x].length; t++) {
					if (timelines[x][t] == -2) return false;
				}
			} else {
				for (int t = 0; t < timelines[x].length; t++) {
					if (timelines[x][t] <= 0) continue;
					
					for (int taskIdx : list) {
						if (timelines[taskIdx][t] == -2) return false;  // if the higher priority tasks is ready state
					}
				}
			}
		}
		return true;
	}
	
	/**
	 * When a task is in the ready state, only higher priority tasks then the task should be running.
	 * @return
	 */
	public boolean verifyStartTime() throws Exception{
		// timeline
		ArrayList<Integer> list = new ArrayList<>();
		for (int t = 0; t < MaxTime; t++) {
			list.clear();
			
			int maxPriority = Integer.MAX_VALUE;
			for (int x = 0; x < timelines.length; x++) {
				if(timelines[x][t] == -2){
					if (priorities[x]<maxPriority) maxPriority = priorities[x];
				}
				else if(timelines[x][t]>0){
					list.add(priorities[x]);
				}
			}
			if (maxPriority == Integer.MAX_VALUE) continue;
			
			for(int priority:list) {
				if (priority > maxPriority)
					throw new AssertionError("Error to verify start time at "+t+" time.");
			}
		}
		
		return true;
	}
	
	
	protected ArrayList<Integer> getHigherPriorityTaskIdxs(int taskIdx){
		ArrayList<Integer> list = new ArrayList<>();
		int priorityT = priorities[taskIdx];
		for (int x=0; x<priorities.length; x++){
			if (priorities[x]<priorityT) list.add(x);
		}
		return list;
	}
	
	
	/**
	 * All task executions have the same execution time of their WCET
	 * @return
	 */
	public boolean verifyExecutionTime(){
		for (int x=0; x<schedules.length; x++) {
			for (int i = 0; i < schedules[x].length; i++) {
				if (Tasks[x].WCET != schedules[x][i].executedTime)
					throw new AssertionError("Error to verify execution time");
			}
		}
		return true;
	}
	
	/**
	 * Tasks are depends each other should not be executed at the same time unit
	 *
	 * @return
	 */
	public boolean verifyBlocking(){
		// find max number of resources;
		int maxResource = TaskDescriptor.getMaxNumResources(Tasks);
		
		// build resource relationship matrix by task index
		// Resource 1  => B, C
		// Resource 2  => A, D
		HashSet<Integer>[] resources = new HashSet[maxResource];
		for (int tIDX=0; tIDX<Tasks.length; tIDX++) {
			for (int d=0; d<Tasks[tIDX].Dependencies.length; d++){
				int rIDX = Tasks[tIDX].Dependencies[d]-1;
				if (resources[rIDX]==null)
					resources[rIDX] = new HashSet<>();
				resources[rIDX].add(tIDX);
			}
		}
		
		// check tasks that have dependency are executed at the same time unit
		for(int rIDX=0; rIDX<resources.length; rIDX++){
			if (resources[rIDX].size()==0) continue;
			
			for (int t=0; t<MaxTime; t++) {
				int cnt = 0;
				for (Integer tIDX : resources[rIDX]) {
					if (timelines[tIDX][t] > 0) cnt +=1;
				}
				if (cnt>1) throw new AssertionError("Error to verify exclusive access to resources");
			}
		}
		return true;
	}
	
	/**
	 * Task executions at one time unit should be less than the number of CPUs
	 * When a CPU is idle, no tasks are ready (some tasks can be blocked state)
	 * @return
	 */
	public boolean verifyCPUExecution(){
		for (int t=0; t<MaxTime; t++) {
			int cntRunning = 0;
			int cntReady = 0;
			for (int x=0; x<timelines.length; x++){
				if (timelines[x][t] > 0) cntRunning +=1;
				if (timelines[x][t] == -2) cntReady +=1;
			}
			if (cntRunning>Settings.N_CPUS) throw new AssertionError("Error to verify the number of CPU are executing");
			if (cntRunning==0 && cntReady>0) throw new AssertionError("Error to verify the CPU idle state");
		}
		return true;
	}
	
	public boolean verifyTriggers(){
		for (int x=0; x<schedules.length; x++) {
			for (int i = 0; i < schedules[x].length; i++) {
				if (Tasks[x].WCET == schedules[x][i].executedTime)
					throw new AssertionError("Error to verify triggers");
			}
		}
		return true;
	}
}