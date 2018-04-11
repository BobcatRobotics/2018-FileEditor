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
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.usfirst.frc.team177.lib.CommandRecord;
import org.usfirst.frc.team177.lib.Commands;
import org.usfirst.frc.team177.lib.RioLogger;
import org.usfirst.frc.team177.lib.SpeedRecord;

public class FileEditor {
	protected Shell shlFileEditor;
	private String fileName = "right2scale.speeds.txt";
	private String cmdFileName = "right2scale.txt";
	private boolean convertGrayHill = true;
	private boolean direction = true;
	private boolean writeCMDFile = false;
	private boolean writeSpeedFile = false;


	// SpeedFile Components
	private final JChartManager chartMgr = new JChartManager(fileName);
	private final JFreeChart fileGraph = chartMgr.initChart();
	private ChartComposite displayGraph = null;
	private Label statusBar = null;
	private Label statusBarCmd = null;
	private Table speedData = null;
	private TableDataEditor tdeSpeed;

	// Chart File Components
	private final CmdFileManager cmdMgr = new CmdFileManager(cmdFileName);
	private Table cmdData;
	private TableDataEditor tdeCMD;

	// Colors
    private final Display display = new Display();
    private final Color red = display.getSystemColor(SWT.COLOR_RED);
    private final Color white = display.getSystemColor(SWT.COLOR_WHITE);
    private final Color black = display.getSystemColor(SWT.COLOR_BLACK);

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
				// Check if the files were edited
				checkForFileEdits();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlFileEditor = new Shell(SWT.NO_REDRAW_RESIZE|SWT.CLOSE|SWT.TITLE|SWT.MIN|SWT.MAX); // SWT.NO_REDRAW_RESIZE, SHELL_TRIM (CLOSE|TITLE|MIN|MAX|RESIZE)
		shlFileEditor.setSize(1024, 738);
		shlFileEditor.setText("File Editor - " + getFileFirstName());
		shlFileEditor.setLayout(getLayout());
		shlFileEditor.setMenuBar(buildMenu());

		// Chart is used to display 2D Charts of the data.
		// CMDFileEdit is used to display CmdFile in a table
		int yPos = 0;
		int xPos = 0;
		int yBorder = 2;
		
		// Speed Status Bar
		Point pstatus = new Point(772, 30);
		statusBar = new Label(shlFileEditor, SWT.BORDER/* SWT.NONE */);
		statusBar.setText(" " + fileName + " | Records - " + chartMgr.getTotalRecords());
		statusBar.setLayoutData(new GridData(pstatus.x, pstatus.y));
		statusBar.setSize(pstatus.x, pstatus.y-2);
		statusBar.setBackground(white);
		statusBar.setBounds(xPos + 2, yPos + 2, pstatus.x - 2, pstatus.y - 2); // make room for a border
		drawBorder(statusBar);
		yPos += statusBar.getSize().y + yBorder + 2;
//		RioLogger.debugLog("Status Bar size is " + statusBar.getSize());
//		RioLogger.debugLog("Status Bar set size is x, y " + pstatus.x + " " + pstatus.y);
//		RioLogger.debugLog("Status Bar set bounds is x, y, width, height " + (xPos+2) + " " + (yPos+2) + " " + (pstatus.x-2) + " " + (pstatus.y-2));
//		RioLogger.debugLog("(1) x, y is " + xPos + ", " + yPos);

		// Command Status Bar
		Point pstatus2 = new Point(772, 30);
		statusBarCmd = new Label(shlFileEditor, SWT.BORDER/* SWT.NONE */);
		statusBarCmd.setText(" " + cmdFileName + " | Command - " + cmdMgr.getTotalCommands());
		statusBarCmd.setLayoutData(new GridData(pstatus2.x, pstatus2.y));
		statusBarCmd.setSize(pstatus2.x, pstatus2.y-2);
		statusBarCmd.setBackground(white);
		statusBarCmd.setBounds(xPos + 2, yPos + 2, pstatus2.x - 2, pstatus2.y - 2); // make room for a border
		drawBorder(statusBarCmd);
//		RioLogger.debugLog("Status Bar size is " + statusBarCmd.getSize());
//		RioLogger.debugLog("Status Bar set size is x, y " + pstatus2.x + " " + pstatus2.y);
//		RioLogger.debugLog("Status Bar set bounds is x, y, width, height " + (xPos+2) + " " + (yPos+2) + " " + (pstatus2.x-2) + " " + (pstatus2.y-2));
//		RioLogger.debugLog("(1a) x, y is " + xPos + ", " + yPos);

