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

package com.xerox.amazonws.sqs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.xerox.amazonws.common.AWSQueryConnection;
import com.xerox.amazonws.typica.jaxb.AddGrantResponse;
import com.xerox.amazonws.typica.jaxb.AttributedValue;
import com.xerox.amazonws.typica.jaxb.ChangeMessageVisibilityResponse;
import com.xerox.amazonws.typica.jaxb.DeleteMessageResponse;
import com.xerox.amazonws.typica.jaxb.DeleteQueueResponse;
import com.xerox.amazonws.typica.jaxb.GetQueueAttributesResponse;
import com.xerox.amazonws.typica.jaxb.ListGrantsResponse;
import com.xerox.amazonws.typica.jaxb.PeekMessageResponse;
import com.xerox.amazonws.typica.jaxb.ReceiveMessageResponse;
import com.xerox.amazonws.typica.jaxb.RemoveGrantResponse;
import com.xerox.amazonws.typica.jaxb.SendMessageResponse;
import com.xerox.amazonws.typica.jaxb.SetQueueAttributesResponse;

/**
 * This class provides an interface with the Amazon SQS message queue. It provides methods
 * for sending / receiving messages and deleting queues and messsages on queues.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class MessageQueue extends AWSQueryConnection {
    public static final int MAX_MESSAGES = 600;
    public static final int MAX_MESSAGE_BODIES_SIZE = 4096;

	protected String queueId;
	private boolean enableEncoding = true;

    protected MessageQueue(String queueUrl, String awsAccessId,
							String awsSecretKey, boolean isSecure,
							String server) throws SQSException {
        super(awsAccessId, awsSecretKey, isSecure, server, isSecure ? 443 : 80);
		if (queueUrl.startsWith("http")) {
			queueId = queueUrl.substring(queueUrl.indexOf("//")+2);
		}
		else {
			queueId = queueUrl;	// this is the case where the queue is created from a
								// fully qualified queue name, not a full queue URL
		}
		queueId = queueId.substring(queueId.indexOf("/")+1);
		QueueService.setVersionHeader(this);
    }

	/**
	 * This method provides the URL for the message queue represented by this object.
	 *
	 * @return generated queue service url
	 */
	public URL getUrl() {
		try {
			return new URL(super.getUrl().toString());
		} catch (MalformedURLException ex) {
			return null;
		}
	}

	/**
	 * This method returns the state of the base64 encoding flag. By default, all messages
	 * are encoded on send and decoded on receive.
	 *
	 * @return state of encoding flag
	 */
	public boolean isEncoding() {
		return enableEncoding;
	}

	/**
	 * This method sets the state of the encoding flag. Use this to override the default and
	 * turn off automatic base64 encoding.
	 *
	 * @param enable the new state of the encoding flag
	 */
	public void setEncoding(boolean enable) {
		enableEncoding = enable;
	}

	/**
	 * Sends a message to a specified queue. The message must be between 1 and 256K bytes long.
	 *
	 * @param msg the message to be sent
	 */
    public String sendMessage(String msg) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		String encodedMsg = enableEncoding?new String(Base64.encodeBase64(msg.getBytes())):msg;
		PostMethod method = new PostMethod();
		try {
			method.setRequestEntity(new StringRequestEntity(encodedMsg, "text/plain", null));
			SendMessageResponse response =
					makeRequest(method, "SendMessage", params, SendMessageResponse.class);
			return response.getMessageId();
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
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
	 * @param numMessages the maximum number of messages to return
	 * @param visibilityTimeout the duration (in seconds) the retrieved message is hidden from
	 *                          subsequent calls to retrieve.
	 * @return an array of message objects
	 */
    protected Message[] receiveMessages(BigInteger numMessages, BigInteger visibilityTimeout) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		if (numMessages != null) {
			params.put("NumberOfMessages", numMessages.toString());
		}
		if (visibilityTimeout != null) {
			params.put("VisibilityTimeout", visibilityTimeout.toString());
		}
		GetMethod method = new GetMethod();
		try {
			ReceiveMessageResponse response =
					makeRequest(method, "ReceiveMessage", params, ReceiveMessageResponse.class);
			if (response.getMessages() == null) {
				return new Message[0];
			}
			else {
				ArrayList<Message> msgs = new ArrayList();
				for (com.xerox.amazonws.typica.jaxb.Message msg : response.getMessages()) {
					String decodedMsg = enableEncoding?
								new String(Base64.decodeBase64(msg.getMessageBody().getBytes())):
											msg.getMessageBody();
					msgs.add(new Message(msg.getMessageId(), decodedMsg));
				}
				return msgs.toArray(new Message [msgs.size()]);
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Returns a specified message. This does not affect and is not affected by the visibility
	 * timeout of either the queue or the message.
	 *
	 * @param msgId the id of the message to be read
	 * @return the message object
	 */
    public Message peekMessage(String msgId) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("MessageId", msgId);
		GetMethod method = new GetMethod();
		try {
			PeekMessageResponse response =
					makeRequest(method, "PeekMessage", params, PeekMessageResponse.class);
			com.xerox.amazonws.typica.jaxb.Message msg = response.getMessage();
			if (msg == null) {
				return null;
			}
			else {
				String decodedMsg = enableEncoding?
								new String(Base64.decodeBase64(msg.getMessageBody().getBytes())):
										msg.getMessageBody();
				return new Message(msg.getMessageId(), decodedMsg);
			}
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
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
		GetMethod method = new GetMethod();
		try {
			DeleteMessageResponse response =
					makeRequest(method, "DeleteMessage", params, DeleteMessageResponse.class);
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
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
		GetMethod method = new GetMethod();
		try {
			DeleteQueueResponse response =
					makeRequest(method, "DeleteQueue", params, DeleteQueueResponse.class);
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Sets the message visibility timeout. 
	 *
	 * @param msg the message
	 * @param timeout the duration (in seconds) the retrieved message is hidden from
	 *                          subsequent calls to retrieve.
	 */
    public void setVisibilityTimeout(Message msg, int timeout) throws SQSException {
		setVisibilityTimeout(msg.getMessageId(), timeout);
	}

	/**
	 * Sets the message visibility timeout. 
	 *
	 * @param msgId the id of the message
	 * @param timeout the duration (in seconds) the retrieved message is hidden from
	 *                          subsequent calls to retrieve.
	 */
    public void setVisibilityTimeout(String msgId, int timeout) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		params.put("MessageId", ""+msgId);
		params.put("VisibilityTimeout", ""+timeout);
		GetMethod method = new GetMethod();
		try {
			ChangeMessageVisibilityResponse response =
					makeRequest(method, "ChangeMessageVisibility", params, ChangeMessageVisibilityResponse.class);
		} catch (JAXBException ex) {
			throw new SQSException("Problem setting the visibility timeout.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Sets the messages' visibility timeout. 
	 *
	 * @param msgIds the ids of the messages
	 * @param timeout the duration (in seconds) the retrieved message is hidden from
	 *                          subsequent calls to retrieve.
	 */
    public void setVisibilityTimeout(String[] msgIds, int timeout) throws SQSException {
		for (String id : msgIds) {
			setVisibilityTimeout(id, timeout);
		}
	}

	/**
	 * Gets the visibility timeout for the queue. Uses {@link #getQueueAttributes(QueueAttribute)}.
	 */
    public int getVisibilityTimeout() throws SQSException {
		return Integer.parseInt(getQueueAttributes(QueueAttribute.VISIBILITY_TIMEOUT)
										.values().iterator().next());
	}

	/**
	 * Gets the visibility timeout for the queue. Uses {@link #getQueueAttributes(QueueAttribute)}.
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
		GetMethod method = new GetMethod();
		try {
			GetQueueAttributesResponse response =
					makeRequest(method, "GetQueueAttributes", params, GetQueueAttributesResponse.class);
			Map<String,String> ret = new HashMap<String,String>();
			List<AttributedValue> attrs = response.getAttributedValues();
			for (AttributedValue attr : attrs) {
				ret.put(attr.getAttribute(), attr.getValue());
			}
			return ret;
		} catch (JAXBException ex) {
			throw new SQSException("Problem getting the visilibity timeout.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Sets the visibility timeout of the queue. Uses {@link #setQueueAttribute(String, String)}.
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
		GetMethod method = new GetMethod();
		try {
			SetQueueAttributesResponse response =
					makeRequest(method, "SetQueueAttributes", params, SetQueueAttributesResponse.class);
		} catch (JAXBException ex) {
			throw new SQSException("Problem setting the visibility timeout.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Adds a grant for a specific user.
	 *
	 * @param eMailAddress the amazon address of the user
	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
	 */
    public void addGrantByEmailAddress(String eMailAddress, String permission) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		if (permission != null && !permission.trim().equals("")) {
			params.put("Permission", permission);
		}
		params.put("Grantee.EmailAddress", eMailAddress);
		addGrant(params);
	}

	/**
	 * Adds a grant for a specific user.
	 *
	 * @param id the amazon user id of the user
	 * @param displayName not sure if this can even be used
	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
	 */
    public void addGrantByCustomerId(String id, String displayName, String permission) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		if (permission != null && !permission.trim().equals("")) {
			params.put("Permission", permission);
		}
		params.put("Grantee.ID", id);
		addGrant(params);
	}

	private void addGrant(Map<String, String> params) throws SQSException {
		GetMethod method = new GetMethod();
		try {
			AddGrantResponse response =
					makeRequest(method, "AddGrant", params, AddGrantResponse.class);
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Removes a grant for a specific user.
	 *
	 * @param eMailAddress the amazon address of the user
	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
	 */
    public void removeGrantByEmailAddress(String eMailAddress, String permission) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (permission != null && !permission.trim().equals("")) {
			params.put("Permission", permission);
		}
		params.put("Grantee.EmailAddress", eMailAddress);
		removeGrant(params);
	}

	/**
	 * Removes a grant for a specific user.
	 *
	 * @param id the amazon user id of the user
	 * @param permission the permission to add (ReceiveMessage | SendMessage | FullControl)
	 */
    public void removeGrantByCustomerId(String id, String permission) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (permission != null && !permission.trim().equals("")) {
			params.put("Permission", permission);
		}
		params.put("Grantee.ID", id);
		removeGrant(params);
	}

	private void removeGrant(Map<String, String> params) throws SQSException {
		GetMethod method = new GetMethod();
		try {
			RemoveGrantResponse response =
					makeRequest(method, "RemoveGrant", params, RemoveGrantResponse.class);
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Retrieves a list of grants for this queue. The results can be filtered by specifying
	 * a grantee or a particular permission.
	 *
	 * @param grantee the optional user or group
	 * @param permission the optional permission
	 * @return a list of objects representing the grants
	 */
    public Grant[] listGrants(Grantee grantee, String permission) throws Exception {
		Map<String, String> params = new HashMap<String, String>();
		if (permission != null && !permission.trim().equals("")) {
			params.put("Permission", permission);
		}
		if (grantee instanceof CanonicalUser) {
			params.put("Grantee.ID", ((CanonicalUser)grantee).getID());
		}
		GetMethod method = new GetMethod();
		try {
			ListGrantsResponse response =
					makeRequest(method, "ListGrants", params, ListGrantsResponse.class);
			Grant [] grants = new Grant[response.getGrantLists().size()];
			int i=0;
			for (com.xerox.amazonws.typica.jaxb.Grant g : response.getGrantLists()) {
				Grantee g2 = null;
				if (g.getGrantee() instanceof com.xerox.amazonws.typica.jaxb.Group) {
					com.xerox.amazonws.typica.jaxb.Group grp =
							(com.xerox.amazonws.typica.jaxb.Group)g.getGrantee();
					g2 = new Group(new URI(grp.getURI()));
				}
				else if (g.getGrantee() instanceof com.xerox.amazonws.typica.jaxb.CanonicalUser) {
					com.xerox.amazonws.typica.jaxb.CanonicalUser u =
							(com.xerox.amazonws.typica.jaxb.CanonicalUser)g.getGrantee();
					g2 = new CanonicalUser(u.getID(), u.getDisplayName());
				}
				grants[i] = new Grant(g2, g.getPermission());
				i++;
			}
			return grants;
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (HttpException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} finally {
			method.releaseConnection();
		}
	}

	/**
	 * Overriding this because the queue name is baked into the URL and QUERY
	 * assembles the URL within the baseclass.
	 */
	protected URL makeURL(String resource) throws MalformedURLException {
		return super.makeURL(queueId+resource);
	}

	public static List<MessageQueue> createList(String [] queueUrls, String awsAccesseyId, String awsSecretKey, boolean isSecure, String server) throws SQSException {
		ArrayList<MessageQueue> ret = new ArrayList<MessageQueue>();
		for (int i=0; i<queueUrls.length; i++) {
			ret.add(new MessageQueue(queueUrls[i], awsAccesseyId, awsSecretKey, isSecure, server));
		}
		return ret;
	}
}
