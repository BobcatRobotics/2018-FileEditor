package org.bobcat.robotics;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.usfirst.frc.team177.lib.RioLogger;

public class TableDataEditor {
	private Table tbl = null;
	private TableEditor editor = null;
	private int [] editableColumns = null;
	private boolean inEditMode = false;
	private boolean dataChanged = false;
	private int currentEditRow = -1;
	private int currentEditColumn = -1;
	private int columnEdited = -1;
	private int rowEdited = -1;
	private String newEditValue = "";

	private TableDataEditor() {
		super();
	}

	public TableDataEditor(Table table) {
		this();
		this.tbl = table;
		initTableEditor();
		editValue();
	}
	
	public void resetEdit() {
		newEditValue = "";
		dataChanged = false;
		inEditMode = false;
		rowEdited = -1;
		columnEdited = -1;
		currentEditRow = -1;
		currentEditColumn = -1;

	}
	
	public boolean isDataChanged() {
		return dataChanged;
	}

//	public void setDataChanged(boolean dataChanged) {
//		this.dataChanged = dataChanged;
//	}
	
	public String getEditValue() {
		return newEditValue;
	}

	public int getRowEdited() {
		return rowEdited;
	}

	public int getColumnEdited() {
		return columnEdited;
	}

	public void setEditableColumns(int[] editableColumns) {
		this.editableColumns = editableColumns;
	}

	private void initTableEditor() {
		// The editor must have the same size as the cell and must
		// not be any smaller than 50 pixels.
		editor = new TableEditor(tbl);

		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		editor.minimumWidth = 50;
	}

	public void editValue() {
		tbl.addMouseListener(new MouseAdapter() {
			public void mouseDown(MouseEvent event) {

RioLogger.debugLog("!!!! TableDataEditor.editValue() changed is  " + dataChanged);
				
				Control old = editor.getEditor();
				if (old != null)
					old.dispose();
				Point pt = new Point(event.x, event.y);

				final TableItem item = tbl.getItem(pt);
				if (item == null) {
					return;
				}
				int column = -1;
				for (int colCtr = 0, n = tbl.getColumnCount(); colCtr < n; colCtr++) {
					Rectangle rect = item.getBounds(colCtr);
					if (rect.contains(pt)) {
						column = colCtr;
						break;
					}
				}

				// Check the column against editable columns
				boolean correctColumn = false;
				for(int editCol : editableColumns) {
					if (editCol == column) { 
						correctColumn = true;
						break;
					}
				}
				// Determine if a field has changed 
RioLogger.debugLog("!!!! TableDataEditor.editValue() editMode correctColumn " + inEditMode + " " + correctColumn);				
				if (inEditMode == false && correctColumn) {
					inEditMode = true;
					currentEditRow = new Integer(item.getText(0)); // Row Number
					currentEditColumn = column;
					// Row Selected for editing
					rowEdited = currentEditRow;
RioLogger.debugLog("!!!! TableDataEditor.editValue() setting columnEdited " +column)	;				
					columnEdited = column;
				} 
				if (inEditMode) {
					int mouseEditRow = new Integer(item.getText(0));
					int mouseEditColumn = column;
					if ((mouseEditRow != currentEditRow) ||
						(mouseEditColumn != currentEditColumn)) {
						dataChanged = true;
						currentEditRow = mouseEditRow;
						currentEditColumn = mouseEditColumn;
						inEditMode = correctColumn;
					}
				}
				if (!correctColumn) {
					return;
				}
				// Edit Data
				final Text txtEdit = new Text(tbl, SWT.NONE);
				txtEdit.setForeground(item.getForeground());
				txtEdit.setText(item.getText(column));
				txtEdit.setForeground(item.getForeground());
				txtEdit.selectAll();
				txtEdit.setFocus();
				editor.minimumWidth = txtEdit.getBounds().width;
				editor.setEditor(txtEdit, item, column);
				final int col = column;
				txtEdit.addModifyListener(new ModifyListener() {
					public void modifyText(ModifyEvent event) {
						String chg = txtEdit.getText();
						item.setText(col, chg);
						newEditValue = chg;
					}
				});
			}
		});
	}
}
