
package com.xerox.amazonws.ec2;

/**
 * Attribute class for the launchPermission attribute type.
 */
public class LaunchPermissionAttribute extends ImageListAttribute {
	public LaunchPermissionAttribute() {
		super(ImageAttributeType.launchPermission);
	}
	
	public boolean itemTypeCompatible(ImageListAttributeItemType type) {
		return 	type == ImageListAttributeItemType.userId ||
				type == ImageListAttributeItemType.group;
	}
}

