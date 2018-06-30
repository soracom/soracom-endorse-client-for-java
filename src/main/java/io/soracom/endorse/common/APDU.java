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
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.xml.ws.RespectBindingFeature;

import io.soracom.endorse.utils.Utilities;


/**
 * Hexadecimal String APDU class modeled as per ISO7816-4 specially for the telecom industry
 * 
 * @author olivier.comarmond
 *
 */

public class APDU {
    
	private String name;
	private String cla;
	private String ins;
	private String p1;
	private String p2;
	private String lc;
	private String cmdData;
	private String le;
	private String respData;
	private String sw1;
	private String sw2;
	
    
    public APDU(){
    	
    }
	
    //Additional constructors for the 4 cases of APDU
    /*	Case	Command data	Expected response data
    *	1		No data			No data
    *	2		No data			Data
    *	3		Data			No data
    *	4		Data			Data
    */
    
    /**
     * In case 1, the length Lc is null; therefore the Lc field and the data field are empty. 
     * The length Le is also null; therefore the Le field is empty. 
     * Consequently, the body is empty.
     * @param pName - An optional Name for the command (eg "SELECT")
     * @param pCLA - Class in hexadecimal representation (eg "00")
     * @param pINS - Instruction in hexadecimal representation (eg "A4")
     * @param pP1 - Parameter 1 -in hexadecimal representation (eg "00")
     * @param pP2 - Parameter 2 in hexadecimal representation (eg "00")
     */
    public APDU(String pName, String pCLA, String pINS, String pP1, String pP2){
    	
    	name=pName;
    	cla=pCLA.replace(" ", "");;
    	ins=pINS.replace(" ", "");;
    	p1=pP1.replace(" ", "");;
    	p2=pP2.replace(" ", "");;
    	lc="";
    	cmdData="";
    	le="";
    	respData="";
    	sw1="";
    	sw2="";
    }

    /**
     * In case 2, the length Lc is null; therefore the Lc field and the data field are empty. 
     * The length of Le is not null; therefore the Le field is present. 
     * Consequently, the body consists of the Le field.
     * @param pName - An optional Name for the command (eg "SELECT")
     * @param pCLA - Class in hexadecimal representation (eg "00")
     * @param pINS - Instruction in hexadecimal representation (eg "A4")
     * @param pP1 - Parameter 1 -in hexadecimal representation (eg "00")
     * @param pP2 - Parameter 2 in hexadecimal representation (eg "00")
     * @param pLe - Expected length
     */
    public APDU(String pName, String pCLA, String pINS, String pP1, String pP2, String pLe){
    	name=pName;
    	cla=pCLA.replace(" ", "");;
    	ins=pINS.replace(" ", "");;
    	p1=pP1.replace(" ", "");;
    	p2=pP2.replace(" ", "");;
    	lc="";
    	cmdData="";
    	le=pLe.replace(" ", "");;
    	respData="";
    	sw1="";
    	sw2="";
    }

    
    /**
     *  In case 3, the length Lc is not null; therefore the Lc field is present and the data field consists of the Lc subsequent bytes. 
     * The length Le is null; therefore the Le field is empty. 
     * Consequently, the body consists of the Lc field followed by the data field.
     * @param pName - An optional Name for the command (eg "SELECT")
     * @param pCLA - Class in hexadecimal representation (eg "00")
     * @param pINS - Instruction in hexadecimal representation (eg "A4")
     * @param pP1 - Parameter 1 -in hexadecimal representation (eg "00")
     * @param pP2 - Parameter 2 in hexadecimal representation (eg "00")
     * @param pLc - Length of command data in hexadecimal representation (eg "7F")
     * 		Leave this as empty string to have the value calculated
     * 		If it is smaller than the length of the data hexadecimal string in data, a subset of the data corresponding to the Lc will be used
     * 		If it is larger,  Lc shall be readjusted
     * @param pCmdData - The Command Data in hexadecimal string
     */
    public APDU(String pName, String pCLA, String pINS, String pP1, String pP2, String pLc, String pCmdData){
    	name=pName;
    	cla=pCLA.replace(" ", "");
    	ins=pINS.replace(" ", "");
    	p1=pP1.replace(" ", "");
    	p2=pP2.replace(" ", "");
    	if (pCmdData.isEmpty()){
    		lc="";
    		cmdData = pCmdData;
    	}
    	else{
	    	byte[] data = Utilities.hexStringToByteArray(pCmdData);
	    	int len =  ((pLc.isEmpty())?0xFF:(Utilities.hexStringToByte(pLc) & 0x000000FF));
	    	if (len>data.length){
	    		len = data.length;
	    	}
	    	lc= Utilities.byteToHexString((byte)(len&0x000000FF));
	    	cmdData = Utilities.byteArrayToHexString(data, 0, len, false);
    	}
    	le="";
    	respData="";
    	sw1="";
    	sw2="";
    }
    
