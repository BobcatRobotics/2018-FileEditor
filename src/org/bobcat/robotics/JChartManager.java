package org.bobcat.robotics;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.usfirst.frc.team177.lib.RioLogger;
import org.usfirst.frc.team177.lib.SpeedFile;
import org.usfirst.frc.team177.lib.SpeedRecord;

public class JChartManager {
	private String fileName = null;
	private int nbrRecords = 0;
	private JFreeChart jChart = null;
	
	// Make up vars for constants, and some arrays to hold data from calculations
	private double eps = 0.000001;           // A small number to keep from dividing by zero
	private double xxmax = 648.0;
	private double xxmin = -648.0;
	private double yymax = 648.0;
	private double yymin = -648.0;
	private double Rmax = 1000000.0;
	private double Rmin = -1000000.0;
	private double deltadot;                 // delta between rddotf and lddotf
	private double wb = 26;                  // robot wheel base in inches
	private double xx[] = new double[10000]; // x position of robot in inches
	private double yy[] = new double[10000]; // y position of robot in inches
	private double w[] = new double[10000];  // angualr velocity of robot, calc'd from wheel velocity in rads/sec
	private double theta[] = new double[10000]; // angle of robot forward vector wrt x axis in rads
	private double iccx[] = new double[10000]; // x position of ICC in inches
	private double iccy[] = new double[10000]; // y position of ICC in inches
	private double R[] = new double[10000]; // signed distance from center of drive train to icc
	private double lddotf[] = new double[10000];  // forward diff calc of left vel [v(i) = (d(i+1) - d(i))/dt(i+1)]
	private double rddotf[] = new double[10000];  // forward diff calc of right vel

	private JChartManager() {
		super();
	}

	public  JChartManager(String fileName) {
		this();
		this.fileName = fileName;
	}
	
	public int getTotalRecords() {
		return nbrRecords;
	}
	public XYSeriesCollection getChartData(GraphElements type) {
		// line plot
		// X Axis = Time, Y Axis = [Speed, or Distance, or Velocity]
		XYSeries lineData1 = new XYSeries("Left");
		XYSeries lineData2 = new XYSeries("Right");

		SpeedFile sFile = new SpeedFile(fileName);
		sFile.readRecordingFile();
		int recCtr = 0;
		do {
			SpeedRecord sRec = sFile.getRawData(recCtr);
			if (sRec.getID() == SpeedRecord.EOF)  {
				break;
			}
			double [] distance = sFile.getDistance();
			double [] velocity = sFile.getVelocity();
			double [] powers = sFile.getPower();
			if (GraphElements.POWER == type) {
				lineData1.add(sRec.getElapsedTime(false),powers[0]);
				lineData2.add(sRec.getElapsedTime(false),powers[1]);
			} 
			if (GraphElements.DISTANCE == type) {
				lineData1.add(sRec.getElapsedTime(false),distance[0]);
				lineData2.add(sRec.getElapsedTime(false),distance[1]);
			} 
			if (GraphElements.VELOCITY == type) {
				lineData1.add(sRec.getElapsedTime(false),velocity[0]);
				lineData2.add(sRec.getElapsedTime(false),velocity[1]);
			}
			recCtr++;
		} while(true);
		nbrRecords = recCtr;
		XYSeriesCollection sColl = new XYSeriesCollection();
		sColl.addSeries(lineData1);
		sColl.addSeries(lineData2);
		return sColl;
	}

