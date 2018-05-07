package tool;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ui.UIUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static void saveTimePlot(HashMap<StegoTechnique, ArrayList<Long>> inputMap,
                                       String outputDir) throws IOException {
        JFreeChart chart = timeChart(inputMap);
        File barChartFile = new File(outputDir);
        ChartUtils.saveChartAsPNG(barChartFile, chart, PLOT_WIDTH, PLOT_HEIGHT);
    }

    private static JFreeChart timeChart(HashMap<StegoTechnique, ArrayList<Long>> inputMap) {

        ArrayList<Long> dataLSB = inputMap.get(StegoTechnique.LSB);
        ArrayList<Long> dataGreenBlue = inputMap.get(StegoTechnique.GREENBLUE);
        ArrayList<Long> dataDFT = inputMap.get(StegoTechnique.DFT);
        ArrayList<Long> dataDWT = inputMap.get(StegoTechnique.DWT);

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String series1 = "Series1";

        if (dataLSB != null) {
            dataset.setValue(averageValLong(dataLSB) / 25000.0,
                    series1, "LSB");
        }
        if (dataGreenBlue != null) {
            dataset.setValue(averageValLong(dataGreenBlue) / (0.6667 * 25000.0),
                    series1, "GreenBlue");
        }
        if (dataDFT != null) {
            dataset.setValue(averageValLong(dataDFT) / (0.25 * 25000.0),
                    series1, "DFT");
        }
        if (dataDWT != null) {
            dataset.setValue(averageValLong(dataDWT) / (0.25 * 25000.0),
                    series1, "DWT");
        }


        JFreeChart chart = ChartFactory.createBarChart(
                "Average Time to Encode Each Word",
                "Steganography Technique",
                "Encode Time (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
        barRenderer.setSeriesPaint(0, Color.GREEN);
        return chart;
    }

    // Specifies the Storage Efficiency Chart axis labels
    private static JFreeChart storageEfficiencyChart(HashMap<StegoTechnique, Double> inputMap) {

        DefaultCategoryDataset dataset = map2dataset(inputMap);
        JFreeChart chart = ChartFactory.createBarChart(
                "Maximum Message Length for 1024 * 1024 Image",
                "Steganography Technique",
                "Number of Words Encoded",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
        barRenderer.setSeriesPaint(0, Color.BLUE);
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
            dataset.setValue(averageValDouble(dataLSB), series1, "LSB");
        }
        if (dataGreenBlue != null) {
            dataset.setValue(averageValDouble(dataGreenBlue), series1, "GreenBlue");
        }
        if (dataDFT != null) {
            dataset.setValue(averageValDouble(dataDFT), series1, "DFT");
        }
        if (dataDWT != null) {
            dataset.setValue(averageValDouble(dataDWT), series1, "DWT");
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

    private static double averageValDouble(ArrayList<Double> data) {
        OptionalDouble average = data
                .stream()
                .mapToDouble(a -> a)
                .average();

        return average.isPresent() ? average.getAsDouble() : 0;
    }

    private static double averageValLong(ArrayList<Long> data) {
        OptionalDouble average = data
                .stream()
                .mapToLong(a -> a)
                .average();

        return average.isPresent() ? average.getAsDouble() : 0;
    }


}