package algorithm_compare.gui;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import algorithm_compare.logic.ILogicService;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SelectNetwork extends JFrame {
	
	private List<String> networkNames;

	public static final int RUN = 0;
	public static final int VISUALISE = 1;
	
	private int mode;
	private Launcher launcher;
	
	private JList<String> selectedNetworksList;
	private JList<String> availableNetworksList;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SelectNetwork frame = new SelectNetwork();
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
	public SelectNetwork() {
		setTitle("Network selection");
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				//Open launcher again
				launcher.setVisible(true);
			}
		});
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout(0, 0));
		getContentPane().setLayout(new BorderLayout(0, 0));
		
		JLabel lblSelectNetworks = new JLabel("Select Networks");
		lblSelectNetworks.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblSelectNetworks, BorderLayout.NORTH);
		
		JButton btnGo = new JButton("Go");
		btnGo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectAlgorithm sa = new SelectAlgorithm();
				sa.setMode(mode);
				sa.setLauncher(launcher);
				sa.setSelectNetworkWindow(SelectNetwork.this);
				sa.setNetworks(selectedNetworksList.getSelectedValuesList());
				sa.load();
				sa.setVisible(true);
				SelectNetwork.this.setVisible(false);
			}
		});
		getContentPane().add(btnGo, BorderLayout.SOUTH);
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 0, 0, 0};
		gbl_panel.rowHeights = new int[]{0, 0, 0};
		gbl_panel.columnWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);
		
		JButton btnNewButton = new JButton("Add ->");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String name = availableNetworksList.getSelectedValue();
				if (name != null) {
					DefaultListModel<String> model = (DefaultListModel<String>) selectedNetworksList.getModel();
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
		
		selectedNetworksList = new JList<String>();
		GridBagConstraints gbc_selectedNetworksList = new GridBagConstraints();
		gbc_selectedNetworksList.gridheight = 2;
		gbc_selectedNetworksList.fill = GridBagConstraints.BOTH;
		gbc_selectedNetworksList.gridx = 2;
		gbc_selectedNetworksList.gridy = 0;
		panel.add(selectedNetworksList, gbc_selectedNetworksList);
		
		availableNetworksList = new JList<String>();
		GridBagConstraints gbc_availableNetworksList = new GridBagConstraints();
		gbc_availableNetworksList.gridheight = 2;
		gbc_availableNetworksList.insets = new Insets(0, 0, 0, 5);
		gbc_availableNetworksList.fill = GridBagConstraints.BOTH;
		gbc_availableNetworksList.gridx = 0;
		gbc_availableNetworksList.gridy = 0;
		panel.add(availableNetworksList, gbc_availableNetworksList);
		
		JButton btnNewButton_1 = new JButton("Remove");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int index = selectedNetworksList.getSelectedIndex();
				if (index >= 0) {
					DefaultListModel<String> model = (DefaultListModel<String>) selectedNetworksList.getModel();
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
	 * @param mode
	 */
	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public void setLauncher(Launcher launcher) {
		this.launcher = launcher;
	}
	
	public void load() {
		//MAX: Read networks
		try {
			networkNames = launcher.logic().getListOfNetworks();
			DefaultListModel<String> model = new DefaultListModel<String>();
			model.addAll(networkNames);
			availableNetworksList.setModel(model);
			DefaultListModel<String> model2 = new DefaultListModel<String>();
			selectedNetworksList.setModel(model2);
		} catch (IOException e1) {
			Launcher.showErrorMessage(this, e1.getMessage());
		}
	}
	
}