    /**
     *  In case 4, the length Lc is not null; therefore the Lc field is present and the data field consists of the Lc subsequent bytes. 
     *  The length Le is also not null; therefore the Le field is also present. 
     *  Consequently, the body consists of the Lc field followed by the data filed and the Le field.
     * @param pName - An optional Name for the command (eg "SELECT")
     * @param pCLA - Class in hexadecimal representation (eg "00")
     * @param pINS - Instruction in hexadecimal representation (eg "A4")
     * @param pP1 - Parameter 1 -in hexadecimal representation (eg "00")
     * @param pP2 - Parameter 2 in hexadecimal representation (eg "00")
     * @param pLc - Length of command data in hexadecimal representation (eg "7F")
     * 		If it is smaller than the length of the data hexadecimal string in data, a subset of the data corresponding to the Lc will be used
     * 		If it is larger,  Lc shall be readjusted
     * @param pLe - Expected length in hexadecimal representation (eg "7F")
     * @param pCmdData - The Command Data in hexadecimal string
     */
    public APDU(String pName, String pCLA, String pINS, String pP1, String pP2, String pLc, String pCmdData, String pLe){
    	name=pName;
    	cla=((pCLA==null)?"":pCLA.replace(" ", ""));
    	ins=((pINS==null)?"":pINS.replace(" ", ""));
    	p1=((pP1==null)?"":pP1.replace(" ", ""));
    	p2=((pP2==null)?"":pP2.replace(" ", ""));
    	if (pCmdData==null || pCmdData.isEmpty()){
    		lc="";
    		cmdData = "";
    	}
    	else{
	    	byte[] data = Utilities.hexStringToByteArray(pCmdData);
	    	int len =  ((pLc.isEmpty())?0xFF:(Utilities.hexStringToByte(pLc) & 0x000000FF));
	    	if (len>data.length){
	    		len = data.length;
	    	}
	    	lc= Utilities.byteToHexString((byte)(len&0x000000FF));
	    	cmdData = Utilities.byteArrayToHexString(data, 0, len, false);
    	}
    	le=((pLe==null)?"":pLe.replace(" ", ""));
    	respData="";
    	sw1="";
    	sw2="";
    }
    
    public void setName(String value) { name = value; }
	public String getName(){ return name; }
	
    public void setCLA(String value) { cla = value.replace(" ", ""); }
	public String getCLA(){ return cla; }
	
	public void setINS(String value) { ins = value.replace(" ", ""); }
	public String getINS(){ return ins; }
	
	public void setP1(String value) { p1 = value.replace(" ", ""); }
	public String getP1(){ return p1; }
	
	public void setP2(String value) { p2 = value.replace(" ", ""); }
	public String getP2(){ return p2; }

	public void setLc(String value) { lc = value.replace(" ", ""); }
	public String getLc(){ return lc; }
	
	public void setCommandData(String value) { cmdData = value.replace(" ", ""); }
	public String getCommandData(){ return cmdData; }
	
	public void setLe(String value) { le = value.replace(" ", ""); }
	public String getLe(){ return le; }

	public void setResponseData(String value) { respData = value.replace(" ", ""); }
	public String getResponseData(){ return respData; }
	
