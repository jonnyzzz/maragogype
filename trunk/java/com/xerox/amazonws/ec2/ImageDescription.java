
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

