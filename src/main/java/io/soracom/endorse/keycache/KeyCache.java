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

public interface KeyCache {
	public static final String ENV_NAME_ENDORSE_KEY_STORE_KEY = "ENDORSE_KEY_STORE_KEY";

	AuthResult getAuthResultFromCache(String imsi);
	
	void saveAuthResult(AuthResult authResult);

	boolean isStillValid(String alias);

	void initKeyStore(String path);

	String[] listKeyAliases();

	Key getKey(String alias);

	void setKey(String alias, Key key);

	void unsetKey(String alias);

	void clear();

	void save();

	byte[] getKeyBytes(String alias);

	void setKeyBytes(String alias, byte[] value);
}
