
package com.xerox.amazonws.ec2;

import java.util.Set;
import java.util.HashSet;

/**
 * The base class for all AMI list attributes.
 */
public abstract class ImageListAttribute extends ImageAttribute {
	/**
	 * Enumerates image list attribute item types.
	 */
	public enum ImageListAttributeItemType {
		group,
		userId
	}

	public ImageListAttribute(ImageAttribute.ImageAttributeType _type) {
		super(_type);
		items = new HashSet<ImageListAttributeItem>();
	}

	/**
	 * Add an item to the attribute's list of key-value pairs.
	 * @param type
	 *         The type of list attribute item to add.
	 * @param value
	 *         The value of the item.
	 * @return True if the item was successfully added to the list, false if the operation failed.
	 */
	public boolean addImageListAttributeItem(ImageListAttributeItemType type, String value) {
		if (itemTypeCompatible(type))
		  return items.add(new ImageListAttributeItem(type, value));
		else
		  return false;
	}
	
	/**
	 * Indicates if the list attribute may contain items of the given type.
	 * @param type
	 *         The list item type to test if membership is valid for this list attribute.
	 * @return true if the item type is admissable, false otherwise
	 * 
	 */
	public abstract boolean itemTypeCompatible(ImageListAttributeItemType type);
	
	public Set<ImageListAttributeItem> items;
}

