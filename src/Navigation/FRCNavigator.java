package frcsplines;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Scanner;

public class FRCNavigator extends JPanel implements MouseListener, MouseMotionListener {


    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////// P A R A M E T E R S : ///////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    String imagePath = "fieldImage.jpg"; ///////////////////////
    String[] splineType = {"BSpline"}; /////////////////////////
    Color[] splineColors = {Color.green}; //////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////

    // Members:
    Spline[] spline = new Spline[splineType.length];
    Image map = getImage(imagePath);
    int mapWidth;
    int mapHeight;
    ArrayList<Point> points = new ArrayList<>();
    ArrayList<Point> rPoints = new ArrayList<>();
    ArrayList<Point> lPoints = new ArrayList<>();
    boolean save = false;
    String savePath = "";


    /**
     * Constructor.
     */
    public FRCNavigator(boolean graphicalMode) {

        if (graphicalMode) {
            map.getWidth(this);
            map.getHeight(this);
            wait(1000);

            // Set JFrame
            JFrame frame = new JFrame("Tiger Team Navigator");
            frame.add(this);
            frame.setSize(map.getWidth(this), map.getHeight(this) + 30);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);

            // Add listeners:
            this.addMouseListener(this);
            this.addMouseMotionListener(this);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (save) {
                        save(savePath);
                    }
                }
            });
        }

    }

    /**
     * Sets save mode -> The program will automatically save the points when the window is closed.
     * @param save
     * @param path Can be passed with "default" to save the points locally in a default path (current directory),
     *             or null in case there's no need to save anything.
     *
     */
    public void saveAtEnd(boolean save, String path){
        this.save = save;
        this.savePath = path.equals("default")?"src/frcsplines/points.txt":path;
    }

    /**
     * Initializes the chosen spline with a new set of points.
     *
     * @param points
     */
    public void reinitializeSpline(ArrayList<Point> points) {
        for (int i = 0; i < spline.length; i++) {
            switch (splineType[i]) {
                case "BSpline":
                    spline[i] = new BSpline(points);
                    break;
                case "HermiteSpline":
                    try {
                        spline[i] = new HermiteSpline(points);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("Unresolved splineType! Available: BSpline, HermiteSpline");
            }
        }
    }

    /**
     * Wait for milliSeconds.
     *
     * @param milliSeconds
     */
    private void wait(int milliSeconds) {
        try {
            Thread.sleep(milliSeconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an Image object.
     *
     * @param path
     * @return
     */
    public Image getImage(String path) {
        Image tempImage = null;
        try {
            URL imageURL = FRCNavigator.class.getResource(path);
            tempImage = Toolkit.getDefaultToolkit().getImage(imageURL);
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
        return tempImage;
    }


    /**
     * Painter.
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.drawImage(map, 0, 0, this);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setStroke(new BasicStroke(4));
        for (int i = 1; i < points.size(); i++) {
            g2d.setColor(Color.red);
            g2d.drawLine((int) points.get(i - 1).getX(), (int) points.get(i - 1).getY(), (int) points.get(i).getX(), (int) points.get(i).getY());
        }

        for (int i = 0; i < points.size(); i++) {
            g2d.setColor(Color.black);
            g2d.fillOval((int) points.get(i).x - 10, (int) points.get(i).y - 10, 20, 20);
        }

        if (spline[0] != null) {
            for (int j = 0; j < spline.length; j++) {
                Polygon p = new Polygon();
                for (double i = 0; i <= 1; i += 0.0001) {
                    p.addPoint((int) spline[j].interpolate_X(i), (int) spline[j].interpolate_Y(i));
                }
                g2d.setColor(splineColors[j]);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawPolyline(p.xpoints, p.ypoints, p.npoints);
            }
        }

        if (points.size() > 1) {
            calculate_left_right_splines(15,  1000, 0);
            Polygon right = new Polygon();
            Polygon left = new Polygon();
            for (Point p : rPoints){
                right.addPoint((int)p.x, (int)p.y);
            }
            for (Point p : lPoints){
                left.addPoint((int)p.x, (int)p.y);
            }
            g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
            g2d.setColor(new Color(100, 100, 100));
            g2d.drawPolyline(right.xpoints, right.ypoints, right.npoints);
            g2d.setColor(new Color(150, 150, 150));
            g2d.drawPolyline(left.xpoints, left.ypoints, left.npoints);
        }

        // For a better design:
        if (!points.isEmpty()) {
            g2d.setColor(Color.BLACK);
            g2d.fillOval((int) points.get(0).x - 10, (int) points.get(0).y - 10, 20, 20);
        }
    }

    /**
     * Checks if the mouse is currently over one of the points.
     *
     * @param e
     * @return Index of the currently covered point, -1 if none are covered.
     */
    private int inRange(MouseEvent e) {
        for (Point p : points) {
            if (calculate_distance_between_points(p, new Point(e.getX(), e.getY())) <= 10) {
                return points.indexOf(p);
            }
        }
        return -1;
    }

    /**
     * Calculates the distance in pxs between two given points.
     *
     * @param point1
     * @param point2
     * @return
     */
    private double calculate_distance_between_points(Point point1, Point point2) {
        double deltaX = point2.x - point1.x;
        double deltaY = point2.y - point1.y;
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    /**
     * Returns a specific amount of equally spaced points from the spline.
     * @param numberOfPoints
     * @param splineIndex
     * @return
     */
    private double[][] getSplinePoints(int numberOfPoints, int splineIndex) {
        double[][] splinePoints = new double[numberOfPoints][2];
        for (double i = 0; i < numberOfPoints; i++) {
            splinePoints[(int) i][0] = spline[splineIndex].interpolate_X(i / numberOfPoints);
            splinePoints[(int) i][1] = spline[splineIndex].interpolate_Y(i / numberOfPoints);
        }
        return splinePoints;
    }

    /**
     * Calculates the splines for each motor, for a given distance between the wheel and the center of the robot.
     * @param distance
     * @param numberOfPoints
     * @param splineIndex
     */
    private void calculate_left_right_splines(double distance, int numberOfPoints, int splineIndex){
        double[][] splinePoints = getSplinePoints(numberOfPoints, splineIndex);
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
     * @param numberOfPoints Number of points to be taken form the spline.
     * @param splineIndex Which spline to use.
     * @param timeForStep Time for each step.
     * @return
     */
    public double[][] get_desired_velocities(double wheelDistance, int numberOfPoints, int splineIndex, double timeForStep){
        calculate_left_right_splines(wheelDistance, numberOfPoints, splineIndex);
        double[][] velocities = new double[rPoints.size()-1][2]; // 0 = right, 1 = left
        for (int i = 0; i<velocities.length; i++){
            velocities[i][0] = calculate_distance_between_points(rPoints.get(i), rPoints.get(i+1)) / timeForStep;
            velocities[i][1] = calculate_distance_between_points(lPoints.get(i), lPoints.get(i+1)) / timeForStep;
        }
        return velocities;
    }

    /**
     * Get the spline's length. As higher the numberOfPoints is, the length will become more accurate.
     * @param numberOfPoints
     * @param splineIndex Which spline to use.
     * @return
     */
    public double getSplineLength(int numberOfPoints, int splineIndex){
        double[][] ps = getSplinePoints(numberOfPoints, splineIndex);
        double length = 0;
        for (int i = 0; i<ps.length-1; i++){
            length += calculate_distance_between_points(new Point(ps[i][0], ps[i][1]), new Point(ps[i+1][0], ps[i+1][1]));
        }
        return length;
    }

    /**
     * Saves the points in a local txt file.
     * @param path
     */
    private void save(String path){
        try {
            Formatter formatter = new Formatter(path + (path.endsWith(".txt")?"":".txt"));
            for (Point p : points) {
                formatter.format("%s", p.x + "\r\n");
                formatter.format("%s", p.y + "\r\n");
            }
            formatter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the points from a local txt file.
     * @param path Can be passed with "default" to load the points from a default path (current directory).
     */
    public void load(String path){
        path = path.equals("default")?"src/frcsplines/points.txt":path;
        File file = new File(path + (path.endsWith(".txt")?"":".txt"));
        points = new ArrayList<>();
        try {
            Scanner sc = new Scanner(file);
            while(sc.hasNext()){
                points.add(new Point(Double.parseDouble(sc.next()), Double.parseDouble(sc.next())));
            }
            sc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        reinitializeSpline(points);
        repaint();
    }

    /**
     * Main.
     *
     * @param args
     */
    public static void main(String[] args) {
        new FRCNavigator(true);
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    int draggingPoint = -1;

    /**
     * Handles mouse pressed event.
     * @param mouseEvent
     */
    @Override
    public void mousePressed(MouseEvent mouseEvent) {

        draggingPoint = inRange(mouseEvent);

        if (SwingUtilities.isRightMouseButton(mouseEvent) && draggingPoint != -1) {
            points.remove(draggingPoint);
            reinitializeSpline(points);
        } else if (points.isEmpty()) {
            for (int i : new int[2]) points.add(new Point(mouseEvent.getX(), mouseEvent.getY()));
            reinitializeSpline(points);
        } else if (draggingPoint == -1) {
            points.add(new Point(mouseEvent.getX(), mouseEvent.getY()));
            reinitializeSpline(points);
        }

        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        draggingPoint = -1;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    /**
     * Handles mouse dragged event.
     * @param mouseEvent
     */
    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if (draggingPoint != -1) {
            points.set(draggingPoint, new Point(mouseEvent.getX(), mouseEvent.getY()));
        }
        reinitializeSpline(points);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }
}
