//package Navigation;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.util.LinkedList;
//import java.util.Scanner;
//
//import EV3.FileAccess;
//import EV3.MoveTank;
//import EV3.Wait;
//import Tools.RunsMenu;
//
//public class DeadReckoning extends Thread{
//
//	// Members:
//	static int width = 75;
//	static int height = 92;
//	public static final double wheelDiameter = 81.6;
//	static int x = 100;
//	static int y = 100;
//	static double theta = Math.toRadians(0);
//
//	public static double vr = 0;
//	public static double vl = 10;
//
//	static rectangle robot = new rectangle();
//
//	public static final double time = 1; // In seconds.
//	static LinkedList<rectangle> rects = new LinkedList<rectangle>();
//
//
//	public static void calculatePosition() {
//		double s = (vr+vl)/2;
//		theta = (vr-vl)/width + theta;
//		x = (int)(s*Math.cos(theta) + x);
//		y = (int)(s*Math.sin(theta) + y);
//		robot.setBounds(x-width/2, y-height/2, x+width/2, y+height/2);
//	}
//
//	public static void downloadData() {
//
//		read();
//
//		FileAccess f = new FileAccess("obstacles");
//		f.delete();
//		f.create();
//
//		f.write(rects.size());
//
//		for(int i = 0; i<rects.size(); i++) {
//			f.write(rects.get(i).x1);
//			f.write(rects.get(i).y1);
//			f.write(rects.get(i).x2);
//			f.write(rects.get(i).y2);
//		}
//
//		f.close();
//
//	}
//
//	public static void read() {
//
//		File obstacles = new File("C:\\\\Users\\\\OWNER\\\\git\\\\repository\\\\FLL 2018-2019\\\\Data\\\\obstacles");
//
//		try {
//			Scanner obstaclesSacn = new Scanner(obstacles);
//
//
//			while(obstaclesSacn.hasNext()) {
//				rects.add(new rectangle(Integer.parseInt(obstaclesSacn.next()), Integer.parseInt(obstaclesSacn.next()),
//						Integer.parseInt(obstaclesSacn.next()), Integer.parseInt(obstaclesSacn.next())));
//			}
//
//			obstaclesSacn.close();
//
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	public static void setObstacles() {
//
//		FileAccess f = new FileAccess("obstacles");
//
//		int size = (int)f.readNumeric();
//
//		for(int i = 0; i<size; i++) {
//			rects.add(new rectangle((int)f.readNumeric(), (int)f.readNumeric(), (int)f.readNumeric(), (int)f.readNumeric()));
//		}
//
//		f.close();
//
//	}
//
//	public void measure() {
//
//		double P = (Math.PI*wheelDiameter)/360;
//		vl = (MoveTank.leftMotor.getTachoCount()/time)*P*0.5; // pixels per time.
//		vr = (MoveTank.rightMotor.getTachoCount()/time)*P*0.5; // pixels per time.
//
//		calculatePosition();
//
//		MoveTank.leftMotor.resetTachoCount();
//		MoveTank.rightMotor.resetTachoCount();
//
//	}
//
//	@Override
//	public void run() {
//		while(RunsMenu.active) {
//			measure();
//			Wait.time((int)(time*1000));
//
//		}
//	}
//}
