package frequency.DWT;

import lib.BitBuilder;
import lib.BitIterator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Optional;


/**
 * Impl based off of Kamila, Roy, and Changder, "A DWT based Steganography Scheme with Image Block Partitioning"
 * Uses a 2D Haar Wavelet transform to encode message bits
 */

// TODO replace variable names and properly implement
public class DWTEncoder {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();
    private int p = 2; //block width
    private int k = 6; //for case 3, we will embed bits in all segments of the 3 x 2 block

    //TODO Allow different embedding strategies? Can choose strategy from Enum and set p and k based off that
    public DWTEncoder(String imageFileName) {
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

    //TODO implement
    //TODO Allow different embedding strategies?

    /**
     * Encodes the hidden message using Case 3 for now
     * We are handling the encryption separately, so we do not need to worry about the Cantor Encryption Strategy
     * @param message
     * @throws IOException
     */
    public void Encode(String message) throws IOException {
        //We do not need to construct the decimal array, as the iterator handles this
        BitIterator B = new BitIterator(message);

        BufferedImage C = ImageIO.read(new File(this.imageFileName)); //size is MxN
        BufferedImage I = apply2DHaarDWT(C);

        //Embed all of the message data into the transformed image blocks

        //for each color component: R, G, B:
        //  while there is message data and image data:
        //      form a 3x2 block of image data; the blocks are taken from the following bands:
        //          HH HH
        //          HL HL
        //          LH LH
        //      embed a bit of the message in the LSB of each of these values for the current color component

        //Store the inverted image
        this.stegoImage = Optional.of(invert2DHaarDWT(I));
    }

    //TODO implement
    private BufferedImage apply2DHaarDWT(BufferedImage C) {
        return C;
    }

    private BufferedImage invert2DHaarDWT(BufferedImage I) {
        return I;
    }

    //TODO implement
    public String Decode() throws IOException {
        BitBuilder result = new BitBuilder();
        int b = 0x00;
        BufferedImage S = ImageIO.read(new File(this.imageFileName));
        BufferedImage I = apply2DHaarDWT(S);

        //for each color component: R, G, B:
        //  while there is message data left to get and image data:
        //      form a 3x2 block of image data; the blocks are taken from the following bands:
        //          HH HH
        //          HL HL
        //          LH LH
        //      take the last bit from the LSB of each of these values for the current color component

        //return final string formed from bytes
        return null;
    }
}
