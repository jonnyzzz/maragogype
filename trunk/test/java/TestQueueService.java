
import java.util.List;

import org.apache.log4j.Logger;

import com.xerox.amazonws.sqs.QueueService;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.SQSException;
import com.xerox.amazonws.tools.LoggingConfigurator;

public class TestQueueService {
    private static Logger log = LoggingConfigurator.configureLogging(TestQueueService.class);

	public static void main(String [] args) throws Exception {
        final String AWSAccessKeyId = "[AWS Access Id]";
        final String SecretAccessKey = "[AWS Secret Key]";

		QueueService qs = new QueueService(AWSAccessKeyId, SecretAccessKey, false, "localhost");
		List<MessageQueue> queues = qs.listMessageQueues(null);
		for (MessageQueue queue : queues) {
			log.debug("Queue : "+queue.getUrl().toString());
			// delete queues that contain a certain phrase
			if (queue.getUrl().toString().indexOf("test")>-1) {
//				queue.deleteQueue();
			}
		}
		for (int i=0; i<args.length; i++) {
			MessageQueue mq = qs.getOrCreateMessageQueue(args[i]);
			for (int j=0; j<50; j++) {
				mq.sendMessage("Testing 1, 2, 3");
			}
			for (int j=0; j<50; j++) {
				Message msg = mq.receiveMessage();
				log.debug("Message "+(j+1)+" = "+msg.getMessageBody());
				msg = mq.peekMessage(msg.getMessageId());
				mq.deleteMessage(msg.getMessageId());
			}
		}
		// test forced deletion
		MessageQueue mq = qs.getOrCreateMessageQueue("deleteTest-12345");
		for (int j=0; j<10; j++) {	// throw 10 messages in the queue
			mq.sendMessage("Testing 1, 2, 3");
		}
		log.debug("approximate queue size = "+mq.getApproximateNumberOfMessages());
		try {
			mq.deleteQueue();
			log.error("Queue deletion was allowed, even with messages and force=false !!");
		} catch (SQSException ex) {
			log.debug("queue can't be deleted (this is exptected)");
		}
		try {
			mq.deleteQueue(true);
			log.debug("queue deleted with force=true (this is exptected)");
		} catch (SQSException ex) {
			log.error("Queue deletion failed (this is not exptected) !!");
		}
	}
}
