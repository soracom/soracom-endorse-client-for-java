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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.soracom.endorse.SORACOMEndorseClientConfig.CommunicationDeviceConfig;
import io.soracom.endorse.common.TextLog;
import io.soracom.endorse.interfaces.UiccInterfaceType;
import io.soracom.endorse.keycache.AuthResult;
import io.soracom.endorse.keycache.KeyCache;
import io.soracom.endorse.utils.Utilities;

/**
 * Command line interface for SORACOM Endorse
 * 
 * @author c9katayama
 *
 */
public class SORACOMEndorseCLI {

	private static void displayHelp(){
		
		StringBuilder helpText = new StringBuilder();
		helpText.append("Soracom Endorse Client for Java\r\n");

		helpText.append("\r\n");
		helpText.append("USAGE: \r\n");
		helpText.append("soracom-endorse [--help | --listComPorts | --deviceInfo] [-i interface]  \r\n");
		helpText.append("The other parameters depend on command issued \r\n");
		helpText.append("\r\n");
		helpText.append("EXAMPLES: \r\n");
		helpText.append("To execute SIM(AKA) authentication using a card reader: \r\n");
		helpText.append("	soracom-endorse -i iso7816 \r\n");

		helpText.append("To execute SIM(AKA) authentication using modem:\r\n");
		helpText.append("	soracom-endorse -i comm -c /dev/tty1 \r\n");
		
		helpText.append("To use modem manager on some linux distros:\r\n");
		helpText.append("	soracom-endorse -i mmcli -m 0 \r\n");
		
		helpText.append("\r\n");
		helpText.append("  -au		   		Override the default authentication URL API with this switch\r\n");
		helpText.append("  				   	Eg: -au=https://keyurl.soracom.io/keyservice/\r\n");
		helpText.append("  -i			   	UICC Interface to use. Valid values are iso7816, comm, mmcli or autoDetect \r\n");
		helpText.append("  -c			   	Port name of communication device (eg -c COM1 or -c /dev/tty1)\r\n");
		helpText.append("  -b			   	Baud rate for communication device (eg -b 57600)\r\n");
		helpText.append("  -d			   	Data bits for communication device (eg -d 8)\r\n");
		helpText.append("  -s			   	Stop bits for communication device (eg -s 1)\r\n");
		helpText.append("  -p			   	Parity bits for communication device (eg -p 0)\r\n");
		helpText.append("  -m              	Modem manager index if mmcli flag is set (eg -m 0)\r\n");
		helpText.append("  --listComPorts  	List All available Communication devices and exit\r\n");
		helpText.append("  --deviceInfo    	Query the Communication device and print the information\r\n");
		helpText.append("  --disableKeyCache		Disable key cache.If you want to set a encryption key of the keystore, please set a value as environment variable " + KeyCache.ENV_NAME_ENDORSE_KEY_STORE_KEY+"\r\n");
		helpText.append("  --clearKeyCache	Clear key cache\r\n");
		helpText.append("  --debug		   	Set debug mode on\r\n");
		helpText.append("  --version		Show version\r\n");
		helpText.append("  --help          	Display this help message and stop\r\n");
		helpText.append("\r\n");
		stdout(helpText.toString());
	}
	
	private static void displayVersion(){
		List<String> lines = Utilities.readResource("/soracom-endorse-version");
		for(String line:lines) {
			stdout(line);
		}
	}
	
	private static void stdout(String message) {
		System.out.println(message);
	}
	
