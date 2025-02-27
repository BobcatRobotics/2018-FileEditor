package org.bobcat.robotics;

import java.util.ArrayList;
import java.util.List;

import org.bobcat.robotics.EditData.Mode;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
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
	// SpeedFile - used in getChartData(), getRobotPathData()
	private SpeedFile speedFile = null;
	private List<String> recList = null;
	private boolean direction = true;

	private JChartManager() {
		super();
	}

	public JChartManager(String fileName) {
		this();
		this.fileName = fileName;
	}

	public int getTotalRecords() {
		return nbrRecords;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setDirection(boolean dir) {
		direction = dir;
	}

	public List<String> listRecords() {
		return recList;
	}

	public List<SpeedRecord> listSpeeds() {
		return speedFile.getSpeedFile();
	}
	
	public XYSeriesCollection getChartData(GraphElements type) {
		// line plot
		// X Axis = Time, Y Axis = [Speed, or Distance, or Velocity]
		XYSeries lineData1 = new XYSeries("Left");
		XYSeries lineData2 = new XYSeries("Right");

		recList = new ArrayList<String>();
		speedFile = new SpeedFile(fileName);
		speedFile.readRecordingFile();
		int recCtr = 0;
		do {
			SpeedRecord sRec = speedFile.getRawData(recCtr);
			if (sRec.getID() == SpeedRecord.EOF) {
				RioLogger.debugLog("getChartData() Found EOF " + sRec.toString());
				break;
			}
			recList.add(sRec.toString());

			double[] distance = speedFile.getDistance();
			double[] velocity = speedFile.getVelocity();
			double[] powers = speedFile.getPower();
			if (GraphElements.POWER == type) {
				lineData1.add(sRec.getElapsedTime(false), powers[0]);
				lineData2.add(sRec.getElapsedTime(false), powers[1]);
			}
			if (GraphElements.DISTANCE == type) {
				lineData1.add(sRec.getElapsedTime(false), distance[0]);
				lineData2.add(sRec.getElapsedTime(false), distance[1]);
			}
			if (GraphElements.VELOCITY == type) {
				lineData1.add(sRec.getElapsedTime(false), velocity[0]);
				lineData2.add(sRec.getElapsedTime(false), velocity[1]);
			}
			recCtr++;
		} while (true);
		nbrRecords = recCtr;
		XYSeriesCollection sColl = new XYSeriesCollection();
		sColl.addSeries(lineData1);
		sColl.addSeries(lineData2);
		return sColl;
	}

	public XYSeriesCollection getChartRobotPathData() {
		// Make up vars for constants, and some arrays to hold data from calculations
		double eps = 0.000001; // A small number to keep from dividing by zero
		double xxmax = 648.0;
		double xxmin = -648.0;
		double yymax = 648.0;
		double yymin = -648.0;
		double Rmax = 1000000.0;
		double Rmin = -1000000.0;
		double deltadot; // delta between rddotf and lddotf
		double wb = 26; // robot wheel base in inches
		double xx[] = new double[10000]; // x position of robot in inches
		double yy[] = new double[10000]; // y position of robot in inches
		double w[] = new double[10000]; // angualr velocity of robot, calc'd from wheel velocity in rads/sec
		double theta[] = new double[10000]; // angle of robot forward vector wrt x axis in rads
		double iccx[] = new double[10000]; // x position of ICC in inches
		double iccy[] = new double[10000]; // y position of ICC in inches
		double R[] = new double[10000]; // signed distance from center of drive train to icc
		double lddotf[] = new double[10000]; // forward diff calc of left vel [v(i) = (d(i+1) - d(i))/dt(i+1)]
		double rddotf[] = new double[10000]; // forward diff calc of right vel
		
		// line plot
		// X Axis = Time, Y Axis = [Speed, or Distance, or Velocity]
		XYSeries lineData1 = new XYSeries("Path");

		recList = new ArrayList<String>();
		speedFile = new SpeedFile(fileName);
		speedFile.readRecordingFile();
		int nbrRecords = speedFile.getNbrOfRows();
		RioLogger.debugLog("nbr Rows = " + nbrRecords);
		int recCtr = 0;
		double[][] distance = new double[nbrRecords][2];
		double[][] velocity = new double[nbrRecords][2];
		double[][] power = new double[nbrRecords][2];
		double[] deltaTime = new double[nbrRecords];
		double[] elapsedTime = new double[nbrRecords]; // For Testing only
		do {
			SpeedRecord sRec = speedFile.getRawData(recCtr);
			if (sRec.getID() == SpeedRecord.EOF) {
				RioLogger.debugLog("getChartRobotPathData() Found EOF "
						+ sRec.toString() /* + " prev veloc " + prevVeloticy[0] */);
				break;
			}
			recList.add(sRec.toString());

			double[] dist = speedFile.getDistance();
			double[] vel = speedFile.getVelocity();
			double[] pow = speedFile.getPower();
			for (int idx = 0; idx < 2; idx++) {
				power[recCtr][idx] = pow[idx];
				distance[recCtr][idx] = dist[idx];
				velocity[recCtr][idx] = vel[idx];
				// if (idx == 0) {
				// RioLogger.debugLog("id distance,velocity " + sRec.getID() + " " +
				// distance[recCtr][0] + ", " + velocity[recCtr][0]);
				// }
			}
			elapsedTime[recCtr] = sRec.getElapsedTime(false);
			deltaTime[recCtr] = sRec.getDeltaTime(false);

			recCtr++;
		} while (true);
		nbrRecords = recCtr;

		// This is an example, for fun multiple charts so that they have approximate
		// same range
		// add left distance + left velocity + left power
		//
		// Set the first point by hand, then loop over the rest of the points stopping 1
		// before the end.
		xx[0] = 0.0;
		yy[0] = 0.0;
		if (direction) {
			RioLogger.debugLog("setting theta to +1.571");
			theta[0] = 1.571;
		} else {
			RioLogger.debugLog("setting theta to -1.571");
			theta[0] = -1.571;
		}
		lineData1.add(yy[0], -1.0 * xx[0]); // (X,Y)
		for (int point = 0; point < recCtr - 1; point++) {
			// lddotf[point] = (distance[point+1][0] -
			// distance[point][0])/deltaTime[point+1];
			// rddotf[point] = (distance[point+1][1] -
			// distance[point][1])/deltaTime[point+1];
			lddotf[point] = velocity[point][0];
			rddotf[point] = velocity[point][1];
			deltadot = rddotf[point] - lddotf[point];
			if ((deltadot < eps) && (deltadot > -eps)) {
				R[point] = 1000000.0; // robot driving straight or stopped
				w[point] = lddotf[point] / 1000000.0; // and not turning
			} else {
				w[point] = (rddotf[point] - lddotf[point]) / wb;
				R[point] = wb / 2.0 * (lddotf[point] + rddotf[point]) / deltadot; // robot curving
			}
			if (R[point] > Rmax) {
				R[point] = Rmax;
			}
			if (R[point] < Rmin) {
				R[point] = Rmin;
			}

			iccx[point] = xx[point] - R[point] * Math.sin(theta[point]);
			iccy[point] = yy[point] + R[point] * Math.cos(theta[point]);

			xx[point + 1] = Math.cos(w[point] * deltaTime[point + 1]) * (xx[point] - iccx[point])
					- Math.sin(w[point] * deltaTime[point + 1]) * (yy[point] - iccy[point]) + iccx[point];
			yy[point + 1] = Math.sin(w[point] * deltaTime[point + 1]) * (xx[point] - iccx[point])
					+ Math.cos(w[point] * deltaTime[point + 1]) * (yy[point] - iccy[point]) + iccy[point];
			theta[point + 1] = theta[point] + w[point] * deltaTime[point + 1];

			// Keep xx and yy in the 'vicinity' of the field
			if (xx[point + 1] < xxmin) {
				xx[point + 1] = xxmin;
			}
			if (xx[point + 1] > xxmax) {
				xx[point + 1] = xxmax;
			}
			if (yy[point + 1] < yymin) {
				yy[point + 1] = yymin;
			}
			if (yy[point + 1] > yymax) {
				yy[point + 1] = yymax;
			}

			// Print some debugging info
			// if ((xx[point+1] < -25.0) && (xx[point+1] > -35.0)) {
			if (point < 300) {
				RioLogger.debugLog("pass=" + point + " lddotf=" + lddotf[point] + " rddotf=" + rddotf[point] + " theta="
						+ theta[point] + " iccx=" + xx[point] + " iccy=" + iccy[point] + " xx=" + xx[point + 1] + " yy="
						+ yy[point + 1]);
			}
			if (point < 200) {
				lineData1.add(yy[point + 1], -1.0 * xx[point + 1]); // (X,Y)
			}
		}
		// Create intermediate vars in the last row just for completeness
		lddotf[recCtr] = lddotf[recCtr - 1];
		rddotf[recCtr] = rddotf[recCtr - 1];
		w[recCtr] = w[recCtr - 1];
		R[recCtr] = R[recCtr - 1];
		iccx[recCtr] = iccx[recCtr - 1];
		iccy[recCtr] = iccy[recCtr - 1];

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
		String xLegend = "Time        Blue->Left   Red->Right";
		switch (gElem) {
		case POWER:
			yLegend = "Power";
			break;
		case DISTANCE:
			yLegend = "Distance";
			break;
		case VELOCITY:
			yLegend = "Velocity";
			break;
		case ROBOTPATH:
			yLegend = "X Point";
			xLegend = "Y Point";
			break;
		case EMPTY:
		default:
			break;
		}
		JFreeChart newChart = ChartFactory.createXYLineChart(null, xLegend, yLegend, data, PlotOrientation.VERTICAL,
				false, false, false);
		// jChart.fireChartChanged();
		// jChart.addChangeListener(ChangeListener);

		return newChart;
	}

	public boolean updateSpeedFile(Mode mode, boolean isPower, String inputFrom, String inputTo,String leftValue,String rightValue) {
		return speedFile.updateRecordingFile(mode, isPower, new Integer(inputFrom),
				new Integer(inputTo),new Double(leftValue),new Double(rightValue));
	}

	// TODO:: Remove when competition GrayHills are in sync,
	// changes made for correct velocity and distance 04-05-195
	// TODO:: XXXXXXXXXXX
	public boolean convertSpeedFile() {
		return speedFile.updategrayHillValue();
	}

	public void saveSpeedFile() {
		speedFile.saveFile();
	}

	public boolean updateSpeedFileValue(int row, int col, String value) {
		if (value == null || value.length() == 0)
			value = "0.0";
		RioLogger.debugLog("**** JChartManager.updateCmdFileValue updateSpeedFileValue() " + row + " " + col + " " + value);		
		
		return speedFile.updateValue(row,col,new Double(value));
	}
}
