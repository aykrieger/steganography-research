package lib;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class TestingLib {

    public String inputImagesDir = "";
    public String outputImagesDir = "";
    public String inputMessagesDr = "";

    final String directoryConfigFilename = "config/directory_config.txt";

    /*
    To set up this class, create a folder named "config" in this repository and save the
    "directory_config.txt" file inside. Here is an example of a directory_config.txt file:

    InputImages_Directory=/home/user/steganography-research/testImagesInput/
    OutputImages_Directory=/home/user/steganography-research/testImagesOutput/
    InputMessages_Directory=/home/user/steganography-research/testMessages/
    */

    public TestingLib() {
        try (Stream<String> stream = Files.lines(Paths.get(directoryConfigFilename))) {

            stream.forEach(line -> {
                if (line.startsWith("InputImages_Directory=")) {
                    this.inputImagesDir = line.split("InputImages_Directory=")[1];
                } else if (line.startsWith("OutputImages_Directory=")) {
                    this.outputImagesDir = line.split("OutputImages_Directory=")[1];
                } else if (line.startsWith("InputMessages_Directory=")) {
                    this.inputMessagesDr = line.split("InputMessages_Directory=")[1];
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String readTextFile(String filePath) throws IOException {
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            return new String(encoded, StandardCharsets.UTF_8);
    }
}
