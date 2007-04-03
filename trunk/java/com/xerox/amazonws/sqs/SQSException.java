package com.xerox.amazonws.sqs;

/**
 * A wrapper exception to simplify catching errors related to queue activity.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class SQSException extends Exception {

    public SQSException(String s) {
        super(s);
    }
    public SQSException(String s, Exception ex) {
        super(s, ex);
    }
}
