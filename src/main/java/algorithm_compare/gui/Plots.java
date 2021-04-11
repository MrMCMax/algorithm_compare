package algorithm_compare.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Paint;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.jfree.chart.ChartColor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class Plots extends JFrame {

	private JPanel contentPane;
	
	private List<String> networkNames;
	private List<String> algorithmNames;
	private Long[][] values;

	private Launcher launcher;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Plots frame = new Plots();
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
	public Plots() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
	}
	
	public void setData(List<String> nets, List<String> algs, Long[][] data) {
		networkNames = nets;
		algorithmNames = algs;
		values = data;
	}
	
	public void setLauncher(Launcher l) {
		this.launcher = l;
	}

	public void load() {
		//Create dataset
		XYSeriesCollection dataset = new XYSeriesCollection();
		for (int i = 0; i < algorithmNames.size(); i++) {
			XYSeries series = new XYSeries(algorithmNames.get(i));
			for (int j = 0; j < networkNames.size(); j++) {
				series.add(j, values[j][i]); //X: network index, Y: net j, alg i
			}
			dataset.addSeries(series);
		}
		//Create chart
		JFreeChart chart = ChartFactory.createXYLineChart("Times in ms", 
	            "Networks", "Milliseconds", dataset);
		

		XYPlot plot = chart.getXYPlot();
	    XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	    
	    //Set color for each series
	    for (int i = 0; i < algorithmNames.size(); i++) {
	    	renderer.setSeriesPaint(i, randomColor());
		    //sets thickness for series (using strokes)
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
