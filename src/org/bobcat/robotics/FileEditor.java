package org.bobcat.robotics;

import javax.swing.JFileChooser;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

public class FileEditor {

	protected Shell shlRecorderFileEditor;
	
	// Application Variables
	//private String fileName = "right2scale_short.txt";
	private String fileName = "center2right.speeds.txt";
	private final JFileChooser fileChooser = new JFileChooser();
	private final JChartManager chartMgr = new JChartManager(fileName);
	private final JFreeChart freeChart = chartMgr.initChart();


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
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		Display display = Display.getDefault();
		createContents();
		shlRecorderFileEditor.open();
		shlRecorderFileEditor.layout();
		while (!shlRecorderFileEditor.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlRecorderFileEditor = new Shell();
		shlRecorderFileEditor.setSize(1024, 768);
		shlRecorderFileEditor.setText("Recorder File Editor");
		
		Menu menuBar = new Menu(shlRecorderFileEditor, SWT.BAR);
		MenuItem cascadeFileMenu = new MenuItem(menuBar, SWT.CASCADE);
		cascadeFileMenu.setText("&File");

		Menu fileMenu = new Menu(shlRecorderFileEditor, SWT.DROP_DOWN);
		cascadeFileMenu.setMenu(fileMenu);
//		fileMenu.addListener (SWT.ALL, new Listener () {
//    	  	@Override
//            public void handleEvent (Event e) {
//           }
//  	   });

		MenuItem openItem = new MenuItem(fileMenu, SWT.PUSH);
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
		openItem.setText("&Open");

		MenuItem exitItem = new MenuItem(fileMenu, SWT.PUSH);
		exitItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//	   	  		System.out.println("Event is " + e);
//                System.out.println("Shell size " + shlRecorderFileEditor.getSize());
//                System.out.println("Client area height " + shlRecorderFileEditor.getClientArea().height);
//                System.out.println(getMenuHeight(menuBar));
  
				shlRecorderFileEditor.getDisplay().dispose();
				System.exit(0);
			}
		});
		exitItem.setText("&Exit");
		
        System.out.println("Shell size " + shlRecorderFileEditor.getSize());
        System.out.println("Client area height " + shlRecorderFileEditor.getClientArea().height);
        System.out.println("Client area width " + shlRecorderFileEditor.getClientArea().width);
        //displayWidgetSizes(menuBar);

		// Layout Components
		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = false;
		rowLayout.justify = true;
		rowLayout.type = SWT.VERTICAL;
		rowLayout.marginLeft = 5;
		rowLayout.marginTop = 5;
		rowLayout.marginRight = 5;
		rowLayout.marginBottom = 5;
		rowLayout.spacing = 0;
		shlRecorderFileEditor.setLayout(rowLayout);
		shlRecorderFileEditor.setMenuBar(menuBar);

		// The Chart
		ChartComposite graph = new ChartComposite(shlRecorderFileEditor, SWT.BORDER, freeChart, true);
		graph.setDisplayToolTips(false);
		graph.setHorizontalAxisTrace(true);
		graph.setVerticalAxisTrace(true);
		graph.setVerticalAxisTrace(false);
		graph.setSize(1002,418);
		graph.setLayoutData(new RowData(1002, 418));
		System.out.println("Graph size is " + graph.getSize());
		
		// The List
		List list = new List(shlRecorderFileEditor, SWT.BORDER);
		chartMgr.listRecords(list); // Populate List
		list.setLayoutData(new RowData(1002, 236));
		list.setSize(1002, 180);
		System.out.println("List size is " + list.getSize());
		
		Label statusBar = new Label(shlRecorderFileEditor, SWT.BORDER);
		statusBar.setText(fileName);
		statusBar.setLayoutData(new RowData(1002, 30));
		statusBar.setSize(1002, 30);
		System.out.println("Status Bar size is " + statusBar.getSize());
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
