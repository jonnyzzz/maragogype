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

