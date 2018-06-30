/*******************************************************************************
 * Copyright (c) 2018 SORACOM, Inc. and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     SORACOM,Inc. - initial API and implementation
 *******************************************************************************/
package io.soracom.endorse.interfaces;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.Callback;

import org.junit.Test;

import io.soracom.endorse.interfaces.AutoDetectManager;
import io.soracom.endorse.interfaces.IUiccInterface;

public class TestAutoDetectManager {

	public static void main(String[] args) {
		new AutoDetectManager();
		System.out.println("end");
	}

	public static class MockIUiccInterface implements IUiccInterface {
		String imsi;
		int wait;

		@Override
		public String readImsi() {
			try {
				Thread.sleep(wait * 1000);
			} catch (InterruptedException e) {
			}
			return imsi;
		}

		@Override
		public byte[] authenticate(byte[] rand, byte[] autn) {
			return null;
		}
		@Override
		public boolean disconnect() {
			// TODO Auto-generated method stub
			return false;
		}
	}

	@Test
	public void testAutoDetect() {
		AutoDetectManager manager = new AutoDetectManager() {
			@Override
			protected List<InterfaceDetectThread> createThreadList(Callback callback) {
				List<InterfaceDetectThread> list = new ArrayList<>();
				{
					MockIUiccInterface mock = new MockIUiccInterface();
					mock.imsi = "FIRST";
					mock.wait = 5;
					list.add(new InterfaceDetectThread(mock, mock.imsi, callback));
				}
				{
					MockIUiccInterface mock = new MockIUiccInterface();
					mock.imsi = null;
					mock.wait = 1;
					list.add(new InterfaceDetectThread(mock, mock.imsi, callback));
				}
				{
					MockIUiccInterface mock = new MockIUiccInterface();
					mock.imsi = "SECOND";
					mock.wait = 10;
					list.add(new InterfaceDetectThread(mock, mock.imsi, callback));
				}
				return list;
			}
		};
		assertEquals("FIRST", manager.readImsi());
	}
}
