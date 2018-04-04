package org.bobcat.robotics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
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
import org.usfirst.frc.team177.lib.CommandRecord;
import org.usfirst.frc.team177.lib.Commands;
import org.usfirst.frc.team177.lib.RioLogger;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class FileEditor {

	protected Shell shlFileEditor;
	private Composite content = null;  // The current layout (SWT or FX)
	private Composite speedContent;
	private ScrolledComposite cntPnl = null; // Panel to house current layout
	private ScrolledComposite speedPnl;
	
	private String fileName = "center2left.speeds.txt";
	private String cmdFileName = "center2left.txt";
	private final JChartManager chartMgr = new JChartManager(fileName);
	private final JFreeChart fileGraph = chartMgr.initChart();
	private final CmdFileManager cmdMgr = new CmdFileManager(cmdFileName);
	private ChartComposite displayGraph = null;
	private Label statusBar = null;
	private Text fileList = null;
	private boolean convertGrayHill = true;
	private Table cmdData;
	
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
		//addUIElements(isChartMode,false);
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
		shlFileEditor.setText("File Editor - " + getFileFirstName());
		
		addUIElements(true, false);
	}
	
	private GridLayout getLayout() {
		GridLayout layout = new GridLayout(1,true);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		//layout.horizontalSpacing = 5;
		return layout;
	}
	
	private void addUIElements(boolean isChartMode,boolean isAnimation) {
		// Chart or FXCanvas (Either the chart  or FXCanvas will be visible)
		// Chart is used to display 2D Charts of the data. FXCanvas is used to animate the Robot path
		//
		// Chart or CmdFileEditor
		shlFileEditor.setLayout(getLayout());
		shlFileEditor.setMenuBar(buildMenu(isChartMode));

		if (!isChartMode) {
			// Scrolled Composite does not use a layout
			ScrolledComposite cmdPnl = new ScrolledComposite(shlFileEditor, SWT.BORDER);
			
			// Create a composite, layout controls
			Composite cmdContent = new Composite(cmdPnl, SWT.NONE);
			cmdContent.setSize(new Point(988, 672));
			
			cmdData = new Table(cmdContent, /* SWT.FULL_SELECTION |*/ SWT.V_SCROLL);
			cmdData.setBounds(0, 0, 982, 670);
			cmdData.setHeaderVisible(true);
			cmdData.setLinesVisible(true);
			GridData gLayout = new GridData(SWT.FILL, SWT.FILL, false, true);
			cmdData.setLayoutData(gLayout);
			
			TableColumn tblclmnNbr = new TableColumn(cmdData, SWT.BORDER);
			tblclmnNbr.setWidth(75);
			tblclmnNbr.setText("#");
			
			TableColumn tblclmnCmd = new TableColumn(cmdData, SWT.BORDER);
			tblclmnCmd.setWidth(200);
			tblclmnCmd.setText("Command Record");
			
			TableColumn tblclmnDrive = new TableColumn(cmdData, SWT.BORDER);
			tblclmnDrive.setResizable(false);
			tblclmnDrive.setWidth(165);
			tblclmnDrive.setText("Drive Train");
			
			TableColumn tblclmnElevator = new TableColumn(cmdData, SWT.BORDER);
			tblclmnElevator.setWidth(85);
			tblclmnElevator.setText("Elevator");
			
			TableColumn tblclmnCubeArms = new TableColumn(cmdData, SWT.BORDER);
			tblclmnCubeArms.setWidth(100);
			tblclmnCubeArms.setText("Arms");
			
			TableColumn tblclmnCubeSpin = new TableColumn(cmdData, SWT.BORDER);
			tblclmnCubeSpin.setWidth(100);
			tblclmnCubeSpin.setText("Spinners");
			
			TableColumn tblclmnFourBar = new TableColumn(cmdData, SWT.BORDER);
			tblclmnFourBar.setWidth(100);
			tblclmnFourBar.setText("Four Bar");
			cmdPnl.setContent(cmdContent);
			cmdPnl.setMinSize(cmdContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			cmdPnl.setMinSize(new Point(988, 672));
			
			// Add Data to the Table
			// Keep track of the row of each command
//			int driveCnt = 0;
//			int elevCnt = 0;
//			int cubeArmCnt = 0;
//			int cubeSpinnerCnt = 0;
//			int fourBarCnt = 0;
	
			Integer cmdCount = 0;
			List<CommandRecord> cmdFileData = cmdMgr.readCmdFile();
			for (CommandRecord cmd : cmdFileData) {
				if (Commands.EOF.equals(cmd.getID()))
					break;
				TableItem item = new TableItem(cmdData, SWT.NONE);
				item.setText(0,cmdCount.toString());
				item.setText(1,cmd.toString().trim().replaceAll("\\s+", " "));
				cmdCount++;
				String doublePower = String.format("%8.5f %8.5f", cmd.getSpeed()[0],cmd.getSpeed()[1]);
				String singlePower = String.format("%8.5f", cmd.getSpeed()[0]);
				switch (cmd.getID()) {
				case DRIVE_CHAIN:
					item.setText(2, doublePower);
					//driveCnt++;
					break;
	
				case ELEVATOR:
					item.setText(3, singlePower);
					break;
					
				case CUBE_ARMS:
					item.setText(4, cmd.getState() ? "OPEN" : "CLOSE");
					break;
	
				case CUBE_SPINNERS:
					item.setText(5, cmd.getState() ? "FWD" : "REV");
					break;
	
				case FOURBAR:
					item.setText(6, cmd.getState() ? "UP" : "DOWN");
					break;
	
				default:
					break;
	
				}
			}
			for (int col = 0; col < 7; col++) {
				cmdData.getColumn(col).pack();
			}
		}
		if (isChartMode) {		
			// Scrolled Composite does not use a layout
			Point pcomp = new Point(988,672);
			speedPnl = new ScrolledComposite(shlFileEditor, SWT.BORDER );
			speedPnl.setBounds(0, 0, pcomp.y, pcomp.x);
			speedPnl.setMinSize(pcomp);
	
			// Create a composite, keep track of the positioning on the content panel
			int yPos = 0;		
			int xPos = 0;
			int xBorder = 2; 
			Point pgraph = new Point(988,448);
			Point pstatus = new Point(986,30);
			Point plist = new Point(988,192);
	
			speedContent = new Composite(speedPnl, SWT.NONE);
			speedContent.setSize(pcomp);
			//content.setBackground(new Color(Display.getCurrent(), 255,0,0));
			speedPnl.setContent(speedContent);
					
			// The Chart
			displayGraph = new ChartComposite(speedContent, /*SWT.BORDER*/ SWT.NONE, fileGraph, true);
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
			statusBar = new Label(speedContent,SWT.BORDER/* SWT.NONE*/);
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
			fileList = new Text(speedContent, /*SWT.BORDER |*/ SWT.V_SCROLL | SWT.MULTI);
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
	}
	
	
	private Menu buildMenu(boolean isChartMode) {
		Menu menuBar = new Menu(shlFileEditor, SWT.BAR | SWT.BORDER);
		
		// File Menu
		MenuItem topFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		topFileMenu.setText("&File");
		Menu fileMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
		topFileMenu.setMenu(fileMenu);
		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
		openItem.setText("&Open");
		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.setText("&Exit");

		// View Menu
		MenuItem topViewMenu = new MenuItem(menuBar, SWT.CASCADE);
		topViewMenu.setText("&View");
		Menu viewMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
		topViewMenu.setMenu(viewMenu);
		MenuItem speedItem = new MenuItem(viewMenu, SWT.PUSH);
		speedItem.setText("&Speed File");
		MenuItem cmdItem = new MenuItem(viewMenu, SWT.PUSH);
		cmdItem.setText("&Command File");

		// Add Selection Handlers
		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String [] filterNames = { "*.txt" };
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
		speedItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addUIElements(true,false);
				refreshUILayout(false,false);

			}
		});
		cmdItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addUIElements(false,false);
				refreshUILayout(false,false);
			}
		});
		
		if (isChartMode) {
			// Data Menu
			MenuItem topDataMenu = new MenuItem(menuBar, SWT.CASCADE);
			topDataMenu.setText("&Data");
			Menu dataMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
			topDataMenu.setMenu(dataMenu);
			MenuItem powerItem = new MenuItem(dataMenu, SWT.PUSH);
			powerItem.setText("&Power");
			MenuItem distanceItem = new MenuItem(dataMenu, SWT.PUSH);
			distanceItem.setText("&Distance");
			MenuItem velocityItem = new MenuItem(dataMenu, SWT.PUSH);
			velocityItem.setText("&Velocity");
			MenuItem pathItem = new MenuItem(dataMenu, SWT.PUSH);
			pathItem.setText("&Robot Path");
//			MenuItem robotAnimateItem = new MenuItem(dataMenu, SWT.PUSH);
//			robotAnimateItem.setText("&Animate Path");
			
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

			// Edit Menu
			MenuItem topEditorMenu = new MenuItem(menuBar, SWT.CASCADE | SWT.NO_RADIO_GROUP);
			topEditorMenu.setText("&Edit");
			Menu editMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
			topEditorMenu.setMenu(editMenu);
		    MenuItem grayhillItem = new MenuItem(editMenu, SWT.CHECK);
		    grayhillItem.setText("Convert GrayHill Values");
		    grayhillItem.setSelection(true);
			MenuItem deleteItem = new MenuItem(editMenu, SWT.PUSH);
			deleteItem.setText("&Delete Rows");
			MenuItem addItem = new MenuItem(editMenu, SWT.PUSH);
			addItem.setText("&Add Rows");
			
			grayhillItem.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boolean state = grayhillItem.getSelection();
					convertGrayHill = !state;
					grayhillItem.setEnabled(convertGrayHill);
					if (convertGrayHill) {
						boolean updated = chartMgr.convertSpeedFile();
						if (updated) {
							MessageDialog.openInformation(shlFileEditor, "Info", fileName + " - GrayHill Adjusted. File is backed up.");
							refreshUIFileComponents();
							refreshUILayout(true, false);

						}
					}
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
		}
	
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
			speedPnl.setBounds(0, 0, pcomp.y, pcomp.x);
			speedPnl.setMinSize(pcomp);
			speedContent.setSize(pcomp);
			speedContent.redraw();
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
	
	private String getFileFirstName() {
		String [] fileNameSplit = fileName.split("\\.");
		return fileNameSplit[0];
	}
}
