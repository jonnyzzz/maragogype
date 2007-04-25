
package com.xerox.amazonws.ec2;

/**
 * An instance of this class represents an instance's console output.
 * <p>
 * {@link Jec2#getConsoleOutput(String instanceId)}
 */
public class ConsoleOutput {
	public String instanceId;
	public java.util.Calendar timestamp;
	public String output;                     // naked (i.e. not escaped, not BASE64)

	public ConsoleOutput(String instanceId,
						java.util.Calendar timestamp,
						String output) {
		this.instanceId = instanceId;
		this.timestamp = timestamp;
		this.output = output;
	}

	public String toString() {
		return "ConsoleOutput[instanceID=" + instanceId +
			", timestamp=" + timestamp + ", output=" + output + "]";
	}
}
