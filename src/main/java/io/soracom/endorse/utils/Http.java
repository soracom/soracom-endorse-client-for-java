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
package io.soracom.endorse.utils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Map.Entry;

import io.soracom.endorse.common.HttpResponse;

public class Http {

	/**
	 * Internal function to handle HTTP Post to the selected URL
	 * 
	 * @param url
	 *            - full URL uncluding http:// or https://
	 * @param postParameters
	 *            - Parameters in JSON format
	 * @return The full http response object
	 * @throws Exception
	 */
	public static HttpResponse sendPost(String url, String postParameters) {
		return sendPost(url, postParameters, null);
	}

	public static HttpResponse sendPost(String url, String postParameters, Map<String, String> headers) {
		HttpResponse retVal = new HttpResponse(url);
		try {
			URL obj = new URL(url);

			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// add request header
			con.setRequestMethod("POST");
			con.setRequestProperty("Accept", "application/json");
			con.setRequestProperty("Content-type", "application/json;  charset=utf-8");
			if (headers != null) {
				for (Entry<String, String> header : headers.entrySet()) {
					con.setRequestProperty(header.getKey(), header.getValue());
				}
			}
			// Send post request
			con.setDoOutput(true);
			if (postParameters != null) {
				DataOutputStream wr = null;
				try {
					wr = new DataOutputStream(con.getOutputStream());
					wr.writeBytes(postParameters);
					wr.flush();
				} finally {
					close(wr);
				}
			}
			retVal.setCode(con.getResponseCode());

			BufferedReader in = null;
			try {
				in = new BufferedReader(
						new InputStreamReader((retVal.getCode() < 400) ? con.getInputStream() : con.getErrorStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
				retVal.setContents(response.toString());
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
			} finally {
				close(in);
			}
		} catch (Exception Ex) {
			retVal.setError(Ex.getMessage());
		}
		return retVal;
	}

	private static void close(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException e) {
			// ingore
		}

	}
}
