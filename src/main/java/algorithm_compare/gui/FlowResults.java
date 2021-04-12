package algorithm_compare.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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
	 */ /*
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
	} */

	/**
	 * Create the frame.
	 */
	public FlowResults() {
		setTitle("Max flow results");
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
		/*
		RowNumberTable rowTable = new RowNumberTable(resultsTable);
		scrollPane.setRowHeaderView(rowTable);
		scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER,
		    rowTable.getTableHeader());
		
		
		//Load data
		
		rowTable.setHeaders(selectedNetworks);
		resultsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		
	    */
		ListModel<String> lm = new AbstractListModel<String>() {
		      String headers[] = selectedNetworks.toArray(new String[0]);

		      @Override
		      public int getSize() {
		        return headers.length;
		      }

		      @Override
		      public String getElementAt(int index) {
		        return headers[index];
		      }
		    }; 
		
		JList<String> rowHeader = new JList<>(lm);
		rowHeader.setFixedCellWidth(50);
		
		
		rowHeader.addMouseMotionListener(new MouseMotionListener() {
	        @Override
	        public void mouseDragged(MouseEvent e) {
	            // Set the list cell width as mouse is dragged.
	            rowHeader.setFixedCellWidth(e.getX());
	      }

	        @Override
	        public void mouseMoved(MouseEvent e) {
	            // If the mouse pointer is near the end region of the 
	            // list cell then change the mouse cursor to a resize cursor.
	            if ((e.getX()>= (rowHeader.getWidth() - 5)) && (e.getX()<= rowHeader.getWidth())) {
	                rowHeader.setCursor(new Cursor(Cursor.E_RESIZE_CURSOR));
	            } 
	            // If the mouse pointer is not near the end region of a cell 
	            // then change the pointer back to its default.
	            else {
	                rowHeader.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	            }
	        }
	    });
		
		rowHeader.setCellRenderer(new RowHeaderRenderer(resultsTable));
		

		scrollPane.setRowHeaderView(rowHeader);
		
		DefaultTableModel tableModel = new DefaultTableModel(selectedAlgorithms.toArray(new String[0]), 0);
		
		
		for (int i = 0; i < results.length; i++) 
			tableModel.addRow(results[i]);
		resultsTable.setModel(tableModel);
	}
	
	
	class RowHeaderRenderer extends JLabel implements ListCellRenderer<String> {

		  RowHeaderRenderer(JTable table) {
		    JTableHeader header = table.getTableHeader();
		    setOpaque(true);
		    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		    setHorizontalAlignment(CENTER);
		    setForeground(header.getForeground());
		    setBackground(header.getBackground());
		    setFont(header.getFont());
		  }

		  @Override
		  public Component getListCellRendererComponent(JList<? extends String> list, String value,
		      int index, boolean isSelected, boolean cellHasFocus) {
		    setText((value == null) ? "" : value.toString());
		    return this;
		  }

	}

}
