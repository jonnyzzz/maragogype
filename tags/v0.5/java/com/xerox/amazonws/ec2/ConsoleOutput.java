//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
// 
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2.1 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
//

package com.xerox.amazonws.ec2;

import java.util.Calendar;

/**
 * An instance of this class represents an instance's console output.
 * <p>
 * {@link Jec2#getConsoleOutput(String instanceId)}
 */
public class ConsoleOutput {
	private String instanceId;
	private Calendar timestamp;
	private String output;                     // naked (i.e. not escaped, not BASE64)

	public ConsoleOutput(String instanceId,
						java.util.Calendar timestamp,
						String output) {
		this.instanceId = instanceId;
		this.timestamp = timestamp;
		this.output = output;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public String getOutput() {
		return output;
	}

	public String toString() {
		return "ConsoleOutput[instanceID=" + instanceId +
			", timestamp=" + timestamp + ", output=" + output + "]";
	}
}
