package org.bobcat.robotics;

//import org.usfirst.frc.team177.lib.Commands;

public enum GraphElements {
	EMPTY(0),
	POWER(1),
	DISTANCE(2),
	VELOCITY(3),
	ROBOTPATH(4);
	
	private int command = 0;
	private GraphElements(int p) {
		command = p;
	}
	
	public int getType () {
		return command;
	}
	
	
}
