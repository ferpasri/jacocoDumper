package org.testar.main;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.testar.main.jacoco.JacocoReportReader;
import org.testar.main.jacoco.MBeanClient;
import org.testar.main.statemodel.StateModelMetrics;

import com.google.common.io.Files;

public class Main {

	private static String metricsFilename = "webCoverageMetrics";
	private static String statemodelFilename = "stateModelMetrics";

	public static void main(String[] args) {

		System.out.println("Welcome to jacocoDumper application");

		// Wait 5 second until the web server is deployed
		while(!localhostWebIsReady()) {
			System.out.println("Waiting for a web service in localhost:8080/parabank ...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		// Add the dumper execution timestamp to the metrics filename
		String fileTimestamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());
		metricsFilename = metricsFilename.concat("_" + fileTimestamp);	
		statemodelFilename = statemodelFilename.concat("_" + fileTimestamp);

		ExecutorService executor = Executors.newFixedThreadPool(10);

		// Web server is ready, now we want to extract the coverage every 5 seconds
		while(localhostWebIsReady()) {

			// Launch a thread that dumps the jacoco report with the current timestamp
			executor.submit(() -> {
				// Prepare the current thread time stamp
				String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());

				try {
					// Extract jacoco.exec file 2021_09_02_11_02_47_jacoco.exec
					String jacocoExecFile = MBeanClient.dumpJacocoReportWithTimestamp(timeStamp);
					String reportDir = new File("reports" + File.separator + timeStamp).getCanonicalPath();

					// Using "HTML destdir" inside build.xml -> Creates the directory automatically
					// But using only "CSV destfile" needs to create this directory first
					if(!new File(reportDir).exists()) {
						new File(reportDir).mkdirs();
					}

					// Launch jacoco report (build.xml) and overwrite desired parameters
					String antCommand = "cd jacoco && ant report"
							+ " -DjacocoFile=" + new File(jacocoExecFile).getCanonicalPath()
							+ " -DreportCoverageDir=" + reportDir;

					// Prepare process execution for linux or windows
					ProcessBuilder builder = new ProcessBuilder("/bin/sh", "-c", antCommand);
					if(System.getProperty("os.name").toLowerCase().contains("windows")) {
						builder = new ProcessBuilder("cmd.exe", "/c", antCommand);
					}

					Process p = builder.start();
					p.waitFor();

					if(!new File(reportDir + File.separator + "report_jacoco.csv").exists()) {
						System.out.println("************************************************");
						System.out.println("ERROR creating JaCoCo report");
						System.out.println("Check: If ant library is installed in the system");
						System.out.println("Command Line: ant -version");
						System.out.println("************************************************");
					}

					String coverage = JacocoReportReader.obtainCSVSummary(new File(reportDir + File.separator + "report_jacoco.csv").getCanonicalFile());
					String coverageInformation = "Time | " + timeStamp + " | " + coverage;
					System.out.println(coverageInformation);
					Writer.writeMetrics(new WriterParams.WriterParamsBuilder()
							.setFilename(metricsFilename)
							.setInformation(coverageInformation)
							.build());

					// Move the jacoco.exec file
					Files.move(new File(jacocoExecFile), new File(reportDir + File.separator + jacocoExecFile));

					// Query the OrientDB database and prepare the state model metrics
					String statemodel = StateModelMetrics.extractStateModelMetrics();
					String statemodelInformation = "Time | " + timeStamp + " | " + statemodel;
					System.out.println(statemodelInformation);
					Writer.writeMetrics(new WriterParams.WriterParamsBuilder()
							.setFilename(statemodelFilename)
							.setInformation(statemodelInformation)
							.build());

				} catch (IOException | InterruptedException e) {
					System.err.println("ERROR creating metrics reports");
					e.printStackTrace();
				}

			});

			// Wait 5 seconds for the next coverage
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println("Shutting down dumper threads...");

		executor.shutdown();
		try {
			executor.awaitTermination(20, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println("Bye Bye jacocoDumper");
	}

	/**
	 * Check if Apache Tomcat localhost web server is ready to access. 
	 * 
	 * @return
	 */
	private static boolean localhostWebIsReady() {
		try {
			// Try to connect to the localhost apache tomcat web server
			URL url = new URL("http://localhost:8080/parabank");
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.setRequestMethod("GET");
			httpConnection.connect();

			// If we have get connection with the web app, everything is ready
			if(httpConnection.getResponseCode() == 200) {
				httpConnection.disconnect();
				return true;
			} 
			// If not, server is probably being deployed
			else {
				httpConnection.disconnect();
				return false;
			}
		} 
		catch (Exception e) { 
			System.out.println("*** http://localhost:8080/parabank is NOT ready ***");
		}
		return false;
	}
}
