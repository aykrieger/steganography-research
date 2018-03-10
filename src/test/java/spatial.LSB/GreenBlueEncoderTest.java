package spatial.LSB;

import lib.DirectoryConfigReader;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import spatial.GreenBlue.GreenBlueEncoder;

public class GreenBlueEncoderTest {

    DirectoryConfigReader directories = new DirectoryConfigReader();

    @Test
    public void encode_decode_nominal1() {
        String inputImg = directories.inputImagesDir + "green_blue_input_1.png";
        String outputImg = directories.outputImagesDir + "green_blue_output_1.png";
        GreenBlueEncoder.encode(inputImg, outputImg, "Test_Message");
    }

    @Test
    public void scrambleMessage_nominal1() {
        String inputMes = "Test Message";

    }

    @Test
    public void swapBits_bits_are_both_1() {
        int input_num = Integer.parseInt("0100100111", 2); // decimal value is 295
        int pos_1 = 0;
        int pos_2 = 2;
        // Both of the bits in position 1 and 9 are 1, so the input number should be unchanged
        assertEquals(295, GreenBlueEncoder.swapBits(input_num, pos_1, pos_2));
    }

    @Test
    public void swapBits_bits_are_different() {
        int input_num = Integer.parseInt("0100100111", 2); // decimal value is 295
        int pos_1 = 2;
        int pos_2 = 7;
        assertEquals(419, GreenBlueEncoder.swapBits(input_num, pos_1, pos_2));
    }
}
