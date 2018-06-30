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

import io.soracom.endorse.utils.TLVHandler;
import io.soracom.endorse.utils.Utilities;

public class DIR {

	private byte[] value;
	
	public static final byte TAG_APPLICATION_TEMPLATE =(byte)0x61;
	public static final byte TAG_APPLICATION_IDENTIFIER =(byte)0x4F;
	public static final byte TAG_APPLICATION_LABEL =(byte)0x50;
	
	public DIR(){

		setValueString("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"); // Empty record
	}
	
	public DIR(String profileString){

		setValueString(profileString);
	}
	
	public DIR(byte[] profileBytes){

		setValueBytes(profileBytes);
	}
	
	public byte[] getValueBytes(){
		return this.value;
	}
	
	public void setValueBytes(byte[] value){
		this.value = value;
	}
	
	public String getValueString(){
		return  ((this.value==null)?"":Utilities.byteArrayToHexString(this.value, 0, value.length, false));
	}
	
	public void setValueString(String value){
		if (value!=null){
			this.value = Utilities.hexStringToByteArray(value);
		}
	}
	
	public boolean isEmpty(){
		if (value==null) {
			return true; 
		}
		if (value[0]!=TAG_APPLICATION_TEMPLATE){
			return true;
		}
		return false;
	}
	
	/**
	 * Return a subelement of EF DIR record 
	 * @param tag Eg. TAG_APPRICATION_IDENTIFIER or TAG_APPRICATION_LABEL
	 * @return
	 */
	public byte[] getTagValue(byte tag){
		if (isEmpty()){
			return null;
		}
		try
		{
			int len = Utilities.makeInt((byte)0, (byte)0, (byte)0, value[1]);
			
			TLVHandler hdlr = new TLVHandler((short)len);
			hdlr.appendArray(value, (short)2, (short)len);
			if (hdlr.findTLV(tag, (byte)1) != TLVHandler.TLV_NOT_FOUND){
				byte[] retVal = new byte[hdlr.getValueLength()];
				hdlr.copyValue((short)0, retVal, (short)0, (short) retVal.length);
				return retVal;
			}
		}
		catch (Exception ex){
			
		}
		return null;
	}
	
	public byte[] getAID(){
		return getTagValue(TAG_APPLICATION_IDENTIFIER);
	}
	
	public byte[] getLabel(){
		return getTagValue(TAG_APPLICATION_LABEL);
	}
}
