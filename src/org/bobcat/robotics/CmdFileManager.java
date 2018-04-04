package org.bobcat.robotics;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.team177.lib.CommandFile;
import org.usfirst.frc.team177.lib.CommandRecord;

public class CmdFileManager {
	private String fileName = null;
	private int nbrCommands = 0;
	private CommandFile cmdFile;
	private List<CommandRecord> cmdData;
	
	// Keep track of the row of each command
	int driveCnt = 0;
	int elevCnt = 0;
	int cubeArmCnt = 0;
	int cubeSpinnerCnt = 0;
	int fourBarCnt = 0;

	private CmdFileManager() {
		super();
	}

	public  CmdFileManager(String fileName) {
		this();
		this.fileName = fileName;
	}
	
	public int getTotalCommands() {
		return nbrCommands;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public List<CommandRecord> readCmdFile() {
		cmdFile = new CommandFile(fileName);
		cmdFile.readRecordingFile();
		nbrCommands = cmdFile.getNbrOfCommands();
		cmdData = new ArrayList<CommandRecord>(nbrCommands);
		for (int rec = 0; rec < nbrCommands; rec++) {
			CommandRecord cmdRec = cmdFile.getRawData(rec);
			cmdData.add(cmdRec);
		}
		return cmdData;
	}

}
