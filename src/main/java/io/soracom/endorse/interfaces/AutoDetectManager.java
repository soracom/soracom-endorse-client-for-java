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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.smartcardio.CardTerminal;

import io.soracom.endorse.common.TextLog;

public class AutoDetectManager implements IUiccInterface {

	private IUiccInterface uiccInterfaceImpl;

	public AutoDetectManager() {
		autoDetectUiccInterface();
	}

	@Override
	public byte[] authenticate(byte[] rand, byte[] autn) {
		return uiccInterfaceImpl.authenticate(rand, autn);
	}

	@Override
	public String readImsi() {
		return uiccInterfaceImpl.readImsi();
	}

	static class Callback {
		private InterfaceDetectThread detectedInterfaceThread;
		private CountDownLatch countDownLatch = new CountDownLatch(1);
		private AtomicInteger totalThreadNum = new AtomicInteger(0);
		private AtomicInteger endedThreadNum = new AtomicInteger(0);

		void incrementalThreadNum() {
			totalThreadNum.incrementAndGet();
		}

		void threadEnd(InterfaceDetectThread thread) {
			
			if (thread.imsi != null && thread.imsi.length() > 0) {
				synchronized (this) {
					if (detectedInterfaceThread == null) {
						detectedInterfaceThread = thread;
						TextLog.log("SIM was detected from " + thread.interfaceDescription + " imsi=" + thread.imsi);
						countDownLatch.countDown();
						return;
					}
				}
			}
			if (endedThreadNum.incrementAndGet() == totalThreadNum.get()) {
				countDownLatch.countDown();
			}
		}

		InterfaceDetectThread get() {
			try {
				countDownLatch.await();
			} catch (InterruptedException e) {
			}
			return detectedInterfaceThread;
		}
	}

	static class InterfaceDetectThread extends Thread {
		private String imsi;
		private IUiccInterface uiccInterface;
		private String interfaceDescription;
		private Callback callback;

		InterfaceDetectThread(IUiccInterface uiccInterface, String description, Callback callback) {
			this.uiccInterface = uiccInterface;
			this.interfaceDescription = description;
			this.callback = callback;
			callback.incrementalThreadNum();
			TextLog.log("detecting SIM from " + interfaceDescription);
		}

		@Override
		public void run() {
			try {
				imsi = uiccInterface.readImsi();
			} catch (Throwable t) {
			}
			callback.threadEnd(this);
		}
	}

	protected void autoDetectUiccInterface() {
		Callback callBack = new Callback();
		List<InterfaceDetectThread> interfaceDetectThreadList = createThreadList(callBack);
		if (interfaceDetectThreadList.size() == 0) {
			throw new UiccInterfaceNotFoundException("There are no interface to be detected.");
		}
		ExecutorService threadPool = Executors.newFixedThreadPool(interfaceDetectThreadList.size());
		for (InterfaceDetectThread thread : interfaceDetectThreadList) {
			threadPool.submit(thread);
		}
		InterfaceDetectThread detectedThread = callBack.get();
		for (InterfaceDetectThread thread : interfaceDetectThreadList) {
			thread.uiccInterface.disconnect();
		}
		threadPool.shutdownNow();
		if (detectedThread == null) {
			throw new UiccInterfaceNotFoundException("failed to detect UICC interface.");
		} else {
			this.uiccInterfaceImpl = detectedThread.uiccInterface;
		}
	}

	protected List<InterfaceDetectThread> createThreadList(Callback callback) {
		List<InterfaceDetectThread> interfaceDetectThreadList = new ArrayList<>();
		{
			List<CardTerminal> readers = Iso7816Manager.listReaders();
			for (CardTerminal reader : readers) {
				Iso7816Manager manager = new Iso7816Manager(reader);
				interfaceDetectThreadList.add(new InterfaceDetectThread(manager, "Iso7816 interface", callback));
			}
		}
		{
			String[] portNames = CommManager.getAvailablePorts();
			for (String portName : portNames) {
				CommManager manager = new CommManager();
				manager.setPortName(portName);
				interfaceDetectThreadList
						.add(new InterfaceDetectThread(manager, "COM port [" + portName + "]", callback));
			}
		}
//		{
//			if (MmcliManager.isUnsupportedPlatform() == false) {
//				MmcliManager manager = new MmcliManager();
//				interfaceDetectThreadList.add(new InterfaceDetectThread(manager,
//						"mmcli modem index [" + manager.getModemIndex() + "]", callback));
//			}
//		}
		return interfaceDetectThreadList;
	}
	
	@Override
	public boolean disconnect() {
		return true;
	}
}
