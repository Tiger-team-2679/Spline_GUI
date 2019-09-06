package frcsplines;

import java.util.ArrayList;

public interface Spline {

    public double interpolate_X(double percent);

    public double interpolate_Y(double percent);

    public double getLength();

    public ArrayList<Point> getPoints();

}
