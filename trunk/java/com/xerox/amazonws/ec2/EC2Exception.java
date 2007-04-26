
package com.xerox.amazonws.ec2;

/**
 * A wrapper exception to simplify catching errors related to queue activity.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class EC2Exception extends Exception {

    public EC2Exception(String s) {
        super(s);
    }
    public EC2Exception(String s, Exception ex) {
        super(s, ex);
    }
}
