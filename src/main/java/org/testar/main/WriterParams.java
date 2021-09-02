package org.testar.main;

public class WriterParams {

	public static class WriterParamsBuilder {
		String filename = "";

		String information = "";

		boolean newLine = true;

		public WriterParamsBuilder setFilename(String filename) {
			this.filename = filename;
			return this;
		}

		public WriterParamsBuilder setInformation(String information) {
			this.information = information;
			return this;
		}

		public WriterParamsBuilder setNewLine(boolean newLine) {
			this.newLine = newLine;
			return this;
		}

		public WriterParams build(){
			return new WriterParams(filename, information, newLine);
		}
	}

	String filename = "";

	String information = "";

	boolean newLine = false;

	public String getFilename() {
		return filename;
	}

	public String getInformation() {
		return information;
	}

	public boolean isNewLine() {
		return newLine;
	}

	private WriterParams(String filename, String information, boolean newLine) {
		this.filename = filename;
		this.information = information;
		this.newLine = newLine;
	}
}
