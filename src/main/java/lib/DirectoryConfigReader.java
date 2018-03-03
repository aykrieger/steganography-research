package lib;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class DirectoryConfigReader {

    public String inputImagesDir = "";
    public String outputImagesDir = "";

    final String directoryConfigFilename = "config/directory_config.txt";

    // Stores the user defined paths for the input and output image directories
    // so they can be used for testing
    public DirectoryConfigReader() {
        try (Stream<String> stream = Files.lines(Paths.get(directoryConfigFilename))) {

            stream.forEach(line -> {
                if (line.startsWith("InputImages_Directory=")) {
                    this.inputImagesDir = line.split("InputImages_Directory=")[1];
                } else if (line.startsWith("OutputImages_Directory=")) {
                    this.outputImagesDir = line.split("OutputImages_Directory=")[1];
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
