package org.bobcat.robotics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

public class FileEditor {

	protected Shell shlFileEditor;
	
	// Application Variables
	//private String fileName = "right2scale_short.txt";
	private String fileName = "center2left.speeds.txt";
	//private final JFileChooser fileChooser = new JFileChooser();
	private final JChartManager chartMgr = new JChartManager(fileName);
	private final JFreeChart fileGraph = chartMgr.initChart();
	private ChartComposite displayGraph = null;
	

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			FileEditor window = new FileEditor();
			window.open();
		} catch (Exception e) {
			System.out.println("File Editor error " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlFileEditor.open();
		shlFileEditor.layout();
		while (!shlFileEditor.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlFileEditor = new Shell(); //SWT.NO_REDRAW_RESIZE, SHELL_TRIM (CLOSE|TITLE|MIN|MAX|RESIZE)
		shlFileEditor.setSize(1024, 768);
		shlFileEditor.setText("File Editor");
		
		
//		shlRecorderFileEditor.addListener (SWT.Resize,  new Listener () {
//			    public void handleEvent (Event e) {
//			      Rectangle shell = shlRecorderFileEditor.getBounds();
//			      Rectangle client = shlRecorderFileEditor.getClientArea ();
//			      System.out.println("Shell area " + shell);
//			      System.out.println("Shell client area " + client);
//			    }
//			  });
//        System.out.println("Shell size " + shlRecorderFileEditor.getSize());
//        System.out.println("Client area height " + shlRecorderFileEditor.getClientArea().height);
//        System.out.println("Client area width " + shlRecorderFileEditor.getClientArea().width);
//        displayWidgetSizes(menuBar);
		
		// Layout Components
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = true;
		rowLayout.justify = false;
		rowLayout.type = SWT.VERTICAL;
		rowLayout.marginLeft = 1;
		rowLayout.marginRight = 1;
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 0;
		shlFileEditor.setLayout(rowLayout);
		shlFileEditor.setMenuBar(buildMenu());

		// The Chart
		displayGraph = new ChartComposite(shlFileEditor, /*SWT.BORDER*/ SWT.NONE, fileGraph, true);
		displayGraph.setDisplayToolTips(false);
		displayGraph.setHorizontalAxisTrace(true);
		displayGraph.setVerticalAxisTrace(true);
		displayGraph.setVerticalAxisTrace(false);
		displayGraph.setLayoutData(new RowData(988, 408));
		displayGraph.setSize(988,408);
		System.out.println("Graph size is " + displayGraph.getSize());
		
		// The List
		Text fileList = new Text(shlFileEditor, /*SWT.BORDER |*/ SWT.V_SCROLL | SWT.MULTI);
		java.util.List<String> records = chartMgr.listRecords(); 
		for (String rec : records) {
			fileList.append(rec + "\n");
		}
		fileList.setLayoutData(new RowData(958, 236));
		fileList.setSize(958, 186);
		System.out.println("List size is " + fileList.getSize());
		
		Label statusBar = new Label(shlFileEditor,SWT.BORDER);
		statusBar.setText(fileName);
		statusBar.setLayoutData(new RowData(988, 30));
		statusBar.setSize(988, 30);
		System.out.println("Status Bar size is " + statusBar.getSize());
	}
	
	private Menu buildMenu() {
		Menu menuBar = new Menu(shlFileEditor, SWT.BAR | SWT.BORDER);
		
		// File Menu
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("&File");
		Menu fileMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);
		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText("&Open");
		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText("&Exit");
		
		// Data Menu
		MenuItem cascadeDataMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeDataMenu.setText("&Data");
		Menu dataMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
		cascadeDataMenu.setMenu(dataMenu);
		MenuItem powerItem = new MenuItem(dataMenu, SWT.PUSH);
		powerItem.setText("&Power");
		MenuItem distanceItem = new MenuItem(dataMenu, SWT.PUSH);
		distanceItem.setText("&Distance");
		MenuItem velocityItem = new MenuItem(dataMenu, SWT.PUSH);
		velocityItem.setText("&Velocity");
		MenuItem pathItem = new MenuItem(dataMenu, SWT.PUSH);
		pathItem.setText("&Robot Path");

		// Add Selection Handlers
		openItem.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				FileNameExtensionFilter filter = new FileNameExtensionFilter("Recorder Files", "txt");
//				fileChooser.setFileFilter(filter);
//				fileChooser.setCurrentDirectory(new File("c://home//lvuser"));
//				int returnVal = fileChooser.showOpenDialog(null);
//				if (returnVal == JFileChooser.APPROVE_OPTION) {
//					System.out.println("You chose to open this file: " + fileChooser.getSelectedFile().getName());
//				}
//			}
		});
		
		exitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlFileEditor.getDisplay().dispose();
				System.exit(0);
			}
		});
		
		powerItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateGraph(GraphElements.POWER);
			}
		});	
		distanceItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateGraph(GraphElements.DISTANCE);
			}
		});
		velocityItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateGraph(GraphElements.VELOCITY);
			}
		});	
		pathItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateGraph(GraphElements.ROBOTPATH);
			}
		});	
		return menuBar;
	}
	
	private void updateGraph(GraphElements gElem) {
		displayGraph.setChart(chartMgr.updateChart(gElem));
		//displayGraph.forceRedraw();
		//shlFileEditor.redraw();
	}
	
//    static int getMenuHeight(Menu parent)
//    {
//        try {
//            Method m = Menu.class.getDeclaredMethod("getBounds", null);
//            m.setAccessible(true);
//            Rectangle r = (Rectangle) m.invoke(parent, null);
//            return r.height;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return 0;
//        }
//    }
    
//    static void displayWidgetSizes(Widget wid) {
//        try {
//            Method m = Widget.class.getDeclaredMethod("getBounds", null);
//            m.setAccessible(true);
//            Rectangle r = (Rectangle) m.invoke(wid, null);
//            System.out.println("Widget is " + wid);
//            System.out.println("Width = " + r.width + "height = " + r.height + " (y,x) = (" + r.y + "," + r.x + ")");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }    
//    }
}
