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

import java.util.Set;
import java.util.HashSet;

/**
 * The base class for all AMI list attributes.
 */
public abstract class ImageListAttribute extends ImageAttribute {
	private Set<ImageListAttributeItem> items;

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

	public Set<ImageListAttributeItem> getImageListAttributeItems() {
		return items;
	}
	
	/**
	 * Indicates if the list attribute may contain items of the given type.
	 * @param type
	 *         The list item type to test if membership is valid for this list attribute.
	 * @return true if the item type is admissable, false otherwise
	 * 
	 */
	public abstract boolean itemTypeCompatible(ImageListAttributeItemType type);
}

