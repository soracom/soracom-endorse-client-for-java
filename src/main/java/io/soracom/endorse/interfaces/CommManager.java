/**
 * Copyright (c) 2018 SORACOM, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.soracom.endorse.interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
//import java.util.ArrayList;
//import java.util.List;
import java.util.concurrent.CountDownLatch;

import io.soracom.endorse.common.TextLog;
import io.soracom.endorse.utils.Utilities;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import jssc.SerialPortList;

/**
 * Serial Port Communications handler (geared towards sending and receiving of
 * AT commands Followed examples from
 * https://code.google.com/archive/p/java-simple-serial-connector/wikis/jSSC_examples.wiki
 * 
 * @author olivier.comarmond
 *
 */
public class CommManager implements IUiccInterface {

	private String lastError;
	private String portName = "";
	private int baudRate = SerialPort.BAUDRATE_57600;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;

	private SerialPort serialPort;
	private SerialHandler serialHandler;

	public CommManager() {

	}

	public static String[] getAvailablePorts() {
		return SerialPortList.getPortNames();
	}

	public class SerialHandler implements SerialPortEventListener {

		private int maxResponseWaitTime = 60000; // Wait for a maximum of 60 second for a full response
		private StringBuilder responseBuffer = new StringBuilder();
		private String lastCommand;
		private boolean expectingResponse;
		private SerialPort serialPort;
		private CountDownLatch countDownLatchAwaitForMessageEnd = new CountDownLatch(1);

		public SerialHandler(SerialPort serialPort, boolean expectingResponse) {
			this.serialPort = serialPort;
			try {
				this.serialPort.addEventListener(this);
			} catch (SerialPortException e) {
			}
			this.expectingResponse = expectingResponse;
		}

		private Thread responseAwaiter = new Thread() {
			public void run() {
				try {
					Thread.sleep(maxResponseWaitTime);
				} catch (InterruptedException ex) {

				} finally {
					String response = responseBuffer.toString().trim();

					// eliminate multiple command echoes in the output stream
					while (response.contains(lastCommand)) {
						response = response.replace(lastCommand, "");
					}
					responseBuffer = new StringBuilder(response);
					countDownLatchAwaitForMessageEnd.countDown();
				}
			}
		};

		public String send(String command) {
			try {
				lastCommand = command;
				if (!command.endsWith("\r\n")) {
					command += "\r\n"; // Command termination character
				}
				try {
					serialPort.writeBytes(command.getBytes(StandardCharsets.US_ASCII));// Write data to port
				} catch (SerialPortException ex) {
					throw new IllegalStateException(ex.getMessage(), ex);
				}
				responseAwaiter.start();
				try {
					countDownLatchAwaitForMessageEnd.await();
				} catch (InterruptedException e) {

				}
				return responseBuffer.toString();
			} finally {
				try {
					serialPort.removeEventListener();
				} catch (SerialPortException e) {
				}
			}
		}

		@Override
		public void serialEvent(SerialPortEvent event) {
			if (event.isRXCHAR()) {// If data is available
				int length = event.getEventValue();// Check bytes count in the input buffer

				TextLog.debug("Received " + Integer.toString(length) + " byte(s)");

				try {
					byte buffer[] = serialPort.readBytes(length);
					if (expectingResponse) {
						// Sometimes a Command echo is received
						String response = (new String(buffer, StandardCharsets.US_ASCII));
						TextLog.debug("Response: " + response);
						responseBuffer.append(response);
						// Analyse full response for early exit of awaiter
						response = responseBuffer.toString().trim();
						if (response.endsWith("OK") || response.endsWith("ERROR") || response.contains("+CME ERROR")) {
							interrupteAwaiter();
						}

					} else {
						String lastMessage = (new String(buffer, StandardCharsets.US_ASCII));
						TextLog.debug("Message: " + lastMessage);
					}
				} catch (SerialPortException ex) {
					interrupteAwaiter();
				}

			} else if (event.isCTS()) {// If CTS line has changed state
				if (event.getEventValue() == 1) {// If line is ON
					// System.out.println("CTS - ON");
					// connected();
				} else {
					// System.out.println("CTS - OFF");
					// disconnected();
				}
			} else if (event.isDSR()) {/// If DSR line has changed state
				if (event.getEventValue() == 1) {// If line is ON
					// System.out.println("DSR - ON");
					// connected();
				} else {
					// System.out.println("DSR - OFF");
					// disconnected();
				}
			}
		}

		protected void interrupteAwaiter() {
			if (responseAwaiter != null) {
				responseAwaiter.interrupt();
			}
		}
	}

	public boolean connect() {

		serialPort = new SerialPort(portName);
		boolean retVal;
		try {
			retVal = serialPort.openPort();// Open serial port
			retVal &= serialPort.setParams(baudRate, dataBits, stopBits, parity);// Set params. Also you can set params
																					// by this string:
																					// serialPort.setParams(9600, 8, 1,
																					// 0);

			int mask = SerialPort.MASK_RXCHAR + SerialPort.MASK_RXFLAG + SerialPort.MASK_CTS + SerialPort.MASK_DSR;// Prepare
																													// mask
			serialPort.setEventsMask(mask);// Set mask

		} catch (SerialPortException ex) {
			lastError = ex.getMessage();
			return false;
		}

		if (retVal) {
			// connected();
		}

		return retVal;

	}

	public boolean disconnect() {

		if (serialHandler != null) {
			serialHandler.interrupteAwaiter();
		}

		if (serialPort != null) {
			try {
				serialPort.closePort();
				// disconnected();
				return true;
			} catch (SerialPortException ex) {
				lastError = ex.getMessage();
				return false;
			} finally {
				serialPort = null;
			}
		}
		return false;

	}

