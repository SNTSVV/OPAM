package lu.uni.svv.PriorityAssignment.analysis;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;
import org.uma.jmetal.util.point.Point;

import java.io.FileNotFoundException;
import java.util.List;


/**
 * Spacing indicator
 * The indicator assumes the minimization scenario in search
 * The lower the value, the better the uniformity
 * The indicator does not need reference Pareto front
 * This class implemented following
 *     Jason R. Schoott's Thesis (1995) "Fault Tolerant Design using Single and Multicriteria Genetic Algorithm Optimization"
 *     Li, M., & Yao, X. (2019) "Quality evaluation of solution sets in multiobjective optimisation: A survey". In ACM Computing Surveys (Vol. 52, Issue 2). https://doi.org/10.1145/3300148
 * @param <S>
 */
public class Spacing<S extends Solution<?>> extends GenericIndicator<S> {
    public Spacing() {
    }

    // Hide a Creator that takes reference Pareto front
    private Spacing(String referenceParetoFrontFile) throws FileNotFoundException {
        super(referenceParetoFrontFile);
    }

    // Hide a Creator that takes reference Pareto front
    private Spacing(Front referenceParetoFront) {
        super(referenceParetoFront);
    }

    public Double evaluate(List<S> solutionList) {
        if (solutionList == null) {
            throw new JMetalException("The pareto front approximation is null");
        }
        else if (solutionList.size()==1){
            return 0.0D; //throw new JMetalException("Not enough number of solutions in the pareto front");
        }
        else{
            return this.calculate(new ArrayFront(solutionList));
        }
    }

    public double calculate(Front front) {
        // calculate distance for each point
        double[] dists = new double[front.getNumberOfPoints()];
        for(int i = 0; i < front.getNumberOfPoints(); ++i) {
            dists[i] = minDistance(i, front);
        }

        // calculate spacing
        double avgDist = average(dists);
        double sum = 0.0D;
        for(int i = 0; i < front.getNumberOfPoints(); ++i) {
            sum += Math.pow(avgDist - dists[i], 2);
        }
        double weight = 1.0D / (front.getNumberOfPoints()-1.0D);
        return Math.sqrt(weight * sum);
    }

    public double average(double[] items){
        double sum = 0.0D;
        for(int i = 0; i < items.length; ++i) {
            sum += items[i];
        }
        return sum/items.length;
    }

    public double minDistance(int index, Front A){
        Point given = A.getPoint(index);
        // calculate min distance
        double minDist = Double.MAX_VALUE;
        for(int x = 0; x < A.getNumberOfPoints(); ++x) {
            if (index==x) continue;   // to prevent every minDist equal to 0
            double dist = manhattanDistance(given, A.getPoint(x));
            minDist = Math.min(minDist, dist);
        }
        return minDist;
    }

    public double manhattanDistance(Point a, Point b) {
        double dist = 0.0D;
        for (int j = 0; j < a.getDimension(); ++j) {
            dist += Math.abs(a.getValue(j) - b.getValue(j));
        }
        return dist;
    }

    public String getName() {
        return "SP";
    }

    public String getDescription() {
        return "Spacing indicator";
    }

    public boolean isTheLowerTheIndicatorValueTheBetter() {
        return true;
    }
}