	public void setSW1(String value) { sw1 = value.replace(" ", ""); }
	public String getSW1(){ return sw1; }
	
	public void setSW2(String value) { sw2 = value.replace(" ", ""); }
	public String getSW2(){ return sw2; }
	
	private short toStatusWord(){
		return Utilities.makeShort(Utilities.hexStringToByte(sw1), Utilities.hexStringToByte(sw2));
	}
	
    public CommandAPDU toCommandAPDU(){
    	
    	if (cmdData.isEmpty()){
    		
    		
    		if (le.isEmpty()){
    			//Case 1
    			return new CommandAPDU(Utilities.hexStringToInt(cla), Utilities.hexStringToInt(ins), Utilities.hexStringToInt(p1), Utilities.hexStringToInt(p2));
    		}
    		else{
    			//Case 2
    			return new CommandAPDU(Utilities.hexStringToInt(cla), Utilities.hexStringToInt(ins), Utilities.hexStringToInt(p1), Utilities.hexStringToInt(p2), (int)Utilities.hexStringToInt(le));
    		}
    		
    	}
    	else {
    		byte[] data = Utilities.hexStringToByteArray(cmdData);
    		if (le.isEmpty()){
    			//Case 3
    			return new CommandAPDU(Utilities.hexStringToInt(cla), Utilities.hexStringToInt(ins), Utilities.hexStringToInt(p1), Utilities.hexStringToInt(p2), data, 0, data.length );
    		}
    		else{
    			//Case 4
    			return new CommandAPDU(Utilities.hexStringToInt(cla), Utilities.hexStringToInt(ins), Utilities.hexStringToInt(p1), Utilities.hexStringToInt(p2), data, 0, data.length,(int)Utilities.hexStringToInt(le));
    		}
    		
			
		}

    }
    
    public static APDU fromCommandAPDU(CommandAPDU apdu){
    	if (apdu==null){
    		return null;
    	}
    	
    	APDU retVal = new APDU();
    	retVal.parse( Utilities.byteArrayToHexString( apdu.getBytes()));
    	return retVal;
    }
    
    public void parseResponse(ResponseAPDU resp){
    	sw1 = Utilities.byteToHexString((byte)(resp.getSW1() & 0x000000FF));
    	sw2 = Utilities.byteToHexString((byte)(resp.getSW2() & 0x000000FF));
    	byte[] data = resp.getData();
    	if (data !=null && data.length>0){
    		respData = Utilities.byteArrayToHexString(data,0,data.length,false);
    	}
    	else{
    		respData="";
    	}
    }

    public void parseResponse(byte[] resp, int offset, int length){
    	if (length>=2){
    		sw1 = Utilities.byteToHexString(resp[offset+length-2]);
    		sw2 = Utilities.byteToHexString(resp[offset+length-1]);
    		if (length>2){
    			respData=Utilities.byteArrayToHexString(resp, offset, length-2, false);
    		}
    		else{
    			respData="";
    		}
    	}
    	else{
    		sw1 = "";
    		sw2 = "";
	    	respData="";
	    }
    }
    
    @Override
    public String toString(){
    	
    	return name;
    }
    
