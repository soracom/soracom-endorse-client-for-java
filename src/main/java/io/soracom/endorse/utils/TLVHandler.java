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



/**
 * A Java implementation of the Javacard UICC Toolkit ViewHandler and EditHandler Interfaces
 * @author olivier.comarmond
 *
 */
public class TLVHandler {
	
	private byte[] bArray;
	private short sLength;
	private short sCapacity;
	private short foundTagOffset;
	
	public static byte TLV_NOT_FOUND =(byte)0;
	public static byte TLV_FOUND_CR_SET =(byte)1;
	public static byte TLV_FOUND_CR_NOT_SET =(byte)2;
	
	public TLVHandler(){
		sCapacity = (short)32767;
		sLength=(short)0;
		foundTagOffset = (short)-1;
		bArray = new byte[sCapacity];
	}
	
	public TLVHandler(short capacity){
		sCapacity = capacity;
		sLength=(short)0;
		foundTagOffset = (short)-1;
		bArray = new byte[sCapacity];
	}
	
	/**Compares the last found TLV element with a buffer.
	 * 
	 * @param valueOffset
	 * @param compareBuffer
	 * @param compareOffset
	 * @param compareLength
	 * @return 0 if identical
		-1 if the first miscomparing byte in Comprehension TLV List is less than that in compareBuffer,
		1 if the first miscomparing byte in Comprehension TLV List is greater than that in compareBuffer.
	 * @throws Exception 
	 */
	public byte compareValue(short valueOffset, byte[] compareBuffer, short compareOffset, short compareLength) throws Exception{
		
		if (foundTagOffset<0) throw new Exception("No current TLV");
		short valueIdx = TLVArray.getValueOffset(bArray, foundTagOffset);
		
		return Utilities.arrayCompare(bArray, (short)(valueIdx+valueOffset), compareBuffer, compareOffset, compareLength);
	}
	
    /**
     * Copies the Comprehension TLV list contained in the handler to the destination byte array.
     * @param dstBuffer
     * @param dstOffset
     * @param dstLength
     * @return dstOffset+dstLength
     */
	public short copy(byte[] dstBuffer, short dstOffset, short dstLength){
		
		return Utilities.arrayCopyNonAtomic(bArray, (short)0, dstBuffer, dstOffset, dstLength);
		
	}
	
	
    /**
     * Copies a part of the last TLV element which has been found, into a destination buffer.
     * @param valueOffset
     * @param dstBuffer
     * @param dstOffset
     * @param dstLength
     * @return
     * @throws Exception 
     */
	public short copyValue(short valueOffset, byte[] dstBuffer, short dstOffset, short dstLength) throws Exception {
		
		if (foundTagOffset<0) throw new Exception("UNAVAILABLE_ELEMENT");
		short valueIdx = TLVArray.getValueOffset(bArray, foundTagOffset);
		
		return Utilities.arrayCopyNonAtomic(bArray, (short)(valueIdx+valueOffset), dstBuffer, dstOffset, dstLength);

	}
	
	/**
	 * Looks for the indicated occurrence of a TLV element from the beginning of the TLV list (handler buffer).
	 * @param tag
	 * @param occurrence
	 * @return
	 */
	public byte findTLV(byte tag, byte occurrence){
		short tlvOffset = (short)0;
		byte cntOccurence = (byte)0;
		byte retVal = TLV_NOT_FOUND;
		do{

			if (bArray[tlvOffset]==(byte)(tag|(byte)0x80)) {
				if ( ++cntOccurence ==occurrence) {
					retVal= TLV_FOUND_CR_SET; 
					break;
				}
			}
			if (bArray[tlvOffset]==(byte)(tag&(byte)0x7F)){
				if ( ++cntOccurence ==occurrence) {
					retVal= TLV_FOUND_CR_NOT_SET; 
					break;
				}
			}
			
			tlvOffset = TLVArray.getNextTLVOffset(bArray, tlvOffset,sLength);
		}
		while (tlvOffset>(short)0);
		
		foundTagOffset=(retVal!=TLV_NOT_FOUND)?tlvOffset:(short)-1;
		
		return retVal;
	}
    
	/**
	 * Looks for the first occurrence of a TLV element from beginning of a TLV list and compare its value with a buffer.
	 * @param tag
	 * @param compareBuffer
	 * @param compareOffset
	 * @return
	 * @throws Exception 
	 */
	public byte findAndCompareValue(byte tag, byte[] compareBuffer, short compareOffset) throws Exception{
		if (findTLV(tag, (byte)1)!=TLV_NOT_FOUND) {
			short tlvLength = TLVArray.getTLVLength(bArray, (short)0);
			return compareValue((short)0,  compareBuffer, compareOffset, tlvLength);
		}
		else throw new Exception("UNAVAILABLE_ELEMENT");
	}
    
