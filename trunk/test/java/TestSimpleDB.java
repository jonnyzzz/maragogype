
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xerox.amazonws.sdb.Domain;
import com.xerox.amazonws.sdb.ListDomainsResult;
import com.xerox.amazonws.sdb.SimpleDB;

public class TestSimpleDB {
    private static Log logger = LogFactory.getLog(TestSimpleDB.class);

	public static void main(String [] args) throws Exception {
		final String AWSAccessKeyId = "[AWS Access Id]";
		final String SecretAccessKey = "[AWS Secret Key]";

		SimpleDB sdb = new SimpleDB(AWSAccessKeyId, SecretAccessKey, true);

		logger.info("domains:");
		String moreToken = "";
		while (moreToken != null) {
			ListDomainsResult result = sdb.listDomains(moreToken, 10);
			List<Domain> domains = result.getDomainList();
			for (Domain dom : domains) {
				logger.info(dom.getName());
			}
			moreToken = result.getMoreToken();
		}
	}
}

