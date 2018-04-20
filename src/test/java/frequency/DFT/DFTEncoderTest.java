package frequency.DFT;

import lib.TestingLib;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class DFTEncoderTest {

    private TestingLib testingLib = new TestingLib();

    @Test
    public void encodeDecode_nominal1() throws IOException {
        // Status: Not Passing
        String inputImgPath = testingLib.inputImagesDir + "dwt.png";
        String outputImgPath = testingLib.outputImagesDir + "dft_result.png";
        String inputMes = "Test Message";
        String expectedMes = "Test Message";
        DFTEncoder dftEncoder = new DFTEncoder(inputImgPath);
        dftEncoder.Encode(inputMes);
        dftEncoder.WriteImage(outputImgPath);

        DFTEncoder dftDecoder = new DFTEncoder(outputImgPath);
        String result = dftDecoder.Decode();
        // There's a bug where the BitBuilder doesn't see the String delimiter character,
        // maybe we could add two delimiter characters so the BitBuilder can't miss it
        assertTrue(result.equals(expectedMes));
    }
}
