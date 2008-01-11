
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xerox.amazonws.sqs.QueueService;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.MessageQueue;

public class TestVisibility {
    private static Log logger = LogFactory.getLog(TestVisibility.class);

	public static void main(String [] args) throws Exception {
        final String AWSAccessKeyId = "[AWS Access Id]";
        final String SecretAccessKey = "[AWS Access Id]";

		QueueService qs = new QueueService(AWSAccessKeyId, SecretAccessKey);
		MessageQueue mq = qs.getOrCreateMessageQueue(args[0]);
		int timeout = mq.getVisibilityTimeout();
		logger.debug("Queue timeout = "+timeout);
		mq.sendMessage("Testing 1, 2, 3");
		try { Thread.sleep(5000); } catch (InterruptedException ex) {}
		Message msg = mq.receiveMessage();
		logger.debug("Message = "+msg.getMessageBody());
		int i=0;
		long start = System.currentTimeMillis();
		while ((msg = mq.receiveMessage()) == null) {
			logger.debug(".");
			try { Thread.sleep(1000); } catch (InterruptedException ex) {}
			i++;
		}
		long end = System.currentTimeMillis();
		logger.debug("Message was invisible for "+(end-start)/1000.0+" seconds");
		mq.deleteMessage(msg.getMessageId());

		// test queue visibility
		logger.debug("setting timeout to 10 seconds.");
		mq.setVisibilityTimeout(10);
		mq.sendMessage("Testing 1, 2, 3");
		try { Thread.sleep(5000); } catch (InterruptedException ex) {}
		msg = mq.receiveMessage();
		logger.debug("Message = "+msg.getMessageBody());
		i=0;
		start = System.currentTimeMillis();
		while ((msg = mq.receiveMessage()) == null) {
			logger.debug(".");
			try { Thread.sleep(1000); } catch (InterruptedException ex) {}
			i++;
		}
		end = System.currentTimeMillis();
		logger.debug("Message was invisible for "+(end-start)/1000.0+" seconds");
		mq.deleteMessage(msg.getMessageId());

		// test change message visibility
		logger.debug("setting timeout to 10 seconds.");
		mq.setVisibilityTimeout(10);
		String msgId = mq.sendMessage("Testing 1, 2, 3");
		try { Thread.sleep(5000); } catch (InterruptedException ex) {}
		msg = mq.receiveMessage();
		logger.debug("Message = "+msg.getMessageBody());
		i=0;
		start = System.currentTimeMillis();
		while ((msg = mq.receiveMessage()) == null) {
			logger.debug(".");
			if (i == 4) {
				logger.debug("change timeout to 60 seconds.");
				mq.setVisibilityTimeout(msgId, 60);
			}
			try { Thread.sleep(1000); } catch (InterruptedException ex) {}
			i++;
		}
		end = System.currentTimeMillis();
		logger.debug("Message was invisible for "+(end-start)/1000.0+" seconds");
		mq.deleteMessage(msg.getMessageId());

		// test receive visibility
		mq.sendMessage("Testing 1, 2, 3");
		try { Thread.sleep(5000); } catch (InterruptedException ex) {}
		msg = mq.receiveMessage(30);
		logger.debug("Message = "+msg.getMessageBody());
		i=0;
		start = System.currentTimeMillis();
		while ((msg = mq.receiveMessage()) == null) {
			logger.debug(".");
			try { Thread.sleep(1000); } catch (InterruptedException ex) {}
			i++;
		}
		end = System.currentTimeMillis();
		logger.debug("Message was invisible for "+(end-start)/1000.0+" seconds");
		mq.deleteMessage(msg.getMessageId());
		// reset queue timeout
		mq.setVisibilityTimeout(timeout);
	}
}
