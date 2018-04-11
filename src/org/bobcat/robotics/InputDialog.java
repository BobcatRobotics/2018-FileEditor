package org.bobcat.robotics;

import org.bobcat.robotics.EditData.Mode;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
	private Shell dialogShell;
	private String fileName = "";
	// For Validation
	private EditData dlgFields = new EditData();
		
	public InputDialog(Shell parent) {
		this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	}

	public InputDialog(Shell parent, int style) {
		super(parent, style);
		setText("Input Dialog");
	}

	public Shell getDialogShell() {
		return dialogShell;
	}
	
	public void setMaxValue(int totRecs) {
		dlgFields.setMaxValue(totRecs);
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isValidInput() {
		return dlgFields.isHasErrors();
	}

	public EditData open() {
		dialogShell = new Shell(getParent(), getStyle());
		dialogShell.setSize(308, 160);
		dialogShell.setText("Edit - " + fileName);
		createContents(dialogShell);
		dialogShell.pack();
		dialogShell.open();
		Display display = getParent().getDisplay();
		while (!dialogShell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return dlgFields;
	}

	private void createContents(final Shell shell) {
		shell.setLayout(new GridLayout(7, false));
	
		Label lblHeaderDel = new Label(shell, SWT.NONE);
		lblHeaderDel.setText(" Delete Rows");
		Button btnDelete = new Button(shell, SWT.CHECK);
		btnDelete.setText(" ");
		btnDelete.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnDelete.addSelectionListener(new SelectionAdapter() {
			@Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            dlgFields.setCurrentMode(btn.getSelection() ? Mode.DELETE : Mode.NO_SELECT); 
	        }
	    });
		Label lblHeaderAdd = new Label(shell, SWT.NONE);
		lblHeaderAdd.setText("    Add Rows");

		Button btnAdd = new Button(shell, SWT.CHECK);
		btnAdd.setText(" ");
		btnAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            dlgFields.setCurrentMode(btn.getSelection() ? Mode.ADD : Mode.NO_SELECT); 
	        }
	    });
	
		Label lblHeaderChg = new Label(shell, SWT.NONE);
		lblHeaderChg.setText(" Change Value");

		Button btnChgVal = new Button(shell, SWT.CHECK);
		btnChgVal.setText(" ");
		btnChgVal.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnChgVal.addSelectionListener(new SelectionAdapter() {
			@Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            dlgFields.setCurrentMode(btn.getSelection() ? Mode.CHANGE : Mode.NO_SELECT); 
	        }
	    });

		new Label(shell,SWT.NONE);
	
		Label lblDelFromRcd = new Label(shell, SWT.NONE);
		lblDelFromRcd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDelFromRcd.setText("From");

		final Text delFrom = new Text(shell, SWT.BORDER);
		delFrom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		delFrom.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setFrom(widget.getText());
			} 
		});
	
		Label lblUpdFromRcd = new Label(shell, SWT.NONE);
		lblUpdFromRcd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUpdFromRcd.setText("From");

		final Text updFrom = new Text(shell, SWT.BORDER);
		updFrom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		updFrom.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setFrom(widget.getText());
			} 
		});
		
		Label lblChangeFromRcd = new Label(shell, SWT.NONE);
		lblChangeFromRcd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblChangeFromRcd.setText("From");

		final Text chgFrom = new Text(shell, SWT.BORDER);
		chgFrom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		chgFrom.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setFrom(widget.getText());
			} 
		});
		
		Button btnChgPwr = new Button(shell, SWT.CHECK);
		btnChgPwr.setText("Power     ");
		btnChgPwr.addSelectionListener(new SelectionAdapter() {
			@Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            dlgFields.setChangePower(btn.getSelection()); 
	        }
	    });

		Label lblDelToRcd = new Label(shell, SWT.NONE);
		lblDelToRcd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDelToRcd.setText("To");

		final Text delTo = new Text(shell, SWT.BORDER);
		delTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		delTo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setTo(widget.getText());
			} 
		});

		Label lblUpdToRcd = new Label(shell, SWT.NONE);
		lblUpdToRcd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUpdToRcd.setText("#Recs");

		final Text updTo = new Text(shell, SWT.BORDER);
		updTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		updTo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setTo(widget.getText());
			} 
		});

		Label lblChgToRcd = new Label(shell, SWT.NONE);
		lblChgToRcd.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblChgToRcd.setText("To");

		final Text chgTo = new Text(shell, SWT.BORDER);
		chgTo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		chgTo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setTo(widget.getText());
			} 
		});

		Button btnChgVel = new Button(shell, SWT.CHECK);
		btnChgVel.setText("Velocity  ");
		btnChgVel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnChgVel.addSelectionListener(new SelectionAdapter() {
			@Override
	        public void widgetSelected(SelectionEvent event) {
	            Button btn = (Button) event.getSource();
	            dlgFields.setChangeVelocity(btn.getSelection()); 
	        }
	    });

		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		Label lblLeftValue = new Label(shell, SWT.NONE);
		lblLeftValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblLeftValue.setText("Left Amt");
	
		final Text chgLeftValue = new Text(shell, SWT.BORDER);
		chgLeftValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		chgLeftValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setLeftValue(widget.getText());
			} 
		});

		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		Label lblRightValue = new Label(shell, SWT.NONE);
		lblRightValue.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblRightValue.setText("Right Amt");
	
		final Text chgRightValue = new Text(shell, SWT.BORDER);
		chgRightValue.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		chgRightValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent mv) {
				Text widget = (Text)mv.getSource();
				dlgFields.setRightValue(widget.getText());
			} 
		});
		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);
		new Label(shell,SWT.NONE);

		Button btnUpdate = new Button(shell, SWT.PUSH);
		btnUpdate.setText("Update");
		btnUpdate.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnUpdate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// Validate 
				validatePanel();
				shell.close();
			}
		});

		shell.setDefaultButton(btnUpdate);

		Button btnCancel = new Button(shell, SWT.PUSH);
		btnCancel.setText("Cancel");
		btnCancel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnCancel.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}
		});
	}
	
	private void validatePanel() {
		dlgFields.validate();
	}
}
