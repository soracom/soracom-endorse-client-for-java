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

public class TLVArray {
	/**
	 * Get the length in a TLV stored in an array
	 * @param arr - the array to use
	 * @param tagOffset - the offset of the tag
	 * @return - length size in the TLV or 0
	 */
	public static final short getTLVLength(byte[] arr, short tagOffset)
	{
		short lenOffset=(short)(tagOffset+1); //TODO: improve on this method
		switch (arr[lenOffset])
		{
		case (byte)0x81:
			return Utilities.makeShort((byte)0, arr[(short)(lenOffset+1)]);
		case (byte)0x82:
			return Utilities.getShort(arr, (short)(lenOffset+1));
		default:
			return Utilities.makeShort((byte)0, arr[lenOffset]);
		}
	}
	
	/**
	 * Get the length in a TLV stored in an array
	 * @param arr - the array to use
	 * @param tagOffset - the offset of the tag
	 * @return - length size in the TLV or 0
	 */
	public static final int getTLVLength(byte[] arr, int tagOffset)
	{
		int lenOffset=tagOffset+1; //TODO: improve on this method
		switch (arr[lenOffset])
		{
		case (byte)0x81:
			return Utilities.makeInt((byte)0,(byte)0, (byte)0, arr[lenOffset+1]);
		case (byte)0x82:
			return Utilities.makeInt((byte)0,(byte)0, arr[lenOffset+1],arr[lenOffset+2]);
		case (byte)0x83:
			return Utilities.makeInt((byte)0, arr[lenOffset+1],arr[lenOffset+2],arr[lenOffset+3]);
		default:
			return Utilities.makeInt((byte)0,(byte)0, (byte)0, arr[lenOffset]);
		}
	}
	
	/**
	 * Set the length in a TLV stored in an array
	 * @param arr - the array to use
	 * @param lenOffset - the offset of the length
	 * @return - lenOffset + length written in array (i.e value offset)
	 */
	public static final short setTLVLength(byte[] arr, short lenOffset, short length)
	{

		if (length<0)
		{
			arr[lenOffset++]=(byte)0;
		}
		else if (length <128)
		{
			arr[lenOffset++]=(byte)(length&(byte)0xFF);
		}
		else if (length <256)
		{
			arr[lenOffset++]=(byte)0x81;
			arr[lenOffset++]=(byte)(length&(byte)0xFF);
		}
		else if (length <65536)
		{
			arr[lenOffset++]=(byte)0x82;
			lenOffset = Utilities.setShort(arr, lenOffset, length);
		}
			
		return lenOffset;
		
	}
	
	/**
	 * Set the length in a TLV stored in an array
	 * @param arr - the array to use
	 * @param lenOffset - the offset of the length
	 * @return - lenOffset + length written in array (i.e value offset)
	 */
	public static final int setTLVLength(byte[] arr, int lenOffset, int length)
	{

		if (length<0)
		{
			arr[lenOffset++]=(byte)0;
		}
		else if (length <128)
		{
			arr[lenOffset++]=(byte)(length&(byte)0xFF);
		}
		else if (length <256)
		{
			arr[lenOffset++]=(byte)0x81;
			arr[lenOffset++]=(byte)(length&(byte)0xFF);
		}
		else if (length <65536)
		{
			arr[lenOffset++]=(byte)0x82;
			arr[lenOffset++]=(byte)(((length&0x0000FF00)>>8)&0xFF);
			arr[lenOffset++]=(byte)(length&(byte)0xFF);
		}
		else if (length <16777216)
		{
			arr[lenOffset++]=(byte)0x83;
			arr[lenOffset++]=(byte)(((length&0x00FF0000)>>16)&0xFF);
			arr[lenOffset++]=(byte)(((length&0x0000FF00)>>8)&0xFF);
			arr[lenOffset++]=(byte)(length&(byte)0xFF);
		}
		return lenOffset;
		
	}
	
	/**
	 * Get the offset of the value portion in a TLV stored in an array
	 * @param arr - the array to use
	 * @param tagOffset - the offset of the current tag
	 * @return - length size in the TLV or 0
	 */
	public static final short getValueOffset(byte[] arr, short tagOffset)
	{
		short lenOffset=(short)(tagOffset+1); //TODO: improve on this method
		switch (arr[lenOffset])
		{
		case (byte)0x81:
			return (short)(lenOffset+2);
		case (byte)0x82:

			return (short)(lenOffset+3);
		default:

			return (short)(lenOffset+1);
		}
	}
	
