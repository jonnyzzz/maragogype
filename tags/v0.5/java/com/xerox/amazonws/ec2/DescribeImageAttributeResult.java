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
 * The results of a call to describe image attributes. 
 */
public class DescribeImageAttributeResult {
	private String imageId;
	private ImageListAttribute imageListAttribute;
	
	public DescribeImageAttributeResult(String imageId, ImageListAttribute imageListAttribute) {
		this.imageId = imageId;
		this.imageListAttribute = imageListAttribute;
	}

	public String getImageId() {
		return imageId;
	}

	public ImageListAttribute getImageListAttribute() {
		return imageListAttribute;
	}
}

