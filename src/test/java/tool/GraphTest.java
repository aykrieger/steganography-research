package tool;

import lib.TestingLib;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;

public class GraphTest {

    private TestingLib testingLib = new TestingLib();

    @Test
    public void displayStoragePlot_nominal() {
        HashMap<StegoTechnique, Double> dataMap = new HashMap<>();

        dataMap.put(StegoTechnique.LSB, 25000.0);
        dataMap.put(StegoTechnique.GREENBLUE, 16666.6);
        dataMap.put(StegoTechnique.DWT, 6250.0);
        dataMap.put(StegoTechnique.DFT, 6250.0);
        Graph.displayStoragePlot(dataMap);

        // This dummy chart is used so the previous chart is rendered while
        // using a debugging tool
        JFreeChart dummyChart = ChartFactory.createBarChart(
                "Title", "Label", "Label",
                new DefaultCategoryDataset()
        );
        // Put the breakpoint here
        ChartFrame frame = new ChartFrame("First", dummyChart);
    }

    @Test
    public void saveStoragePlot_nominal() throws IOException {
        HashMap<StegoTechnique, Double> dataMap = new HashMap<>();

        dataMap.put(StegoTechnique.LSB, 25000.0);
        dataMap.put(StegoTechnique.GREENBLUE, 16666.6);
        dataMap.put(StegoTechnique.DWT, 6250.0);
        dataMap.put(StegoTechnique.DFT, 6250.0);

        String outputPath = testingLib.outputImagesDir + "storage_efficiency_1.png";
        Graph.saveStoragePlot(dataMap, outputPath);
    }

}
