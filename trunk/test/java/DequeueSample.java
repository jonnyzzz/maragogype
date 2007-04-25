
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import ch.inventec.Base64Coder;

import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.QueueService;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.SQSUtils;
import com.xerox.amazonws.tools.LoggingConfigurator;

/**
 * This sample application retrieves (dequeues) a message from the queue specified by the value of the queuename parameter.
 * If successful, it deletes the message from the queue.
 * On error, it retries a number of times.
 */
public class DequeueSample {
    private static Logger log = LoggingConfigurator.configureLogging(DequeueSample.class);

    //------------------------------------------------------------------------
    public static void main( String[] args ) {

        final String AWSAccessKeyId = "1SEQ6QDW2YNW8T6K64R2";
        final String SecretAccessKey = "7P1KY+a4FTtiVBuU935NHHOI19eYrbyWG7CDklmk";

		int count = 0;
        try {
            if (args.length < 1) {
                log.error("usage: DequeueSample <queueId>");
            }
            String queueName = args[0];
			String show = (args.length>2)?args[1]:null;
			boolean showMsg = false;
			if (show != null && show.equals("show")) {
				showMsg = true;
			}

            // Retrieve the message queue object (by name).
            MessageQueue msgQueue = SQSUtils.connectToQueue(queueName, AWSAccessKeyId, SecretAccessKey);
            if (msgQueue == null) {
                System.exit(1);
            }

            // Try to retrieve (dequeue) the message, and then delete it.
			Message [] msg = null;
			//while ((msg = msgQueue.receiveMessage()) != null) {
			while (true) {
				msg = msgQueue.receiveMessages(1);
                if ( msg == null || msg.length == 0 ) {
                    // Sleep for 1 second before the next try
                    log.info("Nothing to read, sleep a second and retry");
                    Thread.sleep(1000);
                } else {                    
//                    msgQueue.setVisibilityTimeout( msg[0].getMessageId(), 60 );
					if (showMsg) {
                       	String text = msg[0].getMessageBody();
						try {
                       		text = Base64Coder.decodeString(text);
						} catch (IllegalArgumentException ex) {
							log.warn("Message wasn't base64 encoded.");
						}
// un-comment these lines and comment out the delete line down lower
// to delete select messages including some specifc content
//						if (text.indexOf("1003") > -1) {
//		                    msgQueue.deleteMessage( msg[0].getMessageId() );
//							log.debug("Deleted message");
//						}
						log.debug("msg : "+text);
					}
                    msgQueue.deleteMessage( msg[0].getMessageId() );
                    log.info( "Deleted message id " + msg[0].getMessageId());
					count++;
                }
            }
        } catch ( Exception ex ) {
            log.error( "EXCEPTION", ex );
        }
		log.debug("Deleted "+count+" messages");
    }
}
