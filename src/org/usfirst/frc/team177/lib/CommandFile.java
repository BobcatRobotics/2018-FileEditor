package org.usfirst.frc.team177.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

import org.bobcat.robotics.EditData.Mode;

//import edu.wpi.first.wpilibj.Timer;

public class CommandFile {
	private static final String path = File.separator + "home" + File.separator + "lvuser" + File.separator;
	private static final String dateFmt = "'.'yyyy-MM-dd_hh.mm.ss'.txt'";

	private static List<CommandRecord> commands = new ArrayList<CommandRecord>();
	private static CommandRecord eof = new CommandRecord().endOfFile();
	private String fileName;
	private String shortName;
	private int[][] fileLinks = null;

	private int passCtr = 0;
	private int maxCtr = 0;
	private double startTime = 0.0;

	private CommandFile() {
		//startTime = Timer.getFPGATimestamp();
		startTime = System.currentTimeMillis();
	}

	public CommandFile(String name) {
		this();
		// Check filename 
		this.shortName = name;
		if (!name.contains(File.separator))
			name = path + name;
		this.fileName = name;
	}

	private void reset() {
		passCtr = 0;
		maxCtr = 0;
		//startTime = Timer.getFPGATimestamp();
		startTime = System.currentTimeMillis();
	}

	public void startRecording() {
		reset();
		commands.clear();
	}

	public void stopRecording() {
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (CommandRecord cmd : commands) {
				printWriter.println(cmd.toString());
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
		} catch (IOException e) {
			RioLogger.errorLog("CommandFile.stopRecording() error " + e.getMessage());
		}
	}

