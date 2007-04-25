package com.xerox.amazonws.sqs;

import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.xerox.amazonws.common.JAXBuddy;
import com.xerox.amazonws.typica.jaxb.DeleteMessageResponse;
import com.xerox.amazonws.typica.jaxb.DeleteQueueResponse;
import com.xerox.amazonws.typica.jaxb.GetVisibilityTimeoutResponse;
import com.xerox.amazonws.typica.jaxb.PeekMessageResponse;
import com.xerox.amazonws.typica.jaxb.ReceiveMessageResponse;
import com.xerox.amazonws.typica.jaxb.SendMessageResponse;
import com.xerox.amazonws.typica.jaxb.SetVisibilityTimeoutResponse;

/**
 * This class provides an interface with the Amazon SQS message queue. It provides methods
 * for sending / receiving messages and deleting queues and messsages on queues.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class MessageQueue extends QueueService {
    public static final int MAX_MESSAGES = 600;
    public static final int MAX_MESSAGE_BODIES_SIZE = 4096;

	protected String queueId;

    protected MessageQueue(String queueUrl, String awsAccessKeyId,
							String awsSecretAccessKey, boolean isSecure,
							String server) throws SQSException {
        super(awsAccessKeyId, awsSecretAccessKey, isSecure, server);
		queueId = queueUrl.substring(queueUrl.indexOf("//")+2);
		queueId = queueId.substring(queueId.indexOf("/")+1);
    }

	/**
	 * This method provides the URL for the message queue represented by this object.
	 *
	 * @return generated queue service url
	 */
	public URL getUrl() {
		try {
			return new URL(super.getUrl().toString()+queueId);
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * Sends a message to a specified queue. The message must be between 1 and 256K bytes long.
	 *
	 * @param msg the message to be sent (should be base64 encoded)
	 * @return the message id for the message just sent
	 */
    public String sendMessage(String msg) throws SQSException {
		try {
			String request = queueId+"/back";
			URLConnection conn = makeRequest("PUT", request, super.headers);
			conn.setDoOutput(true);
			OutputStream oStr = conn.getOutputStream();
			oStr.write(new String(msg).getBytes());
			oStr.flush();
			InputStream iStr = conn.getInputStream();
			SendMessageResponse response = JAXBuddy.deserializeXMLStream(SendMessageResponse.class, iStr);
			return response.getMessageId();
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}

	/**
	 * Attempts to receive a message from the queue. The queue default visibility timeout
	 * is used.
	 *
	 * @return the message object
	 */
    public Message receiveMessage() throws SQSException {
        Message amessage[] = receiveMessages(BigInteger.valueOf(1L), ((BigInteger) (null)));
        if(amessage.length > 0)
            return amessage[0];
        else
            return null;
	}

	/**
	 * Attempts to receive a message from the queue.
	 *
	 * @param visibilityTimeout the duration (in seconds) the retrieved message is hidden from
	 *                          subsequent calls to retrieve.
	 * @return the message object
	 */
    public Message receiveMessage(int visibilityTimeout) throws SQSException {
        Message amessage[] = receiveMessages(BigInteger.valueOf(1L), BigInteger.valueOf(visibilityTimeout));
        if(amessage.length > 0)
            return amessage[0];
        else
            return null;
	}

	/**
	 * Attempts to retrieve a number of messages from the queue. If less than that are availble,
	 * the max returned is the number of messages in the queue, but not necessarily all messages
	 * in the queue will be returned. The queue default visibility timeout is used.
	 *
	 * @param numMessages the maximum number of messages to return
	 * @return an array of message objects
	 */
    public Message[] receiveMessages(int numMessages) throws SQSException {
        return receiveMessages(BigInteger.valueOf(numMessages), ((BigInteger) (null)));
	}

	/**
	 * Attempts to retrieve a number of messages from the queue. If less than that are availble,
	 * the max returned is the number of messages in the queue, but not necessarily all messages
	 * in the queue will be returned.
	 *
	 * @param numMessages the maximum number of messages to return
	 * @param visibilityTimeout the duration (in seconds) the retrieved message is hidden from
	 *                          subsequent calls to retrieve.
	 * @return an array of message objects
	 */
    public Message[] receiveMessages(int numMessages, int visibilityTimeout) throws SQSException {
        return receiveMessages(BigInteger.valueOf(numMessages), BigInteger.valueOf(visibilityTimeout));
	}

	/**
	 * Internal implementation of receiveMessages.
	 *
	 * @param
	 * @return
	 */
    protected Message[] receiveMessages(BigInteger numMessages, BigInteger visibilityTimeout) throws SQSException {
		try {
			String request = queueId+"/front?NumberOfMessages="+numMessages;
			if (visibilityTimeout != null) request += "&VisibilityTimeout="+visibilityTimeout;
			HttpURLConnection conn = makeRequest("GET", request, super.headers);
			if (conn.getResponseCode() < 400) {
				InputStream iStr = conn.getInputStream();
				ReceiveMessageResponse response = JAXBuddy.deserializeXMLStream(ReceiveMessageResponse.class, iStr);
				if (response.getMessages() == null) {
					return new Message[0];
				}
				else {
					ArrayList<Message> msgs = new ArrayList();
					for (com.xerox.amazonws.typica.jaxb.Message msg : response.getMessages()) {
						msgs.add(new Message(msg.getMessageId(), msg.getMessageBody()));
					}
					return msgs.toArray(new Message [msgs.size()]);
				}
			}
			else {
				return new Message[0];
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}

	/**
	 * Returns a specified message. This does not affect and is not affected by the visibility
	 * timeout of either the queue or the message.
	 *
	 * @param
	 * @return
	 */
    public Message peekMessage(String msgId) throws SQSException {
		try {
			InputStream iStr = makeRequest("GET", queueId+"/"+msgId, super.headers).getInputStream();
			PeekMessageResponse response = JAXBuddy.deserializeXMLStream(PeekMessageResponse.class, iStr);
			com.xerox.amazonws.typica.jaxb.Message msg = response.getMessage();
			if (msg == null) {
				return null;
			}
			else {
				return new Message(msg.getMessageId(), msg.getMessageBody());
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}

	/**
	 * Deletes the message identified by msgid on the queue this object represents.
	 *
	 * @param msgId the id of the message to be deleted
	 */
    public void deleteMessage(String msgId) throws SQSException {
		try {
			HttpURLConnection conn = makeRequest("DELETE", queueId+"/"+msgId, super.headers);
			if (conn.getResponseCode() < 400) {
				InputStream iStr = conn.getInputStream();
				DeleteMessageResponse response = JAXBuddy.deserializeXMLStream(DeleteMessageResponse.class, iStr);
			}
			else {
				throw new SQSException("Error deleting message id="+msgId);
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}

	/**
	 * Deletes the message queue represented by this object.
	 */
    public void deleteQueue() throws SQSException {
        int respCode;
		try {
			HttpURLConnection conn = makeRequest("DELETE", queueId, super.headers);
			if ((respCode = conn.getResponseCode()) < 400) {
				InputStream iStr = conn.getInputStream();
				DeleteQueueResponse response = JAXBuddy.deserializeXMLStream(DeleteQueueResponse.class, iStr);
			}
			else {
				throw new SQSException("Error deleting queue, response code = "+respCode);
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}

	/**
	 * Gets the visibility timeout for the queue. 
	 */
    public int getVisibilityTimeout() throws SQSException {
		try {
			String request = queueId+"/";
			HttpURLConnection conn = makeRequest("GET", request, super.headers);
			if (conn.getResponseCode() < 400) {
				InputStream iStr = conn.getInputStream();
				GetVisibilityTimeoutResponse response = JAXBuddy.deserializeXMLStream(GetVisibilityTimeoutResponse.class, iStr);
				if (response.getResponseStatus().getStatusCode().equals("Success")) {
					return response.getVisibilityTimeout().intValue();
				}
				else {
					throw new SQSException("Error getting timeout. Response msg = "+response.getResponseStatus().getMessage());
				}
			}
			else {
				throw new SQSException("Error getting timeout. Response code = "+conn.getResponseCode());
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem getting the visilibity timeout.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}

	/**
	 * Placeholder. Not implemented.
	 */
    public void setVisibilityTimeout(int timeout) throws SQSException {
		try {
			String request = queueId+"?VisibilityTimeout="+timeout;
			HttpURLConnection conn = makeRequest("PUT", request, super.headers);
			if (conn.getResponseCode() < 400) {
				InputStream iStr = conn.getInputStream();
				SetVisibilityTimeoutResponse response = JAXBuddy.deserializeXMLStream(SetVisibilityTimeoutResponse.class, iStr);
				if (response.getResponseStatus().getStatusCode().equals("Success")) {
					return;
				}
				else {
					throw new SQSException("Error setting timeout. Response msg = "+response.getResponseStatus().getMessage());
				}
			}
			else {
				throw new SQSException("Error setting timeout. Response code = "+conn.getResponseCode());
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem setting the visibility timeout.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}

	/**
	 * Placeholder. Not implemented by REST.
	 */
    public void setVisibilityTimeout(String msgId, int timeout) throws SQSException {
	}

	/**
	 * Placeholder. Not implemented by REST.
	 */
    public void setVisibilityTimeout(String[] msgIds, int timeout) throws SQSException {
	}

/*  grants not supported in REST at this time
    public void addGrantByEmail(String, String) throws Exception {
	}

    public void addGrantByCustomerId(String, String, String) throws Exception {
	}

    public void removeGrantByEmailAddress(String, String) throws Exception {
	}

    public void removeGrantByCustomerId(String, String) throws Exception {
	}
    public Grant[] listGrants(Grantee, String) throws Exception {
	}
*/

	public static List<MessageQueue> createList(String [] queueUrls, String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server) throws SQSException {
		ArrayList<MessageQueue> ret = new ArrayList<MessageQueue>();
		for (int i=0; i<queueUrls.length; i++) {
			ret.add(new MessageQueue(queueUrls[i], awsAccessKeyId, awsSecretAccessKey, isSecure, server));
		}
		return ret;
	}
}
