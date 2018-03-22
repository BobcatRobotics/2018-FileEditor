package org.usfirst.frc.team177.lib;

public class TestNoRobot {
	//static final String filename = "right2scale.txt";
	static final String basefilename = "commands.txt";
	static final double NBR_TEST_RECS = 50;

	public static void main(String[] args) throws InterruptedException {
		log("main() called()");
		RioLoggerThread.getInstance();
		log("main RioLoggerThread instance.");

		for (int i = 0; i < NBR_TEST_RECS; i++)
			RioLogger.debugLog("line number is " + i);
		log("sleeping for (seconds) " + 180000 /1000);

		Thread.currentThread();
		Thread.sleep(32000);
		RioLogger.debugLog("done sleeping for 32 seconds.");
		RioLoggerThread.setLoggingParameters(61, 15);
		RioLogger.debugLog("switched to 1 minute every 15 seconds. ");
		
		Thread.sleep(61500);
		RioLogger.debugLog("main thread finished");
		log("main done sleeping");
		RioLoggerThread.stopLogging();
		RioLogger.debugLog("stopLogging() called");
		
	}
	
	private static void log(String text) {
		RioLogger.log(text);
		System.out.println(text);
	}
	

}
