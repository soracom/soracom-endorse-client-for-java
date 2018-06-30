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

/**
 * This class implements the File Control parameters as specified in 
 * Section ETSI TS 102 221 V13.1.0 11.1.1.3.
 * @author olivier.comarmond
 *
 */
public class FCP {

	private byte[] value;
	
	public static final byte TAG_FCP_TEMPLATE =(byte)0x62;
	public static final byte TAG_FILE_DESCRIPTOR =(byte)0x82;
	public static final byte TAG_FILE_IDENTIFIER =(byte)0x83;
	public static final byte TAG_DF_NAME =(byte)0x84;
	public static final byte TAG_PROPRIETARY_INFO =(byte)0xA5;
	public static final byte TAG_LIFE_CYCLE_STATUS_INTEGER =(byte)0x8A;
	public static final byte TAG_SECURITY_ATTRIBUTE_1 =(byte)0x8B;
	public static final byte TAG_SECURITY_ATTRIBUTE_2 =(byte)0x8C;
	public static final byte TAG_SECURITY_ATTRIBUTE_3 =(byte)0xAB;
	public static final byte TAG_PIN_STATUS_TEMPLATE =(byte)0xC6;
	public static final byte TAG_FILE_SIZE =(byte)0x80;
	public static final byte TAG_TOTAL_FILE_SIZE =(byte)0x81;
	public static final byte TAG_SHORT_FILE_IDENTIFIER =(byte)0x88;
	
	public enum FileType {
		Unknown,
		ADF,
		DF,
		EF_Transparent,
		EF_Linear,
		EF_Cyclic,
		EF_BERTLV
	}
	
	public enum LCSI {
		Unknown,
		NoInformation,
		CreationState,
		InitializationState,
		OperationalStateActivated,
		OperationalStateDeactivated,
		TerminationState
	}
	public FCP(){

		setValueString("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"); // Empty record
	}
	
	public FCP(String profileString){

		setValueString(profileString);
	}
	
	public FCP(byte[] profileBytes){

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
		if (value[0]!=TAG_FCP_TEMPLATE){
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
			short i=(value[1]==(byte)0x81)?(short)2:(short)1;
			short len=Utilities.makeShort( (byte)0, value[i++]);
			TLVHandler hdlr = new TLVHandler(len);
			hdlr.appendArray(value,i, len);
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
	
	/**
	 * Returns  a positive value if tag is found else returns -1
	 * @param tag
	 * @return  a positive value if tag is found else returns -1
	 */
	private int getIntValue(byte tag){
		
		int retVal = -1;
		try {
			byte[] val = getTagValue(tag);
			if (val!=null){
				if (val.length==1){
					retVal = Utilities.makeInt((byte)0, (byte)0, (byte)0, val[(short)0]);
				}
				else if (val.length==2){
					retVal = Utilities.makeInt((byte)0, (byte)0, val[(short)0],val[(short)1]);
				}
				else if (val.length==3){
					retVal = Utilities.makeInt((byte)0, val[(short)0], val[(short)1],val[(short)2]);
				}
				else if (val.length>3){
					retVal = Utilities.makeInt(val[(short)0], val[(short)1], val[(short)2], val[(short)3]);
				}
			}
		}
		catch (Exception Ex){
			
		}
		return retVal;
	}
	
	public short getFileId(){
		byte[] fileId= getTagValue(TAG_FILE_IDENTIFIER);
		if (fileId==null){
			return (short)0;
		}
		else{
			return Utilities.getShort(fileId, (short)0);
		}
	}
	
	public byte getShortFileId(){
		byte[] fileId= getTagValue(TAG_SHORT_FILE_IDENTIFIER);
		if (fileId==null || fileId.length==0){
			return (byte)0;
		}
		else{
			return (byte)(fileId[(short)0]>>3 & 0x1F);
		}
	}
	
	public int getFileSize(){
		return getIntValue(TAG_FILE_SIZE);
	}
	
	/*
	public int getTotalFileSize(){
		return getIntValue(TAG_TOTAL_FILE_SIZE);
	}
	*/
	
	
	public FileType getFileType(){
		byte[] fileTypeArray = getTagValue(TAG_FILE_DESCRIPTOR);
		
		if (fileTypeArray.length>0){
			byte bType = (byte)(fileTypeArray[0] & (byte)0x38);
			byte bStructure = (byte)(fileTypeArray[0] & (byte)0x07);
			
			switch (bType){
				case (byte)0x00: // Working EF
				case (byte)0x08: // Internal EF
					switch (bStructure){
						case (byte)0x01:
							return FileType.EF_Transparent;
						case (byte)0x02:
							return FileType.EF_Linear;
						case (byte)0x06:
							return FileType.EF_Cyclic;
					}
					break;
				case (byte)0x38: // DF or ADF or BER TLV
					switch (bStructure){
						case (byte)0x00:
							if ( getTagValue(TAG_DF_NAME)==null){
								return FileType.DF;
							}
							else{
								return FileType.ADF;
							}
						case (byte)0x01:
							return FileType.EF_BERTLV;
	
					}
					break;
			}
		}
		return FileType.Unknown;
	}
	
	public int getRecordSize(){
		byte[] fileDescArray = getTagValue(TAG_FILE_DESCRIPTOR);
		
		if (fileDescArray.length>=4){
			return Utilities.makeInt((byte)0, (byte)0, fileDescArray[2], fileDescArray[3]);
		}
		else{
			return getFileSize();
		}
	}
	
	public int getNumRecords(){
		byte[] fileDescArray = getTagValue(TAG_FILE_DESCRIPTOR);
		
		if (fileDescArray.length>=5){
			return Utilities.makeInt((byte)0, (byte)0, (byte)0, fileDescArray[4]);
		}
		else{
			return 0;
		}
	}
	
	public byte[] getADF_AID(){
		return getTagValue(TAG_DF_NAME);
	}
	
	public LCSI getLifeCycleStatus(){
		byte[] lcsi= getTagValue(TAG_LIFE_CYCLE_STATUS_INTEGER);
		if (lcsi!=null){
			switch ( lcsi[(short)0] ){
				case (byte)0:
					return LCSI.NoInformation;
				case (byte)0x01:
					return LCSI.CreationState;
				case (byte)0x05:
				case (byte)0x07:
					return LCSI.OperationalStateActivated;
				case (byte)0x04:
				case (byte)0x06:
					return LCSI.OperationalStateDeactivated;
				case (byte)0x0C:
				case (byte)0x0D:
				case (byte)0x0E:
				case (byte)0x0F:
					return LCSI.TerminationState;
			}
		}
		return LCSI.Unknown;
	}

	/*
	public boolean isPINEnabled(KeyRef keyRef){
		byte[] pinStat= getTagValue(TAG_PIN_STATUS_TEMPLATE);

		if  (pinStat==null){
			return false;
		}
		
		PS_DO_Template template = new PS_DO_Template(pinStat);
		return template.isEnabled(keyRef);
	}
	*/
	
	public short getARR_FID(){
		byte[] securityAttr = getTagValue(TAG_SECURITY_ATTRIBUTE_1);
		
		if (securityAttr.length>=2){
			//return ConversionUtils.arraySplice(securityAttr, 0, 2);
			return Utilities.getShort(securityAttr, 0);
		}
		else{
			return (short)0;
		}
	}
	
	public int getARR_Record(){
		byte[] securityAttr = getTagValue(TAG_SECURITY_ATTRIBUTE_1);
		
		if (securityAttr.length>=3){
			return Utilities.makeInt((byte)0, (byte)0, (byte)0, securityAttr[2]);
		}
		else{
			return 0;
		}
	}
}
