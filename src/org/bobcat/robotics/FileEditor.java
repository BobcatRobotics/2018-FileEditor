package org.bobcat.robotics;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.bobcat.robotics.EditData.Mode;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.usfirst.frc.team177.lib.CommandRecord;
import org.usfirst.frc.team177.lib.Commands;
import org.usfirst.frc.team177.lib.RioLogger;

public class FileEditor {

	protected Shell shlFileEditor;
	private String fileName = "right2scale.speeds.txt";
	private String cmdFileName = "right2scale.txt";
	private boolean convertGrayHill = true;
	private boolean direction = true;

	// SpeedFile Components
	// private Composite speedContent = null;
	// private ScrolledComposite speedPnl = null;
	private final JChartManager chartMgr = new JChartManager(fileName);
	private final JFreeChart fileGraph = chartMgr.initChart();
	private ChartComposite displayGraph = null;
	private Label statusBar = null;
	private Label statusBarCmd = null;
	private Text fileList = null;

	// Chart File Components
	private final CmdFileManager cmdMgr = new CmdFileManager(cmdFileName);
	//private List<CommandRecord> cmdFileData = cmdMgr.readCmdFile();
	private Table cmdData;
	// private Composite cmdContent = null;
	// private ScrolledComposite cmdPnl = null;

	// private JavaFXManager fxMgr = null;
	// private FXCanvas fileAnimator = null;

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
		refreshUIData();
		updateUILayout();
		shlFileEditor.open();
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
		shlFileEditor = new Shell(); // SWT.NO_REDRAW_RESIZE, SHELL_TRIM (CLOSE|TITLE|MIN|MAX|RESIZE)
		shlFileEditor.setSize(1024, 768);
		shlFileEditor.setText("File Editor - " + getFileFirstName());
		shlFileEditor.setLayout(getLayout());
		shlFileEditor.setMenuBar(buildMenu());

		// Chart is used to display 2D Charts of the data.
		// CMDFileEdit is used to display CmdFile in a table
		int yPos = 0;
		int xPos = 0;
		int yBorder = 2;
		
		// The Status Bar
//		Point pstatus = new Point(1022, 30);
		Point pstatus = new Point(772, 30);
		statusBar = new Label(shlFileEditor, SWT.BORDER/* SWT.NONE */);
		statusBar.setText(" " + fileName + " | Records - " + chartMgr.getTotalRecords());
		statusBar.setLayoutData(new GridData(pstatus.x, pstatus.y));
		statusBar.setSize(pstatus.x, pstatus.y-2);
		statusBar.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		statusBar.setBounds(xPos + 2, yPos + 2, pstatus.x - 2, pstatus.y - 2); // make room for a border
		drawBorder(statusBar);
		RioLogger.debugLog("Status Bar size is " + statusBar.getSize());
		RioLogger.debugLog("Status Bar set size is x, y " + pstatus.x + " " + pstatus.y);
		RioLogger.debugLog("Status Bar set bounds is x, y, width, height " + (xPos+2) + " " + (yPos+2) + " " + (pstatus.x-2) + " " + (pstatus.y-2));
		yPos += statusBar.getSize().y + yBorder + 2;
		RioLogger.debugLog("(1) x, y is " + xPos + ", " + yPos);

		// The Status Bar
//		Point pstatus2 = new Point(1022, 30);
		Point pstatus2 = new Point(772, 30);
		statusBarCmd = new Label(shlFileEditor, SWT.BORDER/* SWT.NONE */);
		statusBarCmd.setText(" " + cmdFileName + " | Command - " + cmdMgr.getTotalCommands());
		statusBarCmd.setLayoutData(new GridData(pstatus2.x, pstatus2.y));
		statusBarCmd.setSize(pstatus2.x, pstatus2.y-2);
		statusBarCmd.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		statusBarCmd.setBounds(xPos + 2, yPos + 2, pstatus2.x - 2, pstatus2.y - 2); // make room for a border
		drawBorder(statusBarCmd);
		RioLogger.debugLog("Status Bar size is " + statusBarCmd.getSize());
		RioLogger.debugLog("Status Bar set size is x, y " + pstatus2.x + " " + pstatus2.y);
		RioLogger.debugLog("Status Bar set bounds is x, y, width, height " + (xPos+2) + " " + (yPos+2) + " " + (pstatus2.x-2) + " " + (pstatus2.y-2));
		//yPos += statusBarCmd.getSize().y + yBorder + 2;
		RioLogger.debugLog("(1a) x, y is " + xPos + ", " + yPos);

