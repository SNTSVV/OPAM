package lu.uni.svv.PriorityAssignment.task;


public class Task {
	public int		    ID;				// Task Identification (Foreign key refers ID of TaskDescriptor)
	public int		    ExecutionID;	// Task's execution ID
	public int		    ExecutionTime;	// Worst-Case Execution Time
	public int		    ArrivedTime;	// time at which a task arrived in the ready queue
	public int		    StartedTime;	// time at which a task starts its execution
	public int		    FinishedTime;	// time at which a task finishes its execution
	public int		    RemainTime;		// remain time to execute
	public int		    Deadline;		// ArrivedTime + Deadline == deadline for this task
	public int		    Priority;		// Fixed Priority
	public TaskSeverity Severity;   // Hard or Soft deadline
	public TaskState    State;		    // Hard or Soft deadline
	public int          StateTime;		// State changed Time
	
	public Task(int _id, int _exID, int _execTime, int _arrivalTime, int _deadline, int _priority, TaskSeverity _severity) {
		ID				= _id;
		ExecutionID     = _exID;
		ExecutionTime	= _execTime;		
		ArrivedTime 	= _arrivalTime;
		StartedTime		= 0;
		FinishedTime	= 0;
		RemainTime		= _execTime;
		Deadline 		= _deadline;
		Priority		= _priority;
		Severity		= _severity;
		State           = TaskState.Idle;
		StateTime       = _arrivalTime;
	}
	
	@Override
	public String toString(){
		return String.format("{ID:%d (%d), exID:%d, arrival:%d, started:%d, ended:%d, remain:%d}", ID, Priority, ExecutionID, ArrivedTime, StartedTime, FinishedTime, RemainTime);
	}
	
	public int updateTaskState(TaskState newState, int timelapsed){
		if (newState==TaskState.Running && this.ExecutionTime==this.RemainTime)
			StartedTime = timelapsed;
			
		if (newState==TaskState.Idle)
			FinishedTime = timelapsed;
		
		int oldTimelapsed = StateTime;
		State = newState;
		StateTime = timelapsed;
		return oldTimelapsed;
	}
	
}
