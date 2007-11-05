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

import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;

import com.xerox.amazonws.common.AWSQueryConnection;
import com.xerox.amazonws.typica.jaxb.CreateQueueResponse;
import com.xerox.amazonws.typica.jaxb.ListQueuesResponse;

/**
 * This class provides an interface with the Amazon SQS service. It provides high level
 * methods for listing and creating message queues.
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class QueueService extends AWSQueryConnection {

    private static Log logger = LogFactory.getLog(QueueService.class);

	/**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
	 */
    public QueueService(String awsAccessId, String awsSecretAccessKey) {
        this(awsAccessId, awsSecretAccessKey, true);
    }

	/**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
	 */
    public QueueService(String awsAccessId, String awsSecretAccessKey, boolean isSecure) {
        this(awsAccessId, awsSecretAccessKey, isSecure, "queue.amazonaws.com");
    }

	/**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
     * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
	 */
    public QueueService(String awsAccessId, String awsSecretAccessKey, boolean isSecure,
                             String server)
    {
        this(awsAccessId, awsSecretAccessKey, isSecure, server,
             isSecure ? 443 : 80);
    }

    /**
	 * Initializes the queue service with your AWS login information.
	 *
     * @param awsAccessId The your user key into AWS
     * @param awsSecretKey The secret string used to generate signatures for authentication.
     * @param isSecure True if the data should be encrypted on the wire on the way to or from SQS.
     * @param server Which host to connect to.  Usually, this will be s3.amazonaws.com
     * @param port Which port to use.
     */
    public QueueService(String awsAccessKeyId, String awsSecretAccessKey, boolean isSecure,
                             String server, int port)
    {
		super(awsAccessKeyId, awsSecretAccessKey, isSecure, server, port);
		ArrayList vals = new ArrayList();
		vals.add("2007-05-01");
		super.headers.put("Version", vals);
    }

	/**
	 * Creates a new message queue. The queue name must be unique within the scope of the
	 * queues you own. Optionaly, you can supply a queue that might be one that belongs to
	 * another user that has granted you access to the queue. In that case, supply the fully
	 * qualified queue name (i.e. "/A98KKI3K0RJ7Q/grantedQueue").
	 *
	 * @param queueName name of queue to be created
	 * @return object representing the message queue
	 */
    public MessageQueue getOrCreateMessageQueue(String queueName) throws SQSException {
		// test for case where the user supplied the fully qualified queue name
		if (queueName.charAt(0) == '/' && queueName.lastIndexOf('/') > 0) {
			return new MessageQueue(queueName,
								getAwsAccessKeyId(), getSecretAccessKey(),
								isSecure(), getServer());
		}
		else {
			Map<String, String> params = new HashMap<String, String>();
			params.put("QueueName", queueName);
			GetMethod method = new GetMethod();
			try {
				CreateQueueResponse response =
						makeRequest(method, "CreateQueue", params, CreateQueueResponse.class);
				return new MessageQueue(response.getQueueUrl(),
									getAwsAccessKeyId(), getSecretAccessKey(),
									isSecure(), getServer());
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
    }

	/**
	 * Retrieves a list of message queues. A maximum of 1,000 queue URLs are returned.
	 * If a value is specified for the optional queueNamePrefix parameter, only those queues
	 * with a queue name beginning with the value specified are returned. The queue name is
	 * specified in the QueueName parameter when a queue is created.
	 *
	 * @param queueNamePrefix the optional prefix for filtering results. can be null.
	 * @return a list of objects representing the message queues defined for this account
	 */
    public List<MessageQueue> listMessageQueues(String queueNamePrefix) throws SQSException {
		Map<String, String> params = new HashMap<String, String>();
		if (queueNamePrefix != null && !queueNamePrefix.trim().equals("")) {
			params.put("QueueNamePrefix", queueNamePrefix);
		}
		GetMethod method = new GetMethod();
		try {
			ListQueuesResponse response =
				makeRequest(method, "ListQueues", params, ListQueuesResponse.class);
			return MessageQueue.createList(response.getQueueUrls().toArray(new String[] {}),
								getAwsAccessKeyId(), getSecretAccessKey(),
								isSecure(), getServer());
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
}
