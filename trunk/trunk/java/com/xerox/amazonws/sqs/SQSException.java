package com.xerox.amazonws.sqs;

public class SQSException extends Exception {

    public SQSException(String s) {
        super(s);
    }
    public SQSException(String s, Exception ex) {
        super(s, ex);
    }
}
