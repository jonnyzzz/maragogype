
package com.xerox.amazonws.sqs;

/**
 * This class is a wrapper for a message received from a queue.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class Message {
	private String messageId;
	private String messageBody;

	protected Message(String messageId, String messageBody) {
		this.messageId = messageId;
		this.messageBody = messageBody;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	public String toString() {
		return "id: "+messageId+" body: "+messageBody;
	}
}
