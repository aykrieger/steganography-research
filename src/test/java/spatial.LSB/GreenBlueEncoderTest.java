package spatial.LSB;

import lib.DirectoryConfigReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import spatial.GreenBlue.GreenBlueEncoder;

@TestInstance(Lifecycle.PER_CLASS)
public class GreenBlueEncoderTest {

    DirectoryConfigReader directories = new DirectoryConfigReader();

    @Test
    public void encode_decode_nominal1() {
        String inputImg = directories.inputImagesDir + "green_blue_input_1.png";
        String outputImg = directories.outputImagesDir + "green_blue_output_1.png";
        GreenBlueEncoder.encode(inputImg, outputImg, "Test_Message");
    }
}
