
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;

import com.xerox.amazonws.simpledb.Domain;
import com.xerox.amazonws.simpledb.DomainMetadataResult;
import com.xerox.amazonws.simpledb.Item;
import com.xerox.amazonws.simpledb.ListDomainsResult;
import com.xerox.amazonws.simpledb.SDBException;
import com.xerox.amazonws.simpledb.SelectResult;
import com.xerox.amazonws.simpledb.SimpleDB;

public class QueryTool extends JPanel implements ActionListener {
	private JFrame parent;
	private JComboBox domainList;
	private JTextArea querySpace;
	private JDesktopPane results;
	private SimpleDB sdb;
	private Domain dom;

	public QueryTool(JFrame parent, String accessId, String secretKey) {
		this.parent = parent;
		sdb = new SimpleDB(accessId, secretKey);
		layoutGUI();
		loadPrefs();
	}
	
	public void shutdown() {
		savePrefs();
	}

	public void setDomain(String name) {
		try {
			dom = sdb.getDomain(name);
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
		gbc.anchor = GridBagConstraints.WEST;
		add(runQuery, gbc);

		String [] domainNames = new String [] {"no domains found"};
		try {
			ListDomainsResult list = sdb.listDomains();
			ArrayList<String> tmp = new ArrayList<String>();
			for (Domain d : list.getDomainList()) {
				tmp.add(d.getName());
			}
			domainNames = tmp.toArray(domainNames);
		} catch (SDBException ex) {
			System.err.println("problem communicating with SimpleDB: "+ex.getMessage());
		}
		domainList = new JComboBox(domainNames);
		domainList.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setDomain((String)domainList.getSelectedItem());
			}
		});
		gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.EAST;
		gbc.weightx = 1.0;
		add(domainList, gbc);

		JButton metadata = new JButton("Get Metadata");
		metadata.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					DomainMetadataResult dm = dom.getMetadata();
					StringBuilder dmOutput = new StringBuilder();
					dmOutput.append("Item Count : ");
					dmOutput.append(dm.getItemCount());
					dmOutput.append("\nAttr Name Count : ");
					dmOutput.append(dm.getAttributeNameCount());
					dmOutput.append("\nAttr Value Count : ");
					dmOutput.append(dm.getAttributeValueCount());
					dmOutput.append("\nItem Names Size : ");
					dmOutput.append(dm.getItemNamesSizeBytes());
					dmOutput.append("\nAttribute Names Size : ");
					dmOutput.append(dm.getAttributeNamesSizeBytes());
					dmOutput.append("\nAttribute Value Size : ");
					dmOutput.append(dm.getAttributeValuesSizeBytes());
					JOptionPane.showInternalMessageDialog(results, dmOutput.toString(),
								dom.getName()+" metadata",
								JOptionPane.PLAIN_MESSAGE);
				} catch (SDBException ex) {
					System.err.println("Problem fetching metadata : "+ex.getMessage());
				}
			}
		});
		gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		add(metadata, gbc);

		querySpace = new JTextArea();

		results = new JDesktopPane();

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
		try {
			int lineNum = querySpace.getLineOfOffset(querySpace.getCaretPosition())+1;
			StringTokenizer st = new StringTokenizer(querySpace.getText(), "\n", true);
			int lineCount = 0;
			String val = "";
			while (st.hasMoreTokens()) {
				String tok = st.nextToken();
				if (tok.equals("\n")) {
					lineCount++;
				}
				else {
					val = tok;
				}
				if (lineCount == lineNum) break;
			}
			final String query = val;
			final ResultsFrame resultFrame = new ResultsFrame(query);

			results.add(resultFrame, 1);
			try {
				resultFrame.setSelected(true);
			} catch (java.beans.PropertyVetoException ex) { }
			resultFrame.show();

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
							nextToken = sr.getNextToken();
							itemCount += items.size();
							updateResults(resultFrame, items);
							updateBoxUsage(resultFrame, sr.getBoxUsage());
							updateItemCount(resultFrame, itemCount);
							if (itemCount > 1000) {
								nextToken = null;
								ArrayList<Item> trunc = new ArrayList<Item>();
								trunc.add(dom.getItem("- truncated -"));
								updateResults(resultFrame, trunc);
							}
						} while (nextToken != null && !nextToken.trim().equals(""));
						long end = System.currentTimeMillis();
						updateTime(resultFrame, ((int)(end-start)/1000.0)+" seconds");
					} catch (SDBException ex) {
						resText.append(ex.getMessage());
					}
					//updateResults(resultSpace, resText.toString());
				}
			}).start();
		} catch (BadLocationException ex) {
		}
	}

	void updateResults(final ResultsFrame resultsFrame, final List<Item> data) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resultsFrame.addItems(data);
			}
		});
	}

	void updateBoxUsage(final ResultsFrame resultsFrame, final String boxUsage) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resultsFrame.addBoxUsage(boxUsage);
			}
		});
	}

	void updateTime(final ResultsFrame resultsFrame, final String time) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resultsFrame.setTime(time);
			}
		});
	}

	void updateItemCount(final ResultsFrame resultsFrame, final int count) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				resultsFrame.addItemCount(count);
			}
		});
	}

	private void loadPrefs() {
		Properties props = new Properties();
		try {
			props.load(new FileInputStream(System.getProperty("user.home", ".")+"/.typica.query.prefs"));
			querySpace.setText(props.getProperty("query.history"));
			domainList.setSelectedItem(props.getProperty("query.domain"));
		} catch (FileNotFoundException ioex) {
			// ignore.... might not be a file yet
		} catch (IOException ioex) {
			System.err.println("Error loading user preferences");
		}
	}

	private void savePrefs() {
		Properties props = new Properties();
		props.setProperty("query.history", querySpace.getText());
		props.setProperty("query.domain", (String)domainList.getSelectedItem());
		try {
			props.store(new FileOutputStream(System.getProperty("user.home", ".")+"/.typica.query.prefs"),
						"http://code.google.com/p/typica");
		} catch (IOException ioex) {
			System.err.println("Error saving user preferences");
		}
	}

	public static void main(String [] args) {
		final JFrame frame = new JFrame("SimpleDB Query Tool");
		final QueryTool controls = new QueryTool(frame, args[0], args[1]);
		Dimension size = controls.getPreferredSize();
		frame.setSize(600, 600);
		frame.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent event) {
					controls.shutdown();
					System.exit(0);
				}
			});
		frame.setContentPane(controls);
		frame.setVisible(true);
	}

	// a table model that shows items from SimpleDB
	public class ItemTableModel extends AbstractTableModel {
		private ArrayList<String> columns;
		private ArrayList<Item> items;

		public ItemTableModel() {
			columns = new ArrayList<String>();
			columns.add("itemName()");
			items = new ArrayList<Item>();
		}

		public void addItems(List<Item> newItems) {
			// ensure all attrs have a column
			int firstRow = items.size();
			boolean colsChanged = false;
			for (Item i : newItems) {
				for (String name : i.getAttributes().keySet()) {
					if (!columns.contains(name)) {
						columns.add(name);
						colsChanged = true;
					}
				}
			}
			items.addAll(newItems);
			if (colsChanged) {
				fireTableStructureChanged();
			}
			fireTableRowsInserted(firstRow, items.size());
		}

		public int getRowCount() {
			return items.size();
		}

		public int getColumnCount() {
			return columns.size();
		}

		public Class getColumnClass(int column) {
			return String.class;
		}

		public String getColumnName(int column) {
			return columns.get(column);
		}

		public boolean isCellEditable(int row, int column) {
			return false;
		}

		public Object getValueAt(int row, int column) {
			if (column == 0) {
				return items.get(row).getIdentifier();
			}
			else {
				String colName = columns.get(column);
				Set<String> vals = items.get(row).getAttributes().get(colName);
				if (vals == null) {
					return " -- ";
				}
				if (vals.size() == 1) {
					return vals.iterator().next();
				}
				else {
					StringBuilder ret = new StringBuilder();
					for (String val : vals) {
						ret.append(val);
						ret.append(",");
					}
					return ret.toString();
				}
			}

		}
	}

	public class ResultsFrame extends JInternalFrame {
		private ItemTableModel tm;
		private double boxUsage = 0.0;
		private String time = "0 seconds";
		private int itemCount = 0;
		private JLabel stats;

		public ResultsFrame(String title) {
			super(title);
			setClosable(true);
			setIconifiable(true);
			setMaximizable(true);
			setResizable(true);
			setBounds(0, 0, 400, 300);

			setLayout(new BorderLayout());
			stats = new JLabel("-");
			add(stats, BorderLayout.NORTH);
			tm = new ItemTableModel();
			JTable resultSpace = new JTable(tm);
			JScrollPane sp = new JScrollPane(resultSpace);
			add(sp, BorderLayout.CENTER);
		}

		public void addItems(List<Item> items) {
			tm.addItems(items);
		}

		public void addBoxUsage(String usage) {
			try {
				double newUsage = Double.parseDouble(usage);
				boxUsage += newUsage;
				updateStats();
			} catch (NumberFormatException ex) {
				System.err.println("error parsing box usage : "+ex.getMessage());
			}
		}

		public void addItemCount(int count) {
			itemCount += count;
			updateStats();
		}

		public void setTime(String time) {
			this.time = time;
			updateStats();
		}

		private void updateStats() {
			stats.setText("Box Usage: "+boxUsage+"  Item Count: "+itemCount+"  Time To Run: "+time);
		}
	}
}
