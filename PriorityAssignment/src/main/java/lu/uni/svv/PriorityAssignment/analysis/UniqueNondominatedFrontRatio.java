package lu.uni.svv.PriorityAssignment.analysis;

import java.io.FileNotFoundException;
import java.util.List;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.Point;


public class UniqueNondominatedFrontRatio<S extends Solution<?>> extends GenericIndicator<S> {
    private double pow;

    public UniqueNondominatedFrontRatio() {
        this.pow = 2.0D;
    }

    public UniqueNondominatedFrontRatio(String referenceParetoFrontFile, double p) throws FileNotFoundException {
        super(referenceParetoFrontFile);
        this.pow = 2.0D;
        this.pow = p;
    }

    public UniqueNondominatedFrontRatio(String referenceParetoFrontFile) throws FileNotFoundException {
        this(referenceParetoFrontFile, 2.0D);
    }

    public UniqueNondominatedFrontRatio(Front referenceParetoFront) {
        super(referenceParetoFront);
        this.pow = 2.0D;
    }

    public Double evaluate(List<S> solutionList) {
        if (solutionList == null) {
            throw new JMetalException("The pareto front approximation is null");
        } else {
            return this.calculateRatio(new ArrayFront(solutionList), this.referenceParetoFront);
        }
    }

    public double calculateRatio(Front front, Front referenceFront) {

        List<Point> uniqueFront = DominateUtils.getUniqueSolutions(front);
        List<Point> uniqueReferenceFront = DominateUtils.getUniqueSolutions(referenceFront);

        List<Point> exFront = DominateUtils.getNonDominateSolutions(uniqueFront, uniqueReferenceFront);

        return exFront.size() / (double)uniqueReferenceFront.size();
    }


    public String getName() {
        return "UNFR";
    }

    public String getDescription() {
        return "Unique Non-dominated Front Ratio quality indicator";
    }

    public boolean isTheLowerTheIndicatorValueTheBetter() {
        return false;
    }
}
