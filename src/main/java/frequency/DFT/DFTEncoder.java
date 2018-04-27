package frequency.DFT;

import lib.BitBuilder;
import lib.BitIterator;
import lib.Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/*
 This DFT encoder is based on the algorithm described in "Discrete Fourier Transform as a Basis
 for Stenographic Method" by M.O. Kozina.
 (UDC 004.056.5:517.443)
 */

public class DFTEncoder implements Encoder {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();
    private List<Integer> colorComponentMasks = new ArrayList<>(Arrays.asList(0x000000FF, 0x0000FF00, 0x00FF0000));


    public DFTEncoder(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public double GetCapacityFactor() {
        return 3/32;
    }

    public void SetImage(String path) {
        this.imageFileName = path;
    }

    public String GetName() {
        return "DFT";
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
     *
     * @param message
     * @throws IOException
     */
    public void Encode(String message) throws IOException {
        message += BitIterator.END_DELIMITER;
        message += BitIterator.END_DELIMITER;
        BitIterator B = new BitIterator(message);

        BufferedImage C = ImageIO.read(new File(this.imageFileName));
        int width = C.getWidth();
        int height = C.getHeight();

        encodingLoop:
        for (int idx = 0; idx < colorComponentMasks.size(); idx++) {
            Integer mask = colorComponentMasks.get(idx);
            int[][] coefficients = forwardFourier(C, mask, idx*8);

            for (int col = 0; col < height; col += 2) {
                for (int row = 0; row < width; row += 2) {
                    if (B.hasNext() == false) {
                        C = reverseFourier(C, coefficients, mask);
                        break encodingLoop;
                    }

                    int nextByte = B.next();

                    if (nextByte == 0) {
                        for (int b = col; b <= col + 1; b++) {
                            for (int a = row; a <= row + 1; a++) {
                                coefficients[a][b] = (coefficients[a][b] / 2) * 2;
                            }
                        }
                    } else {
                        for (int b = col; b <= col + 1; b++) {
                            for (int a = row; a <= row + 1; a++) {
                                coefficients[a][b] = ((coefficients[a][b] / 2) * 2) + 1;
                            }
                        }
                    }
                }
            }

            C = reverseFourier(C, coefficients, mask);
        }


        this.stegoImage = Optional.of(C);
    }

    private int[][] forwardFourier(BufferedImage C, Integer mask, int shiftAmt) {
        int[][] pixelData = new int[C.getHeight()][C.getWidth()];

        //get the pixel data to transform with the color component from the mask
        for (int row = 0; row < C.getHeight(); row++) {
            for (int col = 0; col < C.getWidth(); col++) {
                pixelData[col][row] = (C.getRGB(col, row) & mask) >> shiftAmt;
            }
        }

        return FourierTransform.transformColorPlane(pixelData);
    }

    private BufferedImage reverseFourier(BufferedImage C, int[][] coefficients, Integer mask) {
        //apply the Fourier function
        int[][] pixelData = FourierTransform.transformColorPlane(coefficients);

        for (int row = 0; row < C.getHeight(); row++) {
            for (int col = 0; col < C.getWidth(); col++) {
                int oldColor = C.getRGB(col, row);
                int newColor = 0xFF000000 + (pixelData[col][row] << 16) +
                               (pixelData[col][row] << 8) + (pixelData[col][row]);
                newColor = (newColor & (0xFF000000 | mask)) | (oldColor & ~(0xFF000000 | mask));
                C.setRGB(col, row, newColor);
            }
        }

        return C;
    }

    public String Decode() throws IOException {
        BufferedImage S = ImageIO.read(new File(this.imageFileName));
        String decodedMessage = DecodeFromImage(S);
        // Remove the delimiter character
        return decodedMessage.substring(0, decodedMessage.length() - 1);
    }

    private String DecodeFromImage(BufferedImage S) throws IOException {
        BitBuilder result = new BitBuilder();
        int b = 0x00;

        int width = S.getWidth();
        int height = S.getHeight();

        encodingLoop:
        for (int idx = 0; idx < colorComponentMasks.size(); idx++) {

            Integer mask = colorComponentMasks.get(idx);
            int[][] coefficients = forwardFourier(S, mask, idx * 8);

            for (int j = 0; j < height; j += 2) {
                for (int i = 0; i < width; i += 2) {
                    b = (coefficients[i][j] & 0x00000001);

                    if (result.append((byte) b)) break encodingLoop;
                }
            }
        }

        return result.toString();
    }
}