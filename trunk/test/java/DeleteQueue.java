
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xerox.amazonws.sqs.Grant;
import com.xerox.amazonws.sqs.Message;
import com.xerox.amazonws.sqs.MessageQueue;
import com.xerox.amazonws.sqs.QueueService;
import com.xerox.amazonws.sqs.SQSException;

public class DeleteQueue {
    private static Log logger = LogFactory.getLog(TestQueueService.class);

	public static void main(String [] args) throws Exception {
		Properties props = new Properties();
		props.load(TestJec2.class.getClassLoader().getResourceAsStream("aws.properties"));

		QueueService qs = new QueueService(props.getProperty("aws.accessId"), props.getProperty("aws.secretKey"));
		MessageQueue msgQueue = qs.getOrCreateMessageQueue(args[0]);
		try {
			msgQueue.deleteQueue(true);
		} catch (SQSException ex) {
			ex.printStackTrace();
		}
	}






























        final static String AWSAccessKeyId = "1SEQ6QDW2YNW8T6K64R2";
        final static String SecretAccessKey = "7P1KY+a4FTtiVBuU935NHHOI19eYrbyWG7CDklmk";
}