    public void parse(String str){
    	
    	//remove unnecessary characters
    	//remove spaces
    	str = str.replace(" ","");
    	//remove returns
    	str = str.replace("\r","");
    	//remove newline
    	str = str.replace("\n","");
    	
    	int responseStart = str.indexOf('[');
    	int responseEnd = str.indexOf(']');
    	int swStart = str.indexOf('(');
    	int swEnd = str.indexOf(')');
    	
    	String command = str.substring(0, ((responseStart<0)?((swStart<0)?str.length():swStart) :responseStart) );
    	String response = (responseStart<0)? "":(responseEnd<0)? ((swStart<0)?str.substring(responseStart+1):str.substring(responseStart+1,swStart)):str.substring(responseStart+1,responseEnd);
    	String sw = (swStart<0)? "": ((swEnd<0)? str.substring(swStart+1):str.substring(swStart+1,swEnd));
    	
    	try{
    		//if (Utilities.isPairedHexadecimalString(command)){
    			name = "";
		    	cla = command.substring(0, 2);
		    	ins = command.substring(2, 4);
		    	p1 = command.substring(4, 6);
		    	p2 = command.substring(6, 8);
		    	String p3 = command.substring(8, 10);
		    	cmdData = ((command.length()>10)?command.substring(10):"");
		    	if (p3.equals("00")){
		    		lc = p3;
		    		le = "";
		    		cmdData = "";
		    	}
		    	else
		    	{
			    	if (cmdData.length()>0){
			    		lc = p3;
			    		le = "";
			    	}
			    	else{
			    		le = p3;
			    		lc = "";
			    	}
		    	}
		    	
    		//}
    	}
    	catch (Exception Ex){
    		
    	}
    	
    	try{
    		//if (Utilities.isPairedHexadecimalString(response)){
		    	respData = response.toString();
    		//}
    	}
    	catch (Exception Ex){
    		
    	}
    	
    	try{
    		//if (Utilities.isPairedHexadecimalString(sw)){
    			sw1 = sw.substring(0, 2);
    			sw2 = sw.substring(2);
    		//}
    	}
    	catch (Exception Ex){
    		
    	}
    	
    }
    
    public String toAPDUString(){
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(cla+" ");
    	sb.append(ins+" ");
    	sb.append(p1+" ");
    	sb.append(p2+" ");
    	if (!lc.isEmpty()){
    		sb.append(lc+" ");
    		sb.append(Utilities.spaceHexString(cmdData));
    	} 
    	else if (!le.isEmpty()){
    		sb.append(le+" ");
    	}
    	sb.append("\\\r\n");
    	
    	if (!respData.isEmpty()){
    		sb.append("[");
    		sb.append(Utilities.spaceHexString(respData));
    		sb.append("] ");
    	}
    	sb.append("("+sw1+" "+sw2+")");

    	return sb.toString();
    }
    
    public byte[] toCommandBytes(){
    	
    	StringBuilder sb = new StringBuilder();
    	sb.append(cla);
    	sb.append(ins);
    	sb.append(p1);
    	sb.append(p2);
    	if (!lc.isEmpty()){
    		sb.append(lc);
    		sb.append(cmdData.replace(" ", ""));
    	} 
    	else if (!le.isEmpty()){
    		sb.append(le);
    	}
    	
    	return Utilities.hexStringToByteArray(sb.toString());
    }

    public boolean isSuccess(){
    	switch (Utilities.hexStringToByte(sw1)){
    		case (byte)0x90:
    		case (byte)0x91:
    		
    		case (byte)0x9F:
    		case (byte)0x61:
    			return true;
    		case (byte)0x92:
    			byte s2 = Utilities.hexStringToByte(sw2);
    			if (s2>=(byte)0 && s2<=(byte)0x0F) return true;
    			else return false;
    		default:
    			return false;
    	}
    }
    
