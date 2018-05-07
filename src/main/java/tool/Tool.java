package tool;

import frequency.DFT.DFTEncoder;
import frequency.DWT.DWTEncoder;
import lib.BitIterator;
import spatial.GreenBlue.GreenBlueEncoder;
import spatial.LSB.LeastSignificantBitEncoder;
import warden.BitDeletion.BitDeleter;
import warden.DST.DiscreteSpringTransform;
import warden.RawQuickPair.RawQuickPair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class Tool {

    //this reads the large text file in messages and converts it into a usable string
    private static String readLargeMessage() throws IOException {
        File file = new File("Messages/largeTextFile.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    //this method takes all images in the input folder and creates a Stenographic version of the image
    private static HashMap<StegoTechnique, ArrayList<Long>> sendAllImagesToBeStego(
            final File folder) throws IOException {

        String largeMessage = readLargeMessage();

        ArrayList<Long> timeListLSB = new ArrayList<Long>();
        ArrayList<Long> timeListGreenBlue = new ArrayList<Long>();
        ArrayList<Long> timeListDFT = new ArrayList<Long>();
        ArrayList<Long> timeListDWT = new ArrayList<Long>();

        Timer timer = new Timer();

        int count = 1;
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {

            //gets the name of the files
            String imageName = (imagePointer.getName());
            if (!imageName.equals("Thumbs.db")) {

                //gets the input file path
                String inputFileName = "ToolImages/" + imageName;

                if (imageName.endsWith(".jpg")) {
                    imageName = imageName.replace(".jpg", ".png");
                }

                //creates unique names for all of the different stego images
                String outputFileNameLSB = "StenographicOutputImages/LSB_" + imageName;
                String outputFileNameGreenBlue = "StenographicOutputImages/GreenBlue_" + imageName;
                String outputFileNameDFT = "StenographicOutputImages/DFT_" + imageName;
                String outputFileNameDWT = "StenographicOutputImages/DWT_" + imageName;

                //finds the size of the image to figure out how large of a message to give it divide by 8 to account for char to bit
                BufferedImage image = ImageIO.read(new File(inputFileName));
                int imageSize = image.getHeight() * image.getWidth() / 16;

                //this encodes the image as LSB
                timer.start();
                LeastSignificantBitEncoder leastSignificantBitEncoder = new LeastSignificantBitEncoder(inputFileName);
                leastSignificantBitEncoder.Encode(largeMessage.substring(0, imageSize));
                leastSignificantBitEncoder.WriteImage(outputFileNameLSB);
                timer.end();
                timeListLSB.add(timer.getTotalTime());

                //encodes image as GB secret key is 12345 since it is easy to remember
                timer.start();
                int gbMessageSize = imageSize * 2 / 3;
                GreenBlueEncoder.encode(inputFileName, outputFileNameGreenBlue, largeMessage.substring(0, gbMessageSize), 12345);
                timer.end();
                timeListGreenBlue.add(timer.getTotalTime());

                int frequenceyMessageSize = imageSize / 4;
                //encodes the image as a dwt
                timer.start();
                DFTEncoder dftEncoder = new DFTEncoder(inputFileName);
                dftEncoder.Encode(largeMessage.substring(0, frequenceyMessageSize));
                dftEncoder.WriteImage(outputFileNameDFT);
                timer.end();
                timeListDFT.add(timer.getTotalTime());

                //encodes the image as a dwt
                timer.start();
                DWTEncoder dwtEncoder = new DWTEncoder(inputFileName);
                dwtEncoder.Encode(largeMessage.substring(0, frequenceyMessageSize));
                dwtEncoder.WriteImage(outputFileNameDWT);
                timer.end();
                timeListDWT.add(timer.getTotalTime());

                System.out.println("Finished encoding " + count + "/50 images");
                count++;
            }
        }

        HashMap<StegoTechnique, ArrayList<Long>> resultMap = new HashMap<>();

        resultMap.put(StegoTechnique.LSB, timeListLSB);
        resultMap.put(StegoTechnique.GREENBLUE, timeListGreenBlue);
        resultMap.put(StegoTechnique.DFT, timeListDFT);
        resultMap.put(StegoTechnique.DWT, timeListDWT);

        return resultMap;
    }

    private static void sendAllImagesToWardens(final File folder, BufferedWriter rawQuickPairWriter) throws IOException {
        int count = 1;
        double sumLSBRQP = 0;
        double sumGBRQP = 0;
        double sumDFTRQP = 0;
        double sumDWTRQP = 0;
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {
            //gets the name of the files
            String imageName = (imagePointer.getName());
            if (!imageName.equals("Thumbs.db")) {
                //gets the input file path
                String inputFileName = "StenographicOutputImages/" + imageName;
                //creates unique names for all of the different stego images
                String outputFileNameBitDeletion = "WardenImages/BitDeletion_" + imageName;
                String outputFileNameRawQuickPair = "WardenImages/RawQuickPair_" + imageName;
                String outputFileNameDST = "WardenImages/DST_" + imageName;

                //run the bit deleter
                BitDeleter bitDeleter = new BitDeleter(inputFileName);
                bitDeleter.ScrubImage();
                bitDeleter.WriteImage(outputFileNameBitDeletion);

                //RawQuickPairWarden passes only non stego images
                RawQuickPair rawQuickPair = new RawQuickPair(inputFileName);
                rawQuickPair.writeImage(outputFileNameRawQuickPair);
                double ratio = rawQuickPair.findRatio();
                rawQuickPairWriter.write(imageName + ": \t" + ratio + "\n");
                if (imageName.contains("LSB_")) {
                    sumLSBRQP+=ratio;
                } else if (imageName.contains("GreenBlue_")) {
                    sumGBRQP+=ratio;
                } else if (imageName.contains("DFT_")) {
                    sumDFTRQP+=ratio;
                } else if (imageName.contains("DWT_")) {
                    sumDWTRQP+=ratio;
                }
                DiscreteSpringTransform discreteSpringTransform = new DiscreteSpringTransform(inputFileName);
                discreteSpringTransform.writeImage(outputFileNameDST);

                if (count % 4 == 0) {
                    System.out.println("Warden completed " + (count / 4) + "/50 images");
                }
                count++;
            }
        }
        System.out.println("Close Color Ratio Mean For LSB:\t " +sumLSBRQP/50);
        System.out.println("Close Color Ratio Mean For Green Blue:\t " +sumGBRQP/50);
        System.out.println("Close Color Ratio Mean For DFT:\t " +sumDFTRQP/50);
        System.out.println("Close Color Ratio Mean For DWT:\t " +sumDWTRQP/50);
        System.out.println("Close Color Ratio Mean For All Techniques:\t " + (sumLSBRQP+sumGBRQP+sumDFTRQP+sumDWTRQP)/200);

    }

    private static void rawQuickPairOnNonStegoImages(final File folder, BufferedWriter rawQuickPairWriter) throws IOException {
        int count = 1;
        double ratioArray [] = new double[50];
        double sum = 0.0;
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {
            //gets the name of the files
            String imageName = (imagePointer.getName());
            if (!imageName.equals("Thumbs.db")) {
                //gets the input file path
                String inputFileName = "ToolImages/" + imageName;

                RawQuickPair rawQuickPair = new RawQuickPair(inputFileName);
                rawQuickPair.isImageStegonagraphic();
                double ratio = rawQuickPair.findRatio();
                rawQuickPairWriter.write(imageName + ": \t" + rawQuickPair.findRatio() + "\n");
                sum+=ratio;
                System.out.println("raw quick pair completed " + (count) + "/50 images");
                ratioArray[count-1]=rawQuickPair.findRatio();
                count++;
            }
        }
        Arrays.sort(ratioArray);
    System.out.println("Raw Quick Pair Ratio at 80%:\t"+ratioArray[40]);
    System.out.println("Close Color Ratio Mean For Cover Images:\t " +sum/50);
    }

    private static HashMap<StegoTechnique, ArrayList<Double>> compareMessages(
            final File folder, BufferedWriter comparatorWriter) throws IOException {

        HashMap<StegoTechnique, ArrayList<Double>> resultMap = new HashMap<>();
        resultMap.put(StegoTechnique.LSB, new ArrayList<Double>());
        resultMap.put(StegoTechnique.GREENBLUE, new ArrayList<Double>());
        resultMap.put(StegoTechnique.DFT, new ArrayList<Double>());
        resultMap.put(StegoTechnique.DWT, new ArrayList<Double>());

        String largeMessage = readLargeMessage();
        BufferedWriter ratioWriter = new BufferedWriter(new FileWriter("src/main/java/tool/ratioSucessfullyTransmittedMessage.txt", true));
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {

            StegoTechnique currentTechnique = StegoTechnique.LSB;

            //gets the name of the files
            String imageName = (imagePointer.getName());
            if (!imageName.equals("Thumbs.db")) {
                //gets the input file path
                String inputFileName = "WardenImages/" + imageName;


                //finds the size of the image to figure out how large of a message to give it divide by 8 to account for char to bit
                BufferedImage image = ImageIO.read(new File(inputFileName));

                int imageSize = image.getHeight() * image.getWidth() / 16;
                String incomingMessage = null;
                if (imageName.contains("LSB_")) {
                    currentTechnique = StegoTechnique.LSB;
                    LeastSignificantBitEncoder leastSignificantBitEncoder = new LeastSignificantBitEncoder(inputFileName);
                    incomingMessage = leastSignificantBitEncoder.Decode();
                } else if (imageName.contains("GreenBlue_")) {
                    currentTechnique = StegoTechnique.GREENBLUE;
                    imageSize = imageSize * 2 / 3;
                    incomingMessage = GreenBlueEncoder.decode(inputFileName, 12345);
                } else if (imageName.contains("DFT_")) {
                    currentTechnique = StegoTechnique.DFT;
                    imageSize = imageSize / 4;
                    DFTEncoder dftEncoder = new DFTEncoder(inputFileName);
                    incomingMessage = dftEncoder.Decode();
                } else if (imageName.contains("DWT_")) {
                    currentTechnique = StegoTechnique.DWT;
                    imageSize = imageSize / 4;
                    DWTEncoder dwtEncoder = new DWTEncoder(inputFileName);
                    incomingMessage = dwtEncoder.Decode();
                }
                if (incomingMessage != null) {
                    double ratio = stringComparator(incomingMessage, largeMessage.substring(0, imageSize));
                    comparatorWriter.write("\n" + imageName + " : " + ratio + "\n");
                    comparatorWriter.write(incomingMessage + "\n\n");

                    ArrayList<Double> stegoData = resultMap.get(currentTechnique);
                    stegoData.add(ratio);
                }
            }
        }
        ratioWriter.close();

        return resultMap;
    }

    private static double stringComparator(String actualMessage, String expectedMessage) throws UnsupportedEncodingException {
        BitIterator actualIter = new BitIterator(actualMessage);
        BitIterator outputIter = new BitIterator(expectedMessage);

        BitIterator shorterIter = actualMessage.length() < expectedMessage.length() ? actualIter : outputIter;
        int maxLen = actualMessage.length() > expectedMessage.length() ? actualMessage.length() : expectedMessage.length();

        int correctCount = 0;
        while (shorterIter.hasNext()) {
            if (outputIter.next().equals(actualIter.next())) {
                correctCount++;
            }
        }

        return correctCount / (maxLen * 8.0);
    }

    /*
    You must have the following folders in the project's root directory before running this method:
    "StenographicOutputImages"
    "WardenImages"
    "FinalGraphs" (This means you, Rob)
     */
    public static void main(String[] args) throws IOException {
        BufferedWriter rawQuickPairWriter = new BufferedWriter(new FileWriter("src/test/java/warden/ratioRQP.txt"));
        BufferedWriter comparatorWriter = new BufferedWriter(new FileWriter("src/main/java/tool/ratioSucessfullyTransmittedMessage.txt"));

        final File folderPlain = new File("ToolImages");
        final File folderStego = new File("StenographicOutputImages");
        final File folderComparator = new File("WardenImages");

        //clears the files in the outputting files
        for (final File fileStego : Objects.requireNonNull(folderStego.listFiles())) {
            fileStego.delete();
        }
        for (final File fileWarden : Objects.requireNonNull(folderComparator.listFiles())) {
            fileWarden.delete();
        }

        sendAllImagesToBeStego(folderPlain);
        rawQuickPairWriter.write("\nNonStegoImages:\n");
        rawQuickPairOnNonStegoImages(folderPlain, rawQuickPairWriter);
        rawQuickPairWriter.write("\nStegoImages:\n");

        HashMap<StegoTechnique, ArrayList<Long>> timeMap = sendAllImagesToBeStego(folderPlain);

        sendAllImagesToWardens(folderStego, rawQuickPairWriter);


        HashMap<StegoTechnique, ArrayList<Double>> detectionMap =
                compareMessages(folderComparator, comparatorWriter);

        rawQuickPairWriter.close();
        comparatorWriter.close();

        Graph.saveTimePlot(timeMap, "FinalGraphs/time_plot.png");
        Graph.saveCorrectnessPlot(detectionMap, "FinalGraphs/correctness_plot.png");
    }


    //takes in a new Folder
    // create a loop in for all files in folder
    // send each file to be encoded with a message from a large text file containing a message
    // print each file from the stenographic techniques into a holding folder.
    // read each file from the holding folder and send them through the three wardens
    // asses the files from the other end of the stream to see which contain readable messages.
    // compile data to access the effectiveness of the techniques.
}
