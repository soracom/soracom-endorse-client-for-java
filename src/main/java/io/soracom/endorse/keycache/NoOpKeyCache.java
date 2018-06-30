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
package io.soracom.endorse.keycache;

import java.security.Key;

public class NoOpKeyCache implements KeyCache {

	@Override
	public AuthResult getAuthResultFromCache(String imsi) {
		return null;
	}
	
	@Override
	public void saveAuthResult(AuthResult authResult) {
	}

	@Override
	public boolean isStillValid(String alias) {
		return false;
	}

	@Override
	public void initKeyStore(String path) {

	}

	@Override
	public String[] listKeyAliases() {
		return null;
	}

	@Override
	public Key getKey(String alias) {
		return null;
	}

	@Override
	public void setKey(String alias, Key key) {

	}

	@Override
	public void unsetKey(String alias) {

	}

	@Override
	public void clear() {

	}

	@Override
	public void save() {

	}

	@Override
	public byte[] getKeyBytes(String alias) {
		return null;
	}

	@Override
	public void setKeyBytes(String alias, byte[] value) {
	}
}
