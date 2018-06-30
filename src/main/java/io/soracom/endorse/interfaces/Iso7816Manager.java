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
package io.soracom.endorse.interfaces;

import java.util.Collections;
import java.util.List;

import javax.smartcardio.ATR;
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;

import io.soracom.endorse.common.APDU;
import io.soracom.endorse.common.DIR;
import io.soracom.endorse.common.FCP;
import io.soracom.endorse.common.IMSI;
import io.soracom.endorse.common.TextLog;
import io.soracom.endorse.utils.Utilities;


public class Iso7816Manager implements IUiccInterface {
	public static final int CLA_UICC = 0;
	public static final int INS_SELECT = 0x000000A4;
	public static final int INS_READ_BINARY = 0x000000B0;
	public static final int INS_READ_RECORD = 0x000000B2;
	public static final int INS_GET_RESPONSE = 0x000000C0;
	public static final int INS_AUTHENTICATE = 0x00000088;
	public static final byte[] FID_MF = new byte[] {(byte)0x3F, (byte)0x00};
	public static final byte[] FID_EF_DIR = new byte[] {(byte)0x2F, (byte)0x00};
	public static final byte[] FID_EF_IMSI = new byte[] {(byte)0x6F, (byte)0x07};
	private byte[] adfUsim;
	
	public enum CardProtocol{
		Auto,
		T0,
		T1,
		CL
	}
	
  	byte[] currentATR;
    CardTerminal reader;
    Card card;
    CardChannel chan;
    String lastError;
    
    
    public Iso7816Manager(){
    	setDefaultReader();
    }
    
    public Iso7816Manager(CardTerminal reader){
    	this.reader = reader;
    }

	public boolean connect(CardProtocol protocol){
    		    	
    	if (reader==null) {
    		lastError = "Reader not selected";
    		return false;
    	}
    	//By default smartcardio has some additional handling of APDU responses with SW=61 XX - 
    	//it automatically sends GET RESPONSE APDU request with LE ==XX.  The following line turns this off.
    	System.setProperty("sun.security.smartcardio.t0GetResponse", "false"); 
    	
    	try {
    		String strProtocol;
    		switch (protocol){
    			
    			case T0:
    				strProtocol = "T=0";
    				break;
    			case T1:
    				strProtocol = "T=1";
    				break;
    			case CL:
    				strProtocol = "T=CL";
    				break;
    				
    			default:
    				strProtocol = "*";
    				break;
    				
    		}
 	        card = reader.connect(strProtocol);
	        ATR atr = card.getATR();
	        
	        currentATR = atr.getBytes();
	        chan = card.getBasicChannel();
	        return true;
        
    	}
    	catch (Exception Ex){
    		lastError = Ex.getMessage();
    		return false;
    	}
        
            
    }
    
    public boolean isConnected(){
        return (card!=null);
    }
    
    public boolean disconnect() {

        if(card==null) return false;
        
        try{
        	card.disconnect(true);
        	card=null;
        	return true;
        }
        catch (CardException Ex){
    		lastError = Ex.getMessage();
    		return false;
    	}

    }
    
    
    public CardChannel getChannel(){
        return chan;
    }
     
    public void setDefaultReader() {

    	List<CardTerminal> readers = listReaders();
    	if(readers.size() == 0) {
    		new UiccInterfaceNotFoundException("Reader not found.");
    	}
		reader = readers.get(0);
    }
    
    
    CardTerminal objectToReader(Object iRef){
        return TerminalFactory.getDefault().terminals().getTerminal(iRef.toString());
    }
    
    @SuppressWarnings("unchecked")
	public static List<CardTerminal> listReaders() {
        try {
        	return TerminalFactory.getDefault().terminals().list();
        }catch (CardException Ex){
    		return Collections.EMPTY_LIST;
    	}
    }
    
    public String getCurrentCardATR(){
    	StringBuilder sb = new StringBuilder();
    	if (currentATR!=null){
			for (int i=0;i<currentATR.length;i++){
				sb.append(String.format("%02X",  currentATR[i]));
			}
    	}
    	return sb.toString();
    }
    
    public String getLastError(){
    	return lastError;
    }
    
    public ResponseAPDU sendAPDU(CommandAPDU cmdApdu){
		if (isConnected()){
		
			try{
				ResponseAPDU res = getChannel().transmit(cmdApdu);
				
				APDU apdu = APDU.fromCommandAPDU(cmdApdu);
				apdu.parseResponse(res);
				TextLog.debug("\nREM "+apdu.verboseCommand());
				TextLog.debug("\nCMD "+apdu.toAPDUString());
				
				return res;
				
			}
			catch (CardException Ex)
			{
				lastError = "Error sending APDU: " + Ex.getMessage();
			}

		}
		else{
			lastError = "Error sending APDU: Smatcard not connected!" ;
		}
		return null;
	}
    
    public boolean waitForCardPresent (long timeout) throws CardException{
    	
    	if(reader!=null){
    		return reader. waitForCardPresent(timeout);
    	}
    	else
    	{
    		throw new CardException("No reader selected");
    	}
    	
    }
    
