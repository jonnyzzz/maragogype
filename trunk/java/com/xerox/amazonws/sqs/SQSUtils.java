package com.xerox.amazonws.sqs;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * This class provides helper methods to interact with the Amazon Simple Queue Service.
 */
public class SQSUtils {
    private static final Logger logger = Logger.getLogger(SQSUtils.class.getName());
    
	public static MessageQueue connectToQueue(String queueName, String accessKey, String secretKey) 
			throws SQSException {
		return connectToQueue(null, queueName, accessKey, secretKey);
	}

	public static MessageQueue connectToQueue(String serverName, String queueName, String accessKey, String secretKey) 
			throws SQSException {
		// Create the service object
		QueueService service = getQueueService(accessKey, secretKey, serverName);
		 
		// Retrieve the message queue object (by name).
		return getMessageQueue(service, queueName);
	}

	/**
	 * Create a QueueService object for a given URL.
	 */
	public static QueueService getQueueService(String accessKey, String secretKey, String serverName) 
			throws SQSException {
		QueueService service = null;
		if (serverName != null) {
			service = new QueueService(accessKey, secretKey, true, serverName);
		}
		else {
			service = new QueueService(accessKey, secretKey);
		}
		if (service.getUrl() != null) {
			logger.info( "Service: " + service.getUrl().toString() );
		} else {
			logger.error( "Service: null url!" );
		}
		return service;
	}
	
	
	/**
	 * Looks for a queue by name: if found, return a MessageQuueue object for it.
	 * Else, return null.
	 */
	public static MessageQueue getMessageQueue(QueueService service, String msgQueueName)
			throws SQSException {
		MessageQueue msgQueue = null;
		MessageQueue msgQueueFound = null;
		List<MessageQueue> msgQueuesFound = service.listMessageQueues( null );
		for ( MessageQueue mq : msgQueuesFound ) {
			if ( mq.getUrl().toString().endsWith( msgQueueName ) ) {
				msgQueueFound = mq;
			}
		}
		if (msgQueueFound == null) {
			logger.info("Message queue couldn't be listed, going to create it.");
			msgQueue = service.getOrCreateMessageQueue(msgQueueName.substring(msgQueueName.lastIndexOf("/")+1));
		} else if (msgQueue == null) {
			msgQueue = msgQueueFound;
		}
		if (msgQueue == null) {
			logger.error( "Couldn't find message queue " + msgQueueName);
		} else {
			logger.info( "Using message queue resource at " + msgQueue.getUrl() ); 
		}
		return msgQueue;
	}
}
