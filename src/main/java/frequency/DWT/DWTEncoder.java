package frequency.DWT;

import jwave.Transform;
import jwave.transforms.FastWaveletTransform;
import jwave.transforms.wavelets.haar.Haar1;
import lib.BitBuilder;
import lib.BitIterator;
import lib.TypeConverter;

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
    private int p = 2; //block width
    private int k = 6; //for case 3, we will embed bits in all segments of the 3 x 2 block

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
        //We do not need to construct the decimal array, as the iterator handles this
        BitIterator B = new BitIterator(message);

        BufferedImage C = ImageIO.read(new File(this.imageFileName)); //size is MxN
        int width = C.getWidth();
        int height = C.getHeight();

        //Embed all of the message data into the transformed image blocks

        //for component <- {R: 0xfffeffff, G: 0xfffffeff, B: 0xfffffffe}
        //    offset <- {R: 16, G: 8, B: 0}:
        //  while B is not empty and I has next:
        //      block <- [[next HH pixel, next HH pixel],
        //                [next HL pixel, next HL pixel],
        //                [next LH pixel, next LH pixel]]
        //      for segment <- block:
        //          segment = (segment & component) | (nextBit(B) << offset)

        int[][] coefficients = forwardHaar(C);

        encodingLoop:
        for(int j = height / 2; j < height; j++) {
            for(int i = width/2; i < width; i++) {
                if(B.hasNext() == false) break encodingLoop;

                coefficients[i][j] = (coefficients[i][j] & 0xFFFFFFFE) | B.next();

                System.out.println(coefficients[i][j]);
            }
        }

        //Store the inverted image
        //Inverted image is the final stego image with the message embedded in the DWT values but not the final RGB
        this.stegoImage = Optional.of(reverseHaar(C, coefficients));

        test(coefficients, height, width);
    }

    private int[][] forwardHaar(BufferedImage C) {
        double[][] pixelData = new double[C.getHeight()][C.getWidth()];

        //get the pixel data to transform
        for (int row = 0; row < C.getHeight(); row++) {
            for (int col = 0; col < C.getWidth(); col++) {
                //for now, just take the B plane
                //TODO
                pixelData[col][row] = C.getRGB(col, row) & 0x000000FF;
            }
        }

        //apply the haar function
        Transform t = new Transform( new FastWaveletTransform( new Haar1()));
        pixelData = t.forward(pixelData, 1, 1);

        return TypeConverter.doubleMatrixToIntMatrix(pixelData);
    }

    private BufferedImage reverseHaar(BufferedImage C, int[][] coefficients) {
        //apply the haar function
        Transform t = new Transform( new FastWaveletTransform( new Haar1()));
        double[][] pixelData = t.reverse(TypeConverter.intMatrixToDoubleMatrix(coefficients), 1, 1);

        for (int row = 0; row < C.getHeight(); row++) {
            for (int col = 0; col < C.getWidth(); col++) {
                //for now, just take the B plane
                //TODO
                int newColor = 0xFF000000 + ((int) pixelData[col][row] << 16) + ((int) pixelData[col][row] << 8) + ((int) pixelData[col][row]);
                C.setRGB(col, row, newColor);
            }
        }

        return C;
    }

    //TODO Remove
    //Tests how the transform affects the coefficients
    private void test(int[][] coefficients, int height, int width) {
        Transform t = new Transform( new FastWaveletTransform( new Haar1()));
        double[][] pixelData = t.reverse(TypeConverter.intMatrixToDoubleMatrix(coefficients), 1, 1);

        coefficients = TypeConverter.doubleMatrixToIntMatrix(t.forward(pixelData, 1, 1));
        BitBuilder result = new BitBuilder();
        int b = 0x00;

        encodingLoop:
        for(int j = height / 2; j < height; j++) {
            for(int i = width/2; i < width; i++) {
                System.out.println(coefficients[i][j]);

                b = (coefficients[i][j] & 0x00000001);

                if(result.append((byte) b)) break encodingLoop;
            }
        }

        //return final string formed from bytes
        System.out.println(result.toString());
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

        //for component <- {R: 0x00010000, G: 0x00000100, B: 0x00000001}
        //    offset <- {R: 16, G: 8, B: 0}:
        //  while I has next and End Delimiter has not been reached:
        //      block <- [[next HH pixel, next HH pixel],
        //                [next HL pixel, next HL pixel],
        //                [next LH pixel, next LH pixel]]
        //      for segment <- block:
        //          result.append(segment & component) >> offset)

        int[][] coefficients = forwardHaar(S);

        encodingLoop:
        for(int j = height / 2; j < height; j++) {
            for(int i = width/2; i < width; i++) {
                System.out.println(coefficients[i][j]);

                b = (coefficients[i][j] & 0x00000001);

                if(result.append((byte) b)) break encodingLoop;
            }
        }

        //return final string formed from bytes
        return result.toString();
    }
}
