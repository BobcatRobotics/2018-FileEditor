package org.usfirst.frc.team177.lib;

//import edu.wpi.first.wpilibj.Timer;

public class CommandRecord {
	private static final String ON = "1";
	private static final String OFF = "0"; 
	
	// object keys
	private Commands id;
	private double totalTime = 0.0;

	// Motor Speeds
	private double leftPower = 0.0;
	private double rightPower = 0.0;
	private boolean state = false;
	
	public CommandRecord() {
		this.id = Commands.NONE;
	}

	public void setCommandKeys(Commands id, double startTime) {
		this.id = id;
		//totalTime = Timer.getFPGATimestamp() - startTime;
		totalTime = System.currentTimeMillis() - startTime;
	}

	public void setCommandKeys(String speedObjectReadable) {
		String[] result = speedObjectReadable.split("\\s+");
		id = Commands.fromString(result[0]);
		totalTime = new Double(result[1]);
	}

	public Commands getID() {
		return id;
	}
	
	public double getTotalTime() {
		return totalTime;
	}
	
	public void setPower(double leftPower, double rightPower) {
		this.leftPower = leftPower;
		this.rightPower = rightPower;
	}

	public double[] getSpeed() {
		double[] row = new double[2];
		row[0] = leftPower;
		row[1] = rightPower;
		return row;
	}

	public void setState(String value) {
		if (ON.equals(value)) {
			setStateOn();
		} else {
			setStateOff();
		}
	}
	
	public void setState(boolean value) {
		if (value) {
			setStateOn();
		} else {
			setStateOff();
		}
	}
	
	public void setStateOn() {
		state = true;
		leftPower = 0.0;
		rightPower = 0.0;
	}
	
	public void setStateOff() {
		state = false;
		leftPower = 0.0;
		rightPower = 0.0;
	}
	
	public boolean getState() {
		return state;
	}
	
	public CommandRecord endOfFile() {
		id = Commands.EOF;
		//totalTime = 9999999.0; //System.currentTimeMillis  (millis)
		totalTime = 9999.0; //Timer.getFPGATimestamp()  (seconds)  
		leftPower = 999.0;
		rightPower = 999.0;
		state = false;
		return this;
	}

	@Override
	public String toString() {
		String formatter =  "%2s %10.5f %8.5f %8.5f %1s"; // Convert time to seconds
		String fmt = String.format(formatter,id.getCommand(),totalTime,leftPower,rightPower,(state?ON:OFF));
		return fmt;
	}
}