    public String verboseCommand(){
    	StringBuilder retval = new StringBuilder();
    	byte cl = Utilities.hexStringToByte(cla);
    	
    	if (cl==(byte)0xA0 || (cl>=(byte)0x00 && cl<=(byte)0x0F) || (cl>=(byte)0x80 && cl<=(byte)0x8F))
    	{
    		switch (Utilities.hexStringToByte(ins)){	
		    	case (byte)0xA4:
		    		retval.append("SELECT");
		    		if (Utilities.hexStringToByte(p1)==(byte)4)retval.append(" AID");
		    		break;
		    	case (byte)0xC0:
		    		retval.append("GET RESPONSE");
		    		break;
		    	case (byte)0xF2:
		    		retval.append("STATUS");
		    		break;
		    	case (byte)0x20:
		    		retval.append("VERIFY PIN");
		    		break;
		    	case (byte)0x2C:
		    		retval.append("UNBLOCK PIN");
		    		break;
		    	case (byte)0xB0:
		    		retval.append("READ BINARY");
		    		break;
		    	case (byte)0xB2:
		    		retval.append("READ RECORD");
		    		break;
		    	case (byte)0xD6:
		    		retval.append("UPDATE BINARY");
		    		break;
		    	case (byte)0xDC:
		    		retval.append("UPDATE RECORD");
		    		break;
		    	case (byte)0x10:
		    		retval.append("TERMINAL PROFILE");
		    		break;
		    	case (byte)0xC2:
		    		retval.append("ENVELOPE");
		    		break;
		    	case (byte)0x12:
		    		retval.append("FETCH");
		    		break;
		    	case (byte)0x14:
		    		retval.append("TERMINAL RESPONSE");
		    		break;
		    	case (byte)0x88:
		    		retval.append("AUTHENTICATE");
		    		break;
		    	default:
	    			retval.append("UNKNOWN INSTRUCTION");
		    		break;
		    			
	    	}
    	}
    	else{
    		retval.append("UNKNOWN CLASS OF INSTRUCTION");
    	}
    	return retval.toString();
    }
    
