
import org.apache.log4j.Logger;

import ch.inventec.Base64Coder;

import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.SQSUtils;
import com.xerox.amazonws.tools.LoggingConfigurator;

/**
 * This sample application creates a queue with the specified name (if the queue doesn't
 * already exist), and then sends (enqueues) a message to the queue.
 */
public class EnqueueSample {
	private static Logger log = LoggingConfigurator.configureLogging(EnqueueSample.class);

	public static void main( String[] args ) {
		final String AWSAccessKeyId = "[AWS Access Id]";
		final String SecretAccessKey = "[AWS Secret Key]";

		try {
			if (args.length < 2) {
				log.error("usage: EnqueueSample <queueId> <message>");
			}
			String queueName = args[0];
			String message = args[1];

			// Create the message queue object
			MessageQueue msgQueue = SQSUtils.connectToQueue(queueName, AWSAccessKeyId, SecretAccessKey);
			log.info(" url returned = "+msgQueue.getUrl());

			String msgId = msgQueue.sendMessage( Base64Coder.encodeString(message) );
			log.info( "Sent message with id " + msgId );
		} catch ( Exception ex ) {
			log.error( "EXCEPTION", ex );
		}
	}
}
