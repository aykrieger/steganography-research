package tool;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.UIUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class Graph {

    final static int PLOT_WIDTH = 500;
    final static int PLOT_HEIGHT = 350;

    public static void displayStoragePlot(HashMap<StegoTechnique, Double> inputMap) {
        JFreeChart chart = storageEfficiencyChart(inputMap);
        displayChart(chart);
    }

    public static void saveStoragePlot(HashMap<StegoTechnique, Double> inputMap,
                                       String outputDir) throws IOException {
        JFreeChart chart = storageEfficiencyChart(inputMap);
        File barChartFile = new File(outputDir);
        ChartUtils.saveChartAsPNG(barChartFile, chart, PLOT_WIDTH, PLOT_HEIGHT);
    }

    public static JFreeChart storageEfficiencyChart(HashMap<StegoTechnique, Double> inputMap) {
        DefaultCategoryDataset dataset = map2dataset(inputMap);
        JFreeChart chart = ChartFactory.createBarChart(
                "Storage Efficiency",
                "Technique",
                "Number of Message Bits per Pixel",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        return chart;
    }

    public static void graphDetectability() {

    }

    public static void graphRobustness() {

    }

    public static DefaultCategoryDataset map2dataset(HashMap<StegoTechnique, Double> inputMap) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String series1 = "Series1";
        dataset.setValue(inputMap.get(StegoTechnique.LSB), series1, "LSB");
        dataset.setValue(inputMap.get(StegoTechnique.GREENBLUE), series1, "GreenBlue");
        dataset.setValue(inputMap.get(StegoTechnique.DWT), series1, "DWT");
        dataset.setValue(inputMap.get(StegoTechnique.DFT), series1, "DFT");

        return dataset;
    }

    public static void displayChart(JFreeChart chart) {
        ChartFrame frame = new ChartFrame("First", chart);
        frame.setSize(PLOT_WIDTH, PLOT_HEIGHT);
        UIUtils.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }
}