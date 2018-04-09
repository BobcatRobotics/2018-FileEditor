package org.bobcat.robotics;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bobcat.robotics.EditData.Mode;
import org.usfirst.frc.team177.lib.CommandFile;
import org.usfirst.frc.team177.lib.CommandRecord;
import org.usfirst.frc.team177.lib.Commands;

public class CmdFileManager {
	private static final String dateFmt = "'.'yyyy-MM-dd_hh.mm.ss'.txt'";

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
		cmdData = new ArrayList<CommandRecord>(nbrCommands);
		int recCtr = 0;
		while ( true) {
			CommandRecord cmdRec = cmdFile.getRawData(recCtr);
			if (Commands.EOF.equals(cmdRec.getID())) {
				break;
			}
			cmdData.add(cmdRec);
			recCtr++;
		}
		nbrCommands = recCtr;
		return cmdData;
	}
	
	public boolean updateCmdFile(Mode mode, String inputFrom, String inputTo,String leftValue,String rightValue) {
		String[] namesplit = fileName.split("\\.");
		String datePath = new SimpleDateFormat(dateFmt).format(new Date());
		String archiveCmdFileName = namesplit[0]  + datePath;
		return cmdFile.updateCMDFile(archiveCmdFileName, mode, new Integer(inputFrom),
				new Integer(inputTo),new Double(leftValue),new Double(rightValue));
	}


}
