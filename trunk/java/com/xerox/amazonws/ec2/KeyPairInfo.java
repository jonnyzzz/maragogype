
package com.xerox.amazonws.ec2;

/**
 * An instance of this class represents an EC2 keypair.
 * <p>
 * Instances are returned by calls to
 * {@link Jec2#createKeyPair(String),
 * {@link Jec2#describeKeyPairs(List)}, and
 * {@link Jec2#describeKeyPairs(String[])}.
 */
public class KeyPairInfo {
	public String keyName;

	public String keyFingerprint;

	public String keyMaterial;

	public KeyPairInfo(String keyName, String keyFingerprint,
			String keyMaterial) {
		this.keyName = keyName;
		this.keyFingerprint = keyFingerprint;
		this.keyMaterial = keyMaterial;
	}

	public String toString() {
		return "KeyPair[Name=" + this.keyName + ", fingerprint="
				+ this.keyFingerprint + ", material=" + this.keyMaterial
				+ "]";
	}
}