    public String verboseResponse(){
    	StringBuilder retval = new StringBuilder();
    	byte s2 = Utilities.hexStringToByte(sw2);
    	switch (Utilities.hexStringToByte(sw1)){
    		//ETSI 11.11 and 51.11 and ISO 7816-4
    		//Responses to commands which are correctly executed
	    	case (byte)0x90:
	    		retval.append("Normal ending of the command.");
	    		break;
			case (byte)0x91:
				retval.append("Normal ending of the command with proactive command of 0x"+sw2+" bytes waiting.");
    			break;
			case (byte)0x9E:
				retval.append("Data download error with response of 0x"+sw2+" bytes waiting.");
				break;
			//Responses to commands which are postponed
			case (byte)0x93:
				retval.append("SIM Application Toolkit is busy");
				break;
			case (byte)0x9F:
				retval.append("Normal ending of the command with response 0x"+sw2+" bytes waiting.");
				break;
			//Memory management
			case (byte)0x92:
    			if (s2>=(byte)0 && s2<=(byte)0x0F) {
    				retval.append("Command successful but after using an internal update retry routine 0x"+sw2+" times.");
    			}
    			else if (s2==(byte)0x40){
    				retval.append("Memory problem.");
    			}
    			else{
    				retval.append("Unknown Memory management status words!");
    			}
			//Referencing management
			case (byte)0x94:
				switch (s2){
					case (byte)0x00:
						retval.append("No EF Selected!");
						break;
					case (byte)0x02:
						retval.append("No EF Selected!");
						break;
					case (byte)0x04:
						retval.append("No EF Selected!");
						break;
					case (byte)0x08:
						retval.append("No EF Selected!");
						break;
					default:
						retval.append("Unknown Referencing management status words!");
						break;
				}
				break;
			//Security management
			case (byte)0x98:
				switch (s2){
					case (byte)0x02:
						retval.append("No CHV initialized!");
						break;
					case (byte)0x04:
						retval.append("Access condition not fulfilled - At least 1 attempt left!");
						break;
					case (byte)0x08:
						retval.append("In contradiction with CHV status!");
						break;
					case (byte)0x10:
						retval.append("In contradiction with invalidation status!");
						break;
					case (byte)0x40:
						retval.append("Unsuccessful CHV verification, no attempt left!");
						break;
					case (byte)0x50:
						retval.append("Increase cannot be performed, Max value reached!");
						break;
					default:
						retval.append("Unknown Security management status words!");
						break;
				}
				break;

			//ISO 7816-4	
			case (byte)0x61:
				retval.append("Normal ending of the command with response 0x"+sw2+" bytes waiting.");
				break;
			case (byte)0x62:
				retval.append("State of non-volatile memory unchanged");
				switch (s2){
					case (byte)0x00:
						retval.append(" - no additional informatin given!");
					break;
					case (byte)0x81:
						retval.append(" - part of returned data my be corrupted!");
						break;
					case (byte)0x82:
						retval.append(" - end of file/record reached before reading Le bytes!");
						break;
					case (byte)0x83:
						retval.append(" - Selected file invalidated!");
						break;
					case (byte)0x84:
						retval.append(" - FCI not formated according to ISO 7816-4!");
						break;
					default:
						retval.append("!");
						break;
				}
				break;
			case (byte)0x63:
				retval.append("State of non-volatile memory changed");
				switch (s2){
					case (byte)0x00:
						retval.append(" - no additional informatin given!");
					break;
					case (byte)0x81:
						retval.append(" - file filled up by the last write!");
						break;
					default:
						retval.append("!");
						break;
				}
				break;
			case (byte)0x64:
				retval.append("State of non-volatile memory unchanged");
				break;
			case (byte)0x65:
				retval.append("State of non-volatile memory changed");
				switch (s2){
					case (byte)0x00:
						retval.append(" - no additional information given!");
					break;
					case (byte)0x81:
						retval.append(" - Memory failure!");
						break;
					default:
						retval.append("!");
						break;
				}
				break;
			case (byte)0x66:
				retval.append("Security related issue!");
				break;
			case (byte)0x67:
				retval.append("Wronng length!");
				break;
			case (byte)0x68:
				retval.append("Functions in CLA not supported");
				switch (s2){
					case (byte)0x00:
						retval.append(" - no additional information given!");
					break;
					case (byte)0x81:
						retval.append(" - Logical channel not supported!");
						break;
					case (byte)0x82:
						retval.append(" - Secure messaging not supported!");
						break;
					default:
						retval.append("!");
						break;
				}
				break;
			case (byte)0x69:
				retval.append("Command not allowed");
				switch (s2){
					case (byte)0x00:
						retval.append(" - no additional information given!");
					break;
					case (byte)0x81:
						retval.append(" - Command incompatible with file structure!");
						break;
					case (byte)0x82:
						retval.append(" - Security status not satisfied!");
						break;
					case (byte)0x83:
						retval.append(" - Authentication method blocked!");
						break;
					case (byte)0x84:
						retval.append(" - Referenced data invalidated!");
						break;
					case (byte)0x85:
						retval.append(" - Cons!");
						break;
					case (byte)0x86:
						retval.append(" - Command not allowed (no current EF)!");
						break;
					case (byte)0x87:
						retval.append(" - Expected SM data objects missing!");
						break;
					case (byte)0x88:
						retval.append(" - SM data objects incorrect!");
						break;
					default:
						retval.append("!");
						break;
				}
				break;
			case (byte)0x6A:
				retval.append("Wrong parameter(s) P1-P2");
				switch (s2){
					case (byte)0x00:
						retval.append(" - no additional information given!");
					break;
					case (byte)0x80:
						retval.append(" - Incorrect parameters in the data field!");
						break;
					case (byte)0x81:
						retval.append(" - Function not supported!");
						break;
					case (byte)0x82:
						retval.append(" - File not found!");
						break;
					case (byte)0x83:
						retval.append(" - Record not found!");
						break;
					case (byte)0x84:
						retval.append(" - Not enough memory space in the file!");
						break;
					case (byte)0x85:
						retval.append(" - Lc inconsistent with TLV structure!");
						break;
					case (byte)0x86:
						retval.append(" - Incorrect parameters P1-P2!");
						break;
					case (byte)0x87:
						retval.append(" - Lc inconsistent with P1-P2!");
						break;
					case (byte)0x88:
						retval.append(" - Referenced data not found!");
						break;
					default:
						retval.append("!");
						break;
				}
				break;
			case (byte)0x6B:
				retval.append("Incorrect P1 or P2!");
				break;
			case (byte)0x6D:
				retval.append("Unknown instruction code given in the command!");
				break;
			case (byte)0x6E:
				retval.append("Wrong instruction class given in the command!");
				break;
			case (byte)0x6F:
				retval.append("Technical problem with no diagnostic given!");
				break;
			default:
				retval.append("Unknown status words!");
				break;
    	}
    	return retval.toString();
    }
}
