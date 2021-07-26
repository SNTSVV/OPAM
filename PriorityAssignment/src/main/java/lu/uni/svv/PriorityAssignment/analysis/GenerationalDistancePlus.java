package lu.uni.svv.PriorityAssignment.analysis;

import java.io.FileNotFoundException;
import java.util.List;

import org.uma.jmetal.qualityindicator.impl.GenericIndicator;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.front.util.FrontUtils;


public class GenerationalDistancePlus<S extends Solution<?>> extends GenericIndicator<S> {
    private double pow;

    public GenerationalDistancePlus() {
        this.pow = 2.0D;
    }

    public GenerationalDistancePlus(String referenceParetoFrontFile, double p) throws FileNotFoundException {
        super(referenceParetoFrontFile);
        this.pow = 2.0D;
        this.pow = p;
    }

    public GenerationalDistancePlus(String referenceParetoFrontFile) throws FileNotFoundException {
        this(referenceParetoFrontFile, 2.0D);
    }

    public GenerationalDistancePlus(Front referenceParetoFront) {
        super(referenceParetoFront);
        this.pow = 2.0D;
    }

    public Double evaluate(List<S> solutionList) {
        if (solutionList == null) {
            throw new JMetalException("The pareto front approximation is null");
        } else {
            return this.generationalDistance(new ArrayFront(solutionList), this.referenceParetoFront);
        }
    }

    public double generationalDistance(Front front, Front referenceFront) {
        EuclidianDistanceBySuperior distanceMeasure = new EuclidianDistanceBySuperior();

        double sum = 0.0D;

        for(int i = 0; i < front.getNumberOfPoints(); ++i) {
            sum += Math.pow(FrontUtils.distanceToClosestPoint(front.getPoint(i), referenceFront, distanceMeasure), this.pow);
        }

        sum = Math.pow(sum, 1.0D / this.pow);
        return sum / (double)front.getNumberOfPoints();
    }

    public String getName() {
        return "GD+";
    }

    public String getDescription() {
        return "Generational distance plus quality indicator";
    }

    public boolean isTheLowerTheIndicatorValueTheBetter() {
        return true;
    }
}
