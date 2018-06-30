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
package io.soracom.endorse.keycache;

import io.soracom.endorse.utils.Utilities;

/**
 * Value of SIM authentication result.
 * 
 * @author c9katayama
 *
 */
public class AuthResult {
	private String ck;// base 64 encoded
	private String keyId;
	private String imsi;

	public byte[] ckBytes() {
		return Utilities.base64toBytes(ck);
	}

	public void ckBytes(byte[] ck) {
		this.ck = Utilities.bytesToBase64(ck);
	}

	public String getCk() {
		return ck;
	}

	public void setCk(String ck) {
		this.ck = ck;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}
	
	public void setImsi(String imsi) {
		this.imsi = imsi;
	}
	
	public String getImsi() {
		return imsi;
	}
}