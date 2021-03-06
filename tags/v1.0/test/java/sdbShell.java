
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.xerox.amazonws.sdb.Domain;
import com.xerox.amazonws.sdb.Item;
import com.xerox.amazonws.sdb.ItemAttribute;
import com.xerox.amazonws.sdb.ListDomainsResult;
import com.xerox.amazonws.sdb.QueryResult;
import com.xerox.amazonws.sdb.SDBException;
import com.xerox.amazonws.sdb.SimpleDB;

/**
 * Sample application demonstrating various operations against SDS.  
 *
 */
public class sdbShell {

	/**
	 * Executes specified query against given domain while demonstrating pagination.
	 *
	 * @param domain	    query domain 
	 * @param queryString	query string
	 * @param maxResults	maximum number of values to return per page of results
	 */
	private static void executeQuery(Domain domain, String queryString, int maxResults) {

        String nextToken = "";
        do {
            try {
                QueryResult result = domain.listItems(queryString, nextToken, maxResults);
				List<Item> items = result.getItemList();
                for (Item i : items) {
                    System.out.println(i.getIdentifier());
                }
                System.out.println("--- end of page ---");
                nextToken = result.getNextToken();
            }
            catch (SDBException ex) {
                System.out.println("Query '" + queryString + "' Failure: ");
                ex.printStackTrace();
            }
        } while (nextToken != null && nextToken.trim().length() > 0);
        System.out.println("Done.");
	}

	/**
	 * Executes specified query against given domain.
	 *
	 * @param domain	    query domain 
	 * @param queryString	query string
	 */
	private static void executeQuery(Domain domain, String queryString) {
        executeQuery(domain, queryString, 0);
    }

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
				}
				else if (cmd.equals("aa") || cmd.equals("addattr")) {
					if (checkDomain(dom)) {
						if (st.countTokens() != 3) {
							System.out.println("Error: need item id, attribute name and value.");
							continue;
						}
						Item item = dom.getItem(st.nextToken());
						List<ItemAttribute> list = new ArrayList<ItemAttribute>();
						list.add(new ItemAttribute(st.nextToken(), st.nextToken(), true));
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
						Item item = dom.getItem(st.nextToken());
						List<ItemAttribute> attrs = item.getAttributes(null);
						System.out.println("Item : "+item.getIdentifier());
						for (ItemAttribute attr : attrs) {
							System.out.println(" "+attr.getName()+" = "+attr.getValue());
						}
					}
				}
				else if (cmd.equals("l") || cmd.equals("list")) {
					if (checkDomain(dom)) executeQuery(dom, null);
				}
				else {
					if (checkDomain(dom)) executeQuery(dom, line);
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
		System.out.println("SDS Shell Commands:");
		System.out.println("adddomain(ad) <domain name> : add new domain");
		System.out.println("deletedomain(dd) <domain name> : delete domain (not functional in SDS yet)");
		System.out.println("domains(d) : list domains");
		System.out.println("setdomain(sd) <domain name> : set current domain");
		System.out.println("addattr(aa) <item id> <attr name> <attr value> : add attribute to item in current domain");
		System.out.println("deleteattr(da) <item id> <attr name> : delete attribute of item in current domain");
		System.out.println("deleteitem(di) <item id> : delete item in current domain");
		System.out.println("list(l) or <filter string> : lists items matching filter in current domain");
		System.out.println("item(i) <item id> : shows item attributes");
		System.out.println("help(h,?) : show help");
		System.out.println("quit(q) : exit the shell");
	}
}
