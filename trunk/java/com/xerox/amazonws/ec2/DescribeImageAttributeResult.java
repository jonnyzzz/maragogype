
package com.xerox.amazonws.ec2;

/**
 * The results of a call to describe image attributes. 
 */
public class DescribeImageAttributeResult {
	public String imageId;
	public ImageListAttribute imageListAttribute;
	
	public DescribeImageAttributeResult(String imageId, ImageListAttribute imageListAttribute) {
		this.imageId = imageId;
		this.imageListAttribute = imageListAttribute;
	}
}

