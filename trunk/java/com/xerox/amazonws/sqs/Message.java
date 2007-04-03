
package com.xerox.amazonws.sqs;

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
		return "id: "+messageId+" text: "+messageBody;
	}
}
