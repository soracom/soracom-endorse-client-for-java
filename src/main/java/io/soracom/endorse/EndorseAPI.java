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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.soracom.endorse.beans.KeyRequestBean;
import io.soracom.endorse.beans.MilenageParamsBean;
import io.soracom.endorse.beans.SessionDataBean;
import io.soracom.endorse.beans.XresBean;
import io.soracom.endorse.common.HttpRequestException;
import io.soracom.endorse.common.HttpResponse;
import io.soracom.endorse.common.EndorseClientRuntimeException;
import io.soracom.endorse.common.TextLog;
import io.soracom.endorse.utils.Http;
import io.soracom.endorse.utils.Utilities;

public class EndorseAPI {

	// HTTP POST request
	/**
	 * Internal function to handle HTTP Post to the key distribution API
	 * 
	 * @param url
	 *            - full URL uncluding http:// or https://
	 * @param keyRequestBean
	 *            - The object containing the necessary input parameters
	 * @param signature
	 *            - The calculated signature
	 * @return The full http response object
	 * @throws Exception
	 */
	private static HttpResponse postKeyRequest(String url, String body, String timestamp, String algorithm,
			String signature) {
		Map<String, String> headers = new HashMap<>();
		headers.put("x-soracom-timestamp", timestamp);
		headers.put("x-soracom-digest-algorithm", algorithm);
		headers.put("x-soracom-signature", signature);
		return Http.sendPost(url, body, headers);
	}

	/**
	 * Create master key - This is the first API call in the key agreement service
	 * API message sequence
	 * 
	 * @param url
	 *            - The URL to send the inital API call
	 * @param imsi
	 *            - The IMSI retrieved from the SIM
	 * @return - The input parameters required to run the authenticate algorithm on
	 *         the SIM
	 */
	public static MilenageParamsBean initKeyAgreement(String url, String imsi) throws HttpRequestException {

		return initKeyAgreement(url, imsi, null, null);
	}

	/**
	 * Create master key with rand and auts parameters too (use in case of resync)
	 * 
	 * @param url
	 *            - The URL to send the inital API call
	 * @param imsi
	 *            - The IMSI retrieved from the SIM
	 * @return - The input parameters required to re-run the authenticate algorithm
	 *         on the SIM
	 */
	public static MilenageParamsBean initKeyAgreement(String url, String imsi, String rand, String auts)
			throws HttpRequestException {

		MilenageParamsBean retVal = new MilenageParamsBean();
		SessionDataBean content = new SessionDataBean();
		if (imsi != null) {
			content.setImsi(imsi);
		}
		if (rand != null) {
			content.setRand(rand);
		}
		if (auts != null) {
			content.setAuts(auts);
		}
		TextLog.log("invoke KeyAgreement. params=" + content.toJson());

		HttpResponse response = Http.sendPost(url, content.toJson());
		if ((response.getCode() == 200 || response.getCode() == 401) && response.getContents() != null) {
			retVal = MilenageParamsBean.fromJson(response.getContents());
		} else {
			String errorMessage = "While calling key agreement URL " + url + ", received http response: "
					+ Integer.toString(response.getCode());
			TextLog.error(errorMessage);
			throw new HttpRequestException(response, errorMessage);
		}
		return retVal;
	}

	/**
	 * Verify master key - This is the second API call in the key agreement service
	 * API message sequence to validate the master key before use
	 * 
	 * @param url
	 *            - The URL to send the verify call(The url must contain the key Id
	 *            returned in first step eg: keyAgreementUrl+"/"+keyId+"/verify")
	 * @param xres
	 *            - the signed response
	 * @return - True if the master key is correctly verified, false otherwise
	 */
	public static boolean verifyMasterKey(String url, String xres) throws HttpRequestException {

		XresBean content = new XresBean();
		content.setXres(xres);
		HttpResponse response = Http.sendPost(url, content.toJson());
		if (response.getCode() == 200) {
			return true;
		} else {
			String errorMessage = "While calling verify URL " + url + ", received http response: "
					+ Integer.toString(response.getCode());
			TextLog.error(errorMessage);
			throw new HttpRequestException(response, errorMessage);
		}
	}

