package lu.uni.svv.PriorityAssignment.analysis;

import junit.framework.TestCase;
import org.uma.jmetal.qualityindicator.impl.GenerationalDistance;
import org.uma.jmetal.util.front.imp.ArrayFront;
import org.uma.jmetal.util.point.PointSolution;
import java.util.ArrayList;
import java.util.List;


public class IndicatorsTest extends TestCase {

    public PointSolution makePointSolution(int a, int b){
        // put point info
        PointSolution sol = new PointSolution(2);
        sol.setObjective(0, a);
        sol.setObjective(1, b);
        return sol;
    }

    public void testEvaluateGDP() {
        List<PointSolution> A = new ArrayList<PointSolution>();
        A.add(makePointSolution(2,5));

        List<PointSolution> B = new ArrayList<PointSolution>();
        B.add(makePointSolution(3,9));

        //reference pareto front
        List<PointSolution> ref = new ArrayList<PointSolution>();
        ref.add(makePointSolution(1,0));
        ref.add(makePointSolution(0,10));
        ArrayFront refFront = new ArrayFront(ref);

        //calculate GD+
        GenerationalDistance<PointSolution> gd = new GenerationalDistance<>(refFront);
        double dist = gd.evaluate(A);
        System.out.printf("%s for A: %.5f\n", gd.getName(), dist*dist);

        dist = gd.evaluate(B);
        System.out.printf("%s for B: %.5f\n\n", gd.getName(), dist*dist);

        //calculate GD+
        GenerationalDistancePlus<PointSolution> gdp = new GenerationalDistancePlus<>(refFront);
        dist = gdp.evaluate(A);
        System.out.printf("%s for A: %.5f\n", gdp.getName(), dist*dist);

        dist = gdp.evaluate(B);
        System.out.printf("%s for B: %.5f\n", gdp.getName(), dist*dist);

    }

