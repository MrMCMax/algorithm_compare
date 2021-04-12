package algorithm_compare.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Paint;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import javax.swing.JTextPane;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class Plots extends JFrame {

	private JPanel contentPane;

	private List<String> networkNames;
	private List<String> algorithmNames;
	private Long[][] values;

	private Launcher launcher;
	private JTextPane textPane;

	private Map<String, Color> algColors = new HashMap<>();
	/**
	 * Launch the application.
	 */
	/*
	 * public static void main(String[] args) { EventQueue.invokeLater(new
	 * Runnable() { public void run() { try { Plots frame = new Plots();
	 * frame.setVisible(true); } catch (Exception e) { e.printStackTrace(); } } });
	 * }
	 */

	/**
	 * Create the frame.
	 */
	public Plots() {
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

		textPane = new JTextPane();
		contentPane.add(textPane, BorderLayout.CENTER);

		/*
		 * Max: Colors for the algorithms
		 */
		algColors.put("EdmondsKarp", Color.getHSBColor(0f, 0.75f, 0.65f));
		algColors.put("ScalingEdmondsKarp", Color.YELLOW);
		algColors.put("DinicsAlgorithm", Color.GREEN);
	}

	public void setData(List<String> nets, List<String> algs, Long[][] data) {
		networkNames = nets;
		algorithmNames = algs;
		values = data;
	}

	public void setLauncher(Launcher l) {
		this.launcher = l;
	}

	public void printPython() throws IOException {
		StringBuilder sb = new StringBuilder();
		sb.append("#!/usr/bin/env python3\nimport matplotlib.pyplot as plt\n");
		//Network names array
		sb.append("x=[");
		sb.append(launcher.logic().getNetworkNVertices(networkNames.get(0)));
		for (int i = 1; i < networkNames.size(); i++) {
			sb.append(",").append(launcher.logic().getNetworkNVertices(networkNames.get(i)));
		}
		sb.append("]\n").append("xi = list(range(len(x)))\n");
		// Y ranges
		for (int i = 0; i < algorithmNames.size(); i++) {
			sb.append("y").append(i).append("=[");
			sb.append(values[0][i]);// First network, algorithm
			for (int j = 1; j < networkNames.size(); j++) {
				sb.append(",").append(values[j][i]);
			}
			sb.append("]\n");
		}
		for (int i = 0; i < algorithmNames.size(); i++) {
			sb.append("plt.plot(xi, y").append(i).append(", marker='o', linestyle='-', label='")
					.append(algorithmNames.get(i)).append("')\n");
		}
		sb.append("plt.xlabel('Nº of vertices')\n");
		sb.append("plt.ylabel('Milliseconds')\n");
		sb.append("plt.xticks(xi, x)\n");
		sb.append("plt.title('Times')\n");
		sb.append("plt.legend()\n");
		sb.append("plt.show()\n");
		textPane.setText(sb.toString());
	}

	public void load() {
		// Create dataset
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i = 0; i < algorithmNames.size(); i++) {
			XYSeries series = new XYSeries(algorithmNames.get(i));
			for (int j = 0; j < networkNames.size(); j++) {
				try {
					int x = launcher.logic().getNetworkNVertices(networkNames.get(j));
					series.add(x, values[j][i]); // X: network index, Y: net j, alg i
				} catch (IOException e) {
					Launcher.showErrorMessage(this, "Cannot find network " + networkNames.get(j));
				}
			}
			dataset.addSeries(series);
		}
		// Create chart
		JFreeChart chart = ChartFactory.createXYLineChart("Times in ms", "Networks", "Milliseconds", dataset);

		XYPlot plot = chart.getXYPlot();
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
		// plot.setDomainAxis(new NumberAxis().);
		// Set color for each series
		for (int i = 0; i < algorithmNames.size(); i++) {
			renderer.setSeriesPaint(i, algColors.get(algorithmNames.get(i)));
			// sets thickness for series (using strokes)
			renderer.setSeriesStroke(i, new BasicStroke(4.0f));
		}

		// sets paint color for plot outlines
		plot.setOutlinePaint(Color.BLUE);
		plot.setOutlineStroke(new BasicStroke(2.0f));

		// sets renderer for lines
		plot.setRenderer(renderer);

		// sets plot background
		plot.setBackgroundPaint(Color.DARK_GRAY);

		// sets paint color for the grid lines
		plot.setRangeGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.BLACK);

		plot.setDomainGridlinesVisible(true);
		plot.setDomainGridlinePaint(Color.BLACK);

		contentPane.add(new ChartPanel(chart));
	}

	private Paint randomColor() {
		Random random = new Random();
		float r = random.nextFloat();
		float g = random.nextFloat();
		float b = random.nextFloat();
		return new Color(r, g, b).brighter();
	}
}
