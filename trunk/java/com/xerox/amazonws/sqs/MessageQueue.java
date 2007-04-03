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

import com.xerox.amazonws.jaxb.DeleteMessageResponse;
import com.xerox.amazonws.jaxb.DeleteQueueResponse;
import com.xerox.amazonws.jaxb.PeekMessageResponse;
import com.xerox.amazonws.jaxb.ReceiveMessageResponse;
import com.xerox.amazonws.jaxb.SendMessageResponse;

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

	public URL getUrl() {
		try {
			return new URL(super.getUrl().toString()+queueId);
		} catch (MalformedURLException ex) {
			return null;
		}
	}

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

    public Message receiveMessage() throws SQSException {
        Message amessage[] = receiveMessages(BigInteger.valueOf(1L), ((BigInteger) (null)));
        if(amessage.length > 0)
            return amessage[0];
        else
            return null;
	}

    public Message receiveMessage(int visibilityTimeout) throws SQSException {
        Message amessage[] = receiveMessages(BigInteger.valueOf(1L), BigInteger.valueOf(visibilityTimeout));
        if(amessage.length > 0)
            return amessage[0];
        else
            return null;
	}

    public Message[] receiveMessages(int numMessages) throws SQSException {
        return receiveMessages(BigInteger.valueOf(numMessages), ((BigInteger) (null)));
	}

    public Message[] receiveMessages(int numMessages, int visibilityTimeout) throws SQSException {
        return receiveMessages(BigInteger.valueOf(numMessages), BigInteger.valueOf(visibilityTimeout));
	}

    protected Message[] receiveMessages(BigInteger numMessages, BigInteger visibilityTimeout) throws SQSException {
		try {
			String request = queueId+"/front?NumberOfMessages="+numMessages;
			if (visibilityTimeout != null) request += "&VisibilityTimeout="+visibilityTimeout;
			HttpURLConnection conn = makeRequest("GET", request, super.headers);
			if (conn.getResponseCode() < 400) {
				InputStream iStr = conn.getInputStream();
				ReceiveMessageResponse response = JAXBuddy.deserializeXMLStream(ReceiveMessageResponse.class, iStr);
				if (response.getMessage() == null) {
					return new Message[0];
				}
				else {
					ArrayList<Message> msgs = new ArrayList();
					for (com.xerox.amazonws.jaxb.Message msg : response.getMessage()) {
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

    public Message peekMessage(String msgId) throws SQSException {
		try {
			InputStream iStr = makeRequest("GET", queueId+"/"+msgId, super.headers).getInputStream();
			PeekMessageResponse response = JAXBuddy.deserializeXMLStream(PeekMessageResponse.class, iStr);
			com.xerox.amazonws.jaxb.Message msg = response.getMessage();
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

    public int getVisibilityTimeout() throws SQSException {
		return 0;
	}

    public void setVisibilityTimeout(int timeout) throws SQSException {
	}

    public void setVisibilityTimeout(String msgId, int timeout) throws SQSException {
	}

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
