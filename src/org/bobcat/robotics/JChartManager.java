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
				if (idx == 0) {
					RioLogger.debugLog("id distance,velocity " + sRec.getID() + " " + distance[recCtr][0] + ", " + velocity[recCtr][0]);
				}
			}
			elapsedTime[recCtr] = sRec.getElapsedTime(false);
			deltaTime[recCtr] = sRec.getDeltaTime(false);

			recCtr++;
		} while(true);
		nbrRecords = recCtr;

		// This is an example, for fun multiple charts so that they have approximate same range 
		// add left distance +  left velocity + left power
		//
		// You would replaced by the path calculated values for (X,Y)
		for (int point = 0; point < recCtr; point++) {
			double tot = power[point][0] * 275;
			tot += distance[point][0] * 1.31;
			tot += velocity[point][0] * 2.5;
			tot /= 100.0;
			lineData1.add(elapsedTime[point],tot);  // (X,Y)
		}
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
				"Record File",  
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
