package lu.uni.svv.PriorityAssignment.priority;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.uma.jmetal.solution.impl.AbstractGenericSolution;
import lu.uni.svv.PriorityAssignment.utils.GAWriter;
import lu.uni.svv.PriorityAssignment.utils.Settings;


/**
 * Class Responsibility
 *  - A method to create a solution
 *  - A method to copy a solution
 * So, this class need to reference Problem object
 * @author jaekwon.lee
 */

@SuppressWarnings("serial")
public class PrioritySolution extends AbstractGenericSolution<Integer, PriorityProblem> {//implements Solution<Integer>{
	private static long UUID = 1L;
	public static void initUUID(){
		PrioritySolution.UUID = 1L;
	}
	
	public long ID = 0L;
	
	public PrioritySolution(PriorityProblem _problem, boolean isEmpty) {
		super(_problem);
	}
	/**
	 * Create solution following Testing problem
	 * @param _problem
	 */
	public PrioritySolution(PriorityProblem _problem)
	{
		super(_problem);
		ID = PrioritySolution.UUID++;
		
		List<Integer> randomSequence = new ArrayList(problem.getNumberOfVariables());
		
		int i;
		for(i = 0; i < problem.getNumberOfVariables(); ++i) {
			randomSequence.add(i);
		}
		
		Collections.shuffle(randomSequence);
		
		for(i = 0; i < this.getNumberOfVariables(); ++i) {
			this.setVariableValue(i, randomSequence.get(i));
		}
	}
	
	public PrioritySolution(PriorityProblem _problem, List<Integer> _priorities)
	{
		super(_problem);
		ID = PrioritySolution.UUID++;
		
		for(int i = 0; i < this.problem.getNumberOfVariables(); ++i) {
			this.setVariableValue(i, _priorities.get(i));
		}
	}
	
	public PrioritySolution(PriorityProblem _problem, Integer[] _priorities) {
		super(_problem);
		ID = PrioritySolution.UUID++;
		
		for (int i = 0; i < this.problem.getNumberOfVariables(); ++i) {
			this.setVariableValue(i, _priorities[i]);
		}
	}
	
	public PrioritySolution(PrioritySolution _solution) {
		super(_solution.problem);
		ID = _solution.ID;
		
		for (int i = 0; i < this.problem.getNumberOfVariables(); ++i) {
			this.setVariableValue(i, _solution.getVariableValue(i));
		}
	}
	
	/**
	 * return variables as String (JSON notation)
	 * @return
	 */
	public String getVariablesString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		
		int length = this.getNumberOfVariables();
		for (int x=0; x < length; x++) {
			sb.append(this.getVariableValue(x));
			if (x!=(length-1))
				sb.append(", ");
		}
		sb.append(" ]");
		
		return sb.toString();
	}
	
	@Override
	@SuppressWarnings("resource")
	public String getVariableValueString(int index) {
		return String.valueOf(getVariableValue(index));
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PrioritySolution{ ID(");
		sb.append(ID);
		sb.append("), ");
		sb.append(getVariablesString());
		sb.append(", fitness: [");
		sb.append(getObjective(0));
		sb.append(", ");
		sb.append(getObjective(1));
		sb.append("], ");
		if (attributes.containsKey("DeadlineMiss")) {
			sb.append("DM: [");
			int[] dms = (int[])attributes.get("DeadlineMiss");
			for(int i=0; i<dms.length; i++){
				sb.append(dms[i]);
				sb.append(",");
			}
			sb.append("], ");
		}
		sb.append(attributes);
		sb.append("}");
		return sb.toString();
	}
	
	/**
	 * store solution into file
	 * @param _filepath
	 */
	public void store(String _filepath){
		GAWriter writer = new GAWriter(_filepath, Level.FINE, null);
		writer.info(this.getVariablesString());
		writer.close();
	}

	
	/**
	 * copy of this solution
	 * all values of objectives are initialized by 0 (This means the solution is not evaluated)
	 */
	@Override
	public PrioritySolution copy() {
		return new PrioritySolution(this.problem, getVariables());
	}
	
	public PrioritySolution clone() {
		return new PrioritySolution(this);
	}
	
	
	@Override
	public Map<Object, Object> getAttributes() {
		return this.attributes;
	}
	
	public boolean hasAttribute(Object key){
		return this.attributes.containsKey(key);
	}
	/**
	 * Load solution from file (static function)
	 * @param _problem
	 * @param _filepath
	 * @return
	 */
	public static PrioritySolution loadFromJSON(PriorityProblem _problem, String _filepath){
		Integer[] variables = null;
		FileReader reader = null;
		
		try {
			reader = new FileReader(_filepath);
			JSONParser parser = new JSONParser();
			JSONArray json = (JSONArray) parser.parse(reader);
			
			variables = new Integer[json.size()];
			for (int i = 0; i < json.size(); i++) {
				variables[i] = ((Long)json.get(i)).intValue();
			}
		}
		catch (IOException | ParseException e){
			e.printStackTrace();
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return new PrioritySolution(_problem, variables);
	}
	
	@Override
	protected void finalize() throws Throwable{
		for (int i=0; i<this.getNumberOfVariables(); i++)
			this.setVariableValue(i, null);
		
		this.attributes.clear();
		this.attributes = null;
		super.finalize();
//		Utils.printMemory("delete "+this.ID);
	}
	

	
	////////////////////////////////////////////////////////////////////////
	// Exchange internal variables
	////////////////////////////////////////////////////////////////////////
	/**
	 * convert to arrays
	 * @return
	 */
	public Integer[] toArray(){
		Integer[] arr = new Integer[this.getNumberOfVariables()];
		
		for (int i=0; i<this.getNumberOfVariables(); i++)
			arr[i] = this.getVariableValue(i);
		
		return arr;
	}
	
	/**
	 * Convert solutions to list of arrivals
	 * @param solutions
	 * @return
	 */
	public static List<Integer[]> toArrays(List<PrioritySolution> solutions) {
		List<Integer[]> prioritySets = new ArrayList<>();
		for(PrioritySolution solution: solutions){
			prioritySets.add(solution.toArray());
		}
		return prioritySets;
	}
}
