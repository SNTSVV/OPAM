package lu.uni.svv.PriorityAssignment.analysis;

import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DominateUtils {

    /**
     * Return a unique non-dominated solutsion in a front (remove duplicate solutions)
     * Assuming that the search to minimize objectives
     * a subset of solutions (A_unf) in the front (A) that
     *      the solutions in A_unf belong in the front A and
     *      the solutions in A_unf do weakly dominate the front A and
     *      for the different i and j, a solution a_j in A_unf should not weakly dominate a solution a_i in A_unf
     * @param front
     */
    public static List<Point> getUniqueSolutions(Front front){
        List<Point> uniques = new ArrayList<>();
        for (int j=0; j<front.getNumberOfPoints(); j++){
            Point a = front.getPoint(j);

            boolean flag = true;
            for (int i=0; i<uniques.size(); i++){
                if ( isWeaklyDominate(a, uniques.get(i)) ){
                    flag=false;
                    break;
                }
            }
            if(flag)   uniques.add(a);
        }
        return uniques;
    }


    /**
     * Get the set of solutions in A that are weakly dominated by some solution of B
     * Parameter types are the Front
     * @param frontA
     * @param frontB
     */
    public static List<Point> getSomeWeeklyDominatedSolutions(Front frontA, Front frontB){
        List<Point> ret = new ArrayList<>();

        // for all points A
        for(int x=0; x<frontA.getNumberOfPoints(); x++){
            Point a = frontA.getPoint(x);

            // checking conditions
            boolean flag = false;
            for(int y=0; y<frontB.getNumberOfPoints(); y++){
                Point b = frontB.getPoint(y);
                if (isWeaklyDominate(b, a)){
                    flag = true;
                    break;
                }
            }
            if (flag) ret.add(a);
        }
        return ret;
    }

    /**
     * Get the set of solutions in A that are weakly dominated by some solution of B
     * Parameter types are the List<Point>
     * @param frontA
     * @param frontB
     */
    public static List<Point> getSomeWeeklyDominatedSolutions(List<Point> frontA, List<Point> frontB){
        List<Point> ret = new ArrayList<>();

        // for all solutions in frontA
        for (Point a : frontA){
            // checking if solution a is dominated by any solution in frontB
            boolean flag = false;
            for(Point b: frontB){
                if (isWeaklyDominate(b, a)){
                    flag = true;
                    break;
                }
            }
            if (flag) ret.add(a);
        }
        return ret;
    }


    /**
     * Return a subset of solutions (A_unf) in the front (A) that
     *      the solutions in A_unf belong in the front A and
     *      the solutions in A_unf do weakly dominate the front A and
     *      for the different i and j, a solution a_j in A_unf should not weakly dominate a solution a_i in A_unf
     */
    public static List<Point> getNonDominateSolutions(Front front, Front ref){
        List<Point> ret = new ArrayList<>();
        for (int j=0; j<front.getNumberOfPoints(); j++){
            Point a = front.getPoint(j);

            boolean flag = true;
            for (int i=0; i<ref.getNumberOfPoints(); i++){
                if ( isDominate(ref.getPoint(i), a) ){
                    flag=false;
                    break;
                }
            }
            if(flag)   ret.add(a);
        }
        return ret;
    }


    /**
     * Return a subset of solutions in the front (A) that are non-dominated by solutions in the front ref
     */
    public static List<Point> getNonDominateSolutions(List<Point> front, List<Point> ref){
        List<Point> ret = new ArrayList<>();
        for (int j=0; j<front.size(); j++){
            Point a = front.get(j);

            boolean flag = true;
            for (int i=0; i<ref.size(); i++){
                if ( isDominate(ref.get(i), a) ){
                    flag=false;
                    break;
                }
            }
            if(flag)   ret.add(a);
        }
        return ret;
    }

    /**
     * Get the set of solutions in front A that are not comparable to solutions in front B
     * frontA_{\not\preceq \cap \not\succ frontB} =
     * @param frontA
     * @param frontB
     * @return
     */
    public static List<Point> getNotComparableSolutions(Front frontA, Front frontB){
        List<Point> C = getIntersectionFront(frontA, frontB);
        List<Point> W = getSomeDominateSolutions(frontA, frontB);
        List<Point> L = getSomeDominatedSolutions(frontA, frontB);

        List<Point> union = getUnionPoints(C, W);
        union = getUnionPoints(union, L);

        // Different set (frontA - union set)
        List<Point> unique = new ArrayList<>();
        for(int x=0; x<frontA.getNumberOfPoints(); x++){
            Point a = frontA.getPoint(x);
            boolean flag = true;
            for(Point u: union){
                if(isDuplicate(a, u)) flag = false;
            }
            if (flag) unique.add(a);
        }
        return unique;
    }

    public static boolean isDuplicate(Point a, Point b){
       for(int t=0; t<a.getDimension(); t++){
            if (a.getValue(t)!=b.getValue(t)) return false;
        }
        return true;
    }

    public static List<Point> getUnionPoints(List<Point> setA, List<Point> setB){
        List<Point> union = new ArrayList<>();
        for(Point a: setA){ union.add(a); }

        for(Point b: setB){
            boolean flag = true;
            for(Point u:union) {
                if (isDuplicate(b, u)){
                    flag=false;
                    break;
                }
            }
            // add point b if it is not included in the union set
            if (flag) union.add(b);
        }
        return union;
    }

    /**
     * Return a subset of solutions that exist in front A and front B
     */
    public static List<Point> getIntersectionFront(Front A, Front B){
        List<Point> intersection = new ArrayList<>();

        // looping front A
        for (int x=0; x<A.getNumberOfPoints(); x++){
            Point a = A.getPoint(x);

            // looping front B
            for (int y=0; y<B.getNumberOfPoints(); y++){
                Point b = B.getPoint(y);

                // check whether b is the same with a
                boolean flag = true;
                for(int i=0; i<a.getDimension(); i++){
                    if (a.getValue(i) != b.getValue(i)) {
                        flag = false;
                        break;
                    }
                }
                // add a to intersection if a is equal to b
                if (flag) {
                    intersection.add(a);
                    break;
                }
            }
        }
        return intersection;
    }

    /**
     * Get the set of solutions in A that are dominated by some solution of B
     * frontA_{\prec frontB}
     * @param frontA
     * @param frontB
     */
    public static List<Point> getSomeDominatedSolutions(Front frontA, Front frontB){
        List<Point> ret = new ArrayList<>();

        // for all points A
        for(int x=0; x<frontA.getNumberOfPoints(); x++){
            Point a = frontA.getPoint(x);

            // checking conditions
            boolean flag = false;
            for(int y=0; y<frontB.getNumberOfPoints(); y++){
                Point b = frontB.getPoint(y);
                if (isDominate(b, a)){
                    flag = true;
                    break;
                }
            }
            if (flag) ret.add(a);
        }
        return ret;
    }

    /**
     * Get the set of solutions in A that dominate some solution of B
     * frontA_{\prec frontB}
     * @param frontA
     * @param frontB
     */
    public static List<Point> getSomeDominateSolutions(Front frontA, Front frontB){
        List<Point> ret = new ArrayList<>();
        for(int x=0; x<frontA.getNumberOfPoints(); x++){
            Point a = frontA.getPoint(x);
            boolean flag = false;
            for(int y=0; y<frontB.getNumberOfPoints(); y++){
                Point b = frontB.getPoint(y);
                if (isDominate(a, b)){
                    flag=true;
                    break;
                }
            }
            if (flag) ret.add(a);
        }
        return ret;
    }

    /**
     * return TRUE whether the point a dominates b
     * This function assumes the minimization scenario (minimum value is higher value for each objective)
     * This function compares the values for each objective in both points whether a_i <= b_i, 1<=i<=m, m is the number of objectives
     * This function compares the dominance for each objective in the points a_i <= b_i, 1<=i<=m, m is the number of objectives
     * @param a
     * @param b
     * @return
     */
    public static boolean isDominate(Point a, Point b){
        boolean flag = isWeaklyDominate(a, b);
        if (!flag) return false;

        for (int i=0; i<a.getDimension(); i++){
            if (a.getValue(i) < b.getValue(i)){
                return true;
            }
        }
        return false;
    }

    /**
     * return TRUE whether the point a does weakly dominate b
     * This function assumes the minimization scenario (minimum value is higher value for each objective)
     * This function compares the dominance for each objective in the points (a_i <= b_i, 1<=i<=m, m is the number of objectives)
     * @param a
     * @param b
     * @return
     */
    public static boolean isWeaklyDominate(Point a, Point b){
        for (int i=0; i<a.getDimension(); i++){
            if (!(a.getValue(i) <= b.getValue(i))){
                return false;
            }
        }
        return true;
    }

}