	public XYSeriesCollection getChartRobotPathData() {
		// line plot
		// X Axis = Time, Y Axis = [Speed, or Distance, or Velocity]
		XYSeries lineData1 = new XYSeries("Path");

		SpeedFile sFile = new SpeedFile(fileName);
		sFile.readRecordingFile();
		int nbrRecords = sFile.getNbrOfRows();
		RioLogger.debugLog("nbr Rows = " + nbrRecords);
		int recCtr = 0;
		double [][] distance = new double[nbrRecords][2];
		double [][] velocity = new double[nbrRecords][2];
		double [][] power = new double[nbrRecords][2];
		double [] deltaTime = new double[nbrRecords];
		double [] elapsedTime =  new double[nbrRecords]; // For Testing only 
		do {
			SpeedRecord sRec = sFile.getRawData(recCtr);
			if (sRec.getID() == SpeedRecord.EOF)  {
				break;
			}
			double [] dist = sFile.getDistance();
			double [] vel = sFile.getVelocity();
			double [] pow = sFile.getPower();
			for (int idx=0;idx < 2;idx++) {
				power[recCtr][idx] = pow[idx];
				distance[recCtr][idx] = dist[idx];
				velocity[recCtr][idx]  = vel[idx];
//				if (idx == 0) {
//					RioLogger.debugLog("id distance,velocity " + sRec.getID() + " " + distance[recCtr][0] + ", " + velocity[recCtr][0]);
//				}
			}
			elapsedTime[recCtr] = sRec.getElapsedTime(false);
			deltaTime[recCtr] = sRec.getDeltaTime(false);

			recCtr++;
		} while(true);
		nbrRecords = recCtr;

		// This is an example, for fun multiple charts so that they have approximate same range 
		// add left distance +  left velocity + left power
		//
		// Set the first point by hand, then loop over the rest of the points stopping 1 before the end.
		xx[0] = 0.0;
		yy[0] = 0.0;
		theta[0] = 1.571;
		lineData1.add(xx[0],yy[0]);  // (X,Y)
		for (int point = 0; point < recCtr-1; point++) {
//			lddotf[point] = (distance[point+1][0] - distance[point][0])/deltaTime[point+1];
//			rddotf[point] = (distance[point+1][1] - distance[point][1])/deltaTime[point+1];
			lddotf[point] = velocity[point][0];
			rddotf[point] = velocity[point][1];
			deltadot = rddotf[point] - lddotf[point];
			if ((deltadot < eps) && (deltadot > -eps)) {
				R[point] = 1000000.0;    // robot driving straight or stopped
				w[point] = lddotf[point]/1000000.0;          // w to allow the robot to progress straight
			} else {
				w[point] = (rddotf[point] - lddotf[point])/wb;
				R[point] = wb/2.0 * (lddotf[point] + rddotf[point])/deltadot; // robot curving
			}
			if (R[point] > Rmax) {R[point]=Rmax;}
			if (R[point] < Rmin) {R[point]=Rmin;}

			iccx[point] = xx[point] - R[point]*Math.sin(theta[point]);
			iccy[point] = yy[point] + R[point]*Math.cos(theta[point]);

			xx[point+1] = Math.cos(w[point]*deltaTime[point+1])*(xx[point] - iccx[point]) - Math.sin(w[point]*deltaTime[point+1])*(yy[point] - iccy[point]) + iccx[point];
			yy[point+1] = Math.sin(w[point]*deltaTime[point+1])*(xx[point] - iccx[point]) + Math.cos(w[point]*deltaTime[point+1])*(yy[point] - iccy[point]) + iccy[point];
			theta[point+1] = theta[point] + w[point]*deltaTime[point+1];
			
			// Keep xx and yy in the 'vicinity' of the field
			if (xx[point+1] < xxmin) {xx[point+1] = xxmin;}
			if (xx[point+1] > xxmax) {xx[point+1] = xxmax;}
			if (yy[point+1] < yymin) {yy[point+1] = yymin;}
			if (yy[point+1] > yymax) {yy[point+1] = yymax;}

			// Print some debugging info
			// if ((xx[point+1] < -25.0) && (xx[point+1] > -35.0)) {
			if (point < 300) {
				RioLogger.debugLog("pass="+point+" lddotf="+lddotf[point]+" rddotf="+rddotf[point]+" theta="+theta[point]+" iccx="+xx[point]+" iccy="+iccy[point]+" xx="+xx[point+1] + " yy="+yy[point+1]);
			}
			if (point < 200) {
			   lineData1.add(yy[point+1],-1.0*xx[point+1]);  // (X,Y)
			}
		}
		// Create intermediate vars in the last row just for completeness
		lddotf[recCtr] = lddotf[recCtr-1];
		rddotf[recCtr] = rddotf[recCtr-1];
		w[recCtr] = w[recCtr-1];
		R[recCtr] = R[recCtr-1];
		iccx[recCtr] = iccx[recCtr-1];
		iccy[recCtr] = iccy[recCtr-1];
		
		XYSeriesCollection sColl = new XYSeriesCollection();
		sColl.addSeries(lineData1);
		return sColl;
	}
	
	public JFreeChart initChart() {
		jChart = createJChart(GraphElements.POWER);
		return jChart;
	}
	
	public JFreeChart updateChart(GraphElements gElem) {
		jChart = createJChart(gElem);
		return jChart;
	}

	private JFreeChart createJChart(GraphElements gElem) {
		XYSeriesCollection data;
		if (GraphElements.ROBOTPATH == gElem) {
			data = getChartRobotPathData();
		} else {
			data = getChartData(gElem);
		}
		String yLegend = null;
		switch (gElem) {
			case POWER: yLegend = "Power"; break;
			case DISTANCE: yLegend = "Distance"; break;
			case VELOCITY: yLegend = "Velocity"; break;
			case ROBOTPATH: yLegend = "Robot Path"; break;
			case EMPTY:
			default:
				break;
		}
		JFreeChart newChart = ChartFactory.createXYLineChart( 
				null,  
				"Time",  
				yLegend,
				data,   
				PlotOrientation.VERTICAL, 
				false,  
				false,  
				false 
			); 
//	 	jChart.fireChartChanged();
//		jChart.addChangeListener(ChangeListener);

		return newChart;
	}
	
	// TODO :: Add list record functionality to getChartData(), getChartRobotPathData()
	//      :: XXXX Why read file twice
	private int MAX_ROWS_TO_DISPLAY = 1000;
	public List<String> listRecords() {
		List<String> recList = new ArrayList<String>();
		SpeedFile sFile = new SpeedFile(fileName);
		sFile.readRecordingFile();
		int recCtr = 0;
		do {
			SpeedRecord sRec = sFile.getRawData(recCtr);
			if (sRec.getID() == SpeedRecord.EOF)  {
				RioLogger.debugLog("listRecords Found EOF " + sRec.toString() /*+ " prev veloc " + prevVeloticy[0]*/);
					break;
			}
			recList.add(sRec.toString());
			recCtr++;
			if (recCtr > MAX_ROWS_TO_DISPLAY)
				break;
		} while(true);	
		return recList;
	}
	
//	public void test()
//	{
//	      final JFreeChart chart = createChart();
//	        final Display display = new Display();
//	        Shell shell = new Shell(display);
//	        shell.setSize(600, 300);
//	        shell.setLayout(new FillLayout());
//	        shell.setText("Test for jfreechart running with SWT");
//	        ChartComposite frame = new ChartComposite(shell, SWT.NONE, chart, true);
//	        frame.setDisplayToolTips(false);
//	        frame.setHorizontalAxisTrace(true);
//	        frame.setVerticalAxisTrace(true);
//	        shell.open();
//	        while (!shell.isDisposed()) {
//	            if (!display.readAndDispatch())
//	                display.sleep();
//	        }	
//	}

//	class  ChangeListener implements ChartChangeListener {
//		@Override
//		public void chartChanged(ChartChangeEvent arg0) {
//			// TODO Auto-generated method stub
//			
//		}	
//	}
}
