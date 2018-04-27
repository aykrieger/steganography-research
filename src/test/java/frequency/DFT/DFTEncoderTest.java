package frequency.DFT;

import lib.TestingLib;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

public class DFTEncoderTest {

    private TestingLib testingLib = new TestingLib();

    @Test
    public void encodeDecode_nominal1() throws IOException {
        // Status: Passing
        String inputImgPath = testingLib.inputImagesDir + "dwt.png";
        String outputImgPath = testingLib.outputImagesDir + "dft_result.png";
        String inputMes = "Test Message";
        String expectedMes = "Test Message";
        DFTEncoder dftEncoder = new DFTEncoder(inputImgPath);
        dftEncoder.Encode(inputMes);
        dftEncoder.WriteImage(outputImgPath);

        DFTEncoder dftDecoder = new DFTEncoder(outputImgPath);
        String result = dftDecoder.Decode();
        assertTrue(result.equals(expectedMes));
    }

    @Test
    public void encodeDecode_garbageCharacters() throws IOException {
        // Status: Passing
        String inputImgPath = testingLib.inputImagesDir + "dwt.png";
        String outputImgPath = testingLib.outputImagesDir + "dft_result.png";
        String inputMes = "Garbage Characters: {)(*jk2&fjs98j2rgdsg32j2@#2j15sd(*kjd9";
        String expectedMes = "Garbage Characters: {)(*jk2&fjs98j2rgdsg32j2@#2j15sd(*kjd9";
        DFTEncoder dftEncoder = new DFTEncoder(inputImgPath);
        dftEncoder.Encode(inputMes);
        dftEncoder.WriteImage(outputImgPath);

        DFTEncoder dftDecoder = new DFTEncoder(outputImgPath);
        String result = dftDecoder.Decode();
        assertTrue(result.equals(expectedMes));
    }

    @Test
    public void encodeDecode_shortMessage() throws IOException {
        // Status: Passing
        String inputImgPath = testingLib.inputImagesDir + "dwt.png";
        String outputImgPath = testingLib.outputImagesDir + "dft_result.png";
        String inputMes = "A";
        String expectedMes = "A";
        DFTEncoder dftEncoder = new DFTEncoder(inputImgPath);
        dftEncoder.Encode(inputMes);
        dftEncoder.WriteImage(outputImgPath);

        DFTEncoder dftDecoder = new DFTEncoder(outputImgPath);
        String result = dftDecoder.Decode();
        assertTrue(result.equals(expectedMes));
    }
}
