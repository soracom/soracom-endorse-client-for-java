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

import java.util.ArrayList;
import java.util.List;

import io.soracom.endorse.common.TextLogItem.TextLogItemType;

public class TextLog {

	private static final List<ITextLogListener> listeners = new ArrayList<ITextLogListener>();

	static {
		// default logger
		listeners.add(new ITextLogListener() {
			@Override
			public void itemAdded(TextLogItem item) {
				System.out.println(item.toString());
			}
		});
	}

	public static void clerListener() {
		listeners.clear();
	}

	public static void addListener(ITextLogListener toAdd) {
		listeners.add(toAdd);
	}

	public static void removeListener(ITextLogListener toRemove) {
		listeners.remove(toRemove);
	}

	public static void debug(String message) {
		add(new TextLogItem(TextLogItemType.DEBUG, message));
	}
	
	public static void log(String message) {
		add(new TextLogItem(TextLogItemType.LOG, message));
	}

	public static void warn(String message) {
		add(new TextLogItem(TextLogItemType.WARN, message));
	}

	public static void error(String message) {
		add(new TextLogItem(TextLogItemType.ERR, message));
	}

	public static void add(TextLogItem item) {
		itemAdded(item);
	}

	private static void itemAdded(TextLogItem item) {
		for (ITextLogListener hl : listeners) {
			hl.itemAdded(item);
		}
	}
}
