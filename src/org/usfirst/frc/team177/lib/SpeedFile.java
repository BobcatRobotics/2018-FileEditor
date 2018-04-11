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

//import edu.wpi.first.wpilibj.DriverStation;
//import edu.wpi.first.wpilibj.Timer;

public class SpeedFile {
	private static String path = File.separator + "home" + File.separator + "lvuser" + File.separator;
	private static final String dateFmt = "yyyy-MM-dd_hh.mm.ss'.txt'";

	private static List<SpeedRecord> speeds = new ArrayList<SpeedRecord>();
	private static SpeedRecord eof = new SpeedRecord().endOfFile();
	private String fileName;
	private String shortName;

	private int passCtr = 0;
	private int maxCtr = 0;
	private double speedEntryTime = 0.0;
	private double startTime = 0.0;

	private SpeedFile() {
		//speedEntryTime = Timer.getFPGATimestamp();
		speedEntryTime = System.currentTimeMillis();
	}

	public SpeedFile(String name) {
		this();
		this.shortName = name;
		// Check filename 
		if (!name.contains(File.separator))
			name = path + name;
		this.fileName = name;	
	}

	private void reset() {
		passCtr = 0;
		maxCtr = 0;
		//speedEntryTime = Timer.getFPGATimestamp();
		speedEntryTime = System.currentTimeMillis();
		startTime = speedEntryTime;
	}

	public void startRecording() {
		reset();
		speeds.clear();
	}

	public void stopRecording() {
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (SpeedRecord speedObj : speeds) {
				printWriter.println(speedObj.toString());
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "SpeedFile.stopRecording() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
		}
	}
	
	public List<SpeedRecord> getSpeedFile() {
		return speeds;
	}

	public void readRecordingFile() {
		reset();
		speeds.clear();
		String sEOF = new Integer(SpeedRecord.EOF).toString();
		Scanner sc;
		try {
			sc = new Scanner(new File(fileName));
			while (sc.hasNextLine()) {
				String row = sc.nextLine();
				String[] result = row.split("\\s+");
				if (sEOF.equals(result[0]))
					break;
				SpeedRecord speedObj = new SpeedRecord();
				speedObj.setReadKeys(row);
				speedObj.setPower(new Double(result[3]), new Double(result[4]));
				speedObj.setDistance(new Double(result[5]), new Double(result[6]));
				speedObj.setVelocity(new Double(result[7]), new Double(result[8]));
				speeds.add(speedObj);
				//RioLogger.debugLog("added record " + passCtr + " " +speedObj.toString());
				passCtr++;
			}
			sc.close();
			maxCtr = passCtr;
			RioLogger.debugLog("maxCtr = " + maxCtr);
			// File was read, now prime the passCounter for reading back each row
			passCtr = 0;
		} catch (FileNotFoundException e) {
			String err = "SpeedFile.readRecoring() error " + e;
			RioLogger.debugLog(err);
		}
	}
	