	public void readRecordingFile() {
		reset();
		commands.clear();

		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			while (sc.hasNextLine()) {
				String row = sc.nextLine();
				String[] result = row.split("\\s+");
				CommandRecord cmd = new CommandRecord();
				cmd.setCommandKeys(row);
				cmd.setState(result[4]);
				cmd.setPower(new Double(result[2]), new Double(result[3]));
				commands.add(cmd);
				passCtr++;
			}
			maxCtr = passCtr;
			// File was read, now prime the passCounter for reading back each row
			passCtr = 0;
			// File was read, build the array that links Speed Records to CMD Records
			fileLinks = new int[maxCtr][maxCtr];
			int speedRec = 0;
			int cmdRec = 0;
			int fileLinkCtr = 0;
			for (CommandRecord cmd : commands) {
				fileLinks[fileLinkCtr][0] = speedRec;
				fileLinks[fileLinkCtr][1] = cmdRec;
				if(Commands.DRIVE_CHAIN.equals(cmd.getID())) {
					speedRec++;
				}
				cmdRec++;
				fileLinkCtr++;
			}
		} catch (FileNotFoundException e) {
			RioLogger.errorLog("CommandFile.readRecording() error " + e.getMessage());
		}
	}

	public boolean updateCMDFile(Mode mode, int fromRec, int toRec,double leftValue,double rightValue) {
		RioLogger.debugLog("fromRec - toRec " + fromRec + ", " + toRec);
		boolean updated = backUpFile();
		if (updated) {
			if (Mode.DELETE.equals(mode)) {
				updated = deleteCMDFile(fromRec,toRec);
			} else if (Mode.ADD.equals(mode)) {
				updated = addCMDFile(fromRec,toRec);
			} else if (Mode.CHANGE.equals(mode)) {
				updated = changeCMDFile(fromRec,toRec,leftValue,rightValue);
			}
		}
		return updated;
	}

	private boolean deleteCMDFile(int fromRec, int toRec) {
		double timeDeleted = ((toRec - fromRec) + 1) * 0.02; // nbrRecords * 20 millis
		double prevCmdTime = 0.0;
		boolean updated = true;
		int actualFromRecord = 0;
		int actualToRecord = 0;
		//int driveRecordsCnt = 0; 
		int recCnt = 0;
		RioLogger.debugLog("CommandFile.deleteCMDFile() fromRec, toRec " + fromRec + " " + toRec );
		for (int[] fLink : fileLinks) {
			if(actualFromRecord == 0 && fLink[0] == fromRec)
				actualFromRecord = fLink[1];
			if(actualToRecord == 0 && fLink[0] == toRec)
				actualToRecord = fLink[1];
		}
		RioLogger.debugLog("CommandFile.deleteCMDFile() actualFromRecord, actualToRecord " + actualFromRecord + " " + actualToRecord );
		recCnt = 0;
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (CommandRecord cmd : commands) {
				if (Commands.EOF.equals(cmd.getID()))
					break;
	
				if ((recCnt < actualFromRecord)  || (recCnt > actualToRecord)) {
					if (recCnt > actualToRecord)
						cmd.updateTime(cmd.getTotalTime() - timeDeleted);
					printWriter.println(cmd.toString());
				} else {
					// Inside the Block
					if (!Commands.DRIVE_CHAIN.equals(cmd.getID())) {
						cmd.updateTime(prevCmdTime);
						printWriter.println(cmd.toString());
					}
				}
				if (recCnt == (actualFromRecord - 1)) {
					prevCmdTime = cmd.getTotalTime();
				}
				recCnt++;
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "CommandFile.deleteCMDFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		
		return updated;		
	}

	private boolean changeCMDFile(int fromRec, int toRec,double leftPower,double rightPower) {
		boolean updated = true;
		int actualFromRecord = 0;
		int actualToRecord = 0;
		int recCnt = 0;
		RioLogger.debugLog("CommandFile.changeCMDFile() fromRec, toRec " + fromRec + " " + toRec );
		for (int[] fLink : fileLinks) {
			if(actualFromRecord == 0 && fLink[0] == fromRec)
				actualFromRecord = fLink[1];
			if(actualToRecord == 0 && fLink[0] == toRec)
				actualToRecord = fLink[1];
		}
		RioLogger.debugLog("CommandFile.changeCMDFile() actualFromRecord, actualToRecord " + actualFromRecord + " " + actualToRecord );
		recCnt = 0;
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (CommandRecord cmd : commands) {
				if (Commands.EOF.equals(cmd.getID()))
					break;
				if ((recCnt >= actualFromRecord)  && (recCnt <= actualToRecord)) {
					cmd.setPower(leftPower, rightPower);
				} 
				printWriter.println(cmd.toString());
				recCnt++;
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "CommandFile.changeCMDFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		return updated;		
	}
	
	private boolean addCMDFile(int fromRec, int toRec) {
		double timeAdded = ((toRec - fromRec) + 1) * 0.02; // nbrRecords * 20 millis
		double prevCmdTime = 0.0;
		boolean updated = true;
		int actualFromRecord = 0;
		int actualToRecord = 0;
		int recCnt = 0;
		RioLogger.debugLog("CommandFile.addCMDFile() fromRec, toRec " + fromRec + " " + toRec );
		for (int[] fLink : fileLinks) {
			if(actualFromRecord == 0 && fLink[0] == fromRec)
				actualFromRecord = fLink[1];
			if(actualToRecord == 0 && fLink[0] == toRec)
				actualToRecord = fLink[1];
		}
		RioLogger.debugLog("CommandFile.addCMDFile() actualFromRecord, actualToRecord " + actualFromRecord + " " + actualToRecord );
		recCnt = 0;
		CommandRecord newRec = new CommandRecord();
		newRec.setID(Commands.DRIVE_CHAIN);
		newRec.setPower(0.0, 0.0);
		newRec.setStateOff();

		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (CommandRecord cmd : commands) {
				if (Commands.EOF.equals(cmd.getID()))
					break;
				if ((recCnt > actualFromRecord)) {
						cmd.updateTime(cmd.getTotalTime() + timeAdded);
					printWriter.println(cmd.toString());
				} else if (recCnt == actualFromRecord) {
					// Inside the Block
					for (int newRecCtr = 0; newRecCtr < (toRec - fromRec);newRecCtr++) {
						newRec.updateTime(prevCmdTime + (newRecCtr * 0.02));
						printWriter.println(newRec.toString());
					}
				}
				if (recCnt == actualFromRecord) {
					prevCmdTime = cmd.getTotalTime();
				}			
				recCnt++;
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "CommandFile.addCMDFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		
		return updated;		
	}
	
	public boolean saveCmdFile() {
		boolean updated = backUpFile();
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (CommandRecord cmd : commands) {
				if (Commands.EOF.equals(cmd.getID()))
					break;
				printWriter.println(cmd.toString());
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "CommandFile.saveCmdFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		readRecordingFile();
		return updated;	
	}
	
	private boolean backUpFile() {
		boolean updated = true;
		String[] namesplit = shortName.split("\\.");
		String datePath = new SimpleDateFormat(dateFmt).format(new Date());
		String archiveCmdFileName = namesplit[0]  + datePath;
		File source = new File(fileName);
		File dest = new File(path + archiveCmdFileName);
		try {
			Files.copy(source.toPath(), dest.toPath());
		} catch (IOException e) {
			String err = "CommandFile.backUpFile() error " + e;
			RioLogger.debugLog(err);
			updated = false;
		} 
		return updated;
	}
	
	public CommandRecord getRawData(int index) {
		CommandRecord cmd = eof;
		if (index < maxCtr) {
			cmd = commands.get(index);
		}
		return cmd;
	}

	public void addCommand(Commands id,double leftPower, double rightPower, boolean state) {
		CommandRecord cmd = new CommandRecord();
		cmd.setState(state);
		cmd.setCommandKeys(id, startTime);
		cmd.setPower(leftPower, rightPower);
		commands.add(cmd);

		passCtr++;
		maxCtr = passCtr;
	}

	public int getNbrOfCommands() {
		return maxCtr;
	}

	public double[] getSpeed() {
		CommandRecord cmd = eof;
		if (passCtr < maxCtr) {
			cmd = commands.get(passCtr);
		}
		passCtr++;
		return cmd.getSpeed();
	}

	public boolean getState() {
		CommandRecord cmd = eof;
		if (passCtr < maxCtr) {
			cmd = commands.get(passCtr);
		}
		return cmd.getState();
	}

	public boolean updateLeftPower(int cmdNbr, Double leftPower) {
		boolean updated = false;
		if (cmdNbr < maxCtr) {
			CommandRecord rec = commands.get(cmdNbr);
			double rightPower = rec.getSpeed()[1];
			rec.setPower(leftPower, rightPower);
			updated = true;		
		}
		return updated;
	}

	public boolean updateRightPower(int cmdNbr, Double rightPower) {
		boolean updated = false;
		if (cmdNbr < maxCtr) {
			CommandRecord rec = commands.get(cmdNbr);
			double leftPower = rec.getSpeed()[0];
			rec.setPower(leftPower, rightPower);
			updated = true;		
		}
		return updated;
	}
}
