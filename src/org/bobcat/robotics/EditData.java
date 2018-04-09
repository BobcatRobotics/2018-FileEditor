package org.bobcat.robotics;



public class EditData {
	private Mode currentMode = Mode.NO_SELECT;
	private String from = "";
	private String to = "";
	private String leftValue = "";
	private String rightValue = "";
	private boolean changePower = false;
	private boolean changeVelocity = false;
	private int maxValue = 0;
	private boolean hasErrors = false;
	private String errors = "";
	
	public EditData() {
		super();
	}

	public Mode getCurrentMode() {
		return currentMode;
	}
	
	public String getErrors() {
		return errors;
	}

	public boolean isHasErrors() {
		return hasErrors;
	}

	public boolean isChangePower() {
		return changePower;
	}

	public boolean isChangeVelocity() {
		return changeVelocity;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public String getLeftValue() {
		return leftValue;
	}

	public String getRightValue() {
		return rightValue;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setCurrentMode(Mode currentMode) {
		this.currentMode = currentMode;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public void setLeftValue(String value) {
		this.leftValue = value;
	}

	public void setRightValue(String value) {
		this.rightValue = value;
	}

	public void setChangePower(boolean changePower) {
		this.changePower = changePower;
	}

	public void setChangeVelocity(boolean changeVelocity) {
		this.changeVelocity = changeVelocity;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public void validate() {
		hasErrors = false;
		// Check if a mode has been selected 
		if (Mode.NO_SELECT.equals(currentMode)) {
			errors = "Please select Add, Delete, or Change";
			hasErrors = true;
			return;
		}
		// Once Selected Make sure fields are good
		if (validateStrings(from,to)) {
			if(Mode.DELETE.equals(currentMode)) {
				errors =  "Delete Error - From,To values are not numeric or not in range.";
				hasErrors = true;
			}
			else if (Mode.CHANGE.equals(currentMode)) {
				errors = "Change Value Error - From,To values are not numeric or not in range.";
				hasErrors = true;
			}
			else {
				errors = "Add Error - From,To values are not numeric or not in range.";
				hasErrors = true;
			}
		}
		// Check if Change Values
		if (Mode.CHANGE.equals(currentMode)) {
			if (!changePower && !changeVelocity) {
				errors = "Change Values Error - Select Power or Velocity";
				hasErrors = true;
			}
			if (!isDouble(leftValue)) {
				errors = "Change Values Error - Left Value Field must be a double";
				hasErrors = true;
			}
			if (!isDouble(rightValue)) {
				errors = "Change Values Error - Right Value Field must be a double";
				hasErrors = true;
			}
		}
		return;
	}
	
	private boolean validateStrings(String inpt1,String inpt2) {
		boolean goodVals = true;
		if (isInteger(inpt1) && isInteger(inpt2))
		{
			int val1 = new Integer(inpt1);
			int val2 = new Integer(inpt2);
			if (val1 < 0  || val1 > maxValue-1)
				goodVals = false;
			if (val2 < 0  || val2 > maxValue-1)
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
	
	private boolean isDouble(String s) {
		boolean isValid = true;
		if (s.length() == 0) {
			isValid = false;
		} else {
		    try { 
		        Double.parseDouble(s); 
		    } catch(NumberFormatException e) { 
		    	isValid = false; 
		    } 
		}
	    return isValid;
	}
	
	/**
	 *  Edit Modes
	 *
	 */
	public enum Mode {
		/**
		 *  D = Delete
		 *  A = Add 
		 *  C = Change
		 */
		NO_SELECT ("X"),
		DELETE ("D"),
		ADD("A"),
		CHANGE("C");
		
		private String command = "  ";
		private Mode(String p) {
			command = p;
		}
		
		public String getCommand () {
			return command;
		}
		
		public static Mode fromString(String text) {
			for (Mode cmd : Mode.values()) {
				if (cmd.command.equalsIgnoreCase(text)) {
					return cmd;
				}
			}
			return null;
		}
	}
	

}
