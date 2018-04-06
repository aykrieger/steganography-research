package frequency.DWT;

import lib.BitBuilder;
import lib.BitIterator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;


/**
 * Impl based off of Kamila, Roy, and Changder, "A DWT based Steganography Scheme with Image Block Partitioning"
 * Uses a 2D Haar Wavelet transform to encode message bits
 */

public class DWTEncoder {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();

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

    /**
     * Encodes the hidden message using Case 3 for now
     * We are handling the encryption separately, so we do not need to worry about the Cantor Encryption Strategy
     * @param message
     * @throws IOException
     */
    public void Encode(String message) throws IOException {
        message += BitIterator.END_DELIMITER;
        BitIterator B = new BitIterator(message);

        BufferedImage C = ImageIO.read(new File(this.imageFileName)); //size is MxN
        int width = C.getWidth();
        int height = C.getHeight();

        //Embed all of the message data into the transformed image blocks
        int[][] coefficients = forwardHaar(C);

        encodingLoop:
        for (int j = height / 2; j < height; j++) {
            for (int i = width/2; i < width; i++) {
                if (B.hasNext() == false) break encodingLoop;

                int nextByte = B.next();
                int coefficientParody = coefficients[i][j] & 0x00000001;

                if (coefficientParody == 1 && nextByte == 0) {
                    //effectively adding two instead of subtracting one
                    //done to preserve value through haar inverse
                    coefficients[i][j] += 1;
                }
                else if (coefficientParody == 0 && nextByte == 1) {
                    coefficients[i][j] += 1;
                }
            }
        }

        //Store the inverted image
        //Inverted image is the final stego image with the message embedded in the DWT values but not the final RGB
        this.stegoImage = Optional.of(reverseHaar(C, coefficients));

//        test(coefficients, height, width);
    }

    private int[][] forwardHaar(BufferedImage C) {
        int[][] pixelData = new int[C.getHeight()][C.getWidth()];

        //get the pixel data to transform
        for (int row = 0; row < C.getHeight(); row++) {
            for (int col = 0; col < C.getWidth(); col++) {
                //for now, just take the B plane
                //TODO
                pixelData[col][row] = C.getRGB(col, row) & 0x000000FF;
            }
        }

        return HaarTransformer.forward(pixelData);
    }

    private BufferedImage reverseHaar(BufferedImage C, int[][] coefficients) {
        //apply the haar function
        int[][] pixelData = HaarTransformer.reverse(coefficients);

        for (int row = 0; row < C.getHeight(); row++) {
            for (int col = 0; col < C.getWidth(); col++) {
                //for now, just take the B plane
                //TODO
                int newColor = 0xFF000000 + (pixelData[col][row] << 16) + (pixelData[col][row] << 8) + (pixelData[col][row]);
                C.setRGB(col, row, newColor);
            }
        }

        return C;
    }

    private BufferedImage visualizeHaar(BufferedImage C, int[][] coeffs) {
        double max = coeffs[0][0];
        for(int i = 0; i < coeffs.length; i++) {
            for(int j = 0; j < coeffs[0].length; j++) {
                if(max < coeffs[i][j]) {
                    max = coeffs[i][j];
                }
            }
        }

        //create the haar image
        for (int row = 0; row < coeffs.length; row++) {
            for (int col = 0; col < coeffs[0].length; col++) {
                //for now, just take the B plane
                //TODO
                double val = 255 * (coeffs[col][row] / max);
                int newColor = 0xFF000000 + ((int) val << 16) + ((int) val << 8) + ((int) val);
                C.setRGB(col, row, newColor);
            }
        }

        return C;
    }

    public String Decode() throws IOException {
        BitBuilder result = new BitBuilder();
        int b = 0x00;

        BufferedImage S = ImageIO.read(new File(this.imageFileName));
        int width = S.getWidth();
        int height = S.getHeight();

        int[][] coefficients = forwardHaar(S);

        encodingLoop:
        for(int j = height / 2; j < height; j++) {
            for(int i = width/2; i < width; i++) {
                b = (coefficients[i][j] & 0x00000001);

                if(result.append((byte) b)) break encodingLoop;
            }
        }

        //return final string formed from bytes
        return result.toString();
    }
}