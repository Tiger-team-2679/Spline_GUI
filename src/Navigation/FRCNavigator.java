package Navigation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

public class FRCNavigator extends JPanel implements MouseListener, MouseMotionListener {

    // Parameters:
    String imagePath = "_intoOrbit.jpg";
    String splineType = "Spline2D";

    // Members:
    Spline spline;
    Image map = getImage(imagePath);
    int mapWidth;
    int mapHeight;
    ArrayList<Point> points = new ArrayList<>();

    /**
     * Constructor.
     */
    public FRCNavigator() {
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
    }

    public void reinitializeSpline(ArrayList<Point> points) {
        switch (splineType) {
            case "Spline2D":
                spline = new Spline2D(points);
                break;
            case "HermiteSpline":
                try {
                    spline = new HermiteSpline(points);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Unresolved splineType! Available: Spline2D, HermiteSpline");
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
            URL imageURL = GUI.class.getResource(path);
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
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setStroke(new BasicStroke(4));
        for (int i = 1; i<points.size(); i++) {
            g2d.setColor(Color.red);
            g2d.drawLine((int) points.get(i - 1).getX(), (int) points.get(i - 1).getY(), (int) points.get(i).getX(), (int) points.get(i).getY());
        }

        for (int i = 0; i<points.size(); i++){
            g2d.setColor(Color.black);
            g2d.fillOval((int)points.get(i).x-10, (int)points.get(i).y-10, 20, 20);
        }

        if (spline != null) {
            Polygon p = new Polygon();
            for (double i = 0; i<=1; i+=0.0001){
                p.addPoint((int)spline.interpolate_X(i), (int)spline.interpolate_Y(i));
            }
            g2d.setColor(new Color(100, 200, 0));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawPolyline(p.xpoints, p.ypoints, p.npoints);
        }

    }

    private int inRange(MouseEvent e){
        for (Point p : points){
            if(calculate_distance_between_points(p, new Point(e.getX(), e.getY())) <= 10){
                return points.indexOf(p);
            }
        }
        return -1;
    }

    private double calculate_distance_between_points(Point point1, Point point2) {
        double deltaX = point2.x - point1.x;
        double deltaY = point2.y - point1.y;
        return Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaY, 2));
    }

    private double[][] getSplinePoints(int numberOfPoints){
        double[][] splinePoints = new double[numberOfPoints][2];
        for (double i = 0; i<numberOfPoints; i++){
            splinePoints[(int)i][0] = spline.interpolate_X(i / numberOfPoints);
            splinePoints[(int)i][1] = spline.interpolate_Y(i / numberOfPoints);
        }
        return splinePoints;
    }

    /**
     * Main.
     *
     * @param args
     */
    public static void main(String[] args) {
            new FRCNavigator();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    int draggingPoint = -1;

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

        draggingPoint = inRange(mouseEvent);

        if(SwingUtilities.isRightMouseButton(mouseEvent) && draggingPoint != -1){
            points.remove(draggingPoint);
            reinitializeSpline(points);
        }

        else if (points.isEmpty()){
            for (int i : new int[2]) points.add(new Point(mouseEvent.getX(), mouseEvent.getY()));
            reinitializeSpline(points);
        }

        else if (draggingPoint == -1){
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

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        if(draggingPoint != -1){
            points.set(draggingPoint, new Point(mouseEvent.getX(), mouseEvent.getY()));
        }
        reinitializeSpline(points);
        repaint();
    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }
}
