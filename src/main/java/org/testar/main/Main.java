package org.testar.main;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.testar.main.jacoco.MBeanClient;

public class Main {

	public static void main(String[] args) {
		// Maybe I dont need processPid if we hardcode jacoco jmx port to be 5000
		// -Dcom.sun.management.jmxremote.port=5000

		// Check that process pid exists
		// TODO: Change to process name or get the pid from the apache java process
		int processPid = Integer.parseInt(args[0]);

		// Check that seconds are between valid range ( 2 < x < 10) maybe
		int milisecondsExtract = Integer.parseInt(args[1]);

		while(isProcessIdRunningOnWindows(processPid)) {
			// Dump the jacoco report with the current timestamp
			MBeanClient.dumpJacocoReport();
			
			try {
				Thread.sleep(milisecondsExtract);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * https://stackoverflow.com/a/18004153
	 * 
	 * @param pid
	 * @return
	 */
	public static boolean isProcessIdRunningOnWindows(int pid){
		try {
			Runtime runtime = Runtime.getRuntime();
			String cmds[] = {"cmd", "/c", "tasklist /FI \"PID eq " + pid + "\""};
			Process proc = runtime.exec(cmds);

			InputStream inputstream = proc.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line;
			while ((line = bufferedreader.readLine()) != null) {
				//Search the PID matched lines single line for the sequence: " 1300 "
				//if you find it, then the PID is still running.
				if (line.contains(" " + pid + " ")){
					return true;
				}
			}

			return false;
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("Cannot query the tasklist for some reason.");
			System.exit(0);
		}

		return false;
	}
}
