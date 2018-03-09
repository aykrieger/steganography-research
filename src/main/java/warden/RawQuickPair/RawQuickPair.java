package warden.RawQuickPair;

import lib.BitBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;

public class RawQuickPair {

    private String imageFileName;
    private byte[] pixelArray = new byte[2^21]; // it is of size 2^21 since that is all of the possible combinations of the first seven bytes of RGB
    private double ratio;

    public RawQuickPair(String imageFileName) {
        this.imageFileName = imageFileName;
        this.ratio = .3;
    }

    public RawQuickPair(String imageFileName, double ratio) {
        this.imageFileName = imageFileName;
        this.ratio = ratio;
    }

    private void pixelArraySort ()throws IOException {
        BufferedImage image = ImageIO.read(new File(this.imageFileName));
        for (int y = 0; y < image.getHeight(); y ++) {
            for (int x = 0; x < image.getWidth(); x ++) {
                // Pixel
                int pixelColor = image.getRGB(x, y);
                // break up the pixel into the LSB and the other bits
                int red = ((pixelColor & 0x00fe0000) >> 17);
                int green = ((pixelColor & 0x0000fe00) >>9);
                int blue = ((pixelColor& 0x000000fe) >> 1);
                int redLastBit = ((pixelColor & 0x00010000) >> 14);
                int greenLastBit = ((pixelColor & 0x00000100) >> 7);
                int blueLastBit = (pixelColor & 0x00000001) ;

                int index = red+green+blue;

                int leastSignificantBitIndex = redLastBit + greenLastBit + blueLastBit;
                byte leastSignificantBits = pixelArray[index];
                if ((leastSignificantBits & leastSignificantBitIndex) == 0)
                    leastSignificantBits = (byte) ((leastSignificantBits & leastSignificantBitIndex) | 1);
                pixelArray[index] = leastSignificantBits;
            }
        }
    }

    private double findRatio(){
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
        double ratio = closeColors / uniqueColors;
        return ratio;
    }

    public boolean isImageStegonagraphic () throws IOException{
        this.pixelArraySort();
        return findRatio() > ratio;
    }

}
