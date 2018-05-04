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
import java.util.Objects;

public class Tool  {

    //this reads the large text file in messages and converts it into a usable string
    public static String readLargeMessage () throws IOException {
        File file = new File("Messages/largeTextFile.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null){
            stringBuilder.append(line);
        }
        return stringBuilder.toString();
    }

    //this method takes all images in the input folder and creates a Stenographic version of the image
    private static void sendAllImagesToBeStego(final File folder) throws IOException {
        String largeMessage = readLargeMessage();

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
                LeastSignificantBitEncoder leastSignificantBitEncoder = new LeastSignificantBitEncoder(inputFileName);
                leastSignificantBitEncoder.Encode(largeMessage.substring(0, imageSize));
                leastSignificantBitEncoder.WriteImage(outputFileNameLSB);

                //encodes image as GB secret key is 12345 since it is easy to remember
                int gbMessageSize = imageSize * 2 / 3;
                GreenBlueEncoder.encode(inputFileName, outputFileNameGreenBlue, largeMessage.substring(0, gbMessageSize), 12345);

                int frequenceyMessageSize = imageSize/4;
                //encodes the image as a dwt

                DFTEncoder dftEncoder = new DFTEncoder(inputFileName);
                dftEncoder.Encode(largeMessage.substring(0,frequenceyMessageSize));
                dftEncoder.WriteImage(outputFileNameDFT);

                //encodes the image as a dwt
                DWTEncoder dwtEncoder = new DWTEncoder(inputFileName);
                dwtEncoder.Encode(largeMessage.substring(0,frequenceyMessageSize));
                dwtEncoder.WriteImage(outputFileNameDWT);

                System.out.println("Finished encoding " + count + "/50 images");
                count++;
            }
        }
    }

    private static void sendAllImagesToWardens(final File folder, BufferedWriter rawQuickPairWriter) throws IOException {
        int count = 1;

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
                rawQuickPairWriter.write(imageName+ ": \t"+ rawQuickPair.findRatio()+ "\n");

                DiscreteSpringTransform discreteSpringTransform = new DiscreteSpringTransform(inputFileName);
                discreteSpringTransform.writeImage(outputFileNameDST);

                if (count % 4 == 0) {
                    System.out.println("Warden completed " + (count / 4) + "/50 images");
                }
                count++;
            }
        }
    }

    private static void compareMessages(final File folder, BufferedWriter comparatorWriter) throws IOException {
        String largeMessage = readLargeMessage();
        BufferedWriter ratioWriter = new BufferedWriter(new FileWriter("src/main/java/tool/ratioSucessfullyTransmittedMessage.txt", true));
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {

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
                    LeastSignificantBitEncoder leastSignificantBitEncoder = new LeastSignificantBitEncoder(inputFileName);
                    incomingMessage = leastSignificantBitEncoder.Decode();
                } else if (imageName.contains("GreenBlue_")) {
                    imageSize = imageSize * 2 / 3;
                    incomingMessage = GreenBlueEncoder.decode(inputFileName, 12345);
                } else if (imageName.contains("DFT_")) {
                    imageSize = imageSize / 4;
                    DFTEncoder dftEncoder = new DFTEncoder(inputFileName);
                    incomingMessage = dftEncoder.Decode();
                } else if (imageName.contains("DWT_")) {
                    imageSize = imageSize / 4;
                    DWTEncoder dwtEncoder = new DWTEncoder(inputFileName);
                    incomingMessage = dwtEncoder.Decode();
                }
                if (incomingMessage != null) {
                    double ratio = stringComparator(incomingMessage, largeMessage.substring(0, imageSize));
                    comparatorWriter.write("\n" + imageName + " : " + ratio +"\n");
                    comparatorWriter.write(incomingMessage + "\n\n");
                }
            }
        }
        ratioWriter.close();
    }

    private static double stringComparator(String actualMessage, String expectedMessage) throws UnsupportedEncodingException {
        BitIterator actualIter = new BitIterator(actualMessage);
        BitIterator outputIter = new BitIterator(expectedMessage);

        BitIterator shorterIter = actualMessage.length() < expectedMessage.length() ? actualIter : outputIter;
        int maxLen = actualMessage.length() > expectedMessage.length() ? actualMessage.length() : expectedMessage.length();

        int correctCount = 0;
        while(shorterIter.hasNext()) {
            if(outputIter.next() == actualIter.next()) {
                correctCount++;
            }
        }

        return correctCount / (maxLen * 8.0);
    }

    /*
    You must have the folders "StenographicOutputImages" and "WardenImages" in the project's root
    directory before running this method.
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
        for (final File fileWarden: Objects.requireNonNull(folderComparator.listFiles())){
            fileWarden.delete();
        }

        sendAllImagesToBeStego(folderPlain);
        sendAllImagesToWardens(folderStego, rawQuickPairWriter);


        compareMessages(folderComparator, comparatorWriter);
        rawQuickPairWriter.close();
        comparatorWriter.close();
    }


    //takes in a new Folder
    // create a loop in for all files in folder
    // send each file to be encoded with a message from a large text file containing a message
    // print each file from the stenographic techniques into a holding folder.
    // read each file from the holding folder and send them through the three wardens
    // asses the files from the other end of the stream to see which contain readable messages.
    // compile data to access the effectiveness of the techniques.
}
