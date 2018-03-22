package org.bobcat.robotics;

import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import org.usfirst.frc.team177.lib.SpeedFile;
import org.usfirst.frc.team177.lib.SpeedRecord;

public class JChartManager {
	private String fileName = null;
	private JFreeChart jChart = null;

	private JChartManager() {
		super();
	}

	public  JChartManager(String fileName) {
		this();
		this.fileName = fileName;
	}
	
	public XYSeriesCollection getChartData(GraphElements type) {
		// line plot
		// X Axis = Time, Y Axis = [Speed, or Distance, or Velocity]
		XYSeries lineData1 = new XYSeries("Plot 1");
		XYSeries lineData2 = new XYSeries("Plot 2");

		SpeedFile sFile = new SpeedFile(fileName);
		sFile.readRecordingFile();
		int recCtr = 0;
		do {
			SpeedRecord sRec = sFile.getRawData(recCtr);
			double [] distance = sFile.getDistance();
			// If distances are negative it means encoders are reversed
			if (distance[0] < 0.0)
				distance[0] *= -1.0;
			if (distance[1] < 0.0)
				distance[1] *= -1.0;
			double [] velocity = sFile.getVelocity();
			double [] speeds = sFile.getSpeed();
			if (speeds[0] == 999.0) {
				break;
			}
			if (GraphElements.POWER == type) {
				lineData1.add(sRec.getElapsedTime(false),speeds[0]);
				lineData2.add(sRec.getElapsedTime(false),speeds[1]);
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
		XYSeriesCollection sColl = new XYSeriesCollection();
		sColl.addSeries(lineData1);
		sColl.addSeries(lineData2);
		return sColl;
	}


	public JFreeChart initChart() {
		 jChart = ChartFactory.createXYLineChart( 
				"Record File", // Title 
				"Time", // x-axis Label 
				//"Distance", // y-axis Label 
				null,
				getChartData(GraphElements.POWER), // Dataset  (Default => POWER)
				PlotOrientation.VERTICAL, // Plot Orientation 
				false, // Show Legend 
				true, // Use tooltips 
				false // Configure chart to generate URLs? 
				); 
		return jChart;
	}
	
	public JFreeChart updateChart(GraphElements gElem) {
	 jChart = ChartFactory.createXYLineChart( 
				"Record File",  
				"Time",  
				null,
				getChartData(gElem),   
				PlotOrientation.VERTICAL, 
				false,  
				true,  
				false 
				); 
	 	//jChart.fireChartChanged();
		return jChart;
	}

	private int MAX_ROWS_TO_DISPLAY = 1000;
	public List<String> listRecords() {
		List<String> recList = new ArrayList<String>();
		SpeedFile sFile = new SpeedFile(fileName);
		sFile.readRecordingFile();
		int recCtr = 0;
		do {
			SpeedRecord sRec = sFile.getRawData(recCtr);
			double [] speeds = sFile.getSpeed();
			if (speeds[0] == 999.0) {
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

}
