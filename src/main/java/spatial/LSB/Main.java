package spatial.LSB;

import java.io.*;

public class Main {

    //TODO make image file and message a cmdline parameter
    public static void main( String[] args ) {
        StringBuilder message = new StringBuilder();

        // get message to encode
        try (BufferedReader reader = new BufferedReader(new FileReader("Messages/message.txt"))){
            String line;

            while((line = reader.readLine()) != null) {
                message.append(line);
                message.append("\n");
            }
        } catch(IOException e) {
            System.err.println(e);
        }

        try {
            EncodeMessage("InputImages/lsb.png",
                    "OutputImages/lsb_out.png",
                    message.toString());
        } catch(IOException e) {
            System.err.println(e);
        }

        try {
            DecodeMessage("OutputImages/lsb_out.png");
        } catch(IOException e) {
            System.err.println(e);
        }
    }

    public static void EncodeMessage(String inputFileName, String outputFileName, String message) throws IOException{
        LeastSignificantBitEncoder encoder = new LeastSignificantBitEncoder(inputFileName);

        encoder.Encode(message);
        encoder.WriteImage(outputFileName);

    }

    public static void DecodeMessage(String fileName) throws IOException {
        LeastSignificantBitEncoder decoder = new LeastSignificantBitEncoder(fileName);

        System.out.println(decoder.Decode());
    }
}
