//
// typica - A client library for Amazon Web Services
// Copyright (C) 2007 Xerox Corporation
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.xerox.amazonws.ec2;

import java.util.List;

/**
 * An instance of this class represents an AMI description.
 * <p>
 * Instances are returned by calls to {@link Jec2#describeImages(List)} or
 * {@link Jec2#describeImages(String[])}.
 */
public class ImageDescription {
	private String imageId;
	private String imageLocation;
	private String imageOwnerId;
	private String imageState;
	private boolean isPublic;
	private List<String> productCodes;

	public ImageDescription(String id, String loc, String owner,
			String state, Boolean isPublic, List<String> productCodes) {
		this.imageId = id;
		this.imageLocation = loc;
		this.imageOwnerId = owner;
		this.imageState = state;
		this.isPublic = isPublic;
		this.productCodes = productCodes;
	}

	public String getImageId() {
		return imageId;
	}

	public String getImageLocation() {
		return imageLocation;
	}

	public String getImageOwnerId() {
		return imageOwnerId;
	}

	public String getImageState() {
		return imageState;
	}

	public boolean isPublic() {
		return isPublic;
	}

	public List<String> getProductCodes() {
		return productCodes;
	}

	public String toString() {
		return "Image[ID=" + imageId + ", Loc=" + imageLocation + ", own="
				+ imageOwnerId + ", state=" + imageState + " isPublic="
				+ isPublic + "]";
	}
}

