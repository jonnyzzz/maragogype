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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;

import com.xerox.amazonws.common.AWSAuthConnection;
import com.xerox.amazonws.common.JAXBuddy;
import com.xerox.amazonws.typica.jaxb.CreateQueueResponse;
import com.xerox.amazonws.typica.jaxb.ListQueuesResponse;
import com.xerox.amazonws.tools.LoggingConfigurator;

/**
 * This class provides an interface with the Amazon SQS service. It provides high level
 * methods for listing and creating message queues.
 *
 * Http authentication code borrowed from Amazon S3 AWSAuthConnection code
 * (see amazon copyright below).
 *
 * @author D. Kavanagh
 * @author developer@dotech.com
 */
public class QueueService extends AWSAuthConnection {

    private static Logger logger = LoggingConfigurator.configureLogging(QueueService.class);

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
		super.headers.put("AWS-Version", vals);
    }

	/**
	 * Creates a new message queue. The queue name must be unique within the scope of the
	 * queues you own.
	 *
	 * @param queueName name of queue to be created
	 * @return object representing the message queue
	 */
    public MessageQueue getOrCreateMessageQueue(String queueName) throws SQSException {
		try {
			InputStream iStr =
					makeRequest("POST", "?QueueName="+queueName, this.headers).getInputStream();
			CreateQueueResponse response =
					JAXBuddy.deserializeXMLStream(CreateQueueResponse.class, iStr);
			return new MessageQueue(response.getQueueUrl(),
								getAwsAccessKeyId(), getSecretAccessKey(),
								isSecure(), getServer());
		} catch (JAXBException ex) {
			throw new SQSException("Problem parsing returned message.", ex);
		} catch (MalformedURLException ex) {
			throw new SQSException(ex.getMessage(), ex);
		} catch (IOException ex) {
			throw new SQSException(ex.getMessage(), ex);
		}
    }

	/**
	 * Retrieves a list of message queues. A maximum of 10,000 queue URLs are returned.
	 * If a value is specified for the optional queueNamePrefix parameter, only those queues
	 * with a queue name beginning with the value specified are returned. The queue name is
	 * specified in the QueueName parameter when a queue is created.
	 *
	 * @param queueNamePrefix the optional prefix for filtering results. can be null.
	 * @return a list of objects representing the message queues defined for this account
	 */
    public List<MessageQueue> listMessageQueues(String queueNamePrefix) throws SQSException {
		try {
			InputStream iStr =
				makeRequest("GET", (queueNamePrefix!=null)?("?QueueNamePrefix="+queueNamePrefix):"",
									this.headers).getInputStream();
			ListQueuesResponse response =
					JAXBuddy.deserializeXMLStream(ListQueuesResponse.class, iStr);
			return MessageQueue.createList(response.getQueueUrls().toArray(new String[] {}),
								getAwsAccessKeyId(), getSecretAccessKey(),
								isSecure(), getServer());
		} catch (ArrayStoreException ex) {
			logger.error("ArrayStore problem, fetching response again to aid in debug.");
			try {
				logger.error(makeRequest("GET", (queueNamePrefix!=null)?("?QueueNamePrefix="+queueNamePrefix):"", this.headers).getResponseMessage());
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
}
