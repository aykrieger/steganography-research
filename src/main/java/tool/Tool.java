package tool;

import frequency.DFT.DFTEncoder;
import frequency.DWT.DWTEncoder;
import spatial.GreenBlue.GreenBlueEncoder;
import spatial.LSB.LeastSignificantBitEncoder;
import warden.BitDeletion.BitDeleter;
import warden.DST.DiscreteSpringTransform;
import warden.RawQuickPair.RawQuickPair;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

public class Tool {

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
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {

            //gets the name of the files
            String imageName = (imagePointer.getName());
            if (!imageName.equals("Thumbs.db")) {
                //gets the input file path
                String inputFileName = "ToolImages/" + imageName;

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
            }
        }
    }

    private static void sendAllImagesToWardens(final File folder) throws IOException {
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


                DiscreteSpringTransform discreteSpringTransform = new DiscreteSpringTransform(inputFileName);
                discreteSpringTransform.writeImage(outputFileNameDST);
            }
        }
    }

    private static void compareMessages(final File folder) throws IOException {
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
                    ratioWriter.write("\n" + imageName + ":\n");
                    ratioWriter.write(incomingMessage + "\n\n");
                }
            }
        }
        ratioWriter.close();
    }

    private static double stringComparator(String incomingMessage, String expectedMessage){
        int k = expectedMessage.length();
        int j = incomingMessage.length();
        if (incomingMessage.length()!=expectedMessage.length()){
            return -1.0;
        }
        double numberOfRightChar = 0.0;
        for (int i = 0; i < incomingMessage.length(); i++) {
            if (incomingMessage.charAt(i) == expectedMessage.charAt(i)){
                numberOfRightChar++;
            }
        }
        return numberOfRightChar/ (double) incomingMessage.length();
    }

    public static void main(String[] args) throws IOException {
        BufferedWriter ratioWriter = new BufferedWriter(new FileWriter("src/test/java/warden/ratioRQP.txt"));
        ratioWriter.write(""); // clear file
        ratioWriter.close();
        BufferedWriter comparatorWriter = new BufferedWriter(new FileWriter("src/main/java/tool/ratioSucessfullyTransmittedMessage.txt"));
        comparatorWriter.write(""); // clear file
        comparatorWriter.close();

        final File folderPlain = new File("ToolImages");
        final File folderStego = new File("StenographicOutputImages");
        final File folderComparator = new File("WardenImages");
        sendAllImagesToBeStego(folderPlain);
        sendAllImagesToWardens(folderStego);
        compareMessages(folderComparator);

        String message = readLargeMessage();
//        Double score = Robustness.calculate(folderStego, new LeastSignificantBitEncoder(), message);
//        System.out.println("LSB : " + score);
    }


    //takes in a new Folder
    // create a loop in for all files in folder
    // send each file to be encoded with a message from a large text file containing a message
    // print each file from the stenographic techniques into a holding folder.
    // read each file from the holding folder and send them through the three wardens
    // asses the files from the other end of the stream to see which contain readable messages.
    // compile data to access the effectiveness of the techniques.
}
