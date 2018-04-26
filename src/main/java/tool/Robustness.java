package tool;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Calculated per technique as % of message bits preserved after compression
 * For a full technique, this is the average across all trials for input folder of images
 */
public class Robustness {

    /**
     * @return a map of technique name to calculated robustness
     */
    public static Map<String, Double> calculate(File imageFolder) {
        //TODO filter by regex
        //So only grab LSB_*

        //for technique {

        for (final File imagePointer : Objects.requireNonNull(imageFolder.listFiles())) {
            //gets the name of the files
            String imageName = (imagePointer.getName());

            //gets the input file path
            String inputFileName = imageFolder.getName() + "/" + imageName;

            //compress
            //calc diff and put into list
        }
        // average technique values
        //}

        Map<String, Double> results = new HashMap<>();

        return results;
    }

    private static Double average(List<Double> trials) {
        Double sum = trials.stream()
                            .reduce(0d, (Double d1, Double d2) -> d1 + d2);

        return sum / trials.size();
    }
}