	/**
	 * Looks for the indicated occurrence of a TLV element from the beginning of a TLV list and compare its value with a buffer.
	 * @param tag
	 * @param occurrence
	 * @param valueOffset
	 * @param compareBuffer
	 * @param compareOffset
	 * @param compareLength
	 * @return
	 * @throws Exception 
	 */
	public byte findAndCompareValue(byte tag, byte occurrence, short valueOffset, byte[] compareBuffer, short compareOffset, short compareLength) throws Exception{
		if (findTLV(tag, (byte)1)!=TLV_NOT_FOUND) {

			return compareValue((short)0,  compareBuffer, compareOffset, compareLength);
		}
		else throw new Exception("UNAVAILABLE_ELEMENT");
	}
	
	/**
	 * Looks for the first occurrence of a TLV element from the beginning of a TLV list and copy its value into a destination buffer.
	 * @param tag
	 * @param dstBuffer
	 * @param dstOffset
	 * @return
	 * @throws Exception 
	 */
	public short findAndCopyValue(byte tag, byte[] dstBuffer, short dstOffset) throws Exception{
		if (findTLV(tag, (byte)1)!=TLV_NOT_FOUND) {
			short tlvLength = TLVArray.getTLVLength(bArray, (short)0);
			return copyValue((short)0, dstBuffer, dstOffset, tlvLength);
		}
		else throw new Exception("UNAVAILABLE_ELEMENT");
	}
    	
	/**
	 * Looks for the indicated occurrence of a TLV element from the beginning of a TLV list and copy its value into a destination buffer.
	 * @param tag
	 * @param occurrence
	 * @param valueOffset
	 * @param dstBuffer
	 * @param dstOffset
	 * @param dstLength
	 * @return
	 * @throws Exception 
	 */
	public short findAndCopyValue(byte tag, byte occurrence, short valueOffset, byte[] dstBuffer, short dstOffset, short dstLength) throws Exception{
	
		if (findTLV(tag, (byte)1)!=TLV_NOT_FOUND) {
			return copyValue((short)0, dstBuffer, dstOffset, dstLength);
		}
		else throw new Exception("UNAVAILABLE_ELEMENT");
	}
    
	
    
	/**
	 * Returns the maximum size of the Comprehension TLV list managed by the handler.
	 * @return
	 */
	public short getCapacity() {
		return sCapacity;
	}
    
	/**
	 * Returns the length of the TLV list.
	 * @return
	 */
	public short getLength() {
		return sLength;
	}
    
	/**
	 * Gets a byte from the last TLV element which has been found in the handler.
	 * @param valueOffset
	 * @return
	 * @throws Exception 
	 */
	public byte getValueByte(short valueOffset) throws Exception {
		if (foundTagOffset<0) throw new Exception("UNAVAILABLE_ELEMENT");
		short valueIdx = TLVArray.getValueOffset(bArray, foundTagOffset);
		
		return bArray[(short)(valueIdx+valueOffset)];

	}
    
	/**
	 * Gets the binary length of the value field for the last TLV element which has been found in the handler.
	 * @return
	 * @throws Exception 
	 */
	public short getValueLength() throws Exception {
		
		if (foundTagOffset<0) throw new Exception("UNAVAILABLE_ELEMENT");
		return TLVArray.getTLVLength(bArray, foundTagOffset);
	}
    
	/**
	 * Gets a short from the last TLV element which has been found in the handler
	 * @param valueOffset
	 * @return
	 * @throws Exception 
	 */
	public short getValueShort(short valueOffset) throws Exception {
		if (foundTagOffset<0) throw new Exception("UNAVAILABLE_ELEMENT");
		short valueIdx = TLVArray.getValueOffset(bArray, foundTagOffset);
		
		return Utilities.getShort(bArray, (short)(valueIdx+valueOffset));
	}
    
	
	///EDIT HANDLER
	/**
	 * Appends a buffer into the EditHandler buffer.
	 * @param buffer
	 * @param offset
	 * @param length
	 */
	public void appendArray(byte[] buffer, short offset, short length) {
		sLength = Utilities.arrayCopyNonAtomic(buffer, offset, bArray, sLength, length);
	}
    
	/**
	 * Appends a TLV element to the current TLV list (1-byte element).
	 * @param tag
	 * @param value
	 */
	public void appendTLV(byte tag, byte value){
		bArray[sLength++]=tag;
		bArray[sLength++]=(byte)0x01;
		bArray[sLength++]=value;
		
	}
    
	/**
	 * Appends a TLV element to the current TLV list (byte array format).
	 * @param tag
	 * @param value
	 * @param valueOffset
	 * @param valueLength
	 */
	public void appendTLV(byte tag, byte[] value, short valueOffset, short valueLength){
		bArray[sLength++]=tag;
		sLength = TLVArray.setTLVLength(bArray, sLength, valueLength);
		sLength = Utilities.arrayCopyNonAtomic(value, valueOffset, bArray, sLength, valueLength);
	}
    
