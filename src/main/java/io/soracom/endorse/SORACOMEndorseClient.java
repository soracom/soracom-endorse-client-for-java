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

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.soracom.endorse.SORACOMEndorseClientConfig.CommunicationDeviceConfig;
import io.soracom.endorse.beans.MilenageParamsBean;
import io.soracom.endorse.common.AuthenticationResponse;
import io.soracom.endorse.common.AuthenticationResponse.ResultState;
import io.soracom.endorse.common.ITextLogListener;
import io.soracom.endorse.common.KryptonClientRuntimeException;
import io.soracom.endorse.common.TextLog;
import io.soracom.endorse.common.TextLogItem;
import io.soracom.endorse.interfaces.AutoDetectManager;
import io.soracom.endorse.interfaces.CommManager;
import io.soracom.endorse.interfaces.IUiccInterface;
import io.soracom.endorse.interfaces.Iso7816Manager;
import io.soracom.endorse.interfaces.MmcliManager;
import io.soracom.endorse.interfaces.UiccInterfaceType;
import io.soracom.endorse.keycache.AuthResult;
import io.soracom.endorse.keycache.JCEKeyCache;
import io.soracom.endorse.keycache.KeyCache;
import io.soracom.endorse.keycache.NoOpKeyCache;
import io.soracom.endorse.utils.Utilities;

public class SORACOMEndorseClient {

	private KeyCache keyCache;
	
	private SORACOMEndorseClientConfig clientConfig;
	
	public SORACOMEndorseClient(SORACOMEndorseClientConfig endorseClientConfiig) {
		this(endorseClientConfiig,null);
	}
	public SORACOMEndorseClient(SORACOMEndorseClientConfig endorseClientConfiig, ITextLogListener logListener) {
		this.clientConfig = endorseClientConfiig;
		initLogger(logListener);
		initKeyCache();
	}
	
	private void initLogger(ITextLogListener logListener) {
		if(logListener == null) {
			logListener = new KryptonClientLogListener(clientConfig.isDebug() == false);
		}
		TextLog.clerListener();
		TextLog.addListener(logListener);
	}
	
	private void initKeyCache() {
		if(clientConfig.isDisableKeyCache()) {
			keyCache = new NoOpKeyCache();
		}else {
			keyCache = new JCEKeyCache(System.getProperty("user.home")+ File.separator + ".soracom-endorse-jce");
			if(clientConfig.isClearKeyCache()) {
				clearKeyCache();
			}
		}
	}
	