	/**
	 * Method used to calculate the signature to append to key request messages
	 * 
	 * @param message
	 *            - body contents of the http request
	 * @param timestamp
	 *            - unix timestamp in milliseconds
	 * @param secretKey
	 *            - the criptographic key used (typically ck)
	 * @param algorithm
	 *            - the hashing algorithm to use (eg:
	 * @return - The Signature to append to key request messages
	 */
	public static String calculateSignature(String message, long timestamp, byte[] secretKey, String algorithm) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			digest.update(message.getBytes(StandardCharsets.UTF_8));
			digest.update(Long.toString(timestamp).getBytes(StandardCharsets.UTF_8));
			digest.update(secretKey);
			byte[] hash = digest.digest();
			return Utilities.bytesToBase64(hash);
		} catch (NoSuchAlgorithmException e) {
			throw new EndorseClientRuntimeException(e.getMessage(), e);
		}
	}

	/**
	 * Method used to calculate the shared ApplicationKey
	 * 
	 * @param nonce
	 *            - the challenge
	 * @param timestamp
	 *            - unix timestamp in milliseconds
	 * @param secretKey
	 *            - the criptographic key used (typically ck)
	 * @param keyLength
	 *            - The required length of key to be generated
	 * @param algorithm
	 *            - Algorithm to use eg: SHA-256
	 * @return The shared key for further communications
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] calculateApplicationKey(byte[] nonce, long timestamp, byte[] secretKey, int keyLength,
			String algorithm) throws NoSuchAlgorithmException {
		MessageDigest digest = MessageDigest.getInstance(algorithm);
		digest.update(nonce);
		digest.update(Long.toString(timestamp).getBytes(StandardCharsets.UTF_8));
		digest.update(secretKey);
		byte[] encodedhash = digest.digest();
		return Utilities.arraySplice(encodedhash, 0, keyLength);
	}

	/**
	 * Communicate with key distribution service - This is the third call in the key
	 * agreement service API message sequence
	 * 
	 * @param url
	 *            - The URL to which the call should be posted (eg:
	 *            keyAgreementUrl+"/"+keyId+"/generate_app_key")
	 * @param ck
	 *            - The Cryptographic Key return from Milenage authentication run on
	 *            the SIM
	 * @param timestamp
	 *            - The current Timestamp in miliseconds
	 * @param keyId
	 *            - The Key reference ID on server
	 * @param keyLength
	 *            - The required length of key to be generated
	 * @param algorithm
	 *            - Algorithm to use eg: SHA-256
	 * @param jsonParameters
	 *            - Any additional parameters to include in the body
	 * 
	 * @return The result of the API call (HTTP body content)
	 */
	public static String requestService(String url, byte[] ck, long timestamp, String keyId, int keyLength,
			String algorithm, String jsonParameters) throws HttpRequestException {

		KeyRequestBean content = new KeyRequestBean();
		content.setKeyId(keyId);
		content.setLength(keyLength);
		content.setTimestamp(Long.toString(timestamp));
		content.setAlgorithm(algorithm);

		// Merge json objects
		String body = "";
		if (jsonParameters == null) {
			body = content.toJson();
		} else {
			Gson gson = new Gson();
			JsonObject contentJson = Utilities.fromJson(content.toJson(), JsonObject.class);
			JsonObject paramJson = Utilities.fromJson(jsonParameters, JsonObject.class);
			JsonObject jsonResult = Utilities.jsonMerge(contentJson, paramJson);
			body = gson.toJson(jsonResult);
		}

		String sig = calculateSignature(body, timestamp, ck, algorithm);
		HttpResponse response = postKeyRequest(url, body, content.getTimestamp(), content.getAlgorithm(), sig);
		if (response.getCode() == 200 && response.getContents() != null) {
			String retVal = response.getContents();
			return retVal;
		} else {
			String errorMessage = "While calling key distribution service URL " + url + ", received http response: "
					+ Integer.toString(response.getCode());
			TextLog.error(errorMessage);
			throw new HttpRequestException(response, errorMessage);
		}

	}
}
