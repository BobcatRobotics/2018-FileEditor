package org.bobcat.robotics;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InputDialog extends Dialog {
	private String [] input;
	private Shell dialogShell;
	// For Validation
	private int totRecords= 0; 
	private boolean isValid = false;


	public InputDialog(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	public InputDialog(Shell parent, int style) {
		super(parent, style);
		setText("Input Dialog");
		input = new String[2];
	}

	public Shell getDialogShell() {
		return dialogShell;
	}
	
	public void setRecordCount(int totRecs) {
		totRecords = totRecs;
	}

	public boolean validInput() {
		return isValid;
	}

	public String [] open() {
		dialogShell = new Shell(getParent(), getStyle());
		dialogShell.setSize(308, 160);
		dialogShell.setText(getText());
		createContents(dialogShell);
		dialogShell.pack();
		dialogShell.open();
		Display display = getParent().getDisplay();
		while (!dialogShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return input;
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(2, true));
		
		Label lblFromRecord = new Label(shell, SWT.NONE);
		lblFromRecord.setText("From Record:");

		final Text textFrom = new Text(shell, SWT.BORDER);
		GridData dataFrom = new GridData(GridData.FILL_HORIZONTAL);
		textFrom.setLayoutData(dataFrom);

		Label lblToRecord = new Label(shell, SWT.NONE);
		lblToRecord.setText("To Record:");

		final Text textTo = new Text(shell, SWT.BORDER);
		GridData dataTo = new GridData(GridData.FILL_HORIZONTAL);
		textTo.setLayoutData(dataTo);

		Button ok = new Button(shell, SWT.PUSH);
		ok.setText("OK");
		dataFrom = new GridData(GridData.FILL_HORIZONTAL);
		ok.setLayoutData(dataFrom);
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				input[0] = textFrom.getText();
				input[1] = textTo.getText();
				// Validate 
				boolean valid = validateStrings(input[0],input[1]);
				if (!valid) {
					isValid = false;
					MessageDialog.openError(dialogShell, "Error", "Field values are incorrect.");
					return;
				}
				isValid = true;
				shell.close();
			}
		});

		shell.setDefaultButton(ok);

		Button cancel = new Button(shell, SWT.PUSH);
		cancel.setText("Cancel");
		dataFrom = new GridData(GridData.FILL_HORIZONTAL);
		cancel.setLayoutData(dataFrom);
		cancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				input[0] = "";
				input[1] = "";
				shell.close();
			}
		});
	}
	
	private boolean validateStrings(String inpt1,String inpt2) {
		boolean goodVals = true;
		if (isInteger(inpt1) && isInteger(inpt2))
		{
			int val1 = new Integer(inpt1);
			int val2 = new Integer(inpt2);
			if (val1 < 0  || val1 > totRecords-1)
				goodVals = false;
			if (val2 < 0  || val2 > totRecords-1)
				goodVals = false;
			if (val2 < val1)
				goodVals = false;
		} else {
			goodVals = false;
		}
		return goodVals;
	}
	
	private boolean isInteger(String s) {
		boolean isValid = true;
		if (s.length() == 0) {
			isValid = false;
		} else {
		    try { 
		        Integer.parseInt(s); 
		    } catch(NumberFormatException e) { 
		    	isValid = false; 
		    } 
		}
	    return isValid;
	}
}
