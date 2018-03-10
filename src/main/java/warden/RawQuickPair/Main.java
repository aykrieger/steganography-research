package warden.RawQuickPair;

import org.apache.commons.cli.*;
import warden.RawQuickPair.RawQuickPair;

import java.io.IOException;
import java.text.ParseException;

public class Main {

    public static void main(String[] args) {

        Options options = CreateCmdLineOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        // Commented this out because "parse(options, args)" is an unhandled exception
        // and was giving me an error
        /*
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("RawQuickPair", options);

            System.exit(1);
            return;
        }


        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");

        try {
            RunWarden(inputFilePath, outputFilePath);
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        */
    }

    private static Options CreateCmdLineOptions() {
        Options options = new Options();

        Option input = new Option("i", "input", true, "input image path");
        input.setRequired(true);
        options.addOption(input);


        return options;
    }

    private static void RunWarden(String inputFilePath, String outputFilePath) throws IOException {
        // Commented this out because the method ScrubImage was not found
        /*
        RawQuickPair warden = new RawQuickPair(inputFilePath);
        warden.ScrubImage();
        if (warden.WriteImage(outputFilePath)){
            formatter.printHelp("Image has Steganography");
        }
        */
    }
}
