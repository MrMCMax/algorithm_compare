package algorithm_compare.gui;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import algorithm_compare.logic.ILogicService;
import algorithm_compare.logic.LogicService;

public class Launcher {

	private JFrame frmAlgorithmCompare;
	private final Action action = new SwingAction();
	
	private ILogicService logicService;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Launcher window = new Launcher();
					window.startLogic();
					window.frmAlgorithmCompare.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Launcher() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmAlgorithmCompare = new JFrame();
		frmAlgorithmCompare.setTitle("Algorithm Compare");
		frmAlgorithmCompare.setBounds(100, 100, 450, 300);
		frmAlgorithmCompare.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmAlgorithmCompare.getContentPane().setLayout(new GridLayout(1, 0, 0, 0));
		
		JButton btnRunAlgorithm = new JButton("Run Algorithm");
		btnRunAlgorithm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectNetwork sn = new SelectNetwork();
				sn.setMode(SelectNetwork.RUN);
				sn.setLauncher(Launcher.this);
				System.out.println(Launcher.this);
				sn.load();
				sn.setVisible(true); //Open new window
				frmAlgorithmCompare.setVisible(false); //Hide current window
			}
		});
		frmAlgorithmCompare.getContentPane().add(btnRunAlgorithm);
		
		JButton btnVisualiseData = new JButton("Visualise data");
		btnVisualiseData.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SelectNetwork sn = new SelectNetwork();
				sn.setMode(SelectNetwork.VISUALISE);
				System.out.println(Launcher.this);
				sn.setLauncher(Launcher.this);
				sn.load();
				sn.setVisible(true); //Open new window
				frmAlgorithmCompare.setVisible(false); //Hide current window
			}
		});
		frmAlgorithmCompare.getContentPane().add(btnVisualiseData);
	}
	


	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	
	/*
	 * 	MY METHODS
	 */
	
	public void setVisible(boolean visible) {
		this.frmAlgorithmCompare.setVisible(visible);
	}
	
	public void startLogic() {
		try {
			logicService = new LogicService();
		} catch (Exception e) {
			showErrorMessage(frmAlgorithmCompare, e.getMessage());
		}
	}
	
	public ILogicService logic() {
		return logicService;
	}
	
	public static void showErrorMessage(Component parent, String message) {
		JOptionPane.showMessageDialog(parent, message, "An error has occurred", JOptionPane.ERROR_MESSAGE);
	}
}
