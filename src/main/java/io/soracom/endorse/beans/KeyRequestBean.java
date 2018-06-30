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
package io.soracom.endorse.beans;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class KeyRequestBean {
	private String keyId;
	private int keyLength;
	private String algorithm;
	private String timestamp;
	
	public int getLength() {
		return keyLength;
	}

	public void setLength(int length) {
		this.keyLength = length;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	
	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public String toJson(){
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.toJson(this);
	}
	
	public static KeyRequestBean fromJson(String json){
		
		Gson gson = new GsonBuilder().disableHtmlEscaping().create();
		return gson.fromJson(json, KeyRequestBean.class);
	}
}