	public String calculateApplicationKey(byte[] nance,long timestamp,byte[] ck) {
		byte[] appKey;
		try {
			appKey = EndorseAPI.calculateApplicationKey(nance,timestamp, ck, clientConfig.getKeyLength(), clientConfig.getKeyAlgorithm());
		} catch (NoSuchAlgorithmException e) {
			throw new KryptonClientRuntimeException(e);
		}
		String appKeyString = Utilities.bytesToBase64(appKey);
		return appKeyString;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> listComPorts() throws KryptonClientRuntimeException{
    	String[] ports = CommManager.getAvailablePorts();
    	if (ports==null || ports.length==0){
    		return Collections.EMPTY_LIST;
    	}
    	return Arrays.asList(ports);
	}
	
	public String getDeviceInfo() throws KryptonClientRuntimeException{
		CommManager	commManager= createCommManager(clientConfig.getCommunicationDeviceConfig());
		return commManager.queryDevice();
	}
	
	public void clearKeyCache() {
		keyCache.clear();
		TextLog.log("key cache has been cleared.");
	}
	
	public AuthResult doAuthentication() {
		IUiccInterface uiccInterface = createUiccInterface(clientConfig);

		String imsi=uiccInterface.readImsi();
		if (imsi==null || imsi.isEmpty()){
			throw new KryptonClientRuntimeException("IMSI not retrieved! Halting key agreement negociation!");
		}
		TextLog.debug("imsi=\""+imsi+"\"");
		//Verify if cached key exist
		AuthResult authResult = keyCache.getAuthResultFromCache(imsi);
		if(authResult == null) {
			authResult = new AuthResult();
			authResult.setImsi(imsi);
		}

		if (authResult.getCk()==null) //Key cache did not return a key, proceed with authentication
		{
			//First step - Create master key
			MilenageParamsBean milenageParams = EndorseAPI.initKeyAgreement(clientConfig.getApiEndpointUrl()+"/v1/keys",imsi);
			if (milenageParams==null || milenageParams.getAutn()==null || milenageParams.getRand()==null){
				throw new KryptonClientRuntimeException("Error negotiating key agreement for imsi "+((imsi==null)?"":imsi.toString()));
			}
			authResult.setKeyId(milenageParams.getKeyId());
			byte[] rand = Utilities.base64toBytes(milenageParams.getRand());
			byte[] autn = Utilities.base64toBytes(milenageParams.getAutn());
			if (autn==null || rand==null){
				throw new KryptonClientRuntimeException("Bad parameters detected while negotiating key agreement!");
			}
			byte[] rsp = uiccInterface.authenticate(rand, autn);
			byte[] res =null;				
			if (rsp!=null){
				AuthenticationResponse authResponse = new AuthenticationResponse(rsp);
				switch (authResponse.getResultState())
				{
					case Success:
						res = authResponse.getRes();
						authResult.ckBytes(authResponse.getCk());
						if (authResult.getKeyId()==null){
							throw new KryptonClientRuntimeException("Key ID is null please try authentication one more time!");
						}
						if (EndorseAPI.verifyMasterKey(clientConfig.getApiEndpointUrl()+"/v1/keys/"+authResult.getKeyId()+"/verify",  Utilities.bytesToBase64(res))){
							keyCache.saveAuthResult(authResult);
						}
						else
						{
							throw new KryptonClientRuntimeException("Could not verify master key!");
						}
						 break;
					case SynchronisationFailure:
						byte[] auts = authResponse.getAuts();
						milenageParams = EndorseAPI.initKeyAgreement(clientConfig.getApiEndpointUrl()+"/v1/keys", imsi,milenageParams.getRand(),Utilities.bytesToBase64(auts));
						rand = Utilities.base64toBytes(milenageParams.getRand());
	        			autn = Utilities.base64toBytes(milenageParams.getAutn());
	        			authResult.setKeyId(milenageParams.getKeyId());
	        			rsp = uiccInterface.authenticate(rand, autn);
	        			TextLog.debug("rand=\""+milenageParams.getRand()+"\"");
	        			TextLog.debug("auts=\""+Utilities.bytesToBase64(auts)+"\"");

						if (rsp==null){
							throw new KryptonClientRuntimeException("Failure to authenticate during resynchronization procedure!");
						}
						else
						{
							authResponse = new AuthenticationResponse(rsp);
							if (authResponse.getResultState()==ResultState.Success){
								res = authResponse.getRes();
								authResult.ckBytes(authResponse.getCk());
								if (EndorseAPI.verifyMasterKey(clientConfig.getApiEndpointUrl()+"/v1/keys/"+authResult.getKeyId()+"/verify",  Utilities.bytesToBase64(res))){
									keyCache.saveAuthResult(authResult);
									//TextLog.debug("keyId=\""+authResult.keyId+"\"");
									//TextLog.debug("xres=\""+Utilities.bytesToBase64(res)+"\"");
									//TextLog.debug("ck=\""+Utilities.bytesToBase64(authResult.ck)+"\"");
	    						}
	    						else
	    						{
	    							throw new KryptonClientRuntimeException("Could not verify master key!");
	    						}
							}
							else
							{
								throw new KryptonClientRuntimeException("Unable to resynchronize while negotiating key agreement!");
							}
						}
	
						break;
					default:
						throw new KryptonClientRuntimeException("Authentication failure while negotiating key agreement!");
				}
			}
		}
		//Save key cache
        keyCache.save();		
		return authResult;
	}
	
	protected IUiccInterface createUiccInterface(SORACOMEndorseClientConfig kryptonClientConfig) {
		UiccInterfaceType uiccInterfaceType = kryptonClientConfig.getUiccInterfaceType();
		switch (uiccInterfaceType){
		case iso7816:{
			return new Iso7816Manager();
		}
		case comm:{
			return createCommManager(kryptonClientConfig.getCommunicationDeviceConfig());
		}
		case mmcli:{
			return createMmcliManager(kryptonClientConfig.getCommunicationDeviceConfig());
		}
		case autoDetect:{
			return new AutoDetectManager();
		}
		default:
			throw new KryptonClientRuntimeException("Unsupported UiccInterfaceType. type:"+uiccInterfaceType.toString());
		}
	}
		
	protected CommManager createCommManager(CommunicationDeviceConfig communicationDeviceConfig) {
		CommManager commManager = new CommManager();
		if(communicationDeviceConfig != null) {
			if  (communicationDeviceConfig.getPortName() != null){
				commManager.setPortName(communicationDeviceConfig.getPortName());
			}
			if (communicationDeviceConfig.getBaudRate() != null){
				commManager.setBaudRate(communicationDeviceConfig.getBaudRate());
			}
			if (communicationDeviceConfig.getDataBits() != null){
				commManager.setDataBits( communicationDeviceConfig.getDataBits());
			}
			if (communicationDeviceConfig.getStopBits() !=null){
				commManager.setStopBits(communicationDeviceConfig.getStopBits());
			}
			if (communicationDeviceConfig.getParity() != null){
				commManager.setParity(communicationDeviceConfig.getParity());
			}
		}
		return commManager;
	}
	protected MmcliManager createMmcliManager(CommunicationDeviceConfig communicationDeviceConfig) {
		MmcliManager mmcliManager = new MmcliManager();
		if(communicationDeviceConfig != null) {
			mmcliManager.setModemIndex(communicationDeviceConfig.getModemIndex());
		}
		return mmcliManager;
	}
	
	public static class KryptonClientLogListener implements ITextLogListener{
		private boolean suppressLogOutput;
		public KryptonClientLogListener() {
		}
		public KryptonClientLogListener(boolean suppressLogOutput) {
			setSuppressLog(suppressLogOutput);
		}
		public void setSuppressLog(boolean suppressLogOutput) {
			this.suppressLogOutput = suppressLogOutput;
		}
		@Override
		public void itemAdded(TextLogItem item) {
			switch (item.getType()){
	
				case DEBUG:
			 	case LOG:
			 	case WARN:
			 		if(suppressLogOutput == false) {
						System.out.println(item.toString());
			 		}
					break;
				case ERR:
					System.err.println(item.toString());
					break;
			}
		}
	}	
}