		// The Chart
//		Point pgraph = new Point(1022, 448);
		Point pgraph = new Point(772, 448);
		displayGraph = new ChartComposite(shlFileEditor, SWT.BORDER/* SWT.NONE*/, fileGraph, true);
		displayGraph.setDisplayToolTips(false);
		displayGraph.setHorizontalAxisTrace(false);
		displayGraph.setVerticalAxisTrace(false);
		displayGraph.setLayoutData(new GridData(pgraph.x, pgraph.y));
		displayGraph.setSize(pgraph.x, pgraph.y);
		displayGraph.setBounds(xPos,yPos, pgraph.x, pgraph.y);
		RioLogger.debugLog("Graph size is " + displayGraph.getSize());
		RioLogger.debugLog("Graph set size is x, y " + pgraph.x + " " + pgraph.y);
		RioLogger.debugLog("Graph set bounds is x, y, width, height " + xPos + " " + yPos + " " + pgraph.x + " " + pgraph.y);
		yPos += displayGraph.getSize().y + yBorder;
		RioLogger.debugLog("(2) y, x is " + yPos + ", " + xPos);

		// The CMD Chart
//		Point pTable = new Point(992, 672);
		Point pTable = new Point(742, 672);
		cmdData = new Table(shlFileEditor, SWT.BORDER | SWT.FULL_SELECTION/* |SWT.CHECK */ | SWT.RESIZE | SWT.V_SCROLL);
		cmdData.setBounds(xPos, yPos, pTable.x, pTable.y);
		cmdData.setSize(pTable.x,pTable.y);
		cmdData.setHeaderVisible(true);
		cmdData.setLinesVisible(true);
		GridData gLayout = new GridData(pTable.x,pTable.y);
		gLayout.horizontalAlignment = GridData.FILL;
		gLayout.verticalAlignment = GridData.FILL;
		gLayout.verticalSpan = 3;
		cmdData.setLayoutData(gLayout);
		RioLogger.debugLog("Table size is " + cmdData.getSize());
		RioLogger.debugLog("Table set size is x, y " + pTable.x + " " + pTable.y);
		RioLogger.debugLog("Table set bounds is x, y, width, height " + 0 + " " + 0 + " " + pTable.x + " " + pTable.y);
	

		TableColumn tblclmnNbr = new TableColumn(cmdData, SWT.BORDER);
		tblclmnNbr.setText("#");
		TableColumn tblclmnCmd = new TableColumn(cmdData, SWT.BORDER);
		tblclmnCmd.setText("Command Record");
		TableColumn tblclmnDrive = new TableColumn(cmdData, SWT.BORDER);
		tblclmnDrive.setText("Drive Train");
		TableColumn tblclmnElevator = new TableColumn(cmdData, SWT.BORDER);
		tblclmnElevator.setText("Elevator");
		TableColumn tblclmnCubeArms = new TableColumn(cmdData, SWT.BORDER);
		tblclmnCubeArms.setText("Arms");
		TableColumn tblclmnCubeSpin = new TableColumn(cmdData, SWT.BORDER);
		tblclmnCubeSpin.setText("Spinners");
		TableColumn tblclmnFourBar = new TableColumn(cmdData, SWT.BORDER);
		tblclmnFourBar.setText("Four Bar");


