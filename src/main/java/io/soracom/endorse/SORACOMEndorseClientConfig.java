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
package io.soracom.endorse;

import io.soracom.endorse.interfaces.UiccInterfaceType;

/**
 * Configuration parameters for KryptonClient class
 * 
 * @author c9katayama
 *
 */
public class SORACOMEndorseClientConfig {

	private String apiEndpointUrl = KeysApiEndpoint.getDefault().getApiEndpoint();
	private UiccInterfaceType uiccInterfaceType = UiccInterfaceType.autoDetect;
	private int keyLength = 32;
	private String keyAlgorithm = "SHA-256";
	private boolean debug = false;
	private boolean clearKeyCache = false;
	private boolean disableKeyCache = false;

	public static class CommunicationDeviceConfig {

		private String portName;
		private Integer baudRate;
		private Integer dataBits;
		private Integer stopBits;
		private Integer parity;
		private String modemIndex;

		public String getPortName() {
			return portName;
		}

		public void setPortName(String portName) {
			this.portName = portName;
		}

		public Integer getBaudRate() {
			return baudRate;
		}

		public void setBaudRate(Integer baudRate) {
			this.baudRate = baudRate;
		}

		public Integer getDataBits() {
			return dataBits;
		}

		public void setDataBits(Integer dataBits) {
			this.dataBits = dataBits;
		}

		public Integer getStopBits() {
			return stopBits;
		}

		public void setStopBits(Integer stopBits) {
			this.stopBits = stopBits;
		}

		public Integer getParity() {
			return parity;
		}

		public void setParity(Integer parity) {
			this.parity = parity;
		}

		public String getModemIndex() {
			return modemIndex;
		}

		public void setModemIndex(String modemIndex) {
			this.modemIndex = modemIndex;
		}
	}

	private CommunicationDeviceConfig communicationDeviceConfig;

	public void setApiEndpointUrl(String apiEndpointUrl) {
		this.apiEndpointUrl = apiEndpointUrl;
	}
	
	public String getApiEndpointUrl() {
		return apiEndpointUrl;
	}

	public UiccInterfaceType getUiccInterfaceType() {
		return uiccInterfaceType;
	}

	public void setUiccInterfaceType(UiccInterfaceType uiccInterfaceType) {
		this.uiccInterfaceType = uiccInterfaceType;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public void setKeyLength(int keyLength) {
		this.keyLength = keyLength;
	}

	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}

	public void setKeyAlgorithm(String keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}

	public boolean isDebug() {
		return debug;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	public void setClearKeyCache(boolean clearKeyCache) {
		this.clearKeyCache = clearKeyCache;
	}
	
	public boolean isClearKeyCache() {
		return clearKeyCache;
	}
	
	public boolean isDisableKeyCache() {
		return disableKeyCache;
	}
	
	public void setDisableKeyCache(boolean disableKeyCache) {
		this.disableKeyCache = disableKeyCache;
	}

	public CommunicationDeviceConfig getCommunicationDeviceConfig() {
		return communicationDeviceConfig;
	}

	public void setCommunicationDeviceConfig(CommunicationDeviceConfig communicationDeviceConfig) {
		this.communicationDeviceConfig = communicationDeviceConfig;
	}
}
