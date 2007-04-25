
package com.xerox.amazonws.ec2;

/**
 * An instance of this class represents an EC2 instance after a request has
 * been issued to terminate that instance.
 * <p>
 * Instances are returned by calls to {@link Jec2#terminateInstances(List)},
 * and {@link Jec2#terminateInstances(String[])}.
 */
public class TerminatingInstanceDescription {
	public String instanceId;

	public String prevState;

	public String shutdownState;

	public int prevStateCode;

	public int shutdownStateCode;

	public TerminatingInstanceDescription(String id, String prevState,
			int prevStateCode, String shutdownState, int shutdownStateCode) {
		this.instanceId = id;
		this.prevState = prevState;
		this.prevStateCode = prevStateCode;
		this.shutdownState = shutdownState;
		this.shutdownStateCode = shutdownStateCode;
	}

	public String toString() {
		return "Instance[ID=" + instanceId + ", prevState=" + prevState
				+ "(" + prevStateCode + "), shutdownState=" + shutdownState
				+ "(" + shutdownStateCode + ")]";
	}
}