    public void testEvaluateCI() {
        List<PointSolution> A = new ArrayList<PointSolution>();
        A.add(makePointSolution(4,2));
        A.add(makePointSolution(3,3));
        A.add(makePointSolution(4,2));
        A.add(makePointSolution(3,3));

        List<PointSolution> B = new ArrayList<PointSolution>();
        B.add(makePointSolution(5,1));
        B.add(makePointSolution(4,2));
        B.add(makePointSolution(3,3));
        B.add(makePointSolution(2,4));
        B.add(makePointSolution(1,5));

        List<PointSolution> C = new ArrayList<PointSolution>();
        C.add(makePointSolution(4,3));
        C.add(makePointSolution(2,5));

        List<PointSolution> D = new ArrayList<PointSolution>();
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));

        List<PointSolution> E = new ArrayList<PointSolution>();
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));

        List<PointSolution> F = new ArrayList<PointSolution>();
        F.add(makePointSolution(6,2));
        F.add(makePointSolution(5,3));
        F.add(makePointSolution(4,4));

        //reference pareto front
        List<PointSolution> ref = new ArrayList<PointSolution>();
        ref.add(makePointSolution(5,1));
        ref.add(makePointSolution(4,2));
        ref.add(makePointSolution(3,3));
        ref.add(makePointSolution(2,4));
        ref.add(makePointSolution(1,5));
        ArrayFront refFront = new ArrayFront(ref);

        //calculate CI
        ContributionIndicator<PointSolution> contributionIndicator = new ContributionIndicator<>(refFront);
        System.out.printf("%s for A: %.5f\n", contributionIndicator.getName(), contributionIndicator.evaluate(A));
        System.out.printf("%s for B: %.5f\n", contributionIndicator.getName(), contributionIndicator.evaluate(B));
        System.out.printf("%s for C: %.5f\n", contributionIndicator.getName(), contributionIndicator.evaluate(C));
        System.out.printf("%s for D: %.5f\n", contributionIndicator.getName(), contributionIndicator.evaluate(D));
        System.out.printf("%s for E: %.5f\n", contributionIndicator.getName(), contributionIndicator.evaluate(E));
        System.out.printf("%s for F: %.5f\n", contributionIndicator.getName(), contributionIndicator.evaluate(F));


        //calculate CI
        ArrayFront test = new ArrayFront(C);
        contributionIndicator = new ContributionIndicator<>(test);
        System.out.printf("\n%s for ref/C: %.5f\n", contributionIndicator.getName(), contributionIndicator.evaluate(ref));
    }


    public void testEvaluateC() {
        List<PointSolution> A = new ArrayList<PointSolution>();
        A.add(makePointSolution(4,2));
        A.add(makePointSolution(3,3));
        A.add(makePointSolution(4,2));
        A.add(makePointSolution(3,3));

        List<PointSolution> B = new ArrayList<PointSolution>();
        B.add(makePointSolution(5,1));
        B.add(makePointSolution(4,2));
        B.add(makePointSolution(3,3));
        B.add(makePointSolution(2,4));
        B.add(makePointSolution(1,5));

        List<PointSolution> C = new ArrayList<PointSolution>();
        C.add(makePointSolution(4,3));
        C.add(makePointSolution(2,5));

        List<PointSolution> D = new ArrayList<PointSolution>();
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));

        List<PointSolution> E = new ArrayList<PointSolution>();
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));

        List<PointSolution> F = new ArrayList<PointSolution>();
        F.add(makePointSolution(6,2));
        F.add(makePointSolution(5,3));
        F.add(makePointSolution(4,4));

        //reference pareto front
        List<PointSolution> ref = new ArrayList<PointSolution>();
        ref.add(makePointSolution(5,1));
        ref.add(makePointSolution(4,2));
        ref.add(makePointSolution(3,3));
        ref.add(makePointSolution(3,3));
        ref.add(makePointSolution(2,4));
        ref.add(makePointSolution(1,5));
        ArrayFront refFront = new ArrayFront(ref);

        //calculate CS
        CoverageIndicator<PointSolution> qi = new CoverageIndicator<>(refFront, false);
        System.out.printf("%s for A: %.5f\n", qi.getName(), qi.evaluate(A));
        System.out.printf("%s for B: %.5f\n", qi.getName(), qi.evaluate(B));
        System.out.printf("%s for C: %.5f\n", qi.getName(), qi.evaluate(C));
        System.out.printf("%s for D: %.5f\n", qi.getName(), qi.evaluate(D));
        System.out.printf("%s for E: %.5f\n", qi.getName(), qi.evaluate(E));
        System.out.printf("%s for F: %.5f\n", qi.getName(), qi.evaluate(F));


        //calculate CS reverse
        ArrayFront test = new ArrayFront(C);
        qi = new CoverageIndicator<>(test, false);
        System.out.printf("\n%s for ref/C: %.5f\n", qi.getName(), qi.evaluate(ref));
    }


    public void testEvaluateUNFS() {
        List<PointSolution> A = new ArrayList<PointSolution>();
        A.add(makePointSolution(4,2));
        A.add(makePointSolution(3,3));
        A.add(makePointSolution(4,2));
        A.add(makePointSolution(3,3));

        List<PointSolution> B = new ArrayList<PointSolution>();
        B.add(makePointSolution(5,1));
        B.add(makePointSolution(4,2));
        B.add(makePointSolution(3,3));
        B.add(makePointSolution(2,4));
        B.add(makePointSolution(1,5));

        List<PointSolution> C = new ArrayList<PointSolution>();
        C.add(makePointSolution(4,3));
        C.add(makePointSolution(2,5));

        List<PointSolution> D = new ArrayList<PointSolution>();
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));

        List<PointSolution> E = new ArrayList<PointSolution>();
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));

        List<PointSolution> F = new ArrayList<PointSolution>();
        F.add(makePointSolution(6,2));
        F.add(makePointSolution(5,3));
        F.add(makePointSolution(4,4));

        //reference pareto front
        List<PointSolution> ref = new ArrayList<PointSolution>();
        ref.add(makePointSolution(5,1));
        ref.add(makePointSolution(4,2));
        ref.add(makePointSolution(3,3));
        ref.add(makePointSolution(2,4));
        ref.add(makePointSolution(1,5));
        ArrayFront refFront = new ArrayFront(ref);

        UniqueNondominatedFrontRatio<PointSolution> unfr = new UniqueNondominatedFrontRatio<>(refFront);
        System.out.printf("%s for A: %.5f\n", unfr.getName(), unfr.evaluate(A));
        System.out.printf("%s for B: %.5f\n", unfr.getName(), unfr.evaluate(B));
        System.out.printf("%s for C: %.5f\n", unfr.getName(), unfr.evaluate(C));
        System.out.printf("%s for D: %.5f\n", unfr.getName(), unfr.evaluate(D));
        System.out.printf("%s for E: %.5f\n", unfr.getName(), unfr.evaluate(E));
        System.out.printf("%s for F: %.5f\n", unfr.getName(), unfr.evaluate(F));

    }

    public void testEvaluateSpacing() {
        List<PointSolution> A = new ArrayList<PointSolution>();
        A.add(makePointSolution(3,3));
        A.add(makePointSolution(3,3));
        A.add(makePointSolution(3,3));
        A.add(makePointSolution(3,3));

        List<PointSolution> B = new ArrayList<PointSolution>();
        B.add(makePointSolution(5,1));
        B.add(makePointSolution(4,2));
        B.add(makePointSolution(3,3));
        B.add(makePointSolution(2,4));
        B.add(makePointSolution(1,5));

        List<PointSolution> C = new ArrayList<PointSolution>();
        C.add(makePointSolution(1,10));
        C.add(makePointSolution(2,9));
        C.add(makePointSolution(7,7));
        C.add(makePointSolution(8,8));
        C.add(makePointSolution(10,3));

        List<PointSolution> D = new ArrayList<PointSolution>();
        D.add(makePointSolution(1,10));
        D.add(makePointSolution(2,9));
        D.add(makePointSolution(7,7));
        D.add(makePointSolution(8,8));
        D.add(makePointSolution(10,3));

        Spacing<PointSolution> sp = new Spacing<>();
        System.out.printf("%s for A: %.5f\n", sp.getName(), sp.evaluate(A));
        System.out.printf("%s for B: %.5f\n", sp.getName(), sp.evaluate(B));
        System.out.printf("%s for C: %.5f\n", sp.getName(), sp.evaluate(C));
    }

    public void testDominate() {
        List<PointSolution> D = new ArrayList<PointSolution>();
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));
        D.add(makePointSolution(3,3));

        List<PointSolution> E = new ArrayList<PointSolution>();
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));
        E.add(makePointSolution(4,4));


        //reference pareto front
        List<PointSolution> ref = new ArrayList<PointSolution>();
        ref.add(makePointSolution(5,1));
        ref.add(makePointSolution(4,2));
        ref.add(makePointSolution(3,3));
        ref.add(makePointSolution(2,4));
        ref.add(makePointSolution(1,5));
        ArrayFront refFront = new ArrayFront(ref);

        //convert to ArrayFront from the List of PointSolutions
        ArrayFront dFront = new ArrayFront(D);
        ArrayFront eFront = new ArrayFront(E);

        System.out.printf("a, b in ref isDominent: %s\n", DominateUtils.isDominate(refFront.getPoint(0), refFront.getPoint(1))?"true":"false");
        System.out.printf("D-E isDominent        : %s\n", DominateUtils.isDominate(dFront.getPoint(0), eFront.getPoint(0))?"true":"false");
        System.out.printf("D-D isDominent        : %s\n", DominateUtils.isDominate(dFront.getPoint(0), dFront.getPoint(0))?"true":"false");
        System.out.printf("D-D isWeaklyDominent  : %s\n", DominateUtils.isWeaklyDominate(dFront.getPoint(0), dFront.getPoint(0))?"true":"false");
    }
}