    public boolean waitForCardAbsent (long timeout) throws CardException{
    	
    	if(reader!=null){
    		return reader.waitForCardAbsent(timeout);
    	}
    	else
    	{
    		throw new CardException("No reader selected");
    	}
    	
    }
    
    private boolean isSuccessfulSW(int sw1, int sw2){
    	switch (sw1){
		case 0x00000090:
		case 0x00000061:
		case 0x0000006F:
			return true;
    	}
    	return false;
    }
    private boolean findADF(){
    	if (isConnected()){
    		ResponseAPDU response = sendAPDU(new CommandAPDU (CLA_UICC, INS_SELECT, 0,4, FID_MF));
    		if (isSuccessfulSW(response.getSW1(),response.getSW2())){
    			response = sendAPDU(new CommandAPDU (CLA_UICC, INS_SELECT, 0,4, FID_EF_DIR));
    			if (response.getSW1()==0x00000061){
    				response = sendAPDU(new CommandAPDU (CLA_UICC, INS_GET_RESPONSE, 0,0,response.getSW2()));
    				if (isSuccessfulSW(response.getSW1(),response.getSW2())){
    					FCP fcp = new FCP(response.getData());
    					int recSize = fcp.getRecordSize();
    					response = sendAPDU(new CommandAPDU (CLA_UICC, INS_READ_RECORD, 1,4,recSize));
        				if (isSuccessfulSW(response.getSW1(),response.getSW2())){
        					DIR record = new DIR(response.getData());
        					this.adfUsim = record.getAID();
        					return true;
        				}
    				}
    			}
    		}
    	}
    	return false;
    }
    
    /**
     * Read the imsi from the SIM
     */
    public String readImsi(){
    	byte[] imsi = null;
    	if (connect(CardProtocol.T0)){
    		if (adfUsim==null){
    			if (!findADF()){
    				return null;
    			}
    		}
    		ResponseAPDU response = sendAPDU(new CommandAPDU (CLA_UICC, INS_SELECT, 4,4, adfUsim));
    		if (isSuccessfulSW(response.getSW1(),response.getSW2())){
    			response = sendAPDU(new CommandAPDU (CLA_UICC, INS_SELECT, 0,4, FID_EF_IMSI));
    			if (isSuccessfulSW(response.getSW1(),response.getSW2())){

					response = sendAPDU(new CommandAPDU (CLA_UICC, INS_READ_BINARY, 0,0,9));
    				if (isSuccessfulSW(response.getSW1(),response.getSW2())){
    					imsi = response.getData();
    				}
				}
    		}
    	}
    	disconnect();
    	if (imsi!=null){
    		IMSI imsiObj = new IMSI();
    		imsiObj.setEncodedValue(Utilities.byteArrayToHexString(imsi));
    		return imsiObj.getValue();
    	}
    	else 
    	{
    		return null;
    	}
    }
    
    /**
     * Authenticate and return the response data
     * @param rand - The 16 byte challenge from network
     * @param autn - The AUTN parameter from network
     * @return - The Authentication response data
     *  if response data byte 0 == 0xDB  //Authentication Success 
     *  else if response data byte 0 = 0xDC //Synchronisation failure 
     */
    public byte[] authenticate(byte[] rand, byte[] autn){
    	byte[] resp = null;
    	if (rand ==null || autn==null){
    		return null;
    	}
    	if (connect(CardProtocol.T0)){
    		if (adfUsim==null){
    			if (!findADF()){
    				return null;
    			}
    		}
    		ResponseAPDU response = sendAPDU(new CommandAPDU (CLA_UICC, INS_SELECT, 4,4, adfUsim));
    		if (response.getSW1()==0x00000061){
				response = sendAPDU(new CommandAPDU (CLA_UICC, INS_GET_RESPONSE, 0,0,response.getSW2()));
    			if (isSuccessfulSW(response.getSW1(),response.getSW2())){
    				FCP fcp = new FCP(response.getData());
    		    	//TODO: Check PIN Status and present PIN code if necessary
    				byte[] commandData = new byte[rand.length + autn.length+2];  
    				int i=0;
    				commandData[i++] = (byte)(rand.length & 0x000000FF);
    				i = Utilities.arrayCopy(rand, 0, commandData, i, rand.length);
    				commandData[i++] = (byte)(autn.length & 0x000000FF);
    				i = Utilities.arrayCopy(autn, 0, commandData, i, autn.length);
					response = sendAPDU(new CommandAPDU (CLA_UICC, INS_AUTHENTICATE, 0,0x00000081,commandData));
					if ((response.getSW1()==0x00000061) ||(response.getSW1()==0x0000006E)){
						response = sendAPDU(new CommandAPDU (CLA_UICC, INS_GET_RESPONSE, 0,0,response.getSW2()));
		    			if (isSuccessfulSW(response.getSW1(),response.getSW2())){
		    				resp = response.getData();
		    			}
    				}
				}
    		}
    	}
    	disconnect();
    	return resp;
    	



    }
}