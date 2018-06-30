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

import io.soracom.endorse.utils.Utilities;

public class IMSI {

	private String value="";
	
	public IMSI(){
		
	}
	
	public IMSI(String value){
		setValue(value) ;
	}


	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		if (value!=null && value.length()<=15 ){
			this.value = value;
		}
	}
	
	public String getEncodedValue(){
		try{
			//*Parity Bit: 
			//1 - Odd Number of identity digits. +001
			//0 - Even Number of identity digits. +001
			String digits = ((value.length()%2==0)?"1":"9") + value;
			if ((digits.length()%2)==1) digits+="F";
			int len = (digits.length()/2);
			
			return String.format("%02d", len) + Utilities.padRight(Utilities.swapHexString(digits), 16, 'F');
		}
		catch (Exception Ex){
			return "";
		}
	}
	
	public void setEncodedValue(String encValue){
		try{
			int len = Utilities.hexStringToInt(encValue.substring(0,2));
			//*Parity Bit: 
			//1 - Odd Number of identity digits. +001
			//0 - Even Number of identity digits. +001
			String digits = Utilities.swapHexString(encValue.substring(2));
			//String ParityNibble =digits.substring(0,1);
			this.value = digits.substring(1,len*2).replace("F", "");
		}
		catch (Exception Ex){
			
		}
	}
	
	/**
	 * Get the Mobile Country Code
	 * @return
	 */
	public String getMCC(){
		if (value==null) return null;
		return value.substring(0,3);
	}
	
	/**
	 * Get the Mobile Network Code
	 * @return
	 */
	public String getMNC(){
		if (value==null) return null;
		return value.substring(3,6);
	}
	
	/**
	 * Get the Mobile Subscriber Identification Number
	 * @return
	 */
	public String getMSIN(){
		if (value==null) return null;
		return value.substring(6);
	}
	
	
	

	
}
