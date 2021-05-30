package algorithm_compare.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import javax.swing.JList;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SelectAlgorithm extends JFrame {

	private JPanel contentPane;

	private List<String> algorithmNames;

	public static final int RUN = 0;
	public static final int VISUALISE = 1;

	private int mode;
	private Launcher launcher;
	private SelectNetwork sn;
	private List<String> selectedNetworks;
	private List<String> selectedAlgorithms;
	private Long[][] results;

	private JList<String> algorithmList;
	private JList<String> selectedAlgorithmsList;
	private JButton btnGo;

	/**
	 * Launch the application.
	 */ /*
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SelectAlgorithm frame = new SelectAlgorithm();
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
	public SelectAlgorithm() {
		setTitle("Select Algorithms");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				sn.setVisible(true);
			}
		});
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JLabel lblSelectAlgorithms = new JLabel("Select Algorithms");
		lblSelectAlgorithms.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblSelectAlgorithms, BorderLayout.NORTH);

		btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				//Set fields
				btnGo.setText("Wait...");
				int n = selectedAlgorithmsList.getModel().getSize();
				selectedAlgorithms = new ArrayList<>(n);
				for (int i = 0; i < n; i++) {
					selectedAlgorithms.add(selectedAlgorithmsList.getModel().getElementAt(i));
				}
				results = new Long[selectedNetworks.size()][selectedAlgorithmsList.getModel().getSize()];
				
				if (mode == RUN) {
					run();
				} else if (mode == VISUALISE) {
					visualise();
				}
			}
		});
		contentPane.add(btnGo, BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 1.0, 0.0, 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		algorithmList = new JList<>();
		GridBagConstraints gbc_algorithmList = new GridBagConstraints();
		gbc_algorithmList.gridheight = 2;
		gbc_algorithmList.insets = new Insets(0, 0, 0, 5);
		gbc_algorithmList.fill = GridBagConstraints.BOTH;
		gbc_algorithmList.gridx = 0;
		gbc_algorithmList.gridy = 0;
		panel.add(algorithmList, gbc_algorithmList);

		JButton btnNewButton = new JButton("Add ->");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = algorithmList.getSelectedValue();
				if (name != null) {
					DefaultListModel<String> model = (DefaultListModel<String>) selectedAlgorithmsList.getModel();
					if (!model.contains(name)) {
						model.addElement(name);
					}
				}
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 1;
		gbc_btnNewButton.gridy = 0;
		panel.add(btnNewButton, gbc_btnNewButton);

		selectedAlgorithmsList = new JList<>();
		GridBagConstraints gbc_selectedAlgorithmsList = new GridBagConstraints();
		gbc_selectedAlgorithmsList.gridheight = 2;
		gbc_selectedAlgorithmsList.fill = GridBagConstraints.BOTH;
		gbc_selectedAlgorithmsList.gridx = 2;
		gbc_selectedAlgorithmsList.gridy = 0;
		panel.add(selectedAlgorithmsList, gbc_selectedAlgorithmsList);

		JButton btnNewButton_1 = new JButton("Remove");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = selectedAlgorithmsList.getSelectedIndex();
				if (index >= 0) {
					DefaultListModel<String> model = (DefaultListModel<String>) selectedAlgorithmsList.getModel();
					model.remove(index);
				}
			}
		});
		GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
		gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
		gbc_btnNewButton_1.gridx = 1;
		gbc_btnNewButton_1.gridy = 1;
		panel.add(btnNewButton_1, gbc_btnNewButton_1);
	}

	/**
	 * Modes: Run or visualise
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}

	public void setLauncher(Launcher launcher) {
		this.launcher = launcher;
	}
	
	public void setSelectNetworkWindow(SelectNetwork sn) {
		this.sn = sn;
	}
	
	public void setNetworks(List<String> networks) {
		this.selectedNetworks = networks;
	}

	public void load() {
		// MAX: Read algorithms
		algorithmNames = launcher.logic().getListOfAlgorithms();
		DefaultListModel<String> model = new DefaultListModel<String>();
		model.addAll(algorithmNames);
		algorithmList.setModel(model);
		DefaultListModel<String> model2 = new DefaultListModel<String>();
		selectedAlgorithmsList.setModel(model2);
	}
	
	private void run() {
		//Get results
		for (int i = 0; i < selectedNetworks.size(); i++) {
			try {
				//Get results
				long[] res = launcher.logic().computeNetworkWithAlgorithms(selectedNetworks.get(i), 
						selectedAlgorithms.toArray(new String[0]));
				for (int j = 0; j < res.length; j++)
					results[i][j] = res[j];
				
			} catch (IOException e1) {
				Launcher.showErrorMessage(SelectAlgorithm.this, e1.getMessage());
			}
		}
		
		//Open window
		FlowResults fr = new FlowResults();
		fr.setAlgorithms(selectedAlgorithms);
		fr.setNetworks(selectedNetworks);
		fr.setLauncher(launcher);
		fr.setResults(results);
		fr.load();
		btnGo.setText("Go");
		fr.setVisible(true);
		SelectAlgorithm.this.setVisible(false);
	}
	
	private void visualise() {
		//Get times
		for (int i = 0; i < selectedNetworks.size(); i++) {
			try {
				long[] res = launcher.logic().retrieveTimes(selectedNetworks.get(i), selectedAlgorithms.toArray(new String[0]));
				for (int j = 0; j < res.length; j++) {
					if (res[j] == -1) {
						Launcher.showWarningMessage(this, "Value not computed for network "
								+ selectedNetworks.get(i) + " and algorithm " + selectedAlgorithms.get(j) + "");
					}
					results[i][j] = res[j];
				}
			} catch (IOException e) {
				Launcher.showErrorMessage(SelectAlgorithm.this, e.getMessage());
			}
		}
		//Show window
		Plots pl = new Plots();
		pl.setData(selectedNetworks, selectedAlgorithms, results);
		pl.setLauncher(launcher);
		try {
			pl.printPython();
		} catch (IOException e) {
			Launcher.showErrorMessage(this, e.getMessage());
		}
		pl.setVisible(true);
		SelectAlgorithm.this.setVisible(false);
	}
}
