package lu.uni.svv.PriorityAssignment.analysis;


import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.util.distance.PointDistance;

public class EuclidianDistanceBySuperior implements PointDistance {
    public EuclidianDistanceBySuperior() {
    }

    public double compute(Point a, Point b) {
        if (a == null) {
            throw new JMetalException("The first point is null");
        } else if (b == null) {
            throw new JMetalException("The second point is null");
        } else if (a.getDimension() != b.getDimension()) {
            throw new JMetalException("The dimensions of the points are different: " + a.getDimension() + ", " + b.getDimension());
        } else {
            double distance = 0.0D;

            for(int i = 0; i < a.getDimension(); ++i) {
                distance += Math.pow(Math.max(a.getValue(i) - b.getValue(i), 0), 2.0D);
            }

            return Math.sqrt(distance);
        }
    }
}