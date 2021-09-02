package org.testar.main.jacoco;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;
import com.opencsv.CSVReader;

/**
 * Utility class for reading and extracting information from JaCoCo coverage reports.
 */
public class JacocoReportReader {

	private JacocoReportReader() {}

	/**
	 * Read the CSV JaCoCo report and create the TOTAL metrics results. 
	 * 
	 * @param jacocoDirReport
	 * @return
	 */
	public static String obtainCSVSummary(File reportCSV) {
		System.out.println("JacocoReportReader obtainCSVSummary: " + reportCSV);

		//GROUP,      PACKAGE, CLASS,           INSTRUCTION_MISSED, INSTRUCTION_COVERED, BRANCH_MISSED, BRANCH_COVERED, LINE_MISSED, LINE_COVERED, COMPLEXITY_MISSED, COMPLEXITY_COVERED, METHOD_MISSED, METHOD_COVERED
		//jacoco ant, default, FileChooserDemo, 191,                208,                 10,            0,              32,          43,           16,                6,                  11,            6
		int[] totalValues = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}; // array for columns

		int numberOfTotalClasses = 0;
		int numberOfMissedClasses = 0;

		CSVReader reader = null;
		try {
			FileReader filereader = new FileReader(reportCSV);
			reader = new CSVReader(filereader);
			String[] nextLine;

			// Check and add all classes coverage values to get the TOTAL one
			while((nextLine = reader.readNext()) != null) {

				// extract values from columns 3 to 12
				for(int i = 3; i <= 12; i++) {
					if(isIntValue(nextLine[i])) {
						totalValues[i] = totalValues[i] + Integer.parseInt(nextLine[i]);
					}
				}
				// Increase number of Total Classes if we have a numeric coverage line
				if(isIntValue(nextLine[3])){
					numberOfTotalClasses = numberOfTotalClasses + 1;
				}
				// If INSTRUCTION_COVERED is 0, increase Missed Classes
				if(isIntValue(nextLine[4]) && Integer.parseInt(nextLine[4]) == 0) {
					numberOfMissedClasses = numberOfMissedClasses + 1;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					//Nothing
				}
			}
		}

		int INSTRUCTION_MISSED = totalValues[3]; //column 3
		int INSTRUCTION_COVERED = totalValues[4]; //column 4
		int TOTAL_INSTRUCTIONS = INSTRUCTION_MISSED + INSTRUCTION_COVERED;

		int BRANCH_MISSED = totalValues[5]; //column 5
		int BRANCH_COVERED = totalValues[6]; //column 6
		int TOTAL_BRANCH = BRANCH_MISSED + BRANCH_COVERED;

		int LINE_MISSED = totalValues[7]; //column 7
		int LINE_COVERED = totalValues[8]; //column 8
		int TOTAL_LINE = LINE_MISSED + LINE_COVERED;

		int COMPLEXITY_MISSED = totalValues[9]; //column 9
		int COMPLEXITY_COVERED = totalValues[10]; //column 10
		int TOTAL_COMPLEXITY = COMPLEXITY_MISSED + COMPLEXITY_COVERED;

		int METHOD_MISSED = totalValues[11]; //column 11
		int METHOD_COVERED = totalValues[12]; //column 12
		int TOTAL_METHOD = METHOD_MISSED + METHOD_COVERED;

		StringBuilder dataContent = new StringBuilder("");

		dataContent.append("Missed Instructions | " + NumberFormat.getNumberInstance(Locale.US).format(INSTRUCTION_MISSED));
		dataContent.append(" of " + NumberFormat.getNumberInstance(Locale.US).format(TOTAL_INSTRUCTIONS));
		dataContent.append(" | Cov | " + String.format("%.2f", (double)INSTRUCTION_COVERED * 100.0 / (double)TOTAL_INSTRUCTIONS).replace(".", ","));

		dataContent.append(" | Missed Branches | " + NumberFormat.getNumberInstance(Locale.US).format(BRANCH_MISSED));
		dataContent.append(" of " + NumberFormat.getNumberInstance(Locale.US).format(TOTAL_BRANCH));
		dataContent.append(" | Cov | " + String.format("%.2f", (double)BRANCH_COVERED * 100.0 / (double)TOTAL_BRANCH).replace(".", ","));

		dataContent.append(" | Missed | " + NumberFormat.getNumberInstance(Locale.US).format(COMPLEXITY_MISSED));
		dataContent.append(" | Cxty | " + NumberFormat.getNumberInstance(Locale.US).format(TOTAL_COMPLEXITY));

		dataContent.append(" | Missed | " + NumberFormat.getNumberInstance(Locale.US).format(LINE_MISSED));
		dataContent.append(" | Lines | " + NumberFormat.getNumberInstance(Locale.US).format(TOTAL_LINE));

		dataContent.append(" | Missed | " + NumberFormat.getNumberInstance(Locale.US).format(METHOD_MISSED));
		dataContent.append(" | Methods | " + NumberFormat.getNumberInstance(Locale.US).format(TOTAL_METHOD));

		dataContent.append(" | Missed | " + numberOfMissedClasses);
		dataContent.append(" | Classes | " + numberOfTotalClasses);

		return dataContent.toString();
	}

	private static boolean isIntValue(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch(NumberFormatException  e) {
			// Exception, nothing but we return false
		}
		return false;
	}
}
