
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ch.inventec.Base64Coder;

import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.QueueService;
import com.xerox.amazonws.sqs.SQSUtils;

/**
 * This sample application retrieves (dequeues) a message from the queue specified by
 * the value of the queuename parameter. If successful, it deletes the message from the queue.
 * On error, it retries a number of times.
 */
public class DequeueSample {
    private static Log logger = LogFactory.getLog(DequeueSample.class);

	public static void main( String[] args ) {
//		final String AWSAccessKeyId = "[AWS Access Id]";
//		final String SecretAccessKey = "[AWS Secret Key]";
//        final String AWSAccessKeyId = "1SEQ6QDW2YNW8T6K64R2";
//        final String SecretAccessKey = "7P1KY+a4FTtiVBuU935NHHOI19eYrbyWG7CDklmk";
        final String AWSAccessKeyId = "0ZZXAZ980M9J5PPCFTR2";
        final String SecretAccessKey = "4sWhM1t3obEYOr2ZkqbcwaWozM+ayVmKfRm/1rjC";

		int count = 0;
		if (args.length < 1) {
			logger.error("usage: DequeueSample <queueId>");
		}
		String queueName = args[0];
		logger.debug("queue : "+queueName);
		try {
			// Retrieve the message queue object (by name).
			QueueService qs = new QueueService(AWSAccessKeyId, SecretAccessKey);
			MessageQueue msgQueue = qs.getOrCreateMessageQueue(queueName);

			// Try to retrieve (dequeue) the message, and then delete it.
			Message msg = null;
			while (true) {
				msg = msgQueue.receiveMessage();
				if (msg == null) {
					logger.debug("nothing... retrying");
					try { Thread.sleep(1000); } catch (Exception ex) {}
					continue;
				}
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
			logger.error("EXCEPTION, queue : "+queueName, ex );
		}
		logger.debug("Deleted "+count+" messages");
	}
}
