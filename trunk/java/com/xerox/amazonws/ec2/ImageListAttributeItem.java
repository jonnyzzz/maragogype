
package com.xerox.amazonws.ec2;

/**
 * An type-value item for a list attribute.
 * <p>
 * ImageListAttributeItems are keyed on type and value. 
 */
public class ImageListAttributeItem {
	public ImageListAttributeItem(ImageListAttribute.ImageListAttributeItemType _type, String _value) {
		type = _type;
		value = _value;
	}

	public boolean equals(Object other) {
		if (this == other) return true;
		if (null == other) return false;
		ImageListAttributeItem item = (ImageListAttributeItem) other;
		return type.equals(item.type) && value.equals(item.value);
	}
	
	public int hashCode() {
		return (type == null ? 17 : type.hashCode()) ^
			   (value == null ? 31 : value.hashCode());
	}
	
	public ImageListAttribute.ImageListAttributeItemType type;
	public String value;
}