	public String send(String command) {
		if (command == null || command.isEmpty()) {
			return "";
		}
		TextLog.debug("SEND:" + command);
		serialHandler = new SerialHandler(this.serialPort, true);
		String result = serialHandler.send(command);
		TextLog.debug("SEND_RESULT:" + result);
		return result;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getDataBits() {
		return dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getStopBits() {
		return stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	public int getParity() {
		return parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public boolean isConnected() {
		return (serialPort != null);
	}

	// Remove any unsolicited response codes from the response
	private String cleanResponse(String response, boolean removeUnsolicited, boolean removeEvents) {
		StringBuilder result = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new StringReader(response))) {
			String line = reader.readLine();
			while (line != null) {
				if (line.startsWith("^")) {
					// This is unsolicited response
					if (!removeUnsolicited) {
						result.append(line + "\r\n");
					}
				} else if (line.startsWith("+")) {
					// This is an event
					if (!removeEvents) {
						result.append(line + "\r\n");
					}
				} else {
					result.append(line + "\r\n");
				}
				line = reader.readLine();
			}
		} catch (IOException exc) {
			// quit
		}
		return result.toString().trim();
	}

	private String targetResponse(String response, String pattern) {

		int index = response.indexOf(pattern);
		if (index != -1) // -1 means "not found"
		{
			response = response.substring(index);
			index = response.indexOf("\r");
			if (index != -1) {
				return response.substring(0, index).trim(); // Just take one line
			} else {
				return response.trim();
			}
		} else {
			return "";
		}
	}

	private String parseCSIMResponse(String fullResponse) {
		String retVal = "";
		String response = targetResponse(fullResponse, "+CSIM:");

		if (!response.isEmpty()) {
			String[] parts = response.substring(6).split(",");
			if (parts.length > 1) {
				retVal = parts[1].replaceAll("\"", "");
			}
		}

		return retVal;
	}

	private String parseGenericResponse(String fullResponse) {
		String retVal = "";
		String response = cleanResponse(fullResponse, true, true);
		if (response.endsWith("OK")) {
			retVal = response.substring(0, response.length() - 2).trim();
		}
		return retVal;
	}

	public String queryDevice() {
		StringBuilder sb = new StringBuilder();
		if (connect()) {
			String response = send("AT+CGMI");
			if (response != null && !response.isEmpty()) {
				String parsedResponse = parseGenericResponse(response);
				sb.append("Manufacturer: " + parsedResponse + "\r\n");
			} else {
				sb.append("Manufacturer: N/A\r\n");
			}
			response = send("AT+CGMM");
			if (response != null && !response.isEmpty()) {
				String parsedResponse = parseGenericResponse(response);
				sb.append("Model: " + parsedResponse + "\r\n");
			} else {
				sb.append("Model: N/A\r\n");
			}
			response = send("AT+CGMR");
			if (response != null && !response.isEmpty()) {
				String parsedResponse = parseGenericResponse(response);
				sb.append("Revision: " + parsedResponse + "\r\n");
			} else {
				sb.append("Revision: N/A\r\n");
			}
			response = send("AT+CGSN");
			if (response != null && !response.isEmpty()) {
				String parsedResponse = parseGenericResponse(response);
				sb.append("S/N: " + parsedResponse + "\r\n");
			} else {
				sb.append("S/N: N/A\r\n");
			}
			disconnect();
			return sb.toString();
		} else {
			lastError = "Could not connect COMM device!";
			return lastError;
		}
	}

	@Override
	public String readImsi() {
		boolean disconnect = false;
		if (!isConnected()) {
			connect();
			disconnect = true;
		}
		String imsi = parseGenericResponse(send("AT+CIMI"));
		if (disconnect) {
			disconnect();
		}
		if (imsi != null && !imsi.isEmpty()) {
			return imsi;
		} else {
			return null;
		}
	}

	@Override
	public byte[] authenticate(byte[] rand, byte[] autn) {
		boolean disconnect = false;
		if (!isConnected()) {
			connect();
			disconnect = true;
		}
		byte[] commandData = new byte[rand.length + autn.length + 2];
		int i = 0;
		commandData[i++] = (byte) (rand.length & 0x000000FF);
		i = Utilities.arrayCopy(rand, 0, commandData, i, rand.length);
		commandData[i++] = (byte) (autn.length & 0x000000FF);
		i = Utilities.arrayCopy(autn, 0, commandData, i, autn.length);
		StringBuilder sb = new StringBuilder();
		sb.append("00880081");
		sb.append(Utilities.byteToHexString((byte) (commandData.length & 0x000000FF)));
		sb.append(Utilities.byteArrayToHexString(commandData));
		String query = sb.toString() + "00";// append expected length of 00
		String deviceResponse = send("AT+CSIM=" + Integer.toString(query.length()) + ",\"" + query + "\"");
		if (deviceResponse.toUpperCase().contains("ERROR")) {
			deviceResponse = send("AT+CSIM=" + Integer.toString(sb.length()) + ",\"" + sb.toString() + "\"");
		}
		String parsedResponse = parseCSIMResponse(deviceResponse);
		String sw = parsedResponse.substring(parsedResponse.length() - 4);
		if (sw.startsWith("61")) {
			deviceResponse = send("AT+CSIM=10,\"00C00000" + sw.substring(2) + "\"");
			parsedResponse = parseCSIMResponse(deviceResponse);
		}
		if (disconnect) {
			disconnect();
		}
		if (parsedResponse.endsWith("9000")) {
			byte[] retVal = Utilities.hexStringToByteArray(parsedResponse.substring(0, parsedResponse.length() - 4));
			return retVal;
		} else {
			return null;
		}
	}
}