		// The List
//		Point plist = new Point(988, 286);
		Point plist = new Point(738, 286);
		fileList = new Text(shlFileEditor, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI);
		fileList.setLayoutData(new GridData(plist.x, plist.y));
		fileList.setSize(plist.x, plist.y);
		fileList.setBounds(xPos, yPos, plist.x, plist.y);
		//fileList.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		RioLogger.debugLog("List size is " + fileList.getSize());
		RioLogger.debugLog("List set size is x, y " + plist.x + " " + plist.y);
		RioLogger.debugLog("List set bounds is x, y, width, height " + xPos + " " + yPos + " " + plist.x + " " + plist.y);
	}

	private GridLayout getLayout() {
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		// layout.horizontalSpacing = 5;
		return layout;
	}

	private Menu buildMenu() {
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

		// Add Selection Handlers
		openItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String[] filterNames = { "*.txt" };
				FileDialog jfc = new FileDialog(shlFileEditor, SWT.OPEN);
				jfc.setFilterPath("\\home\\lvuser");
				jfc.setFilterNames(filterNames);
				jfc.setText("Select Robot Path File");
				String selectFile = jfc.open();
				if (selectFile != null && selectFile.length() > 0) {
					Path pth = Paths.get(selectFile);
					String filename = pth.getFileName().toString();
					RioLogger.log(filename);
						// Determine if this is a speed file or a CMD file
					String[] splitFileName = filename.split("\\.");
					if ("speeds".equals(splitFileName[1])) {
						RioLogger.debugLog("opening speeds file");
						fileName = filename;
						cmdFileName = splitFileName[0]+"."+splitFileName[2];
					} else {
						RioLogger.debugLog("opening CMD file");
						cmdFileName = filename;
						fileName = splitFileName[0]+".speeds."+splitFileName[1];
					}
					// positionUIElements();
					refreshUIData();
					updateUILayout();
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
		// Edit Menu
		MenuItem topEditorMenu = new MenuItem(menuBar, SWT.CASCADE | SWT.NO_RADIO_GROUP);
		topEditorMenu.setText("&Edit");
		Menu editMenu = new Menu(shlFileEditor, SWT.DROP_DOWN);
		topEditorMenu.setMenu(editMenu);
		
		MenuItem editItem = new MenuItem(editMenu, SWT.PUSH);
		editItem.setText("Edit Data Files");
		editItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				InputDialog delDialog = new InputDialog(shlFileEditor);
				delDialog.setMaxValue(chartMgr.getTotalRecords());
				EditData inputs = delDialog.open();
				// Validate inputs
				if (delDialog.isValidInput()) {
					if (updateFile(inputs)) {
						String action = "";
						String files = "SpeedFile and CMD File are backed up.";
						if (Mode.DELETE.equals(inputs.getCurrentMode())) {
							action = "Deleted";
						} else if (Mode.ADD.equals(inputs.getCurrentMode())) {
							action = "Added";
						} else {
							action = "Value Changed";
							if (inputs.isChangeVelocity()) {
								files = "Speed File backed up.";
							}
						}
						MessageDialog.openInformation(shlFileEditor, "Info",
							action + ". [fromRec,toRec] [" +inputs.getFrom()+"," + inputs.getTo() + "]. " + files);
						refreshUIData();
						updateUILayout();
					}
				}
			}
		});
