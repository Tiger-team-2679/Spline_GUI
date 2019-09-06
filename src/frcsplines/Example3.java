package frcsplines;

import java.util.ArrayList;

public class Example3 {

    public static void main(String[] args) {

        /**
         * Example - how to use the spline operation on a randomly (manually) chosen control points.
         */

        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i<10; i++){
            points.add(new Point(Math.random()*i, i));
        }

        Spline spline = new BSpline(points);

        VelocitiesAdapter vs = new VelocitiesAdapter(spline, 15, 0.5);
        System.out.println(vs.getNumPoints());

        // Print velocities:
        for (int i : vs.iterate()){
            double[] ves = vs.getVelocities(i, 50);
            System.out.println("Right speed: " + ves[0]);
            System.out.println("Left speed: " + ves[1] + "\n");
        }

    }

}
