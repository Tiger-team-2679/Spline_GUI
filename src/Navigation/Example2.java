package frcsplines;

public class Example2 {

    public static void main(String[] args) {

        /**
         * No graphics example to load the data from the saved file and get the motor's velocities.
         */

        FRCNavigator nav = new FRCNavigator(false);
        nav.load("default"); // Loads saved points.

        // Calculate the left-right motors velocities:
        double[][] vs = nav.get_desired_velocities(10.5, 500, 0, 0.1);

        // Print velocities:
        for (double[] d : vs){
            System.out.println("Right Speed: " + d[0]);
            System.out.println("Left Speed: " + d[1] + "\n");
        }

    }

}