		// The  Speed File Table 
		Point plist = new Point(738, 256);
		speedData = new Table(shlFileEditor, SWT.BORDER | SWT.FULL_SELECTION/* |SWT.CHECK */ | SWT.RESIZE | SWT.V_SCROLL | SWT.H_SCROLL);
		speedData.setBounds(xPos, yPos, plist.x, plist.y);
		speedData.setSize(plist.x, plist.y);
		speedData.setHeaderVisible(true);
		speedData.setLinesVisible(true);	
		yPos += speedData.getSize().y + yBorder;

		GridData sLayout = new GridData(plist.x,plist.y);
		//sLayout.horizontalAlignment = GridData.FILL;
		//sLayout.verticalAlignment = GridData.FILL;
		sLayout.horizontalIndent = 0;
		sLayout.verticalIndent = 0;
		//sLayout.verticalSpan = 3;
		speedData.setLayoutData(sLayout);
//		speedData.setLayoutData(new GridData(plist.x, plist.y));
//		RioLogger.debugLog("List size is " + fileList.getSize());
//		RioLogger.debugLog("List set size is x, y " + plist.x + " " + plist.y);
//		RioLogger.debugLog("List set bounds is x, y, width, height " + xPos + " " + yPos + " " + plist.x + " " + plist.y);
		
		TableColumn tblspeedNbr = new TableColumn(speedData, SWT.BORDER);
		tblspeedNbr.setText("#");
		TableColumn tblspeedTime = new TableColumn(speedData, SWT.BORDER);
		tblspeedTime.setText("Elapsed");
		TableColumn tblspeedDelta = new TableColumn(speedData, SWT.BORDER);
		tblspeedDelta.setText("Delta");
		TableColumn tblspeedlp = new TableColumn(speedData, SWT.BORDER);
		tblspeedlp.setText("Left Pwr");
		TableColumn tblspeedrp = new TableColumn(speedData, SWT.BORDER);
		tblspeedrp.setText("Right Pwr");
		TableColumn tblspeedld = new TableColumn(speedData, SWT.BORDER);
		tblspeedld.setText("Left Dst");
		TableColumn tblspeedrd = new TableColumn(speedData, SWT.BORDER);
		tblspeedrd.setText("Right Dst");
		TableColumn tblspeedlv = new TableColumn(speedData, SWT.BORDER);
		tblspeedlv.setText("Left Vel");
		TableColumn tblspeedrv = new TableColumn(speedData, SWT.BORDER);
		tblspeedrv.setText("Right Vel");
		// Add the Editor
		tdeSpeed = new TableDataEditor(speedData);
		tdeSpeed.setEditableColumns(new int [] { 3, 4, 7, 8 });
		
		// The CMD File Table
		Point pTable = new Point(742, 672);
		cmdData = new Table(shlFileEditor, SWT.BORDER | SWT.FULL_SELECTION/* |SWT.CHECK */ | SWT.RESIZE | SWT.V_SCROLL);
		cmdData.setBounds(xPos, yPos, pTable.x, pTable.y);
		cmdData.setSize(pTable.x,pTable.y);
		cmdData.setHeaderVisible(true);
		cmdData.setLinesVisible(true);
		GridData gLayout = new GridData(pTable.x,pTable.y);
		//gLayout.horizontalAlignment = GridData.BEGINNING;
		gLayout.verticalAlignment = GridData.FILL_VERTICAL;
		gLayout.horizontalIndent = 0;
		gLayout.verticalIndent = 0;
		gLayout.verticalSpan = 3;
		cmdData.setLayoutData(gLayout);
//		RioLogger.debugLog("Table size is " + cmdData.getSize());
//		RioLogger.debugLog("Table set size is x, y " + pTable.x + " " + pTable.y);
//		RioLogger.debugLog("Table set bounds is x, y, width, height " + 0 + " " + 0 + " " + pTable.x + " " + pTable.y);
			
