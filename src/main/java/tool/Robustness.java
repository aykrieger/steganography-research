package tool;

import lib.BitIterator;
import lib.Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Calculated per technique as % of message bits preserved after compression
 * For a full technique, this is the average across all trials for input folder of images
 */
public class Robustness {

    /**
     * @return a map of technique name to calculated robustness
     */
    public static Double calculate(File imageFolder, Encoder encoder, String message) throws IOException {
        List<Double> scoreList = new ArrayList<>();

        for (final File imagePointer : Objects.requireNonNull(imageFolder.listFiles())) {
            //gets the name of the files
            String imageName = (imagePointer.getName());

            if (imageName.contains(encoder.GetName())) {
                //gets the input file path
                String inputFileName = imageFolder.getName() + "/" + imageName;
                scoreList.add(robustnessScore(inputFileName, encoder, message));
            }

        }

        return average(scoreList);
    }

    private static Double robustnessScore(String imagePath, Encoder encoder, String message) throws IOException {
        BufferedImage image = ImageIO.read(new File(imagePath));

        //todo compress image

        encoder.SetImage(imagePath);
        String output = encoder.Decode();


        int messageLen = (int)encoder.GetCapacityFactor()*message.length();
        BitIterator messageIter = new BitIterator(message.substring(0,messageLen));
        BitIterator outputIter = new BitIterator(output);

        int correctCount = 0;
        while(outputIter.hasNext()) {
            if(outputIter.next() == messageIter.next()) {
                correctCount++;
            }
        }

        return correctCount / (messageLen * 8.0);

    }

    private static Double average(List<Double> trials) {
        Double sum = trials.stream()
                            .reduce(0d, (Double d1, Double d2) -> d1 + d2);

        return sum / trials.size();
    }
}
