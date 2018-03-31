package org.bobcat.robotics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.usfirst.frc.team177.lib.RioLogger;

public class FileEditor {

	protected Shell shlFileEditor;
	private Composite content = null;  // The current layout (SWT or FX)
	private ScrolledComposite cntPnl = null; // Panel to house current layout
	
	private String fileName = "center2left.speeds.txt";
	private final JChartManager chartMgr = new JChartManager(fileName);
	private final JFreeChart fileGraph = chartMgr.initChart();
	private ChartComposite displayGraph = null;
	private Label statusBar = null;
	private Text fileList = null;
	
	//private JavaFXManager fxMgr = null;
	//private FXCanvas fileAnimator = null;

	

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
			RioLogger.debugLog("File Editor error " + e);
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		addUIElements(false);
		//refreshUILayout(false,false);
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
		shlFileEditor.setText("File Editor - " + fileName);
		shlFileEditor.setMenuBar(buildMenu());
		shlFileEditor.setLayout(getLayout());
	
			
//		shlRecorderFileEditor.addListener (SWT.Resize,  new Listener () {
//			    public void handleEvent (Event e) {
//			      Rectangle shell = shlRecorderFileEditor.getBounds();
//			      Rectangle client = shlRecorderFileEditor.getClientArea ();
//			      RioLogger.debugLog("Shell area " + shell);
//			      RioLogger.debugLog("Shell client area " + client);
//			    }
//			  });
//        RioLogger.debugLog("Shell size " + shlRecorderFileEditor.getSize());
//        RioLogger.debugLog("Client area height " + shlRecorderFileEditor.getClientArea().height);
//        RioLogger.debugLog("Client area width " + shlRecorderFileEditor.getClientArea().width);
//        displayWidgetSizes(menuBar);
	}
	
	private GridLayout getLayout() {
		GridLayout layout = new GridLayout(1,true);
		//layout.horizontalSpacing = 5;
		layout.marginHeight = 5;
		layout.marginWidth = 5;
//		layout.marginLeft = 2;
//		layout.marginRight = 2;
//		layout.marginTop = 0;
//		layout.marginBottom = 0;
		return layout;
	}
	
	private void addUIElements(boolean isAnimation) {
		// Chart or FXCanvas (Either the chart  or FXCanvas will be visible)
		// Chart is used to display 2D Charts of the data. FXCanvas is used to animate the Robot path
		//
		
	    // Composite - keep track of the positioning on the content panel
		int yPos = 0;		
		int xPos = 0;
		int xBorder = 2; 
		Point pcomp = new Point(988,672);
		Point pgraph = new Point(988,448);
		Point pstatus = new Point(986,30);
		Point plist = new Point(988,192);
		//RioLogger.debugLog("Point " + pcomp + " (x,y) " + pcomp.x + " " + pcomp.y );
			
		// Scrolled Composite does not use a layout
		cntPnl = new ScrolledComposite(shlFileEditor, SWT.BORDER );
		cntPnl.setBounds(0, 0, pcomp.y, pcomp.x);
		cntPnl.setMinSize(pcomp);

		// Create a composite, layout controls
		content = new Composite(cntPnl, SWT.NONE);
		content.setSize(pcomp);
		//content.setBackground(new Color(Display.getCurrent(), 255,0,0));
		cntPnl.setContent(content);
				
		// The Chart
		displayGraph = new ChartComposite(content, /*SWT.BORDER*/ SWT.NONE, fileGraph, true);
		displayGraph.setDisplayToolTips(false);
		displayGraph.setHorizontalAxisTrace(false);
		displayGraph.setVerticalAxisTrace(false);
		displayGraph.setLayoutData(new GridData(pgraph.x, pgraph.y));
		displayGraph.setSize(pgraph.x, pgraph.y);
		RioLogger.debugLog("Graph size is " + displayGraph.getSize());
		RioLogger.debugLog("Graph computesize is " + displayGraph.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		xPos += displayGraph.getSize().y + xBorder;
		RioLogger.debugLog("(1) y, x is " + yPos + ", " + xPos);

		// The Status Bar
		statusBar = new Label(content,SWT.BORDER/* SWT.NONE*/);
		statusBar.setText(" " + fileName + " | Records - " + chartMgr.getTotalRecords());
		statusBar.setLayoutData(new GridData(pstatus.x,pstatus.y));
		statusBar.setSize(pstatus.x,pstatus.y);
		statusBar.setBackground(new Color(Display.getCurrent(),255,255,255));
		statusBar.setBounds(yPos+2,xPos, pstatus.x - 2, pstatus.y - 2); // make room for a border
		drawBorder(statusBar);
		RioLogger.debugLog("Status Bar size is " + statusBar.getSize());
		RioLogger.debugLog("Status Bar computesize " + statusBar.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		xPos += statusBar.getSize().y + xBorder;
		RioLogger.debugLog("(2) y, x is " + yPos + ", " + xPos);
		
		// The List
		fileList = new Text(content, /*SWT.BORDER |*/ SWT.V_SCROLL | SWT.MULTI);
		List<String> records = chartMgr.listRecords(); 
		for (String rec : records) {
			fileList.append(rec + "\n");
		}
		fileList.setLayoutData(new GridData(plist.x,plist.y));
		fileList.setSize(plist.x,plist.y);
		fileList.setBounds(yPos,xPos,plist.x,plist.y);
		RioLogger.debugLog("List size is " + fileList.getSize());
		RioLogger.debugLog("List computesize is " + fileList.computeSize(SWT.DEFAULT, SWT.DEFAULT));
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
		MenuItem robotAnimateItem = new MenuItem(dataMenu, SWT.PUSH);
		robotAnimateItem.setText("&Animate Path");

		// Data Menu
		MenuItem editorMenu = new MenuItem(menuBar, SWT.CASCADE);
		editorMenu.setText("&Edit");
		Menu editMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
		editorMenu.setMenu(editMenu);
		MenuItem deleteItem = new MenuItem(editMenu, SWT.PUSH);
		deleteItem.setText("&Delete Rows");
		MenuItem addItem = new MenuItem(editMenu, SWT.PUSH);
		addItem.setText("&Add Rows");

		// Add Selection Handlers
		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				JFileChooser jfc = new JFileChooser("\\home\\lvuser");
//				jfc.setDialogTitle("Select a Robot Path File");
//				jfc.setAcceptAllFileFilterUsed(false);
//				FileNameExtensionFilter filter = new FileNameExtensionFilter("Path File", "txt");
//				jfc.addChoosableFileFilter(filter);
//
//				int returnValue = jfc.showOpenDialog(jfc);
//				if (returnValue == JFileChooser.APPROVE_OPTION) {
//					fileName = jfc.getSelectedFile().getPath();
//					RioLogger.debugLog(jfc.getSelectedFile().getPath());
//					refreshUILayout(true, false);
//				}
				
				String [] filterNames = { "*.speeds" };
				FileDialog jfc = new FileDialog(shlFileEditor,SWT.OPEN);
				jfc.setFilterPath("\\home\\lvuser");
				jfc.setFilterNames(filterNames);
				jfc.setText("Select Robot Path File");
				String selectFile = jfc.open();
				if (selectFile != null && selectFile.length() > 0 ) {
					Path pth = Paths.get(selectFile);
					String filename = pth.getFileName().toString();
					RioLogger.log(filename);
					fileName = filename;
					refreshUIFileComponents();
					refreshUILayout(true, false);
				}
			}
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
		robotAnimateItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//updateGraph(GraphElements.ROBOTPATH);
				//refreshUILayout(true,true);
			}
		});	
		deleteItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog delDialog = new InputDialog(shlFileEditor);
				delDialog.setRecordCount(chartMgr.getTotalRecords());
				String [] inputs = delDialog.open();
				// Validate inputs
				if (delDialog.validInput()) {
					if (updateFile(true,inputs)) {
						refreshUIFileComponents();
						refreshUILayout(true, false);
					}
				}
			}
		});	
		addItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				//updateGraph(GraphElements.ROBOTPATH);
				//refreshUILayout(true,true);
			}
		});	

		return menuBar;
	}

	private boolean updateFile(boolean delete,String[] inputs) {
		// TODO Auto-generated method stub
		RioLogger.debugLog("From Field: " + inputs[0]);
		RioLogger.debugLog("To Field: " + inputs[1]);
		return chartMgr.updateSpeedFile(delete,inputs[0],inputs[1]);
	}

	private void refreshUILayout(boolean redraw,boolean animation) {
		// Chart or FXCanvas (Either the chart  or FXCanvas will be visible)
		// Chart is used to display 2D Charts of the data
		// FXCanvas is used to animate the Robot path. 
		//shlFileEditor.setParent(currentParent);
		
		if (animation) {
			//fxMgr.animate();
			//RioLogger.debugLog("FX Animation panel size is " + fileAnimator.getSize());
		}
		
		if (redraw) {
			Point pcomp = new Point(988,672);
			cntPnl.setBounds(0, 0, pcomp.y, pcomp.x);
			cntPnl.setMinSize(pcomp);
			content.setSize(pcomp);
			content.redraw();
			shlFileEditor.layout();
			shlFileEditor.pack();
		}
	}

	private void refreshUIFileComponents( ) {
		// Update Program Title, label
		shlFileEditor.setText("File Editor - " + fileName);
		chartMgr.setFileName(fileName);
		displayGraph.setChart(chartMgr.initChart());
		statusBar.setText(" " + fileName + " | Records - " + chartMgr.getTotalRecords());
		fileList.selectAll();
		fileList.clearSelection();
		List<String> records = chartMgr.listRecords(); 
		for (String rec : records) {
			fileList.append(rec + "\n");
		}

		
	}
	private void updateGraph(GraphElements gElem) {
		displayGraph.setChart(chartMgr.updateChart(gElem));
		refreshUILayout(true, false);
		//displayGraph.forceRedraw();
		//shlFileEditor.redraw();
	}
	
	private void drawBorder(Control cont){
        final Control control = cont;

        cont.getParent().addPaintListener(new PaintListener(){
    		@Override
            public void paintControl(PaintEvent e){
                GC gc = e.gc;
                Color color = new Color(Display.getCurrent(), 0, 0 ,255); // BLUE
                //Color color = new Color(Display.getCurrent(), 128, 128, 128); // BLACK
                gc.setBackground(color);
                Rectangle rect = control.getBounds();
                //RioLogger.debugLog("drawBorder rect bounds are " + rect);
                Rectangle rect1 = new Rectangle(rect.x-2, rect.y - 2,
                        rect.width+4, rect.height+4);
                //RioLogger.debugLog("drawBorder rect bounds are " + rect1);
                gc.setLineStyle(SWT.LINE_SOLID);
                //gc.setLineWidth(2);
                gc.fillRectangle(rect1);
            }
        });
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
//            RioLogger.debugLog("Widget is " + wid);
//            RioLogger.debugLog("Width = " + r.width + "height = " + r.height + " (y,x) = (" + r.y + "," + r.x + ")");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }    
//    }
}
