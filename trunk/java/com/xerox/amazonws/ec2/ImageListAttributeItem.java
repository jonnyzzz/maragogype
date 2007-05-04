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

