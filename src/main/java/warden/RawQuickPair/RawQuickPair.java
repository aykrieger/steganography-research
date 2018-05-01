package warden.RawQuickPair;



import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class RawQuickPair {

    private String imageFileName;
    private int exponet = (int) Math.pow(2,24);
    private byte[] pixelArray = new byte[exponet]; // it is of size 2^21 since that is all of the possible combinations of the first seven bytes of RGB
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
                int red = ((pixelColor & 0x00fe0000) >> 2);
                int green = ((pixelColor & 0x0000fe00) >> 1);
                int blue = ((pixelColor & 0x000000fe));
                int redLastBit = ((pixelColor & 0x00010000) >> 14);
                int greenLastBit = ((pixelColor & 0x00000100) >> 7);
                int blueLastBit = (pixelColor & 0x00000001) ;

                int index = red+green+blue;

                int leastSignificantBitIndex = redLastBit + greenLastBit + blueLastBit;
                byte leastSignificantBits = pixelArray[index];
                    leastSignificantBits = (byte) ((leastSignificantBits  ) | (1) << leastSignificantBitIndex);
                pixelArray[index] = leastSignificantBits;
            }
        }
    }

    public double findRatio(){
        double uniqueColors = 0;
        double closeColors =0;
        double signedBit = 0;
        double ratio = 0;
        for (byte leastSignificantBits : pixelArray) {
            if (leastSignificantBits < 0) {
                signedBit = 1;
                leastSignificantBits += 128;
            }
            double numberOfOnes = Integer.bitCount((int) leastSignificantBits) + signedBit;
            signedBit = 0;
            if (numberOfOnes == 1)
                uniqueColors++;
            else if (numberOfOnes > 1) {
                uniqueColors += numberOfOnes;
                closeColors += numberOfOnes;
            }
        }
        if (uniqueColors != 0){
            ratio = closeColors / uniqueColors;}
        return ratio;
    }

    public Integer findUniqueColors() throws IOException{
        this.pixelArraySort();
        int uniqueColors = 0;
        int signedBit = 0;
        for (byte leastSignificantBits : pixelArray) {
            if (leastSignificantBits < 0) {
                leastSignificantBits += 128;
                signedBit = 1;
            }
            int numberOfOnes = Integer.bitCount((int) leastSignificantBits) + signedBit;
            signedBit = 0;
            if (numberOfOnes != 0)
                uniqueColors += numberOfOnes;
        }
        return uniqueColors;
    }

    public Integer findCloseColors() throws IOException{
        this.pixelArraySort();
        int closeColors =0;
        int signedBit = 0;
        for (byte leastSignificantBits : pixelArray) {
            if (leastSignificantBits < 0) {
                signedBit = 1;
                leastSignificantBits += 128;
            }
            int numberOfOnes = Integer.bitCount((int) leastSignificantBits) + signedBit;
            signedBit = 0;
            if (numberOfOnes > 1) {
                closeColors += numberOfOnes;
            }
        }
        return closeColors;
    }

    public boolean isImageStegonagraphic () throws IOException{
        this.pixelArraySort();
        double ratioFound = findRatio();
        return  ratioFound> this.ratio;
    }

    public void writeImage(String outputFilePath) throws IOException{
        BufferedImage image = ImageIO.read(new File(this.imageFileName));
        File outputImageFile = new File(outputFilePath);
        boolean isImageStegonagraphic = isImageStegonagraphic();
        if (!isImageStegonagraphic){
            ImageIO.write(image, "png", outputImageFile);
        }
    }

}
