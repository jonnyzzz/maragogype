
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.inventec.Base64Coder;

import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.SQSUtils;

/**
 * This sample application retrieves (dequeues) a message from the queue specified by
 * the value of the queuename parameter. If successful, it deletes the message from the queue.
 * On error, it retries a number of times.
 */
public class DequeueSample {
    private static Log logger = LogFactory.getLog(DequeueSample.class);

	public static void main( String[] args ) {
		final String AWSAccessKeyId = "[AWS Access Id]";
		final String SecretAccessKey = "[AWS Secret Key]";

		int count = 0;
		try {
			if (args.length < 1) {
				logger.error("usage: DequeueSample <queueId>");
			}
			String queueName = args[0];

			// Retrieve the message queue object (by name).
			MessageQueue msgQueue = SQSUtils.connectToQueue(queueName, AWSAccessKeyId, SecretAccessKey);

			// Try to retrieve (dequeue) the message, and then delete it.
			Message msg = null;
			while ((msg = msgQueue.receiveMessage()) != null) {
				String text = msg.getMessageBody();
				try {
					text = Base64Coder.decodeString(text);
				} catch (IllegalArgumentException ex) {
					logger.warn("Message wasn't base64 encoded.");
				}
				logger.debug("msg : "+text);
				msgQueue.deleteMessage(msg);
				logger.info( "Deleted message id " + msg.getMessageId());
				count++;
			}
		} catch ( Exception ex ) {
			logger.error( "EXCEPTION", ex );
		}
		logger.debug("Deleted "+count+" messages");
	}
}
