package lu.uni.svv.PriorityAssignment.utils;

public class ListUtil {
	
	public static double average(double[] list){
		double value = 0.0;
		for (int x = 0; x < list.length; x++)
			value += (list[x]/list.length);
		return value;
	}
	
	public static int averageIdx(double[] list){
		int idx = 0;
		double avg = average(list);
		double[] diff = new double[list.length];
		for (int x = 0; x < list.length; x++) {
			diff[x] = Math.abs(list[x] - avg);
		}
		double min = diff[0];
		for (int x = 1; x < diff.length; x++) {
			if (diff[x] < min) {
				min = diff[x];
				idx = x;
			}
		}
		return idx;
	}
	
	public static int maximumIdx(double[] list){
		int idx = 0;
		double value = list[0];
		for (int x = 1; x < list.length; x++) {
			if (list[x] > value) {
				value = list[x];
				idx = x;
			}
		}
		return idx;
	}
	
	public static int minimumIdx(double[] list){
		int idx = 0;
		double value = list[0];
		for (int x = 1; x < list.length; x++) {
			if (list[x] < value) {
				value = list[x];
				idx = x;
			}
		}
		return idx;
	}
}
