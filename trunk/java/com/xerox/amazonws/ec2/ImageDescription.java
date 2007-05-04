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
 * An instance of this class represents an AMI description.
 * <p>
 * Instances are returned by calls to {@link Jec2#describeImages(List)} or
 * {@link Jec2#describeImages(String[])}.
 */
public class ImageDescription {
	public String imageId;

	public String imageLocation;

	public String imageOwnerId;

	public String imageState;

	public boolean isPublic;

	public ImageDescription(String id, String loc, String owner,
			String state, Boolean isPublic) {
		this.imageId = id;
		this.imageLocation = loc;
		this.imageOwnerId = owner;
		this.imageState = state;
		this.isPublic = isPublic;
	}

	public String toString() {
		return "Image[ID=" + imageId + ", Loc=" + imageLocation + ", own="
				+ imageOwnerId + ", state=" + imageState + " isPublic="
				+ isPublic + "]";
	}
}

