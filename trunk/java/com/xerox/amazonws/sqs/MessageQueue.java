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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import com.xerox.amazonws.common.JAXBuddy;
import com.xerox.amazonws.typica.jaxb.AttributedValue;
import com.xerox.amazonws.typica.jaxb.DeleteMessageResponse;
import com.xerox.amazonws.typica.jaxb.DeleteQueueResponse;
import com.xerox.amazonws.typica.jaxb.GetQueueAttributesResponse;
import com.xerox.amazonws.typica.jaxb.PeekMessageResponse;
import com.xerox.amazonws.typica.jaxb.ReceiveMessageResponse;
import com.xerox.amazonws.typica.jaxb.SendMessageResponse;
import com.xerox.amazonws.typica.jaxb.SetQueueAttributesResponse;

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
		Map<String, String> params = new HashMap<String, String>();
		try {
			URLConnection conn = makeRequest("POST", "SendMessage", params);
			conn.setRequestProperty("content-type", "text/plain");
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
		Map<String, String> params = new HashMap<String, String>();
		if (numMessages != null) {
			params.put("NumberOfMessages", numMessages.toString());
		}
		if (visibilityTimeout != null) {
			params.put("VisibilityTimeout", visibilityTimeout.toString());
		}
		try {
			HttpURLConnection conn = makeRequest("GET", "ReceiveMessage", params);
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
		Map<String, String> params = new HashMap<String, String>();
		params.put("MessageId", msgId);
		try {
			InputStream iStr = makeRequest("GET", "PeekMessage", params).getInputStream();
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
	 * Deletes the message identified by message object on the queue this object represents.
	 *
	 * @param msg the message to be deleted
	 */
    public void deleteMessage(Message msg) throws SQSException {
		deleteMessage(msg.getMessageId());
	}

	/**
	 * Deletes the message identified by msgid on the queue this object represents.
	 *
	 * @param msgId the id of the message to be deleted
	 */
    public void deleteMessage(String msgId) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("MessageId", msgId);
		try {
			HttpURLConnection conn = makeRequest("GET", "DeleteMessage", params);
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
	 * Deletes the message queue represented by this object. Will fail if queue isn't empty.
	 */
    public void deleteQueue() throws SQSException {
		deleteQueue(false);
	}

	/**
	 * Deletes the message queue represented by this object.
	 *
	 * @param force when true, non-empty queues will be deleted
	 */
    public void deleteQueue(boolean force) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		if (force) {
			params.put("ForceDeletion", "true");
		}
        int respCode;
		try {
			HttpURLConnection conn = makeRequest("GET", "DeleteQueue", params);
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
	 * Gets the visibility timeout for the queue. Uses {@link getQueueAttribute()}.
	 */
    public int getVisibilityTimeout() throws SQSException {
		return Integer.parseInt(getQueueAttributes(QueueAttribute.VISIBILITY_TIMEOUT)
										.values().iterator().next());
	}

	/**
	 * Gets the visibility timeout for the queue. Uses {@link getQueueAttribute()}.
	 */
    public int getApproximateNumberOfMessages() throws SQSException {
		return Integer.parseInt(getQueueAttributes(QueueAttribute.APPROXIMATE_NUMBER_OF_MESSAGES)
										.values().iterator().next());
	}

	/**
	 * Gets queue attributes. This is provided to expose the underlying functionality.
	 * Currently supported attributes are ApproximateNumberOfMessages and VisibilityTimeout.
	 *
	 * @return a map of attributes and their values
	 */
	public Map<String,String> getQueueAttributes(QueueAttribute qAttr) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("Attribute", qAttr.queryAttribute());
		try {
			HttpURLConnection conn = makeRequest("GET", "GetQueueAttributes", params);
			if (conn.getResponseCode() < 400) {
				InputStream iStr = conn.getInputStream();
				GetQueueAttributesResponse response = JAXBuddy.deserializeXMLStream(GetQueueAttributesResponse.class, iStr);
				if (response.getResponseStatus().getStatusCode().equals("Success")) {
					Map<String,String> ret = new HashMap<String,String>();
					List<AttributedValue> attrs = response.getAttributedValues();
					for (AttributedValue attr : attrs) {
						ret.put(attr.getAttribute(), attr.getValue());
					}
					return ret;
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
	 * Sets the visibility timeout of the queue. Uses {@link setQueueAttribute(String, String)}.
	 *
	 * @param timeout the duration (in seconds) the retrieved message is hidden from
	 *                          subsequent calls to retrieve.
	 */
    public void setVisibilityTimeout(int timeout) throws SQSException {
		setQueueAttribute("VisibilityTimeout", ""+timeout);
	}

	/**
	 * Sets a queue attribute. This is provided to expose the underlying functionality, although
	 * the only attribute at this time is visibility timeout.
	 *
	 * @param attribute name of the attribute being set
	 * @param value the value being set for this attribute
	 */
    public void setQueueAttribute(String attribute, String value) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("Attribute", attribute);
		params.put("Value", value);
		try {
			HttpURLConnection conn = makeRequest("GET", "SetQueueAttributes", params);
			if (conn.getResponseCode() < 400) {
				InputStream iStr = conn.getInputStream();
				SetQueueAttributesResponse response = JAXBuddy.deserializeXMLStream(SetQueueAttributesResponse.class, iStr);
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
    public Grant[] listGrants(Grantee grantee, String queueName) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (queueName != null && !queueName.trim().equals("")) {
			params.put("QueueName", queueName);
		}
		try {
			InputStream iStr =
				makeRequest("GET", "ListGrants", params).getInputStream();
			ListGrantsResponse response =
					JAXBuddy.deserializeXMLStream(ListGrantsResponse.class, iStr);
			return null;
		} catch (ArrayStoreException ex) {
			logger.error("ArrayStore problem, fetching response again to aid in debug.");
			try {
				logger.error(makeRequest("GET", "ListGrants", params).getResponseMessage());
			} catch (Exception e) {
				logger.error("Had trouble re-fetching the request response.", e);
			}
			throw new SQSException("ArrayStore problem, maybe SQS responded poorly?", ex);
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
	}
*/

	/**
	 * Overriding this because the queue name is baked into the URL and QUERY
	 * assembles the URL within the baseclass.
	 */
    protected URL makeURL(String resource) throws MalformedURLException {
		return super.makeURL(queueId+resource);
	}

	public static List<MessageQueue> createList(String [] queueUrls, String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure, String server) throws SQSException {
		ArrayList<MessageQueue> ret = new ArrayList<MessageQueue>();
		for (int i=0; i<queueUrls.length; i++) {
			ret.add(new MessageQueue(queueUrls[i], awsAccessKeyId, awsSecretAccessKey, isSecure, server));
		}
		return ret;
	}
}