		TableColumn tblclmnNbr = new TableColumn(cmdData, SWT.BORDER);
		tblclmnNbr.setText("#");
		TableColumn tblclmnCmd = new TableColumn(cmdData, SWT.BORDER);
		tblclmnCmd.setText("Command Record");
		TableColumn tblclmnpl = new TableColumn(cmdData, SWT.BORDER);
		tblclmnpl.setText("Left Pwr");
		TableColumn tblclmnpr = new TableColumn(cmdData, SWT.BORDER);
		tblclmnpr.setText("Right Pwr");
		TableColumn tblclmnCubeArms = new TableColumn(cmdData, SWT.BORDER);
		tblclmnCubeArms.setText("Arms");
		TableColumn tblclmnCubeSpin = new TableColumn(cmdData, SWT.BORDER);
		tblclmnCubeSpin.setText("Spinners");
		TableColumn tblclmnFourBar = new TableColumn(cmdData, SWT.BORDER);
		tblclmnFourBar.setText("Four Bar");
		// Add the Editor
		tdeCMD = new TableDataEditor(cmdData);
		tdeCMD.setEditableColumns(new int [] { 2, 3 });

		// The Speed Graph
		Point pgraph = new Point(772, 378);
		displayGraph = new ChartComposite(shlFileEditor, SWT.BORDER/* SWT.NONE*/, fileGraph, true);
		displayGraph.setDisplayToolTips(false);
		displayGraph.setHorizontalAxisTrace(false);
		displayGraph.setVerticalAxisTrace(false);
		displayGraph.setLayoutData(new GridData(pgraph.x, pgraph.y));
		displayGraph.setSize(pgraph.x, pgraph.y);
		displayGraph.setBounds(xPos,yPos, pgraph.x, pgraph.y);
		yPos += displayGraph.getSize().y + yBorder;
//		RioLogger.debugLog("Graph size is " + displayGraph.getSize());
//		RioLogger.debugLog("Graph set size is x, y " + pgraph.x + " " + pgraph.y);
//		RioLogger.debugLog("Graph set bounds is x, y, width, height " + xPos + " " + yPos + " " + pgraph.x + " " + pgraph.y);
//		RioLogger.debugLog("(2) y, x is " + yPos + ", " + xPos);

	}

	private GridLayout getLayout() {
		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		// layout.horizontalSpacing = 5;
		return layout;
	}

	private void checkForFileEdits() {
		if (tdeCMD.isDataChanged()) {
			statusBarCmd.setBackground(red);
			cmdMgr.updateCmdFileValue(tdeCMD.getRowEdited(), tdeCMD.getColumnEdited(), tdeCMD.getEditValue());
			tdeCMD.resetEdit(); 
			writeCMDFile = true;
		} 
		if (tdeSpeed.isDataChanged()) {
			statusBar.setBackground(red);
			chartMgr.updateSpeedFileValue(tdeSpeed.getRowEdited(),tdeSpeed.getColumnEdited(),tdeSpeed.getEditValue());
			tdeSpeed.resetEdit();
			writeSpeedFile = true;
		}
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
		MenuItem saveItem = new MenuItem(fileMenu, SWT.PUSH);
		saveItem.setText("&Save");
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
		saveItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean fileSaved = false;
				// Check if the files have changed
				String fileNames =  "";
				if (writeCMDFile) {
					// TODO :: Need to write changes to command file
					// TODO :: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
					cmdMgr.saveCmdFile();
					statusBarCmd.setBackground(white);
					writeCMDFile = false;
					fileSaved = true;
					fileNames = "CMD";
				}
				if (writeSpeedFile) {
					// TODO :: Need to write changes to command file
					// TODO :: XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
					chartMgr.saveSpeedFile();
					RioLogger.debugLog("!!!!!!!!!!!!!!!!!! WRITE Speed"); 
					writeSpeedFile = false;
					statusBar.setBackground(white);
					fileSaved = true;
					if (fileNames.length() > 0)
						fileNames += " and Speed";
					else
						fileNames = "Speed";
				}
				if (fileSaved) {
					MessageDialog.openInformation(shlFileEditor, "Info",fileNames + " - Data files saved.");
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
				delDialog.setFileName(fileName);
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
		RioLogger.debugLog("From Field - To Field : " + from + " - " + to );
		RioLogger.debugLog("Left Amt, Right Amt: " + leftAmt + ", " + rightAmt);
		RioLogger.debugLog("isChangePower: " + fields.isChangePower());
		boolean updated = chartMgr.updateSpeedFile(fields.getCurrentMode(), fields.isChangePower(), from, to,leftAmt, rightAmt);
		if (!fields.isChangeVelocity() ) {
			updated |= cmdMgr.updateCmdFile(fields.getCurrentMode(), from, to, leftAmt, rightAmt);
		}
		return updated;
	}

	private void updateGraph(GraphElements gElem) {
		chartMgr.setDirection(direction);
		displayGraph.setChart(chartMgr.updateChart(gElem));
		displayGraph.pack();
		updateUILayout();
	}

	private void updateUILayout() {
		shlFileEditor.layout();
		shlFileEditor.pack();
	}

	private void refreshUIData() {
		// Update Data
		shlFileEditor.setText("File Editor - " + getFileFirstName());
		chartMgr.setFileName(fileName);
		chartMgr.setDirection(direction);
		updateGraph(GraphElements.POWER);
		statusBar.setText(" " + fileName + " | Records - " + chartMgr.getTotalRecords());
		speedData.selectAll();
		//fileList.clearSelection();
		speedData.removeAll();
		Integer speedCnt = 0;
		List<SpeedRecord> records = chartMgr.listSpeeds();
		for (SpeedRecord rec : records) {
			if (SpeedRecord.EOF == rec.getID()) {
				break;
			}
			TableItem item = new TableItem(speedData, SWT.NONE);
			item.setText(0, speedCnt.toString());
			item.setText(1, String.format("%6.3f",rec.getElapsedTime(false)));
			item.setText(2, String.format("%6.3f",rec.getDeltaTime(false)));
			item.setText(3, String.format("%9.4f", rec.getPower()[0]));
			item.setText(4, String.format("%9.4f", rec.getPower()[1]));
			item.setText(5, String.format("%9.4f", rec.getDistance()[0]));
			item.setText(6, String.format("%9.4f", rec.getDistance()[1]));
			item.setText(7, String.format("%9.4f", rec.getVelocity()[0]));
			item.setText(8, String.format("%9.4f", rec.getVelocity()[1]));
			speedCnt++;
		}
		for (int col = 0; col < speedData.getColumnCount(); col++) {
			speedData.getColumn(col).pack();
		}
		
		Integer cmdCount = 0;
		cmdMgr.setFileName(cmdFileName);
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
		switch (cmd.getID()) {
			case DRIVE_CHAIN:
				item.setText(2, String.format("%9.4f", cmd.getSpeed()[0]));
				item.setText(3, String.format("%9.4f", cmd.getSpeed()[1]));
				// driveCnt++;
				break;
			case ELEVATOR:
				item.setText(2,  String.format("%9.4f", cmd.getSpeed()[0]));
				item.setText(3,  "");
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
		for (int col = 0; col < cmdData.getColumnCount(); col++) {
			cmdData.getColumn(col).pack();
		}
	}

	private void drawBorder(Control cont) {
		final Control control = cont;

		cont.getParent().addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setBackground(black);
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