	public boolean saveFile() {
		boolean updated = backUpFile();
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (SpeedRecord speedObj : speeds) {
				printWriter.println(speedObj.toString());
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "SpeedFile.saveFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		readRecordingFile();
		return updated;
	}
	
	// This is temporary 
	// TODO:: Remove once GrayHills are in sync
	// TODO:: Changes made 04-05-195
	// TODO:: XXXXXXXXXXXXXXX
	public boolean updategrayHillValue() {
		//RioLogger.debugLog("fromRec - toRec " + fromRec + ", " + toRec);
		boolean updated = backUpFile();
		   
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			boolean firstRecord = true;
			double leftDistance = 0.0;
			double rightDistance = 0.0;
			// First find max and min,
			for (SpeedRecord speedObj : speeds) {
				double [] dist = speedObj.getDistance();
				double [] velocity = speedObj.getVelocity();
				if (firstRecord) {
					firstRecord = false;
					leftDistance = dist[0];
					rightDistance = dist[1];
					RioLogger.log("starting distances are " + leftDistance + ", " + rightDistance);
				}
//				double newLeftDistance = dist[0] - leftDistance;
//				double newRightDistance = dist[1] - rightDistance;
				double newLeftDistance = dist[0];
				double newRightDistance = dist[1];
						// newRightDistance *= - 1.0;
				double newRightVelocity = velocity[1] * -1.0;
				//double newRightVelocity = velocity[1];
				speedObj.setVelocity(velocity[0], newRightVelocity);
				speedObj.setDistance(newLeftDistance, newRightDistance);
				printWriter.println(speedObj.toString());
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "SpeedFile.updateRecordingFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		return updated;
	}
	
	private boolean backUpFile() {
		boolean updated = true;
		String[] namesplit = shortName.split("\\.");
		String datePath = new SimpleDateFormat(dateFmt).format(new Date());
		String archiveSpeedFileName = namesplit[0] + ".speeds." + datePath;

		File source = new File(fileName);
		File dest =  new File(path+archiveSpeedFileName);
		   try {
			Files.copy(source.toPath(), dest.toPath());
		} catch (IOException e) {
			String err = "SpeedFile.backUpFile() error " + e;
			RioLogger.debugLog(err);
			updated = false;
		}
		return updated;
	}

	public boolean updateRecordingFile(Mode mode,boolean isPower,int fromRec,int toRec,double leftValue,double rightValue) {
		RioLogger.debugLog("fromRec - toRec " + fromRec + ", " + toRec);
		boolean updated = backUpFile();
		if (Mode.DELETE.equals(mode)) {
			updated = deleteUpdate(fromRec,toRec);
		} else if (Mode.ADD.equals(mode)) {
			updated = addUpdate(fromRec,toRec);
		} else if (Mode.CHANGE.equals(mode)) {
			updated = chgUpdate(isPower,fromRec,toRec,leftValue,rightValue);
		}
		return updated;
	}
	
	private boolean chgUpdate(boolean isPower,int fromRec, int toRec, double leftVal, double rightVal) {
		boolean updated = false;
		int recCtr = 0;
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (SpeedRecord speedObj : speeds) {
				if (recCtr >= fromRec && recCtr <= toRec) {
					if (isPower)
						speedObj.setPower(leftVal, rightVal);
					else
						speedObj.setVelocity(leftVal, rightVal);
				} 
				printWriter.println(speedObj.toString());
				recCtr++;
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
			updated = true;
		} catch (IOException e) {
			String err = "SpeedFile.chgUpdate() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		
		return updated;	
	}

	private boolean deleteUpdate(int fromRec, int toRec) {
		boolean updated = true;
//		double totalTimeFrom = 0.0;
//		double totalTimeTo = 0.0;
		double newTime = 0.0;
		//boolean deleteZeroRec = (fromRec == 0);
		// Special condition, fromRec = 0
//		if (deleteZeroRec) {
//			SpeedRecord firstRecord = speeds.get(0);
//			totalTimeFrom = firstRecord.getElapsedTime(false);
//		} else {
//			SpeedRecord fromRecord = speeds.get(fromRec - 1);
//			totalTimeFrom = fromRecord.getElapsedTime(false);
//		}
//		totalTimeTo = speeds.get(toRec).getElapsedTime(false);
//		double subtractTime = totalTimeTo - totalTimeFrom;
		int recCtr = 0;
		int newRecCtr = 0;
		boolean hitAdjust = false;
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (SpeedRecord speedObj : speeds) {
				// Special condition, fromRec = 0
				// if (deleteZeroRec) {
				// printWriter.println(speedObj.toString());
				// }
				if (recCtr < fromRec) {
					printWriter.println(speedObj.toString());
					recCtr++;
				} else if (recCtr >= fromRec && recCtr <= toRec) {
					if (!hitAdjust) {
						hitAdjust = true;
						newRecCtr = recCtr;
						// if (deleteZeroRec) {
						// newRecCtr = recCtr;
						// deleteZeroRec = false;
						// }
					}
					recCtr++;
				} else if (recCtr > toRec) {
					newTime += 0.02;
					// double newTotalTime = speedObj.getElapsedTime(false) - subtractTime;
					printWriter.println(speedObj.toStringUpdate(newRecCtr, newTime));
					recCtr++;
					newRecCtr++;
				}
				if (recCtr == fromRec)
					newTime = speedObj.getElapsedTime(false);
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "SpeedFile.updateRecordingFile() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		
		return updated;
	}
	
	private boolean addUpdate(int fromRec, int toRec) {
		RioLogger.debugLog("SpeedFile.addUpdate() fromRec, toRec " + fromRec + " " + toRec );
//		double timeAdded = ((toRec - fromRec) + 1) * 0.02; // nbrRecords * 20 millis
		double totTime = 0.0;
		boolean updated = true;
		int recCnt = 0;
		int newRecCnt = 0;
		SpeedRecord prevRec;
		SpeedRecord newRec  = new SpeedRecord();
		try {
			File file = new File(fileName);
			FileWriter fileWriter = new FileWriter(file);
			PrintWriter printWriter = new PrintWriter(fileWriter);
			for (SpeedRecord speedObj : speeds) {
				if (SpeedRecord.EOF == speedObj.getID())
					break;
				if (recCnt == fromRec) {
					prevRec = speedObj;
					newRec.setPower(prevRec.getPower()[0], prevRec.getPower()[1]);
					newRec.setDistance(prevRec.getDistance()[0],prevRec.getDistance()[1]);
					newRec.setVelocity(prevRec.getVelocity()[0], prevRec.getVelocity()[1]);
					totTime = prevRec.getElapsedTime(false);
					totTime += 0.02;
				}		
				if (recCnt > fromRec) {
					speedObj.setReadKeys(formatKey(newRecCnt,totTime,0.02));
					printWriter.println(speedObj.toString());
					totTime += 0.02;
				} else if (recCnt < fromRec) {
					printWriter.println(speedObj.toString());
					totTime += 0.02;
				} else if (recCnt == fromRec) {
					// Inside the Block
					printWriter.println(speedObj.toString());
					for (int newCtr = 0; newCtr < (toRec - fromRec);newCtr++) {
						totTime += 0.02;
						newRecCnt++;
						newRec.setReadKeys(formatKey(newRecCnt,totTime,0.02));
						printWriter.println(newRec.toString());
					}
				}
				newRecCnt++;
				recCnt++;
			}
			printWriter.println(eof.toString());
			printWriter.flush();
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			String err = "SpeedFile.addUpdate() error " + e.getMessage();
			//DriverStation.reportError(err, false);
			//RioLogger.log(err);
			RioLogger.debugLog(err);
			updated = false;
		}
		
		return updated;			
	}
	
	private String formatKey(int recId, double totTime, double deltaTime) {
		return String.format("%04d %10.5f %8.5f ", recId,totTime, deltaTime);
		
	}

	public SpeedRecord getRawData(int index) {
		SpeedRecord speedObj = eof;
		if (index < maxCtr) {
			speedObj = speeds.get(index);
		}
		return speedObj;
	}
	
	public int getID(int index) {
		SpeedRecord speedObj = speeds.get(index);
		return speedObj.getID();
	}

	public void addSpeed(double leftPower, double rightPower, double leftDistance, double rightDistance,
			double leftVelocity, double rightVelocity) {
		SpeedRecord speedObject = new SpeedRecord();
		speedObject.setSpeedKeys(passCtr, startTime, speedEntryTime);
		speedObject.setPower(leftPower, rightPower);
		speedObject.setDistance(leftDistance, rightDistance);
		speedObject.setVelocity(leftVelocity, rightVelocity);
		speeds.add(speedObject);

		//speedEntryTime = Timer.getFPGATimestamp();
		speedEntryTime = System.currentTimeMillis();
		passCtr++;
		maxCtr = passCtr;
	}

	public int getNbrOfRows() {
		return maxCtr;
	}

	public double getTotalTime() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}	
		return speedObj.getElapsedTime(false);
	}
	// getPower() advances passCtr
	public double[] getPower() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}
		passCtr++;
		return speedObj.getPower();
	}

	public double[] getDistance() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}
		return speedObj.getDistance();
	}

	public double[] getVelocity() {
		SpeedRecord speedObj = eof;
		if (passCtr < maxCtr) {
			speedObj = speeds.get(passCtr);
		}
		return speedObj.getVelocity();
	}

	public boolean updateValue(int recordNbr, int column, double value) {
		boolean updated = false;
		RioLogger.debugLog("**** SpeedFile.updateValue maxCtr " + maxCtr);		
		
		if (recordNbr < maxCtr) {
			SpeedRecord rec = speeds.get(recordNbr);
			if (column ==  3 || column == 4) {
				if (column == 3) {
					rec.setPower(value,rec.getPower()[1]);
				} else {
					rec.setPower(rec.getPower()[0], value);
				}
			}
			if (column ==  7 || column == 8) {
				if (column == 7) {
					rec.setVelocity(value,rec.getVelocity()[1]);
				} else {
					rec.setVelocity(rec.getVelocity()[0], value);
				}
			}
			updated = true;		
		}
		return updated;
	}


}
