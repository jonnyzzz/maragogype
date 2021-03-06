
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.xerox.amazonws.simpledb.Domain;
import com.xerox.amazonws.simpledb.DomainMetadataResult;
import com.xerox.amazonws.simpledb.Item;
import com.xerox.amazonws.simpledb.ListDomainsResult;
import com.xerox.amazonws.simpledb.SelectResult;
import com.xerox.amazonws.simpledb.SDBException;
import com.xerox.amazonws.simpledb.SimpleDB;
import com.xerox.amazonws.simpledb.SimpleItemCache;

/**
 * Sample application demonstrating various operations against SDS.  
 *
 */
public class sdbShell {
	static int itemCount;

	/**
	 * Main execution body.
	 *
	 * @param args	    command line arguments (none required or processed)
	 */
    public static void main(String [] args) {
		if (args.length < 2) {
			System.err.println("usage: sdbShell <access key> <secret key> [command file]");
			System.exit(1);
		}
		try {
			String awsAccessId = args[0];
			String awsSecretKey = args[1];

			if (awsAccessId == null || awsAccessId.trim().length() == 0)
			{
				System.out.println("Access key not set");
				return;
			}

			if (awsSecretKey == null || awsSecretKey.trim().length() == 0)
			{
				System.out.println("Secret key not set");
				return;
			}
			SimpleDB sds = new SimpleDB(awsAccessId, awsSecretKey, true);
			sds.setSignatureVersion(2);

			InputStream iStr = System.in;
			if (args.length > 2) {
				iStr = new FileInputStream(args[2]);
			}
			BufferedReader rdr = new BufferedReader(new InputStreamReader(iStr));
			boolean done = false;
			Domain dom = null;
			while (!done) {
				System.out.print("sdbShell> ");
				String line = rdr.readLine();
				if (line == null) { // exit, if end of input
					System.exit(0);
				}
				StringTokenizer st = new StringTokenizer(line);
				if (st.countTokens() == 0) {
					continue;
				}
				String cmd = st.nextToken().toLowerCase();
				if (cmd.equals("q") || cmd.equals("quit")) {
					done = true;
				}
				else if (cmd.equals("h") || cmd.equals("?") || cmd.equals("help")) {
					showHelp();
				}
				else if (cmd.equals("d") || cmd.equals("domains")) {
					ListDomainsResult result = sds.listDomains();
					List<Domain> domains = result.getDomainList();
					for (Domain d : domains) {
						System.out.println(d.getName());
					}
				}
				else if (cmd.equals("ad") || cmd.equals("adddomain")) {
					if (st.countTokens() != 1) {
						System.out.println("Error: need domain name.");
						continue;
					}
					Domain d = sds.createDomain(st.nextToken());
				}
				else if (cmd.equals("dd") || cmd.equals("deletedomain")) {
					if (st.countTokens() != 1) {
						System.out.println("Error: need domain name.");
						continue;
					}
					sds.deleteDomain(st.nextToken());
				}
				else if (cmd.equals("sd") || cmd.equals("setdomain")) {
					if (st.countTokens() != 1) {
						System.out.println("Error: need domain name.");
						continue;
					}
					dom = sds.getDomain(st.nextToken());
					dom.setCacheProvider(new SimpleItemCache());
				}
				else if (cmd.equals("dm") || cmd.equals("domainmetadata")) {
					if (checkDomain(dom)) {
						DomainMetadataResult metadata = dom.getMetadata();
						System.out.println("Domain Metadata for : "+dom.getName());
						System.out.println(" ItemCount: "+metadata.getItemCount());
						System.out.println(" AttributeNameCount: "+metadata.getAttributeNameCount());
						System.out.println(" AttributeValueCount: "+metadata.getAttributeValueCount());
						System.out.println(" ItemNamesSizeBytes: "+metadata.getItemNamesSizeBytes());
						System.out.println(" AttributeNamesSizeBytes: "+metadata.getAttributeNamesSizeBytes());
						System.out.println(" AttributeValuesSizeBytes: "+metadata.getAttributeValuesSizeBytes());
						System.out.println(" Timestamp: "+metadata.getTimestamp());
					}
				}
				else if (cmd.equals("aa") || cmd.equals("addattr")) {
					if (checkDomain(dom)) {
						if (st.countTokens() < 3) {
							System.out.println("Error: need item id, attribute name and value.");
							continue;
						}
						String itemId = st.nextToken();
						Map<String, Set<String>> map = new HashMap<String, Set<String>>();
						String key = st.nextToken();
						String value = st.nextToken();
						if (line.indexOf('"') > -1) {
							value = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
						}
						HashSet<String> vals = new HashSet<String>();
						vals.add(value);
						map.put(key, vals);
						dom.addItem(itemId, map);
					}
				}
	/*
				else if (cmd.equals("ra") || cmd.equals("replaceattr")) {
					if (checkDomain(dom)) {
						if (st.countTokens() < 3) {
							System.out.println("Error: need item id, attribute name and value.");
							continue;
						}
						Item item = dom.getItem(st.nextToken());
						List<ItemAttribute> list = new ArrayList<ItemAttribute>();
						String key = st.nextToken();
						String value = st.nextToken();
						if (line.indexOf('"') > -1) {
							value = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
						}
						list.add(new ItemAttribute(key, value, true));
						item.putAttributes(list);
					}
				}
				else if (cmd.equals("da") || cmd.equals("deleteattr")) {
					if (checkDomain(dom)) {
						if (st.countTokens() != 2) {
							System.out.println("Error: need item id and attribute name.");
							continue;
						}
						Item item = dom.getItem(st.nextToken());
						List<ItemAttribute> list = new ArrayList<ItemAttribute>();
						list.add(new ItemAttribute(st.nextToken(), null, true));
						item.deleteAttributes(list);
					}
				}
*/
				else if (cmd.equals("di") || cmd.equals("deleteitem")) {
					if (checkDomain(dom)) {
						if (st.countTokens() != 1) {
							System.out.println("Error: need item id.");
							continue;
						}
						dom.deleteItem(st.nextToken());
					}
				}
				else if (cmd.equals("i") || cmd.equals("item")) {
					if (checkDomain(dom)) {
						if (st.countTokens() != 1) {
							System.out.println("Error: need item id.");
							continue;
						}
						long start = System.currentTimeMillis();
						Item item = dom.getItem(st.nextToken());
						long end = System.currentTimeMillis();
						System.out.println("Item : "+item.getIdentifier());
						for (String key : item.getAttributes().keySet()) {
							Set<String> values = item.getAttributes().get(key);
							Iterator iter = values.iterator();
							while (iter.hasNext()) {
								System.out.println("  "+key+" = "+iter.next());
							}
						}
						System.out.println("Time : "+((int)(end-start)/1000.0));
					}
				}
				else if (cmd.equals("select")) {
					if (checkDomain(dom)) {
						itemCount = 0;
//						long start = System.currentTimeMillis();
						String nextToken = null;
						try {
							do {
								SelectResult sr = dom.selectItems(line, nextToken);
								List<Item> items = sr.getItems();
								for (Item item : items) {
									System.out.println("Item : "+item.getIdentifier());
									for (String key : item.getAttributes().keySet()) {
										Set<String> values = item.getAttributes().get(key);
										Iterator iter = values.iterator();
										while (iter.hasNext()) {
											System.out.println("  "+key+" = "+iter.next());
										}
									}
									itemCount++;
								}
								nextToken = sr.getNextToken();
							} while (nextToken != null && !nextToken.trim().equals(""));
						} catch (SDBException ex) {
							System.out.println(line + " Failure: ");
							ex.printStackTrace();
						}
//						long end = System.currentTimeMillis();
//						System.out.println("Time : "+((int)(end-start)/1000.0));
//						System.out.println("Number of items returned : "+itemCount);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			if (ex.getCause() != null) {
				System.err.println("caused by : ");
				ex.getCause().printStackTrace();
			}
		}
    }

	private static boolean checkDomain(Domain dom) {
		if (dom == null) {
			System.out.println("domain must be set!");
			return false;
		}
		return true;
	}

	private static void showHelp() {
		System.out.println("SimpleDB Shell Commands:");
		System.out.println("adddomain(ad) <domain name> : add new domain");
		System.out.println("deletedomain(dd) <domain name> : delete domain (not functional in SDS yet)");
		System.out.println("domains(d) : list domains");
		System.out.println("setdomain(sd) <domain name> : set current domain");
		System.out.println("domainmetadata(dm) : show current domain metadata");
		System.out.println("addattr(aa) <item id> <attr name> <attr value> : add attribute to item in current domain");
		System.out.println("replaceattr(ra) <item id> <attr name> <attr value> : replace attribute to item in current domain");
		System.out.println("deleteattr(da) <item id> <attr name> : delete attribute of item in current domain");
		System.out.println("deleteitem(di) <item id> : delete item in current domain");
		System.out.println("item(i) <item id> : shows item attributes");
		System.out.println("select <expression> : runs a SQL like query against the domain specified");
		System.out.println("help(h,?) : show help");
		System.out.println("quit(q) : exit the shell");
	}

	private static String filter(String val) {
		if (val.length() == 0) return val;	// fast exit
		StringBuilder ret = new StringBuilder();
		char [] chars = new char[val.length()];
		val.getChars(0, val.length(), chars, 0);
		for (int i=0; i<chars.length; i++) {
			if (!(chars[i]>0 && chars[i]<128)) {
				ret.append("\\u");
				ret.append(Integer.toHexString(chars[i]));
			}
			else {
				ret.append(chars[i]);
			}
		}
		return ret.toString();
	}
}
