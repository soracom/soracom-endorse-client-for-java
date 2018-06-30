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

public interface IUiccInterface {
	
	/**
	 * Proceed to read the IMSI and return it in the byte encoded form
	 * @return IMSI in binary format
	 */
	public String readImsi();
	
	/**
	 * Proceed to Authenticate with the USIM application and returned the response
	 * @param rand
	 * @param autn
	 * @return
	 * //"Successful 3G authentication" tag = 'DB'
	 * RES, CK, IK if Service no27 is "not available".
	 * or
	 * RES, CK, IK, KC if Service no27 is "available".
	 * //"Synchronization failure" tag = 'DC'
	 * AUTS.
	 */
    public byte[] authenticate(byte[] rand, byte[] autn);
    
    public boolean disconnect();
}
