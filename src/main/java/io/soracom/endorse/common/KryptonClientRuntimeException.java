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

public class KryptonClientRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 4046835488834412175L;

	public KryptonClientRuntimeException(String message) {
		super(message);
	}

	public KryptonClientRuntimeException(Throwable t) {
		super(t.getMessage(), t);
	}

	public KryptonClientRuntimeException(String message, Throwable t) {
		super(message, t);
	}
}
