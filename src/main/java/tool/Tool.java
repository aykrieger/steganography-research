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
    private static String readLargeMessage () throws IOException {
        File file = new File("Messages/largeTextFile.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        StringBuilder stringBuilder = new StringBuilder();
        while ((bufferedReader.readLine()) != null){
            stringBuilder.append(bufferedReader);
        }
        return stringBuilder.toString();
    }

    //this method takes all images in the input folder and creates a Stenographic version of the image
    private static void sendAllImagesToBeStego(final File folder) throws IOException {
        String largeMessage = readLargeMessage();
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {

            //gets the name of the files
            String imageName = (imagePointer.getName());

            //gets the input file path
            String inputFileName = "InputImages/" + imageName;

            //creates unique names for all of the different stego images
            String outputFileNameLSB = "StenographicOutputImages/LSB_" + imageName;
            String outputFileNameGreenBlue = "StenographicOutputImages/GreenBlue_" + imageName;
            String outputFileNameDFT = "StenographicOutputImages/DFT_" + imageName;
            String outputFileNameDWT = "StenographicOutputImages/DWT_" + imageName;

            //finds the size of the image to figure out how large of a message to give it divide by 8 to account for char to bit
            BufferedImage image  = ImageIO.read(new File(inputFileName));
            int imageSize= image.getHeight() * image.getWidth()/16;

            //this encodes the image as LSB
            LeastSignificantBitEncoder leastSignificantBitEncoder = new LeastSignificantBitEncoder(inputFileName);
            leastSignificantBitEncoder.Encode(largeMessage.substring(0,imageSize));
            leastSignificantBitEncoder.WriteImage(outputFileNameLSB);

            //encodes image as GB secret key is 12345 since it is easy to remember
            int gbMessageSize = imageSize*2/3;
            GreenBlueEncoder.encode(inputFileName, outputFileNameGreenBlue, largeMessage.substring(0,gbMessageSize), 12345);

            //int frequenceyMessageSize = imageSize/4;
            //encodes the image as a dwt
            //DFTEncoder dftEncoder = new DFTEncoder(inputFileName);
            //dftEncoder.Encode(largeMessage.substring(0,frequenceyMessageSize));
            //dftEncoder.WriteImage(outputFileNameDWT);

            //encodes the image as a dwt
            //DWTEncoder dwtEncoder = new DWTEncoder(inputFileName);
            //dwtEncoder.Encode(largeMessage.substring(0,frequenceyMessageSize));
            //dwtEncoder.WriteImage(outputFileNameDWT);
        }
    }

    private static void sendAllImagesToWardens(final File folder) throws IOException {
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {
            //gets the name of the files
            String imageName = (imagePointer.getName());

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


    private static void compareMessages(final File folder) throws IOException {
        String largeMessage = readLargeMessage();
        for (final File imagePointer : Objects.requireNonNull(folder.listFiles())) {

            //gets the name of the files
            String imageName = (imagePointer.getName());

            //gets the input file path
            String inputFileName = "WardenImages/" + imageName;


            //finds the size of the image to figure out how large of a message to give it divide by 8 to account for char to bit
            BufferedImage image  = ImageIO.read(new File(inputFileName));
            int imageSize= image.getHeight() * image.getWidth()/16;
            if (imageName.contains("LSB_")){
                LeastSignificantBitEncoder leastSignificantBitEncoder = new LeastSignificantBitEncoder(inputFileName);
                leastSignificantBitEncoder.Decode();
            }
            else if (imageName.contains("GreenBlue_")){
                imageSize=imageSize*2/3;
                GreenBlueEncoder.decode(inputFileName,12345);
            }
            else if (imageName.contains("DFT_")){
                imageSize=imageSize/4;
            }
            else if (imageName.contains("DST_")){
                imageSize=imageSize/4;
            }

            //int frequenceyMessageSize = imageSize/4;
            //encodes the image as a dwt
            //DFTEncoder dftEncoder = new DFTEncoder(inputFileName);
            //dftEncoder.Encode(largeMessage.substring(0,frequenceyMessageSize));
            //dftEncoder.WriteImage(outputFileNameDWT);

            //encodes the image as a dwt
            //DWTEncoder dwtEncoder = new DWTEncoder(inputFileName);
            //dwtEncoder.WriteImage(outputFileNameDWT);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedWriter ratioWriter = new BufferedWriter(new FileWriter("src/test/java/warden/ratioRQP.txt"));
        ratioWriter.write(""); // clear file
        ratioWriter.close();
        final File folderStego = new File("InputImages");
        final File folderWardens = new File("StenographicOutputImages");
        sendAllImagesToBeStego(folderStego);
        sendAllImagesToWardens(folderWardens);
    }


    //takes in a new Folder
    // create a loop in for all files in folder
    // send each file to be encoded with a message from a large text file containing a message
    // print each file from the stenographic techniques into a holding folder.
    // read each file from the holding folder and send them through the three wardens
    // asses the files from the other end of the stream to see which contain readable messages.
    // compile data to access the effectiveness of the techniques.
}
