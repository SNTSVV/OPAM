package lu.uni.svv.PriorityAssignment.analysis;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.Point;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * ContributionIndicator (CI)
 * @param <S>
 */
public class ContributionIndicator<S extends Solution<?>> extends GenericIndicator<S> {

    public ContributionIndicator(String referenceParetoFrontFile) throws FileNotFoundException {
        super(referenceParetoFrontFile);
    }

    public ContributionIndicator(Front referenceParetoFront) {
        super(referenceParetoFront);
    }

    public Double evaluate(List<S> solutionList) {
        if (solutionList == null) {
            throw new JMetalException("The pareto front approximation is null");
        } else {
            return this.calculate(new ArrayFront(solutionList), this.referenceParetoFront);
        }
    }

    /**
     * Calculate the ratio of the solutions of a set (A) that are not dominated by any solution in the other set (B)
     * if all the solutions produced by the set (A) are dominated by the solutions produced by the set (B), then return 0
     * if all the solutions produced by the set (B) are dominated by the solutions produced by the set (A), then return 1
     * if both set (A) and (B) are the same, the function returns 0.5
     * @param A
     * @param B
     * @return
     */
    public double calculate(Front A, Front B) {
        List<Point> intersecFront = DominateUtils.getIntersectionFront(A, B);
        List<Point> someDomA = DominateUtils.getSomeDominateSolutions(A, B);
        List<Point> someDomB = DominateUtils.getSomeDominateSolutions(B, A);

        List<Point> exDomA = DominateUtils.getNotComparableSolutions(A, B);
        List<Point> exDomB = DominateUtils.getNotComparableSolutions(B, A);

        double upper = intersecFront.size()/2.0 + someDomA.size() + exDomA.size();
        double lower = intersecFront.size() + someDomA.size() + exDomA.size() +
                        someDomB.size() + exDomB.size();
        return upper / lower;
    }


    public String getName() {
        return "CI";
    }

    public String getDescription() {
        return "Contribution indicator";
    }

    public boolean isTheLowerTheIndicatorValueTheBetter() {
        return false;
    }
}
