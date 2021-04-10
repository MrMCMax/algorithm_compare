package algorithm_compare.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.JTable;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FlowResults extends JFrame {

	private JPanel contentPane;

	private Launcher launcher;
	private List<String> selectedNetworks;
	private List<String> selectedAlgorithms;
	private Long[][] results;
	private JScrollPane scrollPane;
	private JTable resultsTable;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FlowResults frame = new FlowResults();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public FlowResults() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				launcher.setVisible(true);
			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JLabel lblResults = new JLabel("Results");
		lblResults.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblResults, BorderLayout.NORTH);
		
		scrollPane = new JScrollPane();
		contentPane.add(scrollPane, BorderLayout.CENTER);
		
		resultsTable = new JTable();
		scrollPane.setViewportView(resultsTable);
	}

	
	/*
	 * METHODS
	 */
	
	public void setLauncher(Launcher launcher) {
		this.launcher = launcher;
	}
	
	public void setNetworks(List<String> networks) {
		this.selectedNetworks = networks;
	}
	
	public void setAlgorithms(List<String> algs) {
		this.selectedAlgorithms = algs;
	}
	
	public void setResults(Long[][] results) {
		this.results = results;
	}
	
	public void load() {
		//Create pretty table with row and column headers
		
		//JTable mainTable = new JTable();
		//JScrollPane scrollPane = new JScrollPane(mainTable);
		RowNumberTable rowTable = new RowNumberTable(resultsTable);
		scrollPane.setRowHeaderView(rowTable);
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
		    rowTable.getTableHeader());
		
		//Load data
		rowTable.setHeaders(selectedNetworks);
		DefaultTableModel tableModel = new DefaultTableModel(selectedAlgorithms.toArray(new String[0]), selectedNetworks.size());
		for (int i = 0; i < results.length; i++) 
			tableModel.addRow(results[i]);
		resultsTable.setModel(tableModel);
	}
}
