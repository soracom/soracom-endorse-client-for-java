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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.soracom.endorse.SORACOMEndorseCLI;

public class Utilities {
	
	private static final Gson gson = new Gson();
	
	public static <T> T fromJson(String json,Class<T> clazz){
		return gson.fromJson(json, clazz);
	}
	
	public static <T> T fromJson(JsonObject json,Class<T> clazz){
		return gson.fromJson(json, clazz);
	}
	
	public static String toJson(Object o) {
		return gson.toJson(o);
	}
	
	public static String byteToHexString(byte src){
		return String.format("%02X",  src);
	}
	
	public static String byteArrayToHexString(byte[] src, int offset, int length, boolean spaceBytes){
		StringBuilder sb = new StringBuilder();

		for (int x=0; x<length; x++)
		{
			sb.append(String.format("%02X",  src[offset+x]));
			if (spaceBytes) sb.append(" ");
		}
		return sb.toString();
	}
	
	public static String byteArrayToHexString(byte[] src){
		if (src==null){
			return "";
		}
		else{
			return byteArrayToHexString(src, 0, src.length, false);
		}
	}
	
	public static byte[] hexStringToByteArray(String s) {
		if (s==null) return new byte[0];
		
	    String input = s.replace(" ", "");//Remove all spaces first
		int len = input.length();
	    
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(input.charAt(i), 16) << 4)
	                             + Character.digit(input.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static String padRight (String str, int size, char padChar)
	{
	  StringBuilder padded = new StringBuilder(str);
	  while (padded.length() < size)
	  {
	    padded.append(padChar);
	  }
	  return padded.toString();
	}
	
	/**
	 * Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array.
	 * @param src
	 * @param srcOff
	 * @param dest
	 * @param destOff
	 * @param length
	 * @return
	 */
	public static final short arrayCopy(byte[] src, short srcOff, byte[] dest, short destOff, short length){
		for (short i=0;i<length;i++){
			dest[destOff+i] =src[srcOff+i];
		}
		return (short)(destOff+length);
	}
	
	/**
	 * Copies an array from the specified source array, beginning at the specified position, to the specified position of the destination array.
	 * @param src
	 * @param srcOff
	 * @param dest
	 * @param destOff
	 * @param length
	 * @return
	 */
	public static final int arrayCopy(byte[] src, int srcOff, byte[] dest, int destOff, int length){
		for (int i=0;i<length;i++){
			dest[destOff+i] =src[srcOff+i];
		}
		return (destOff+length);
	}
	/**
	 * Alias for the arrayCopy function
	 * @param src
	 * @param srcOff
	 * @param dest
	 * @param destOff
	 * @param length
	 * @return
	 */
	public static final short arrayCopyNonAtomic(byte[] src, short srcOff, byte[] dest,short destOff, short length){
		return arrayCopy(src, srcOff, dest,destOff, length);
	}
	
	/**
	 * Fills the byte array (non-atomically) beginning at the specified position, for the specified length with the specified byte value.
	 * @param bArray
	 * @param bOff
	 * @param bLen
	 * @param bValue
	 * @return
	 */
	public static final short arrayFillNonAtomic(byte[] bArray, short bOff, short bLen, byte bValue){
		for (short i=0;i<bLen;i++){
			bArray[bOff+i] =bValue;
		}
		return (short)(bOff+bLen);
	}
	
	/**
	 * Compares an array from the specified source array, beginning at the specified position, with the specified position of the destination array from left to right. 
	 * @param src
	 * @param srcOff
	 * @param dest
	 * @param destOff
	 * @param length
	 * @return Returns the ternary result of the comparison : less than(-1), equal(0) or greater than(1).
	 */
	public static final byte arrayCompare(byte[] src, int srcOff, byte[] dest, int destOff, int length)
     {
		
		byte retVal =0;
		
		for (int i=0;i<length;i++){
			if (src[srcOff+i]<dest[destOff+i]) return (byte)-1;
			if (src[srcOff+i]>dest[destOff+i]) return (byte)1;
		}
			
		return retVal;
	}
	
	public static byte[] arraySplice(byte[] srcArray, int offset, int length){
		
		byte[] newOutput = new byte[length];
		System.arraycopy(srcArray, offset, newOutput, 0, length);
		return newOutput;
	}
	


   public static byte[] arrayConcat(byte[] a, byte[] b) {
      byte[] c = new byte[a.length + b.length];
      System.arraycopy(a, 0, c, 0, a.length);
      System.arraycopy(b, 0, c, a.length, b.length);
      return c;
   }
   
   /**
	 * Separates each pair of hexadecimal digits by a space
	 * @param str - Hexadecimal String to transform 
	 * @return - the spaced hexadecimal string
	 */
	public static String spaceHexString (String str)
	{
	  byte[] data = hexStringToByteArray(str);
	  return byteArrayToHexString(data, 0, data.length, true);
	}
	
	/**
	 * Nibble swap each pair of hexadecimal digits 
	 * @param str - Hexadecimal String to transform 
	 * @return - the spaced hexadecimal string
	 */
	public static String swapHexString (String str)
	{
	  byte[] data = hexStringToByteArray(str);
	  for (int i=0;i<data.length;i++){
		  byte o = (byte)((data[i]<<4)&0xF0);
		  o|=(byte)((data[i]>>4)&0x0F);
		  data[i]=o;
	  }
	  return byteArrayToHexString(data, 0, data.length, false);
	}
	
	public static int hexStringToInt(String s) {
		if (s==null || s.isEmpty()) return 0;
	    String input = s.replace(" ", "");//Remove all spaces first
	    int i = Integer.parseInt(input, 16) ;
	    return i;
	}
	
	public static byte hexStringToByte(String s) {
		if (s==null || s.isEmpty()) return (byte)0;
		
	    String input = s.replace(" ", "");//Remove all spaces first
	    byte b = (byte) ((int)Integer.parseInt(input, 16) & 0xFF);
	    return b;
	}
	
	/**
	 * Concatenates the two parameter bytes to form a short value.
	 * @param b1
	 * @param b2
	 * @param b3
	 * @param b4
	 * @return
	 */
	public static int makeInt(byte b1, byte b2, byte b3, byte b4){
		return( ((b1&0xFF)<<24) | ((b2&0xFF)<<16) | ((b3&0xFF)<<8) | (b4&0xFF) );
	} 
	
	/**
	 * Concatenates the two parameter bytes to form a short value.
	 * @param b1
	 * @param b2
	 * @return
	 */
	public static short makeShort(byte b1, byte b2){
		return(short)( ((b1&0xFF)<<8) | (b2&0xFF) );
	}
	/**
	 * Concatenates two bytes in a byte array to form a short value.
	 * @param arr
	 * @param offset
	 * @return
	 */
	public static short getShort(byte[] arr, short offset){
		return(short)( ((arr[offset]&0xFF)<<8) | (arr[offset+1]&0xFF) );
	}
	/**
	 * Concatenates two bytes in a byte array to form a short value.
	 * @param arr
	 * @param offset
	 * @return
	 */
	public static short getShort(byte[] arr, int offset){
		return(short)( ((arr[offset]&0xFF)<<8) | (arr[offset+1]&0xFF) );
	}
	
	/**
	 * Deposits the short value as two successive bytes at the specified offset in the byte array.
	 * @param bArray - Target byte array to write to
	 * @param iOff - Start offset to write
	 * @param sValue - The value to write as short
	 * @return - The new offset after depositing the bytes (iOff +2)
	 */
	public static final short setShort(byte[] bArray, int iOff, short sValue){
		bArray[iOff]=(byte)(((sValue&0xFF00)>>8)&0xFF);
		bArray[iOff+1]=(byte)(sValue&0xFF);
		return (short)(iOff+2);
	}
	
	/**
	 * Deposits the int value as two successive bytes (as short) at the specified offset in the byte array.
	 * @param bArray - Target byte array to write to
	 * @param iOff - Start offset to write
	 * @param iValue - The value to write as short
	 * @return - The new offset after depositing the bytes (iOff +2)
	 */
	public static final short setShort(byte[] bArray, int iOff, int iValue){
		bArray[iOff]=(byte)(((iValue&0x0000FF00)>>8)&0xFF);
		bArray[iOff+1]=(byte)(iValue&0xFF);
		return (short)(iOff+2);
	}
	
	/**
	 * Decode UTF8 Base64 encoded string into a byte array 
	 * @param value - The Base 64 encoded string
	 * @return - A byte array representing the decoded base 64 string
	 */
	public static final byte[] base64toBytes(String value){
		return Base64.getDecoder().decode(value.getBytes(StandardCharsets.UTF_8));
	}
	
	/**
	 * Encode a byte array into a UTF8 Base64 encoded string
	 * @param value - The byte array to be encoded to base 64 string
	 * @return - The Base 64 encoded string
	 */
	public static final String bytesToBase64(byte[] value){
		 byte[] retVal = Base64.getEncoder().encode(value);
		 return new String(retVal,StandardCharsets.UTF_8);
	}
	
	/**
	 * Merge two json strings. Merge "source" into "target". If fields have equal name, merge them recursively. 
	 * @return the merged object (target). 
	 */ 
	public static JsonObject jsonMerge(JsonObject source, JsonObject target) { 
	    for (String key: source.keySet()) { 
            JsonElement value = source.get(key); 
            if (!target.has(key)) { 
                // new value for "key": 
                target.add(key, value); 
            }
	    } 
	    return target; 
	} 
	
	public static List<String> readResource(String resource){
		try {
			InputStream is = SORACOMEndorseCLI.class.getResourceAsStream(resource);
			BufferedReader reader = new BufferedReader(new InputStreamReader(is,StandardCharsets.UTF_8));
			String line = null;
			List<String> lines = new ArrayList<>();
			while((line = reader.readLine()) !=null) {
				if(line.trim().isEmpty()==false) {
					lines.add(line);
				}
			}
			return lines;
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage(),e);
		}
	}

	public static String escapeString( String value, boolean quote )
	{
		StringBuilder builder = new StringBuilder();
		if( quote )
			builder.append( "\"" );
		for( char c : value.toCharArray() )
		{
			if( c == '\'' )
				builder.append( "\\'" );
			else if ( c == '\"' )
				builder.append( "\\\"" );
			else if( c == '\r' )
				builder.append( "\\r" );
			else if( c == '\n' )
				builder.append( "\\n" );
			else if( c == '\t' )
				builder.append( "\\t" );
			else if( c < 32 || c >= 127 )
				builder.append( String.format( "\\u%04x", (int)c ) );
			else
				builder.append( c );
		}
		if( quote )
			builder.append( "\"" );
		return builder.toString();
	}
}
