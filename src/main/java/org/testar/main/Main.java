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

public class Main {

	public static void main(String[] args) {

		System.out.println("Welcome to jacocoDumper application");

		// Wait 1 second until the web server is deployed
		while(!localhostWebIsReady()) {
			System.out.println("Waiting for a web service in localhost:8080 ...");
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		ExecutorService executor = Executors.newFixedThreadPool(10);

		// Web server is ready, now we want to extract the coverage every 5 seconds
		while(localhostWebIsReady()) {

			// Launch a thread that dumps the jacoco report with the current timestamp
			executor.submit(() -> {
				// Prepare the current time stamp
				String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss").format(new Date());

				try {
					// Extract jacoco.exec file 2021_09_02_11_02_47_jacoco.exec
					String jacocoExecFile = MBeanClient.dumpJacocoReportWithTimestamp(timeStamp);
					String reportDir = new File("report_" + timeStamp).getCanonicalPath();

					// Using "HTML destdir" inside build.xml -> Creates the directory automatically
					// But using only "CSV destfile" needs to create this directory first
					if(!new File(reportDir).exists()) {
						new File(reportDir).mkdirs();
					}

					// Launch jacoco report (build.xml) and overwrite desired parameters
					String antCommand = "cd jacoco && ant report"
							+ " -DjacocoFile=" + new File(jacocoExecFile).getCanonicalPath()
							+ " -DreportCoverageDir=" + reportDir;

					ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", antCommand);
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
					String information = "Time | " + timeStamp + " | " + coverage;
					System.out.println(information);
					Writer.writeMetrics(new WriterParams.WriterParamsBuilder()
							.setFilename("webCoverageMetrics")
							.setInformation(information)
							.build());

				} catch (IOException | InterruptedException e) {
					System.err.println("ERROR creating JaCoCo coverage report");
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
	public static boolean localhostWebIsReady() {
		try {
			// Try to connect to the localhost apache tomcat web server
			URL url = new URL("http://localhost:8080");
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
			System.out.println("*** http://localhost:8080 is NOT ready ***");
		}
		return false;
	}
}
