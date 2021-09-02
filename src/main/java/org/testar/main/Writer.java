package org.testar.main;

import java.io.FileWriter;
import java.io.IOException;

public class Writer {

	private Writer() {}

	/**
	 * Write the information metrics in the desired filename. 
	 *
	 */
	public static void writeMetrics(WriterParams params) {
		try {
			String metricsFile = params.getFilename() + ".txt";
			FileWriter myWriter = new FileWriter(metricsFile, true);
			if(params.isNewLine()) myWriter.write(params.getInformation() + "\r\n");
			else myWriter.write(params.getInformation());
			myWriter.close();
		} catch (IOException e) {
			System.err.println("ERROR: Writing Metrics inside " + params.getFilename() + " text file");
			e.printStackTrace();
		}
	}
}