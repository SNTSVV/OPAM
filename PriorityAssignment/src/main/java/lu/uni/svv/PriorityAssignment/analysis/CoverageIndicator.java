package lu.uni.svv.PriorityAssignment.analysis;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.Point;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Coverage Indicator (known as C or CS )
 * C(A, B) gauges the proportion of solutions of B that are weakly dominated by at least one solution of A
 * @param <S>
 */
public class CoverageIndicator<S extends Solution<?>> extends GenericIndicator<S> {
    boolean isUnique = false;
    public CoverageIndicator(String referenceParetoFrontFile, boolean considerUnique) throws FileNotFoundException {
        super(referenceParetoFrontFile);
        isUnique = considerUnique;
    }

    public CoverageIndicator(Front referenceParetoFront, boolean considerUnique) {
        super(referenceParetoFront);
        isUnique = considerUnique;
    }

    public Double evaluate(List<S> solutionList) {
        if (solutionList == null) {
            throw new JMetalException("The pareto front approximation is null");
        } else {
            if (this.isUnique){
                return this.calculateUnique(new ArrayFront(solutionList), this.referenceParetoFront);
            }
            else {
                return this.calculate(new ArrayFront(solutionList), this.referenceParetoFront);
            }
        }
    }

    /**
     * Calculate the proportion of solutions of B that are weakly dominated by at least one solution of A
     * @param A
     * @param B
     * @return
     */
    public double calculate(Front A, Front B) {
        List<Point> dominated = DominateUtils.getSomeWeeklyDominatedSolutions(B, A);
        return dominated.size() / (double)B.getNumberOfPoints();
    }

    public double calculateUnique(Front A, Front B) {
        List<Point> uniqueA = DominateUtils.getUniqueSolutions(A);
        List<Point> uniqueB = DominateUtils.getUniqueSolutions(B);

        List<Point> dominated = DominateUtils.getSomeWeeklyDominatedSolutions(uniqueB, uniqueA);
        return dominated.size() / (double)uniqueB.size();
    }




    public String getName() {
        return "CS";
    }

    public String getDescription() {
        return "CS indicator";
    }

    public boolean isTheLowerTheIndicatorValueTheBetter() {
        return false;
    }
}
