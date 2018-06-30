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
import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.List;

import io.soracom.endorse.common.TextLog;
import io.soracom.endorse.utils.Utilities;

/**
 * Serial Port Communications handler (geared towards sending and receiving of
 * AT commands Followed examples from
 * https://code.google.com/archive/p/java-simple-serial-connector/wikis/jSSC_examples.wiki
 * 
 * @author olivier.comarmond
 *
 */
public class MmcliManager implements IUiccInterface {

	private String lastError;
	private String modemIndex = "0";
	private StringBuilder lastResponse;
	private String lastMessage;
	private int maxResponseWaitTime = 60000; // Wait for a maximum of 60 second for a full response

	public MmcliManager() {

	}

	public static boolean isUnsupportedPlatform() {
		String OS_NAME = System.getProperty("os.name").toLowerCase();
		if (OS_NAME.startsWith("windows") || OS_NAME.startsWith("mac")) {
			return true;
		}
		return false;
	}

	public String send(String command) {
		if (command == null || command.isEmpty()) {
			return "";
		}
		if (!command.endsWith("\r\n")) {
			command += "\r\n"; // Command termination character
		}

		TextLog.debug("Command: " + command);

		lastResponse = new StringBuilder();

		lastResponse.append(executeCliCommand(command));

		return lastResponse.toString();

	}

	private String executeCliCommand(String command) {

		StringBuilder mmCliCommand = new StringBuilder();
		mmCliCommand.append("mmcli -m ");
		mmCliCommand.append(modemIndex);
		mmCliCommand.append(" --timeout=");
		mmCliCommand.append(Integer.toString((int) (maxResponseWaitTime / 1000))); // timeout in seconds
		mmCliCommand.append(" --command=");
		mmCliCommand.append(command);

		StringBuffer output = new StringBuffer();
		StringBuffer error = new StringBuffer();

		Process p;
		try {
			TextLog.debug("Executing command: " + mmCliCommand.toString());

			p = Runtime.getRuntime().exec(mmCliCommand.toString());
			p.waitFor();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

			String line = "";
			while ((line = reader.readLine()) != null) {
				if (line.startsWith("^")) {
					continue; // Ignore unsolicited codes
				}
				if (line.startsWith("response: '")) {
					if (line.endsWith("'")) {
						output.append(line.substring(11, line.length() - 1) + "\r\n");
					} else {
						output.append(line.substring(11) + "\r\n");
					}
				} else {
					if (line.endsWith("'")) {
						output.append(line.substring(0, line.length() - 1) + "\r\n");
					} else {
						output.append(line + "\r\n");
					}
				}
			}

			BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			while ((line = errorReader.readLine()) != null) {
				error.append(line + "\r\n");
			}
			lastError = error.toString();
			if (lastError == null || lastError.isEmpty()) {
				output.append("OK");
			} else {
				output.append("ERROR");
			}

			TextLog.debug("response: '" + output.toString() + "'");
			if (lastError != null && !lastError.isEmpty()) {
				TextLog.debug("error: '" + lastError + "'");
			}

		} catch (Exception e) {
			TextLog.debug("Error invoking mmcli: " + e.getMessage());
		}
		return output.toString();

	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public String getModemIndex() {
		return modemIndex;
	}

	public void setModemIndex(String value) {
		modemIndex = value;
	}

	public String getLastResponse() {
		return lastResponse.toString();
	}

	public void setLastResponse(String lastResponse) {
		this.lastResponse = new StringBuilder(lastResponse);
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	@Override
	public String readImsi() {
		String imsi = send("AT+CIMI");
		if (imsi != null && !imsi.isEmpty()) {
			return imsi;
		} else {
			return null;
		}
	}

	@Override
	public byte[] authenticate(byte[] rand, byte[] autn) {
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

		String sw = deviceResponse.substring(deviceResponse.length() - 4);
		if (sw.startsWith("61")) {
			deviceResponse = send("AT+CSIM=10,\"00C00000" + sw.substring(2) + "\"");
		}
		if (deviceResponse.endsWith("9000")) {
			byte[] retVal = Utilities.hexStringToByteArray(deviceResponse.substring(0, deviceResponse.length() - 4));
			return retVal;
		} else {
			return null;
		}
	}

	@Override
	public boolean disconnect() {
		return true;
	}
}