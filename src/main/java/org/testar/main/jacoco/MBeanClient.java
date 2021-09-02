/*******************************************************************************
 * Copyright (c) 2009, 2020 Mountainminds GmbH & Co. KG and Contributors
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *    Fernando Pastor Ricos - adapt original code for TESTAR purposes
 *
 *******************************************************************************/

package org.testar.main.jacoco;

/**
 * https://github.com/jacoco/jacoco/blob/master/org.jacoco.examples/src/org/jacoco/examples/MBeanClient.java
 */


import java.io.FileOutputStream;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * This example connects to a JaCoCo agent that runs with the option
 * <code>jmx=yes</code> and requests execution data. The collected data is
 * dumped to a local file.
 */
public final class MBeanClient {

	// TODO: Allow jmx port customization
	// Port is coded to 5000, we need to use same number to launch SUT with jacoco + jmx
	private static final String SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:5000/jmxrmi";

	private MBeanClient() {}

	public interface IProxy {
		String getVersion();

		String getSessionId();

		void setSessionId(String id);

		byte[] getExecutionData(boolean reset);

		void dump(boolean reset);

		void reset();
	}

	public static String dumpJacocoReportWithTimestamp(String timeStamp) {
		return dumpJacocoReport(timeStamp + "_jacoco.exec");
	}

	/**
	 * Use JMX service to connect with the JVM and extract one JaCoCo report file.
	 * IF success return the string that represents the path of this jacoco.exec file,
	 * ELSE return empty string.
	 *
	 * @param destFile
	 * @throws Exception
	 * @return string that contains extracted jacoco.exec file
	 */
	private static String dumpJacocoReport(String destFile) {
		try {
			// Open connection to the coverage agent:
			final JMXServiceURL url = new JMXServiceURL(SERVICE_URL);
			final JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
			final MBeanServerConnection connection = jmxc
					.getMBeanServerConnection();

			final IProxy proxy = (IProxy) MBeanServerInvocationHandler
					.newProxyInstance(connection,
							new ObjectName("org.jacoco:type=Runtime"), IProxy.class,
							false);

			// Retrieve JaCoCo version and session id:
			//System.out.println("Version: " + proxy.getVersion());
			//System.out.println("Session: " + proxy.getSessionId());

			// Retrieve dump and write to file:
			final byte[] data = proxy.getExecutionData(false);
			final FileOutputStream localFile = new FileOutputStream(destFile);
			localFile.write(data);
			localFile.close();

			// Close connection:
			jmxc.close();

		} catch(Exception e) {
			System.out.println("MBeanClient ERROR: Trying to connect with the JVM using JMX to extract the jacoco report");
			System.out.println("MBeanClient ERROR: Probably the SUT stops responding or closed unexpectedly");
			System.out.println(e.getMessage());
			// return an empty string to indicate we didn't create any jacoco.exec report
			return "";
		}

		System.out.println("MBeanClient extracted: " + destFile);
		return destFile;
	}
}