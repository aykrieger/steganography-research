package frequency.DFT;

import lib.BitBuilder;
import lib.BitIterator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * Impl based off of Kamila, Roy, and Changder, "A DWT based Steganography Scheme with Image Block Partitioning"
 * Uses a 2D Haar Wavelet transform to encode message bits
 */

public class DFTEncoder {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();
    private List<Integer> colorComponentMasks = new ArrayList<>(Arrays.asList(0x000000FF, 0x0000FF00, 0x00FF0000));


    public DFTEncoder(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public boolean WriteImage(String outputFileName) throws IOException {
        if(this.stegoImage.isPresent() == false) {
            return false;
        }

        File outputImageFile = new File(outputFileName);
        ImageIO.write(this.stegoImage.get(), "png", outputImageFile);

        return true;
    }

    /**
     * Encodes the hidden message using Case 3 for now
     * We are handling the encryption separately, so we do not need to worry about the Cantor Encryption Strategy
     * @param message
     * @throws IOException
     */
    public void Encode(String message) throws IOException {
        message += BitIterator.END_DELIMITER;
        BitIterator B = new BitIterator(message);

        BufferedImage C = ImageIO.read(new File(this.imageFileName));
        int width = C.getWidth();
        int height = C.getHeight();
    }


    public String Decode() throws IOException {
        BufferedImage S = ImageIO.read(new File(this.imageFileName));
        return DecodeFromImage(S);
    }

    private String DecodeFromImage(BufferedImage S) throws IOException {
        BitBuilder result = new BitBuilder();
        int b = 0x00;

        int width = S.getWidth();
        int height = S.getHeight();

        return "";
    }
}