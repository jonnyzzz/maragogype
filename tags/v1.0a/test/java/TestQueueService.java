
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xerox.amazonws.sqs.Grant;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.QueueService;
import com.xerox.amazonws.sqs.SQSException;

public class TestQueueService {
    private static Log logger = LogFactory.getLog(TestQueueService.class);

	public static void main(String [] args) throws Exception {
		final String AWSAccessKeyId = "[AWS Access Id]";
		final String SecretAccessKey = "[AWS Secret Key]";

		QueueService qs = new QueueService(AWSAccessKeyId, SecretAccessKey, false, "localhost");
		List<MessageQueue> queues = qs.listMessageQueues(null);
		for (MessageQueue queue : queues) {
			logger.info("Queue : "+queue.getUrl().toString());
			// delete queues that contain a certain phrase
			if (queue.getUrl().toString().indexOf("input")>-1) {
//				try {
//					queue.deleteQueue(true);
//				} catch (SQSException ex) {
//					ex.printStackTrace();
//				}
			}
		}
		for (int i=0; i<args.length; i++) {
			MessageQueue mq = qs.getOrCreateMessageQueue(args[i]);
/* test send/receive
			for (int j=0; j<5; j++) {
				mq.sendMessage("Testing 1, 2, 3");
			}
*/
			for (int j=0; j<5; j++) {
				Message msg = mq.receiveMessage();
				if (msg == null) { continue; }
				logger.info("Message "+(j+1)+" = "+msg.getMessageBody());
				msg = mq.peekMessage(msg.getMessageId());
				mq.deleteMessage(msg.getMessageId());
			}
/* test grants
			logger.info("Grants for "+mq.getUrl());
			Grant [] grants = mq.listGrants(null, null);
			for (Grant g : grants) {
				logger.info("grant : "+g.getGrantee()+" perm : "+g.getPermission());
			}
			logger.info("Adding Grant");
			mq.addGrantByEmailAddress("dak@directthought.com", "ReceiveMessage");
			logger.info("Grants for "+mq.getUrl());
			grants = mq.listGrants(null, null);
			for (Grant g : grants) {
				logger.info("grant : "+g.getGrantee()+" perm : "+g.getPermission());
			}
*/
			/*
			logger.info("Removing Grant");
			mq.removeGrantByEmailAddress("xrxs33@gmail.com", "ReceiveMessage");
			logger.info("Grants for "+mq.getUrl());
			grants = mq.listGrants(null, null);
			for (Grant g : grants) {
				logger.info("grant : "+g.getGrantee()+" perm : "+g.getPermission());
			}
			*/
		}
		/* test forced deletion
		MessageQueue mq = qs.getOrCreateMessageQueue("deleteTest-12345");
		for (int j=0; j<10; j++) {	// throw 10 messages in the queue
			mq.sendMessage("Testing 1, 2, 3");
		}
		logger.info("approximate queue size = "+mq.getApproximateNumberOfMessages());
		try {
			mq.deleteQueue();
			logger.error("Queue deletion was allowed, even with messages and force=false !!");
		} catch (SQSException ex) {
			logger.info("queue can't be deleted (this is exptected)");
		}
		try {
			mq.deleteQueue(true);
			logger.info("queue deleted with force=true (this is exptected)");
		} catch (SQSException ex) {
			logger.error("Queue deletion failed (this is not exptected) !!");
		}
		*/
	}
}
