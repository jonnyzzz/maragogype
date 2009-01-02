//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.xerox.amazonws.sdb;

import java.util.Calendar;
import java.util.List;

public class DomainMetadataResult extends SDBResult {
	private Calendar timeStamp;
	private int itemCount;
	private int attributeValueCount;
	private int attributeNameCount;
	private long itemNamesSizeBytes;
	private long attributeValuesSizeBytes;
	private long attributeNamesSizeBytes;

	DomainMetadataResult(String requestId, String boxUsage, Calendar timeStamp,
			int itemCount, int attributeValueCount, int attributeNameCount,
			long itemNamesSizeBytes, long attributeValuesSizeBytes, long attributeNamesSizeBytes) {
		super(null, requestId, boxUsage);
		this.timeStamp = timeStamp;
		this.itemCount = itemCount;
		this.attributeValueCount = attributeValueCount;
		this.attributeNameCount = attributeNameCount;
		this.itemNamesSizeBytes = itemNamesSizeBytes;
		this.attributeValuesSizeBytes = attributeValuesSizeBytes;
		this.attributeNamesSizeBytes = attributeNamesSizeBytes;
	}

	public Calendar getTimeStamp() {
		return timeStamp;
	}

	public int getItemCount() {
		return itemCount;
	}

	public int getAttributeValueCount() {
		return attributeValueCount;
	}

	public int getAttributeNameCount() {
		return attributeNameCount;
	}

	public long getItemNamesSizeBytes() {
		return itemNamesSizeBytes;
	}

	public long getAttributeValuesSizeBytes() {
		return attributeValuesSizeBytes;
	}

	public long getAttributeNamesSizeBytes() {
		return attributeNamesSizeBytes;
	}
}
