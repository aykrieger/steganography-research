package warden.BitDeletion;

import lib.BitBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class RawQuickPair {

    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();
    private byte[] pixelArray = new byte[2^21]; // it is of size 2^21 since that is all of the possible combinations of the first seven bytes of RGB

    public RawQuickPair(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    private void pixelArraySort ()throws IOException {
        BufferedImage image = ImageIO.read(new File(this.imageFileName));
        for (int y = 0; y < image.getHeight(); y ++) {
            for (int x = 0; x < image.getWidth(); x ++) {
                // Pixel
                int pixelColor = image.getRGB(x, y);
                // break up the pixel into the LSB and the other bits

                int redLastBit = ((pixelColor & 0x00010000) >> 14);
                int greenLastBit = ((pixelColor & 0x00000100) >> 7);
                int blueLastBit = (pixelColor & 0x00000001) ;

                int index = (pixelColor & 0x00fefefe);

                int leastSignificantBitIndex = redLastBit + greenLastBit + blueLastBit;
                byte leastSignificantBits = pixelArray[index];
                if ((leastSignificantBits & leastSignificantBitIndex) == 0)
                    leastSignificantBits = (byte) ((leastSignificantBits & leastSignificantBitIndex) | 1);
                pixelArray[index] = leastSignificantBits;
            }
        }
    }

    private float findRatio(){
        int uniqueColors = 0;
        int closeColors =0;
        for (byte leastSignificantBits : pixelArray) {
            int numberOfOnes = Integer.bitCount((int) leastSignificantBits);
            if (numberOfOnes == 1)
                uniqueColors++;
            else if (numberOfOnes > 1) {
                uniqueColors += numberOfOnes;
                closeColors += numberOfOnes;
            }
        }
        float ratio = closeColors / uniqueColors;
        return ratio;
    }

    public boolean isImageStegonagraphic () throws IOException{
        this.pixelArraySort();
        return findRatio() > .3;
    }

}
