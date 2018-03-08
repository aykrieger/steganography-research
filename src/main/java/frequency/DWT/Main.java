package frequency.DWT;

import org.apache.commons.cli.*;

import java.io.IOException;

public class Main {

    public static void main( String[] args ) {

        Options options = CreateCmdLineOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("DWT encoder", options);

            System.exit(1);
            return;
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        String messageText = cmd.getOptionValue("message") + '\0';

        if (cmd.hasOption("encode")) {
            try {
                EncodeMessage(inputFilePath,
                        outputFilePath,
                        messageText);
            } catch(IOException e) {
                System.err.println(e);
            }
        }
        else if(cmd.hasOption("decode")) {
            try {
                DecodeMessage(outputFilePath);
            } catch(IOException e) {
                System.err.println(e);
            }
        }

        else {
            System.out.println("No action selected:\n");
            formatter.printHelp("DWT encoder", options);
        }
    }

    private static Options CreateCmdLineOptions() {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input image path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output image path");
        output.setRequired(false);
        options.addOption(output);

        Option message = new Option("m", "message", true, "stego message");
        output.setRequired(false);
        options.addOption(message);

        Option encode = new Option("e", "encode", false, "encode message");
        output.setRequired(false);
        options.addOption(encode);

        Option decode = new Option("d", "decode", false, "decode message");
        output.setRequired(false);
        options.addOption(decode);

        return options;
    }

    public static void EncodeMessage(String inputFileName, String outputFileName, String message) throws IOException{
        DWTEncoder encoder = new DWTEncoder(inputFileName);

        encoder.Encode(message);
        encoder.WriteImage(outputFileName);

    }

    public static void DecodeMessage(String fileName) throws IOException {
        DWTEncoder decoder = new DWTEncoder(fileName);

        System.out.println(decoder.Decode());
    }
}
