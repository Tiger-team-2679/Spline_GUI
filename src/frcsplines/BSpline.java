 package frcsplines;
/*
 * @(#)BSpline.java
 *
 * Copyright (c) 2003 Martin Krueger
 * Copyright (c) 2005 David Benson
 *
 */

import java.util.ArrayList;

 /**
  * Interpolates points given in the 2D plane. The resulting spline
  * is a function s: R -> R^2 with parameter t in [0,1].
  *
  * @author krueger
  */
 public class BSpline implements Spline{

     /**
      *  Array representing the relative proportion of the total distance
      *  of each point in the line ( i.e. first point is 0.0, end point is
      *  1.0, a point halfway on line is 0.5 ).
      */
     private double[] t;
     private BSplineTools splineX;
     private BSplineTools splineY;
     ArrayList<Point> points = new ArrayList<>();

     /**
      * Total length tracing the points on the spline
      */
     private double length;

     /**
      * Creates a new BSpline.
      * @param points
      */
     public BSpline(ArrayList<Point> points) {
         this.points = points;
         double[] x = new double[points.size()];
         double[] y = new double[points.size()];

         for(int i = 0; i< points.size(); i++) {
             x[i] = points.get(i).getX();
             y[i] = points.get(i).getY();
         }

         init(x, y);
     }

     /**
      * Creates a new BSpline.
      * @param x
      * @param y
      */
     public BSpline(double[] x, double[] y) {
         init(x, y);
     }

     private void init(double[] x, double[] y) {
         if (x.length != y.length) {
             throw new IllegalArgumentException("Arrays must have the same length.");
         }

         if (x.length < 2) {
             throw new IllegalArgumentException("Spline edges must have at least two points.");
         }

         t = new double[x.length];
         t[0] = 0.0; // start point is always 0.0

         // Calculate the partial proportions of each section between each set
         // of points and the total length of sum of all sections
         for (int i = 1; i < t.length; i++) {
             double lx = x[i] - x[i-1];
             double ly = y[i] - y[i-1];
             // If either diff is zero there is no point performing the square root
             if ( 0.0 == lx ) {
                 t[i] = Math.abs(ly);
             } else if ( 0.0 == ly ) {
                 t[i] = Math.abs(lx);
             } else {
                 t[i] = Math.sqrt(lx*lx+ly*ly);
             }

             length += t[i];
             t[i] += t[i-1];
         }

         for(int i = 1; i< (t.length)-1; i++) {
             t[i] = t[i] / length;
         }

         t[(t.length)-1] = 1.0; // end point is always 1.0

         splineX = new BSplineTools(t, x);
         splineY = new BSplineTools(t, y);
     }

     /**
      * @param t 0 <= t <= 1
      */
     public double[] getPoint(double t) {
         double[] result = new double[2];
         result[0] = splineX.getValue(t);
         result[1] = splineY.getValue(t);

         return result;
     }

     /**
      * Used to check the correctness of this spline
      */
     public boolean checkValues() {
         return (splineX.checkValues() && splineY.checkValues());
     }

     public double getDx(double t) {
         return splineX.getDx(t);
     }

     public double getDy(double t) {
         return splineY.getDx(t);
     }

     public BSplineTools getSplineX() {
         return splineX;
     }

     public BSplineTools getSplineY() {
         return splineY;
     }

     public double getLength() {
         return length;
     }

     @Override
     public ArrayList<Point> getPoints() {
         return points;
     }

     @Override
     public double interpolate_X(double percent) {
         return getPoint(percent)[0];
     }

     @Override
     public double interpolate_Y(double percent) {
         return getPoint(percent)[1];
     }
 }
