
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xerox.amazonws.common.Result;
import com.xerox.amazonws.sns.NotificationService;

// args : 

public class TestSNS {
    private static Log logger = LogFactory.getLog(TestSNS.class);

	public static void main(String [] args) throws Exception {
		Properties props = new Properties();
		props.load(TestSNS.class.getClassLoader().getResourceAsStream("aws.properties"));

		NotificationService sns = new NotificationService(props.getProperty("aws.accessId"), props.getProperty("aws.secretKey"));
		Result<String> ret = sns.createTopic("TestTopic");
		String topicArn = ret.getResult();
		System.err.println("topicArn: "+topicArn);

		sns.deleteTopic(topicArn);
	}
}