	/**
	 * Appends a TLV element to the current TLV list (2 byte arrays format).
	 * @param tag
	 * @param value1
	 * @param value1Offset
	 * @param value1Length
	 * @param value2
	 * @param value2Offset
	 * @param value2Length
	 */
	public void appendTLV(byte tag, byte[] value1, short value1Offset, short value1Length, byte[] value2, short value2Offset, short value2Length){
		bArray[sLength++]=tag;
		sLength = TLVArray.setTLVLength(bArray, sLength, (short)(value1Length+value2Length));
		sLength = Utilities.arrayCopyNonAtomic(value1, value1Offset, bArray, sLength, value1Length);
		sLength = Utilities.arrayCopyNonAtomic(value2, value2Offset, bArray, sLength, value2Length);
	}
    
	/**
	 * Appends a TLV element to the current TLV list (2-byte element) This method is useful to add double byte elements as Device Identities, Duration or Response Length.
	 * @param tag
	 * @param value1
	 * @param value2
	 */
	public void appendTLV(byte tag, byte value1, byte value2){
		bArray[sLength++]=tag;
		bArray[sLength++]=(byte)0x02;
		bArray[sLength++]=value1;
		bArray[sLength++]=value2;
	}
	

	/**
	 * Appends a TLV element to the current TLV list (1 byte and a byte array format).
	 * @param tag
	 * @param value1
	 * @param value2
	 * @param value2Offset
	 * @param value2Length
	 */
	public void appendTLV(byte tag, byte value1, byte[] value2, short value2Offset, short value2Length){
		bArray[sLength++]=tag;
		sLength = TLVArray.setTLVLength(bArray, sLength, (short)(value2Length+1));
		bArray[sLength++]=value1;
		sLength = Utilities.arrayCopyNonAtomic(value2, value2Offset, bArray, sLength, value2Length);
	}
    
	/**
	 * Appends a TLV element to the current TLV list (3-byte element(1-byte,1-short)) This method is useful to add three byte elements as Command details or Display parameters A successful append does not modify the TLV selected.
	 * @param tag
	 * @param value1
	 * @param value2
	 */
	public void appendTLV(byte tag, byte value1, short value2){
		bArray[sLength++]=tag;
		bArray[sLength++]=(byte)0x03;
		bArray[sLength++]=value1;
		sLength = Utilities.setShort(bArray, sLength, value2);
	}
    
	/**
	 * Appends a TLV element to the current TLV list (2-byte element).
	 * @param tag
	 * @param value
	 */
	public void appendTLV(byte tag, short value){
		bArray[sLength++]=tag;
		bArray[sLength++]=(byte)0x02;
		sLength = Utilities.setShort(bArray, sLength, value);
	}
    
	/**
	 * Appends a TLV element to the current TLV list (4-byte element(2-short)) This method is useful to add three byte elements as Text Attribute, ESN, or C-APDU.
	 * @param tag
	 * @param value1
	 * @param value2
	 */
	public void appendTLV(byte tag, short value1, short value2){
		bArray[sLength++]=tag;
		bArray[sLength++]=(byte)0x04;
		sLength = Utilities.setShort(bArray, sLength, value1);
		sLength = Utilities.setShort(bArray, sLength, value2);
	}
	
	
    
	/**
	 * Clears the TLV list of an EditHandler and resets the current TLV selected.
	 */
	public void clear(){
		Utilities.arrayFillNonAtomic(bArray, (short)0, sLength, (byte)0);
		sLength=0;
	}
    
	
	//Deviation from the original TLV Handler
	
	/**
	 * Appends a TLV element to the current TLV list (byte array format).
	 * @param tag - Tag value
	 * @param value - Value bytes
	 */
	public void appendTLV(byte tag, byte[] value){
		bArray[sLength++]=tag;
		sLength = TLVArray.setTLVLength(bArray, sLength, (short)value.length);
		sLength = Utilities.arrayCopyNonAtomic(value, (short)0, bArray, sLength, (short)value.length);
	}
	

	/**
	 * Appends a TLV element to the current TLV list (1 byte and a byte array format).
	 * @param tag - Tag value
	 * @param value1 - First value byte
	 * @param value2 - Second value byte
	 */
	public void appendTLV(byte tag, byte value1, byte[] value2){
		bArray[sLength++]=tag;
		sLength = TLVArray.setTLVLength(bArray, sLength, (short)(value2.length+1));
		bArray[sLength++]=value1;
		sLength = Utilities.arrayCopyNonAtomic(value2, (short)0, bArray, sLength, (short)(value2.length));
	}
	
	/**
	 * Appends a TLV element to the current TLV list (2-byte element) This method is useful to add double byte elements as Device Identities, Duration or Response Length.
	 * @param tag
	 * @param value1
	 * @param value2
	 * @param value3
	 */
	public void appendTLV(byte tag, byte value1, byte value2, byte value3){
		bArray[sLength++]=tag;
		bArray[sLength++]=(byte)0x03;
		bArray[sLength++]=value1;
		bArray[sLength++]=value2;
		bArray[sLength++]=value3;
	}
    
}
