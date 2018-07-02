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

import java.util.Comparator;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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

	public static class EndorseCLIOptions{
		public static final Option helpOption = Option.builder("h").longOpt("help").desc("Display this help message and stop").build();
		
		public static final Option keysEndpointUrlOption = Option.builder("ku").longOpt("keys-api-endpoint").hasArg(true).desc("Override the default Keys API endpoint url with this switch.\n(eg -au=https://g.api.soracom.io)").build();
		public static final Option interfaceOption = Option.builder("if").longOpt("interface").hasArg(true).desc("UICC Interface to use. Valid values are iso7816, comm, mmcli or autoDetect. autoDetect is used as default.").build();
		public static final Option portNameOption = Option.builder("pn").longOpt("port-name").hasArg(true).desc("Port name of communication device.(eg -pn COM1 or -pn /dev/tty1)").build();
		public static final Option baudRateOption = Option.builder("br").longOpt("baud-rate").hasArg(true).desc("Baud rate for communication device.(eg -br 57600)").build();
		public static final Option dataBitOption = Option.builder("db").longOpt("data-bit").hasArg(true).desc("Data bits for communication device.(eg -db 8)").build();
		public static final Option stopBitOption = Option.builder("sb").longOpt("stop-bit").hasArg(true).desc("Stop bits for communication device.(eg -sb 1)").build();
		public static final Option parityBitOption = Option.builder("pb").longOpt("parity-bit").hasArg(true).desc("Parity bits for communication device.(eg -pb 0)").build();
		public static final Option modemManagerIndexOption = Option.builder("mi").longOpt("model-manager-index").hasArg(true).desc("Modem manager index if mmcli flag is set.(eg -mi 0)").build();
		
		public static final Option listComPortsOption = Option.builder().longOpt("listComPorts").desc("List All available Communication devices and exit").build();
		public static final Option deviceInfoOption = Option.builder().longOpt("deviceInfo").desc("Query the Communication device and print the information").build();
		
		public static final Option disableKeyCacheOption = Option.builder().longOpt("disableKeyCache").desc("Disable key cache.\nIf you want to set a encryption key of the keystore, please set a value as environment variable.\n" + KeyCache.ENV_NAME_ENDORSE_KEY_STORE_KEY +"=xxx").build();
		public static final Option clearKeyCacheOption = Option.builder().longOpt("clearKeyCache").desc("Clear key cache").build();
		public static final Option debugOption = Option.builder().longOpt("debug").desc("Set debug mode on").build();
		public static final Option versionOption = Option.builder().longOpt("version").desc("Display version").build();
	}
	
	private static Options initOptions() {
		final Options options = new Options();
		options.addOption(EndorseCLIOptions.helpOption);
		options.addOption(EndorseCLIOptions.keysEndpointUrlOption);
		options.addOption(EndorseCLIOptions.interfaceOption);
		options.addOption(EndorseCLIOptions.portNameOption);
		options.addOption(EndorseCLIOptions.baudRateOption);
		options.addOption(EndorseCLIOptions.dataBitOption);
		options.addOption(EndorseCLIOptions.stopBitOption);
		options.addOption(EndorseCLIOptions.parityBitOption);
		options.addOption(EndorseCLIOptions.modemManagerIndexOption);
		
		options.addOption(EndorseCLIOptions.listComPortsOption);
		options.addOption(EndorseCLIOptions.deviceInfoOption);
		
		options.addOption(EndorseCLIOptions.disableKeyCacheOption);
		options.addOption(EndorseCLIOptions.clearKeyCacheOption);
		options.addOption(EndorseCLIOptions.debugOption);
		options.addOption(EndorseCLIOptions.versionOption);	
		return options;
	}

	private static void displayHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.setOptionComparator(new Comparator<Option>() {
			public int compare(Option o1, Option o2) {
				return 0;
			}
		});
		formatter.setWidth(200);
		StringBuilder helpText = new StringBuilder();
		helpText.append("\r\n");
		helpText.append("The other parameters depend on command issued. \r\n");
		helpText.append("\r\n");
		helpText.append("To execute SIM(AKA) authentication using a card reader: \r\n");
		helpText.append("soracom-endorse -if iso7816 \r\n");
		helpText.append("\r\n");
		helpText.append("To execute SIM(AKA) authentication using modem:\r\n");
		helpText.append("soracom-endorse -if comm -pn /dev/tty1 \r\n");
		helpText.append("\r\n");
		helpText.append("To use modem manager on some linux distros:\r\n");
		helpText.append("soracom-endorse -if mmcli -mi 0 \r\n");
		helpText.append("\r\n");
		
		formatter.printHelp( "soracom-endorse [--help | --listComPorts | --deviceInfo] [-if interface] [--debug]","", options,helpText.toString() );
	}
	
	private static void displayVersion(){
		List<String> lines = Utilities.readResource("/soracom-endorse-version");
		for(String line:lines) {
			System.out.println(line);
		}
	}
	
	private static void stdout(String message) {
		System.out.println(message);
	}
	
	public static SORACOMEndorseClientConfig createSORACOMEndorseClientConfig(CommandLine commandLine) {
	    SORACOMEndorseClientConfig clientConfig = new SORACOMEndorseClientConfig();
	    if(commandLine.hasOption(EndorseCLIOptions.keysEndpointUrlOption.getLongOpt())) {
	    	clientConfig.setApiEndpointUrl(commandLine.getOptionValue(EndorseCLIOptions.keysEndpointUrlOption.getLongOpt()));
	    }
	    if(commandLine.hasOption(EndorseCLIOptions.interfaceOption.getLongOpt())) {
	    	clientConfig.setUiccInterfaceType(UiccInterfaceType.valueOf(commandLine.getOptionValue(EndorseCLIOptions.interfaceOption.getLongOpt())));
	    }
	    if(commandLine.hasOption(EndorseCLIOptions.clearKeyCacheOption.getLongOpt())) {
			clientConfig.setClearKeyCache(true);
		}
	    if(commandLine.hasOption(EndorseCLIOptions.disableKeyCacheOption.getLongOpt())) {
			clientConfig.setDisableKeyCache(true);
		}
	    if(commandLine.hasOption(EndorseCLIOptions.debugOption.getLongOpt())) {
			clientConfig.setDebug(true);
		}
	    CommunicationDeviceConfig communicationDeviceConfig = createCommunicationDeviceConfig(commandLine);
	    clientConfig.setCommunicationDeviceConfig(communicationDeviceConfig);
	    
	    return clientConfig;		
	}
	
	public static CommunicationDeviceConfig createCommunicationDeviceConfig(CommandLine commandLine) {
	    CommunicationDeviceConfig communicationDeviceConfig = new CommunicationDeviceConfig();
		if  (commandLine.hasOption(EndorseCLIOptions.portNameOption.getLongOpt())){
			communicationDeviceConfig.setPortName( commandLine.getOptionValue(EndorseCLIOptions.portNameOption.getLongOpt()));
		}
		if  (commandLine.hasOption(EndorseCLIOptions.baudRateOption.getLongOpt())){
			communicationDeviceConfig.setBaudRate(Integer.parseInt(commandLine.getOptionValue(EndorseCLIOptions.baudRateOption.getLongOpt())));
		}
		if  (commandLine.hasOption(EndorseCLIOptions.dataBitOption.getLongOpt())){
			communicationDeviceConfig.setDataBits( Integer.parseInt(commandLine.getOptionValue(EndorseCLIOptions.dataBitOption.getLongOpt())));
		}
		if  (commandLine.hasOption(EndorseCLIOptions.stopBitOption.getLongOpt())){
			communicationDeviceConfig.setStopBits(  Integer.parseInt(commandLine.getOptionValue(EndorseCLIOptions.stopBitOption.getLongOpt())));
		}
		if  (commandLine.hasOption(EndorseCLIOptions.parityBitOption.getLongOpt())){
			communicationDeviceConfig.setParity( Integer.parseInt(commandLine.getOptionValue(EndorseCLIOptions.parityBitOption.getLongOpt())));
		}
		if  (commandLine.hasOption(EndorseCLIOptions.modemManagerIndexOption.getLongOpt())){
			int modemIntex =  Integer.parseInt(commandLine.getOptionValue(EndorseCLIOptions.modemManagerIndexOption.getLongOpt()));
			communicationDeviceConfig.setModemIndex( Integer.toString(modemIntex));
		}
		return communicationDeviceConfig;
	}
	
	public static void main(String[] args) {
		Options options = initOptions();
		
		CommandLine line = null;
	    try {
	    	CommandLineParser parser = new DefaultParser();
	        line = parser.parse( options, args, true);
	    }catch( ParseException exp ) {
	    	displayHelp(options);
	    	System.exit(-1);
	    }
	    if(line.hasOption(EndorseCLIOptions.helpOption.getLongOpt())) {
	    	displayHelp(options);
	    	System.exit(0);
	    }
	    if(line.hasOption(EndorseCLIOptions.versionOption.getLongOpt())) {
	    	displayVersion();
	    	System.exit(0);
	    }

	    SORACOMEndorseClientConfig clientConfig = createSORACOMEndorseClientConfig(line);
	    SORACOMEndorseClient client= new SORACOMEndorseClient(clientConfig);
	    
       try{
    	   if(line.hasOption(EndorseCLIOptions.listComPortsOption.getLongOpt())) {
        		List<String> ports = client.listComPorts();
	        	if (ports.size() == 0){
	        		stdout("No serial ports detected!");
	        	}
	        	for (String port:ports){
	        		stdout(port);
	        	}

	        }else if(line.hasOption(EndorseCLIOptions.deviceInfoOption.getLongOpt())) {
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