	public static void main(String[] args) {
		//COLLECT ALL COMMAND LINE ARGUMENTS
		List<String> argsList = new ArrayList<String>(); // List of Command line Arguments
	    HashMap<String, String> optsList = new HashMap<String, String>(); // List of Options
	    List<String> doubleOptsList = new ArrayList<String>();
	
	    for (int i = 0; i < args.length; i++) {
	        switch (args[i].charAt(0)) {
	        case '-':
	            if (args[i].length() < 2)
	                throw new IllegalArgumentException("Not a valid argument: "+args[i]);
	            if (args[i].charAt(1) == '-') {
	                if (args[i].length() < 3)
	                    throw new IllegalArgumentException("Not a valid argument: "+args[i]);
	                // --opt
	                doubleOptsList.add(args[i].substring(2, args[i].length()));
	            } else {
	                if (args.length-1 == i)
	                    throw new IllegalArgumentException("Expected arg after: "+args[i]);
	                // -opt
	
	                optsList.put(args[i], args[i+1]);
	                i++;
	            }
	            break;
	        default:
	            // arg
	            argsList.add(args[i]);
	            break;
	        }
	    }	    
	          
	    for (String opt:doubleOptsList){
        	if (opt.equals("help")){
             	displayHelp();
             	return;
            }
        }
	    
	    for (String opt:doubleOptsList){
        	if (opt.equals("version")){
             	displayVersion();
             	return;
            }
        }
        
        //START COMM DEVICE
       SORACOMEndorseClientConfig clientConfig = new SORACOMEndorseClientConfig();
       try {
    	    if  (optsList.get("-i")!=null){ 
    	    	clientConfig.setUiccInterfaceType(UiccInterfaceType.valueOf(optsList.get("-i")));
    	    }
    	    if  (optsList.get("-au")!=null){   
    	    	clientConfig.setApiEndpointUrl(optsList.get("-au")) ;
      	    }
    	    if  (optsList.get("-kl")!=null){   
    	    	clientConfig.setKeyLength(Integer.parseInt(optsList.get("-kl"))) ;
      	    }
    	    if  (optsList.get("-ka")!=null){  
    	    	clientConfig.setKeyAlgorithm(optsList.get("-ka")) ;
      	    }
    	    CommunicationDeviceConfig communicationDeviceConfig = new CommunicationDeviceConfig();
			if  (optsList.get("-c")!=null){
				communicationDeviceConfig.setPortName( optsList.get("-c") );
			}
			if (optsList.get("-b")!=null){
				communicationDeviceConfig.setBaudRate( Integer.parseInt(optsList.get("-b")) );
			}
			if (optsList.get("-d")!=null){
				communicationDeviceConfig.setDataBits(  Integer.parseInt(optsList.get("-d")) );
			}
			if (optsList.get("-s")!=null){
				communicationDeviceConfig.setStopBits( Integer.parseInt(optsList.get("-s")) );
			}
			if (optsList.get("-p")!=null){
				communicationDeviceConfig.setParity( Integer.parseInt(optsList.get("-p")) );
			}
			if (optsList.get("-m")!=null){
				int modemIntex =  Integer.parseInt(optsList.get("-m"));
				communicationDeviceConfig.setModemIndex( Integer.toString(modemIntex));
			}
			clientConfig.setCommunicationDeviceConfig(communicationDeviceConfig);
			
    		if (doubleOptsList.contains("clearCache")||doubleOptsList.contains("clearKeyCache")){
    			clientConfig.setClearKeyCache(true);
    		}
    		if (doubleOptsList.contains("disableKeyCache")){
    			clientConfig.setDisableKeyCache(true);
    		}
       }
	   catch (Exception ex){
		   TextLog.error("Illegal argument: "+ex.getMessage());
		   System.exit(-1);
	   }
		if (doubleOptsList.contains("debug")){
			clientConfig.setDebug(true);
		}
       try{
    	   SORACOMEndorseClient client= new SORACOMEndorseClient(clientConfig);
    	   
	        if (doubleOptsList.contains("listComPorts")){
        		List<String> ports = client.listComPorts();
	        	if (ports.size() == 0){
	        		stdout("No serial ports detected!");
	        	}
	        	for (String port:ports){
	        		stdout(port);
	        	}

	        }else if (doubleOptsList.contains("deviceInfo")){
	    		stdout(client.getDeviceInfo());
	        }
	    	else {
				AuthResult authResult = client.doAuthentication();
				stdout(Utilities.toJson(authResult));
	    	}
	        System.exit(0);
       }
	   catch (Exception ex){
		   TextLog.error(ex.getMessage());
		   System.exit(-1);
	   }
	}
}