	/**
	 * Get the offset of the value portion in a TLV stored in an array
	 * @param arr - the array to use
	 * @param tagOffset - the offset of the current tag
	 * @return - length size in the TLV or 0
	 */
	public static final int getValueOffset(byte[] arr, int tagOffset)
	{
		int lenOffset=tagOffset+1; //TODO: improve on this method
		switch (arr[lenOffset])
		{
		case (byte)0x81:
			return lenOffset+2;
		case (byte)0x82:
			return lenOffset+3;
		case (byte)0x83:
			return lenOffset+4;
		default:

			return lenOffset+1;
		}
	}

	
	/**
	 * Get the offset of the next Tag in a TLV stored in an array
	 * Example usage:
	 * {@code
	 * while (nextOffset>=0){
	 *      short tlvLength = TLVArray.getTLVLength(bytes, nextOffset);
	 *		short valueOffset = TLVArray.getValueOffset(bytes, nextOffset);
	 *		switch (bytes[nextOffset]){
	 *		case x:
	 *			break;
	 *		}
	 *		nextOffset = TLVArray.getNextTLVOffset(bytes,nextOffset,maxOffset);
	 * }
	 * }
	 * @param arr - the array to use
	 * @param tagOffset - the offset of the current tag
	 * @return - length size in the TLV or -1 in case of failure
	 */
	public static final short getNextTLVOffset(byte[] arr, short tagOffset,short maxOffset)
	{
		short lenOffset=(short)(tagOffset+1); //TODO: improve on this method
		if (lenOffset>(short)arr.length || lenOffset>maxOffset) return (short)-1;
		short len =0;
		short retVal = (short)0;
		switch (arr[lenOffset])
		{
			case (byte)0x81:
				len= Utilities.makeShort((byte)0, arr[(short)(lenOffset+1)]);
				retVal = (short)(lenOffset+len+2);
				break;
			case (byte)0x82:
				len= Utilities.getShort(arr, (short)(lenOffset+1));
				retVal = (short)(lenOffset+len+3);
				break;
			default:
				len= Utilities.makeShort((byte)0, arr[lenOffset]);
				retVal = (short)(lenOffset+len+1);
				break;
		}
		return(retVal>=(short)arr.length || retVal>maxOffset)?(short)-1:retVal;
	}
	
	

	/**
	 * Get the offset of the next Tag in a TLV stored in an array
	 * Example usage:
	 * {@code
	 * while (nextOffset>=0){
	 *      int tlvLength = TLVArray.getTLVLength(bytes, nextOffset);
	 *		int valueOffset = TLVArray.getValueOffset(bytes, nextOffset);
	 *		switch (bytes[nextOffset]){
	 *		case x:
	 *			break;
	 *		}
	 *		nextOffset = TLVArray.getNextTLVOffset(bytes,nextOffset,maxOffset);
	 * }
	 * }
	 * @param arr - the array to use
	 * @param tagOffset - the offset of the current tag
	 * @return - length size in the TLV or -1 in case of failure
	 */
	public static final int getNextTLVOffset(byte[] arr, int tagOffset,int maxOffset)
	{
		int lenOffset=tagOffset+1; //TODO: improve on this method
		if (lenOffset>arr.length || lenOffset>maxOffset) return -1;
		int len =0;
		int retVal = 0;
		switch (arr[lenOffset])
		{
			case (byte)0x81:
				len= Utilities.makeInt((byte)0,(byte)0,(byte)0, arr[lenOffset+1]);
				retVal = lenOffset+len+2;
				break;
			case (byte)0x82:
				len= Utilities.makeInt((byte)0,(byte)0, arr[lenOffset+1], arr[lenOffset+2]);
				retVal = lenOffset+len+3;
				break;
			case (byte)0x83:
				len=  Utilities.makeInt((byte)0, arr[lenOffset+1],arr[lenOffset+2],arr[lenOffset+3]);
				retVal = lenOffset+len+4;
				break;
			default:
				len= Utilities.makeInt((byte)0,(byte)0,(byte)0, arr[lenOffset]);
				retVal = lenOffset+len+1;
				break;
		}
		return(retVal>=arr.length || retVal>maxOffset)?-1:retVal;
	}
	
}
