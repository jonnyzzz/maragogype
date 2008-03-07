
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xerox.amazonws.sqs.Grant;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.QueueService;
import com.xerox.amazonws.sqs.SQSException;

public class TestFastReceive {
    private static Log logger = LogFactory.getLog(TestFastReceive.class);

//	final String AWSAccessKeyId = "[AWS Access Id]";
//	final String SecretAccessKey = "[AWS Secret Key]";
	final String AWSAccessKeyId = "1SEQ6QDW2YNW8T6K64R2";
	final String SecretAccessKey = "7P1KY+a4FTtiVBuU935NHHOI19eYrbyWG7CDklmk";

	private final static int MSGS_TO_SEND = 400;

	private String queueName;

	public TestFastReceive(String queueName) {
		this.queueName = queueName;
	}

	public void run() {
		try {
			double test1 = test(1, 100);
			double test2 = test(10, 20);
			double test3 = test(10, 10);
			logger.info("test 1, 100 : "+test1);
			logger.info("test 10, 20 : "+test2);
			logger.info("test 10, 10 : "+test3);
		} catch (SQSException ex) {
			logger.error("There was a problem creating/getting the message queue", ex);
		}
	}

	public double test(int threads, final int msgsPerReq) throws SQSException {
		QueueService qs = new QueueService(AWSAccessKeyId, SecretAccessKey);
		final MessageQueue mq = qs.getOrCreateMessageQueue(queueName);
		for (int j=0; j<MSGS_TO_SEND; j++) {
			mq.sendMessage("Testing 1, 2, 3");
		}
		try { Thread.sleep(2000); } catch (InterruptedException ex) { }

		final Total total = new Total(0);
		final long startTime = System.currentTimeMillis();
		final int loops = MSGS_TO_SEND / (threads * msgsPerReq);
		ArrayList<Thread> receivers = new ArrayList<Thread>();
		for (int t=0; t<threads; t++) {
			Thread thr = new Thread(new Runnable() {
				public void run() {
					logger.info("thread start");
					for (int j=0; j<loops; j++) {
						try {
							Message [] msg = mq.receiveMessages(msgsPerReq);
							logger.info("read "+msg.length+" messages");
							synchronized (total) {
								total.add(msg.length);
							}
							for (int i=0; i<msg.length; i++) {
								mq.deleteMessage(msg[i]);
							}
logger.info("receive rate : "+((System.currentTimeMillis()-startTime)/1000.0)/total.value()+"seconds/msg");
						} catch (SQSException ex) {
							logger.error("problem with queue service", ex);
						}
					}
					logger.info("thread end");
				}
			});
			thr.start();
			receivers.add(thr);
		}
		for (Thread t : receivers) {
			while (true) {
				try {
					t.join();
					break;
				} catch (InterruptedException ex) {}
			}
		}
		return ((System.currentTimeMillis()-startTime)/1000.0)/total.value();
	}

	public static void main(String [] args) throws Exception {
		new TestFastReceive(args[0]).run();
	}

	public class Total {
		private int value;

		public Total(int value) {
			this.value = value;
		}

		public void add(int more) {
			value += more;
		}

		public int value() {
			return value;
		}
	}
}
