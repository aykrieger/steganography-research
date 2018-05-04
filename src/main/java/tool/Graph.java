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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalDouble;

public class Graph {

    private final static int PLOT_WIDTH = 500;
    private final static int PLOT_HEIGHT = 350;

    // Displays the Storage Efficiency Chart to the user
    public static void displayStoragePlot(HashMap<StegoTechnique, Double> inputMap) {

        JFreeChart chart = storageEfficiencyChart(inputMap);
        displayChart(chart);
    }

    // Saves the Storage Efficiency Chart as an image
    public static void saveStoragePlot(HashMap<StegoTechnique, Double> inputMap,
                                       String outputDir) throws IOException {

        JFreeChart chart = storageEfficiencyChart(inputMap);
        File barChartFile = new File(outputDir);
        ChartUtils.saveChartAsPNG(barChartFile, chart, PLOT_WIDTH, PLOT_HEIGHT);
    }

    // Displays the Correctness Chart to the user
    public static void displayCorrectnessPlot(HashMap<StegoTechnique,
            ArrayList<Double>> inputMap) {

        JFreeChart chart = correctnessChart(inputMap);
        displayChart(chart);
    }

    // Saves the Correctness Chart as an image
    public static void saveCorrectnessPlot(HashMap<StegoTechnique, ArrayList<Double>> inputMap,
                                       String outputDir) throws IOException {

        JFreeChart chart = correctnessChart(inputMap);
        File barChartFile = new File(outputDir);
        ChartUtils.saveChartAsPNG(barChartFile, chart, PLOT_WIDTH, PLOT_HEIGHT);
    }

    // Specifies the Storage Efficiency Chart axis labels
    private static JFreeChart storageEfficiencyChart(
            HashMap<StegoTechnique, Double> inputMap) {

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

    // Specifies the Detectability Chart axis labels
    private static JFreeChart correctnessChart(
            HashMap<StegoTechnique, ArrayList<Double>> inputMap) {

        ArrayList<Double> dataLSB = inputMap.get(StegoTechnique.LSB);
        ArrayList<Double> dataGreenBlue = inputMap.get(StegoTechnique.GREENBLUE);
        ArrayList<Double> dataDFT = inputMap.get(StegoTechnique.DFT);
        ArrayList<Double> dataDWT = inputMap.get(StegoTechnique.DWT);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "Series1";

        if (dataLSB != null) {
            dataset.setValue(averageVal(dataLSB), series1, "LSB");
        }
        if (dataGreenBlue != null) {
            dataset.setValue(averageVal(dataGreenBlue), series1, "GreenBlue");
        }
        if (dataDFT != null) {
            dataset.setValue(averageVal(dataDFT), series1, "DFT");
        }
        if (dataDWT != null) {
            dataset.setValue(averageVal(dataDWT), series1, "DWT");
        }


        JFreeChart chart = ChartFactory.createBarChart(
                "Detectability",
                "Technique",
                "Detection Rate (Percent)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        return chart;
    }

    private static DefaultCategoryDataset map2dataset(HashMap<StegoTechnique, Double> inputMap) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        String series1 = "Series1";
        dataset.setValue(inputMap.get(StegoTechnique.LSB), series1, "LSB");
        dataset.setValue(inputMap.get(StegoTechnique.GREENBLUE), series1, "GreenBlue");
        dataset.setValue(inputMap.get(StegoTechnique.DFT), series1, "DFT");
        dataset.setValue(inputMap.get(StegoTechnique.DWT), series1, "DWT");

        return dataset;
    }

    private static void displayChart(JFreeChart chart) {
        ChartFrame frame = new ChartFrame("First", chart);
        frame.setSize(PLOT_WIDTH, PLOT_HEIGHT);
        UIUtils.centerFrameOnScreen(frame);
        frame.setVisible(true);
    }

    private static double averageVal(ArrayList<Double> data) {
        OptionalDouble average = data
                .stream()
                .mapToDouble(a -> a)
                .average();

        return average.isPresent() ? average.getAsDouble() : 0;
    }
}