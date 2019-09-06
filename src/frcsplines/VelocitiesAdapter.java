package frcsplines;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;

public class VelocitiesAdapter {

    protected ArrayList<Point> rPoints = new ArrayList<>();
    protected ArrayList<Point> lPoints = new ArrayList<>();
    protected double[][] points;
    protected Spline spline;
    private double lengthOp;
    private double wheelDistance;


    /**
     * Constructor.
     * @param spline
     * @param wheelDistance
     * @param lengthOp The number to applied over the length.
     */
    public VelocitiesAdapter(Spline spline, double wheelDistance, double lengthOp){
        this.spline = spline;
        this.wheelDistance = wheelDistance;
        this.lengthOp = lengthOp;
        calculate_left_right_splines(wheelDistance, (int)(spline.getLength() * lengthOp));
    }

    /**
     * Constructor.
     * @param wheelDistance
     * @param lengthOp
     */
    public VelocitiesAdapter(double wheelDistance, double lengthOp){
        this.wheelDistance = wheelDistance;
        this.lengthOp = lengthOp;
    }

    /**
     * Update the spline.
     * @param spline
     */
    public void init(Spline spline){
        this.spline = spline;
        calculate_left_right_splines(wheelDistance, (int)(spline.getLength() * lengthOp));
    }

    /**
     * Calculates the distance in pxs between two given points.
     *
     * @param point1
     * @param point2
     * @return
     */
    public double calculate_distance_between_points(Point point1, Point point2) {
        double deltaX = point2.x - point1.x;
        double deltaY = point2.y - point1.y;
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    /**
     * Returns a specific amount of equally spaced points from the spline.
     * @param numberOfPoints
     * @return
     */
    private double[][] getSplinePoints(int numberOfPoints) {
        double[][] splinePoints = new double[numberOfPoints][2];
        for (double i = 0; i < numberOfPoints; i++) {
            splinePoints[(int) i][0] = spline.interpolate_X(i / numberOfPoints);
            splinePoints[(int) i][1] = spline.interpolate_Y(i / numberOfPoints);
        }
        return splinePoints;
    }

    /**
     * Calculates the splines for each motor, for a given distance between the wheel and the center of the robot.
     * @param distance
     * @param numberOfPoints
     */
    private  void calculate_left_right_splines(double distance, int numberOfPoints){
        double[][] splinePoints = getSplinePoints(numberOfPoints);
        points = splinePoints;
        rPoints = new ArrayList<>();
        lPoints = new ArrayList<>();
        for (int i = 0; i<splinePoints.length-1; i++){
            double m = (splinePoints[i+1][1] - splinePoints[i][1]) / (splinePoints[i+1][0] - splinePoints[i][0]); // (y2 - y1) / (x2 - x1)
            m = -1.0 / m;
            double delta = distance * (Math.sqrt(1.0 / (1 + Math.pow(m, 2))));
            double rx = splinePoints[i][0] + delta;
            double ry = splinePoints[i][1] + m * delta;
            double lx = splinePoints[i][0] - delta;
            double ly = splinePoints[i][1] - m * delta;
            if (i != 0 && !rightSide(rx, ry, distance)){
                lPoints.add(new Point(rx, ry));
                rPoints.add(new Point(lx, ly));
            }
            else{
                rPoints.add(new Point(rx, ry));
                lPoints.add(new Point(lx, ly));
            }
        }
    }

    /**
     * Checks if the calculated point is on the right side of the spline.
     * @param rx
     * @param ry
     * @param distance
     * @return
     */
    private boolean rightSide(double rx, double ry, double distance){
        return calculate_distance_between_points(new Point(rx, ry), rPoints.get(rPoints.size()-1)) < distance;
    }

    /**
     * Get each motor's desired velocity for every point on the spline.
     * @param wheelDistance Distance from the wheel to the center of the robot.
     * @param timeForStep Time for each step.
     * @return
     */
    private double[][] get_desired_velocities(double wheelDistance, double timeForStep){
        double[][] velocities = new double[rPoints.size()-1][2]; // 0 = right, 1 = left
        for (int i = 0; i<velocities.length; i++){
            velocities[i][0] = calculate_distance_between_points(rPoints.get(i), rPoints.get(i+1)) / timeForStep;
            velocities[i][1] = calculate_distance_between_points(lPoints.get(i), lPoints.get(i+1)) / timeForStep;
        }
        return velocities;
    }

    /**
     * Get desired velocities for left and right motors in a given index.
     * @param index
     * @param basicSpeed The desired robot speed for the specific point.
     * @return
     */
    public double[] getVelocities(int index, double basicSpeed){
        double dis = calculate_distance_between_points(new Point(points[index][0], points[index][1]),
                new Point(points[index+1][0], points[index+1][1]));
        double time = dis / basicSpeed;
        double rightV = calculate_distance_between_points(rPoints.get(index), rPoints.get(index+1)) / time;
        double leftV = calculate_distance_between_points(lPoints.get(index), lPoints.get(index+1)) / time;
        return new double[]{rightV, leftV}; // 0 = right, 1 = left
    }

    /**
     * @return Number of points in the spline (depends on the lambda parameter in the constructor.
     */
    public int getNumPoints() {
        return rPoints.size();
    }

    public void saveCSV(double[][] ves, String path){
        try {
            Formatter formatter = new Formatter(path + (path.endsWith(".csv")?"":".csv"));
            formatter.format("%s", "Right Speed, Left Speed\r\n");
            for (int i = 0; i<ves.length; i++) {
                formatter.format("%s", ves[i][0] + ", " + ves[i][1] + "\r\n");
            }
            formatter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int[] iterate(){
        int[] iterator = new int[getNumPoints()-1];
        for (int i = 0; i < iterator.length; i++) {
            iterator[i] = i;
        }
        return iterator;
    }
}
