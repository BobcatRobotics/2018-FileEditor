package org.usfirst.frc.team177.lib;

/**
 * This class enumerates the Commands that can be recorded
 */
public enum Commands {
	/**
	 * "  " = None
	 *  DT = Drive Chain
	 *  EL = Elevator Motors 
	 *  CS = Cube Spinners - Used for Eject and Pick UP
	 *  CA = Cube Arms - Open and Close
	 *  FB = Four Bar - Used for Up and Down
	 *  XX = End of File
	 */
	NONE ("  "),
	DRIVE_CHAIN ("DT"),
	ELEVATOR("EL"),
	CUBE_SPINNERS("CS"),
	CUBE_ARMS ("CA"),
	FOURBAR ("FB"),
	EOF("XX");
	
	private String command = "  ";
	private Commands(String p) {
		command = p;
	}
	
	public String getCommand () {
		return command;
	}
	
	public static Commands fromString(String text) {
		for (Commands cmd : Commands.values()) {
			if (cmd.command.equalsIgnoreCase(text)) {
				return cmd;
			}
		}
		return null;
	}
}
