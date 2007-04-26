
package com.xerox.amazonws.ec2;

/**
 * The base class for all AMI attributes.
 */
public abstract class ImageAttribute {
	/**
	 * Enumerates image attribute types.
	 */
	public enum ImageAttributeType {
		launchPermission
	}

	public ImageAttribute(ImageAttributeType _type) {
		type = _type;
	}		

	public ImageAttributeType type;
}