//		MenuItem deleteItem = new MenuItem(editMenu, SWT.PUSH);
//		deleteItem.setText("&Delete Rows");
//		MenuItem addItem = new MenuItem(editMenu, SWT.PUSH);
//		addItem.setText("&Add Rows");

		MenuItem grayhillItem = new MenuItem(editMenu, SWT.CHECK);
		grayhillItem.setText("Convert GrayHill Values");
		grayhillItem.setSelection(true);
		grayhillItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state = grayhillItem.getSelection();
				convertGrayHill = !state;
				grayhillItem.setEnabled(convertGrayHill);
				if (convertGrayHill) {
					boolean updated = chartMgr.convertSpeedFile();
					if (updated) {
						MessageDialog.openInformation(shlFileEditor, "Info",
								fileName + " - GrayHill Adjusted. File is backed up.");
						// refreshUIFileComponents();
						refreshUIData();
						updateUILayout();

					}
				}
			}
		});
		MenuItem directionItem = new MenuItem(editMenu, SWT.CHECK);
		directionItem.setText("&Direction");
		directionItem.setSelection(direction);
		directionItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state = directionItem.getSelection();
				direction = !state;
				directionItem.setEnabled(direction);
				chartMgr.setDirection(direction);
			}
		});


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
		// MenuItem robotAnimateItem = new MenuItem(dataMenu, SWT.PUSH);
		// robotAnimateItem.setText("&Animate Path");

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

	private boolean updateFile(EditData fields) {
		String from = fields.getFrom();
		String to = fields.getTo();
		String leftAmt = fields.getLeftValue();
		String rightAmt = fields.getRightValue();
		if (leftAmt == null  || leftAmt.length() == 0) {
			leftAmt = "0";
		}
		if (rightAmt ==  null || rightAmt.length() == 0) {
			rightAmt = "0";
		}
		RioLogger.debugLog("From Field: " + from);
		RioLogger.debugLog("To Field: " +to);
		RioLogger.debugLog("Left Amt: " + leftAmt);
		RioLogger.debugLog("Right Amt: " +rightAmt);
		boolean updated = chartMgr.updateSpeedFile(fields.getCurrentMode(), fields.isChangePower(), from, to,leftAmt, rightAmt);
		if (fields.isChangePower()) {
			updated |= cmdMgr.updateCmdFile(fields.getCurrentMode(), from, to, leftAmt, rightAmt);
		}
		return updated;
	}

	private void updateGraph(GraphElements gElem) {
		chartMgr.setDirection(direction);
		displayGraph.setChart(chartMgr.updateChart(gElem));
		// displayGraph.forceRedraw();
		displayGraph.pack();
		// refreshUISpeedData(gElem);
		updateUILayout();
	}

	private void updateUILayout() {
		// Chart is used to display 2D Charts of the data
		// cmdData is used to display CMD File
		shlFileEditor.layout();
		shlFileEditor.pack();
	}

	// private void refreshUISpeedData(GraphElements gElem) {
	//// displayGraph.setChart(chartMgr.updateChart(gElem));
	//// speedContent.pack();
	// }

	private void refreshUIData() {
		// Update Data
		shlFileEditor.setText("File Editor - " + getFileFirstName());
		// if (isSpeedFile) {
		chartMgr.setFileName(fileName);
		chartMgr.setDirection(direction);
		updateGraph(GraphElements.POWER);
		statusBar.setText(" " + fileName + " | Records - " + chartMgr.getTotalRecords());
		fileList.selectAll();
		//fileList.clearSelection();
		fileList.cut();
		List<String> records = chartMgr.listRecords();
		for (String rec : records) {
			fileList.append(rec + "\n");
		}
		Integer cmdCount = 0;
		cmdMgr.setFileName(cmdFileName);
//		cmdData.clearAll();
		cmdData.removeAll();
		List<CommandRecord> cmdFileData = cmdMgr.readCmdFile();
		statusBarCmd.setText(" " + cmdFileName + " | Command - " + cmdMgr.getTotalCommands());
		for (CommandRecord cmd : cmdFileData) {
			if (Commands.EOF.equals(cmd.getID()))
				break;
			TableItem item = new TableItem(cmdData, SWT.NONE);
			item.setText(0, cmdCount.toString());
			item.setText(1, cmd.toString().trim().replaceAll("\\s+", " "));
			cmdCount++;
			String doublePower = String.format("%10.5f %10.5f", cmd.getSpeed()[0], cmd.getSpeed()[1]);
			String singlePower = String.format("%10.5f", cmd.getSpeed()[0]);
			switch (cmd.getID()) {
			case DRIVE_CHAIN:
				item.setText(2, doublePower);
				// driveCnt++;
				break;
			case ELEVATOR:
				item.setText(3, singlePower);
				break;
			case CUBE_ARMS:
				item.setText(4, cmd.getState() ? "OPEN" : "CLOSE");
				break;
			case CUBE_SPINNERS:
				item.setText(5, cmd.getState() ? "FWD " : " REV ");
				break;
			case FOURBAR:
				item.setText(6, cmd.getState() ? " UP " : "DOWN ");
				break;
			default:
				break;
			}
		}
		for (int col = 0; col < 7; col++) {
			cmdData.getColumn(col).pack();
		}
	}

	private void drawBorder(Control cont) {
		final Control control = cont;

		cont.getParent().addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				// Color color = new Color(Display.getCurrent(), 0, 0 ,255); // BLUE
				// Color color = new Color(Display.getCurrent(), 128, 128, 128); // GRAY
				Color color = new Color(Display.getCurrent(), 0, 0, 0); // GRAY
				gc.setBackground(color);
				Rectangle rect = control.getBounds();
				Rectangle rect1 = new Rectangle(rect.x - 2, rect.y - 2, rect.width + 4, rect.height + 4);
				gc.setLineStyle(SWT.LINE_SOLID);
				// gc.setLineWidth(2);
				gc.fillRectangle(rect1);
			}
		});
	}

	private String getFileFirstName() {
		String[] fileNameSplit = fileName.split("\\.");
		return fileNameSplit[0];
	}
}
