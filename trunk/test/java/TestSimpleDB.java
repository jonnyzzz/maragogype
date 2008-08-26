
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xerox.amazonws.sdb.Domain;
import com.xerox.amazonws.sdb.Item;
import com.xerox.amazonws.sdb.ItemAttribute;
import com.xerox.amazonws.sdb.ListDomainsResult;
import com.xerox.amazonws.sdb.SimpleDB;

public class TestSimpleDB {
    private static Log logger = LogFactory.getLog(TestSimpleDB.class);

	public static void main(String [] args) throws Exception {
		Properties props = new Properties();
		props.load(TestSimpleDB.class.getClassLoader().getResourceAsStream("aws.properties"));

		SimpleDB sdb = new SimpleDB(props.getProperty("aws.accessId"), props.getProperty("aws.secretKey"));
		sdb.setSignatureVersion(0);

		logger.info("domains:");
		String nextToken = "";
		while (nextToken != null) {
			ListDomainsResult result = sdb.listDomains(nextToken, 10);
			List<Domain> domains = result.getDomainList();
			for (Domain dom : domains) {
				logger.info(dom.getName());
			}
			nextToken = result.getNextToken();
		}
		Domain dom = sdb.createDomain(args[0]);
		Item i = dom.getItem("ab\u2022cd");
		List<ItemAttribute> list = new ArrayList<ItemAttribute>();
		list.add(new ItemAttribute("test1", "value1", false));
		list.add(new ItemAttribute("test1", "value2", false));
		list.add(new ItemAttribute("test1", "value3", false));
		list.add(new ItemAttribute("test1", "value4", false));
		list.add(new ItemAttribute("test1", "value5", false));
		list.add(new ItemAttribute("test1", "value6", false));
		list.add(new ItemAttribute("test1", "value7", false));
		list.add(new ItemAttribute("test1", "value8", false));
		list.add(new ItemAttribute("test2", "value9", false));
		list.add(new ItemAttribute("test2", "value10", false));
		list.add(new ItemAttribute("test2", "value11", false));
		list.add(new ItemAttribute("test2", "value12", false));
		i.putAttributes(list);

		List<ItemAttribute> attrs = i.getAttributes();
		logger.info("all");
		for (ItemAttribute attr : attrs) {
			logger.info("Item:"+attr.getName()+" Value:"+attr.getValue());
		}
		attrs = i.getAttributes("test1");
		logger.info("test1");
		for (ItemAttribute attr : attrs) {
			logger.info("Item:"+attr.getName()+" Value:"+attr.getValue());
		}
		ArrayList<String> some = new ArrayList<String>();
		some.add("test1");
		some.add("test2");
		attrs = i.getAttributes(some);
		logger.info("test1,test2");
		for (ItemAttribute attr : attrs) {
			logger.info("Item:"+attr.getName()+" Value:"+attr.getValue());
		}
		Map<String, List<String>> attrMap = i.getAttributesMap(some);
		logger.info("test1,test2 - map");
		for (String key : attrMap.keySet()) {
			for (String value : attrMap.get(key)) {
				logger.info("Item:"+key+" Value:"+value);
			}
		}
	}
}

