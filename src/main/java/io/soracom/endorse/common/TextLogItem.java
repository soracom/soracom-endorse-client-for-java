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

import java.text.SimpleDateFormat;
import java.util.Date;

public class TextLogItem {

	public enum TextLogItemType {
		DEBUG, LOG, WARN, ERR
	}

	private TextLogItemType type;
	private String desc;
	private long time;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");

	public TextLogItem() {
		time = System.currentTimeMillis();
		type = TextLogItemType.LOG;
		desc = "";

	}

	public TextLogItem(TextLogItemType itemType, String description) {
		time = System.currentTimeMillis();
		type = itemType;
		desc = description;
	}

	public TextLogItem(long timeStamp, TextLogItemType itemType, String description) {
		time = timeStamp;
		type = itemType;
		desc = description;
	}

	public void setType(TextLogItemType value) {
		type = value;
	}

	public TextLogItemType getType() {
		return type;
	}

	public void setDescription(String value) {
		desc = value;
	}

	public String getDescription() {
		return desc;
	}

	public void setTime(long value) {
		time = value;
	}

	public long getTime() {
		return time;
	}

	public String getReadableTime() {
		Date resultdate = new Date(time);
		return sdf.format(resultdate);
	}

	@Override
	public String toString() {
		return getReadableTime() + " [" + type + "] " + desc;
	}

}
