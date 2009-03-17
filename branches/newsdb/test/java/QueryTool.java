
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.xerox.amazonws.simpledb.Domain;
import com.xerox.amazonws.simpledb.Item;
import com.xerox.amazonws.simpledb.SDBException;
import com.xerox.amazonws.simpledb.SelectResult;
import com.xerox.amazonws.simpledb.SimpleDB;

public class QueryTool extends JPanel implements ActionListener {
	private JFrame parent;
	private JTextArea querySpace;
	private JTabbedPane results;
	private Domain dom;

	public QueryTool(JFrame parent, String accessId, String secretKey, String domain) {
		this.parent = parent;
		layoutGUI();
		SimpleDB sdb = new SimpleDB(accessId, secretKey);
		try {
			dom = sdb.getDomain(domain);
		} catch (SDBException ex) {
			System.err.println("Could not create domain object: "+ex.getMessage());
			System.exit(-1);
		}
	}

	private void layoutGUI() {
		setLayout(new GridBagLayout());

		JButton runQuery = new JButton("run");
		runQuery.addActionListener(this);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(runQuery, gbc);

		querySpace = new JTextArea();

		results = new JTabbedPane();
		//results.add("query1", new JTextArea());

		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, querySpace, results);
		querySpace.setMinimumSize(new Dimension(100, 100));

		gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(split, gbc);
	}

	public void actionPerformed(ActionEvent evt) {
		final String query = querySpace.getText();
		final JTextArea resultSpace = new JTextArea();
		JScrollPane sp = new JScrollPane(resultSpace);
		results.add("query", sp);
		results.setSelectedComponent(sp);
		new Thread(new Runnable() {
			public void run() {
				StringBuilder resText = new StringBuilder();
				try {
					int itemCount = 0;
					long start = System.currentTimeMillis();
					String nextToken = null;
					do {
						SelectResult sr = dom.selectItems(query, nextToken);
						List<Item> items = sr.getItems();
						for (Item item : items) {
							resText.append("Item : "+item.getIdentifier()+"\n");
							for (String key : item.getAttributes().keySet()) {
								String value = item.getAttributes().get(key);
								resText.append("  "+key+" = "+value+"\n");
							}
							itemCount++;
						}
						nextToken = sr.getNextToken();
						resText.append("Box Usage :"+sr.getBoxUsage()+"\n");
						updateResults(resultSpace, resText.toString());
					} while (nextToken != null && !nextToken.trim().equals(""));
					long end = System.currentTimeMillis();
					resText.append("Time : "+((int)(end-start)/1000.0)+"\n");
					resText.append("Number of items returned : "+itemCount+"\n");
				} catch (SDBException ex) {
					resText.append(ex.getMessage());
				}
				updateResults(resultSpace, resText.toString());
			}
		}).start();
	}

	void updateResults(final JTextArea area, final String data) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				area.setText(data);
			}
		});
	}

	public static void main(String [] args) {
		final JFrame frame = new JFrame("SimpleDB Query Tool");
		final QueryTool controls = new QueryTool(frame, args[0], args[1], args[2]);
		Dimension size = controls.getPreferredSize();
		frame.setSize(600, 600);
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent event) {
					System.exit(0);
				}
			});
		frame.setContentPane(controls);
		frame.setVisible(true);
	}
}
