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

/**
 * An instance of this class represents an EC2 instance after a request has
 * been issued to terminate that instance.
 * <p>
 * Instances are returned by calls to {@link Jec2#terminateInstances(List)},
 * and {@link Jec2#terminateInstances(String[])}.
 */
public class TerminatingInstanceDescription {
	private String instanceId;
	private String prevState;
	private String shutdownState;
	private int prevStateCode;
	private int shutdownStateCode;

	public TerminatingInstanceDescription(String id, String prevState,
			int prevStateCode, String shutdownState, int shutdownStateCode) {
		this.instanceId = id;
		this.prevState = prevState;
		this.prevStateCode = prevStateCode;
		this.shutdownState = shutdownState;
		this.shutdownStateCode = shutdownStateCode;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public String getPreviousState() {
		return prevState;
	}

	public String getShutdownState() {
		return shutdownState;
	}

	public int getPreviousStateCode() {
		return prevStateCode;
	}

	public int getShutdownStateCode() {
		return shutdownStateCode;
	}

	public String toString() {
		return "Instance[ID=" + instanceId + ", prevState=" + prevState
				+ "(" + prevStateCode + "), shutdownState=" + shutdownState
				+ "(" + shutdownStateCode + ")]";
	}
}

