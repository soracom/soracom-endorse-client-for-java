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
package io.soracom.endorse.common;


public class AuthenticationResponse {

	public enum ResultState {
		Success,
		SynchronisationFailure,
		Unknown
	}
	
	byte result;
	ResultState resultState = ResultState.Unknown; 
	byte[] auts;
	byte[] res;
	byte[] ck;
	byte[] ik;
	byte[] kc;
	
	public AuthenticationResponse(){
		
	}
	
	public AuthenticationResponse(byte[] response){
		parseResponse(response);
	}
	
	public void parseResponse(byte[] cardResponse){
		if (cardResponse!=null && cardResponse.length>0){
			int offset=0;
		 	int len=0;
		 	result = cardResponse[offset++];
			switch (result){
				case (byte)0xDB: //Authentication Success
					resultState = ResultState.Success;
					//RES,
					if (cardResponse.length>=offset+1){
						len =cardResponse[offset++] & 0x000000FF;
					}
					else
					{
						return;
					} 
					if (cardResponse.length>=offset+len){
						res = new byte[len];
						System.arraycopy(cardResponse, offset, res, 0, len);
						offset+=len;
					}
					else{
						return;
					}
					
					//CK,
					if (cardResponse.length>=offset+1){
						len =cardResponse[offset++] & 0x000000FF;
					}
					else
					{
						return;
					}
					if (cardResponse.length>=offset+len){
						ck = new byte[len];
						System.arraycopy(cardResponse, offset, ck, 0, len);
						offset+=len;
					}
					else{
						return;
					}
					// IK,
					if (cardResponse.length>=offset+1){
						len =cardResponse[offset++] & 0x000000FF;
					}
					else
					{
						return;
					}
					if (cardResponse.length>=offset+len){
						ik = new byte[len];
						System.arraycopy(cardResponse, offset, ik, 0, len);
						offset+=len;
					}
					else{
						return;
					}
					// KC
					if (cardResponse.length>=offset+1){
						len =cardResponse[offset++] & 0x000000FF;
					}
					else
					{
						return;
					}
					if (cardResponse.length>=offset+len){
						kc = new byte[len];
						System.arraycopy(cardResponse, offset, kc, 0, len);
						offset+=len;
					}
					else{
						return;
					}
					break;
				case (byte)0xDC: //Synchronisation failure
					resultState = ResultState.SynchronisationFailure;
					if (cardResponse.length>=offset+1){
						len =cardResponse[offset++] & 0x000000FF;
					}
					else
					{
						return;
					}
					if (cardResponse.length>=offset+len){
						auts = new byte[len];
						System.arraycopy(cardResponse, offset, auts, 0, len);
						offset+=len;
					}
					else{
						return;
					}
					break;
			}
		}
	}

	public byte getResult() {
		return result;
	}

	public void setResult(byte result) {
		this.result = result;
	}

	public ResultState getResultState() {
		return resultState;
	}

	public void setResultState(ResultState resultState) {
		this.resultState = resultState;
	}

	public byte[] getAuts() {
		return auts;
	}

	public void setAuts(byte[] auts) {
		this.auts = auts;
	}

	public byte[] getRes() {
		return res;
	}

	public void setRes(byte[] res) {
		this.res = res;
	}

	public byte[] getCk() {
		return ck;
	}

	public void setCk(byte[] ck) {
		this.ck = ck;
	}

	public byte[] getIk() {
		return ik;
	}

	public void setIk(byte[] ik) {
		this.ik = ik;
	}

	public byte[] getKc() {
		return kc;
	}

	public void setKc(byte[] kc) {
		this.kc = kc;
	}
	
	
}
