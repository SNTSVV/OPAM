package lu.uni.svv.PriorityAssignment.utils;

import lu.uni.svv.PriorityAssignment.arrivals.Arrivals;

import java.util.Formatter;
import java.util.logging.Level;

public class StoreManger {
	
	public static void storePriority(Integer[] priorities, String filename){
		// convert priority
		StringBuilder sb = new StringBuilder();
		sb.append("[ ");
		for (int x=0; x < priorities.length; x++) {
			sb.append(priorities[x]);
			if (x!=(priorities.length-1))
				sb.append(", ");
		}
		sb.append(" ]");
		
		// store priority
		GAWriter writer = new GAWriter(filename, Level.FINE, null);
		writer.info(sb.toString());
		writer.close();
	}
	
	public static void storeArrivals(Arrivals[] arrivals, String filename){
		GAWriter writer = new GAWriter(filename, Level.FINE, null);
		StringBuilder sb = new StringBuilder();
		Formatter fmt = new Formatter(sb);
		
		sb.append("[\n");
		for (int x=0; x < arrivals.length; x++) {
			sb.append("\t");
			
			fmt.format("[");
			Arrivals item = arrivals[x];
			for(int i=0; i< item.size(); i++) {
				fmt.format("%d", item.get(i));
				if ( item.size() > (i+1) )
					sb.append(",");
			}
			fmt.format("]");
			if (x!=(arrivals.length-1))
				sb.append(",");
			sb.append("\n");
		}
		sb.append("]");
		
		writer.info(sb.toString());
		writer.close();
	}
}
