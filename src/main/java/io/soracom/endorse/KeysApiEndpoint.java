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

/**
 * Endpoint definition for SORACOM Endorse
 * 
 * @author c9katayama
 *
 */
public enum KeysApiEndpoint {

	GLOBAL_COVERAGE("https://g.api.soracom.io"), //
	JAPAN_COVERAGE("https://api.soracom.io");
	private String apiEndpoint;

	private KeysApiEndpoint(String url) {
		apiEndpoint = url;
	}

	public String getApiEndpoint() {
		return apiEndpoint;
	}

	private static String trimSlash(String baseUrl) {
		if (baseUrl.endsWith("/")) {
			baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
		}
		return baseUrl;
	}

	public static String verifyMasterKey(String baseUrl, String keyId) {
		return trimSlash(baseUrl) + "/v1/keys/" + keyId + "/verify";
	}

	public static String createKey(String baseUrl) {
		return trimSlash(baseUrl) + "/v1/keys";
	}

	public static KeysApiEndpoint getDefault() {
		return GLOBAL_COVERAGE;
	}
}
