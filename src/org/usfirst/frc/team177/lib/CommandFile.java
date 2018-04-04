package org.usfirst.frc.team177.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

//import edu.wpi.first.wpilibj.Timer;

public class CommandFile {
	private static String path = File.separator + "home" + File.separator + "lvuser" + File.separator;

	private static List<CommandRecord> commands = new ArrayList<CommandRecord>();
	private static CommandRecord eof = new CommandRecord().endOfFile();
	private String fileName;

	private int passCtr = 0;
	private int maxCtr = 0;
	private double startTime = 0.0;

	private CommandFile() {
		//startTime = Timer.getFPGATimestamp();
		startTime = System.currentTimeMillis();
	}

	public CommandFile(String shortName) {
		this();
		// Check filename 
		if (!shortName.contains(File.separator))
			shortName = path + shortName;
		this.fileName = shortName;
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
		} catch (FileNotFoundException e) {
			RioLogger.errorLog("CommandFile.readRecording() error " + e.getMessage());
		}
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
}
