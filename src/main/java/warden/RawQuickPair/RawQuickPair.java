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
        this.ratio = .63;
    }

    public RawQuickPair(String imageFileName, double ratio) {
        this.imageFileName = imageFileName;
        this.ratio = ratio;
    }

    private void pixelArraySort ()throws IOException {
        BufferedImage image = ImageIO.read(new File(this.imageFileName));
        //reads through the pixels of the image
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
                //the index is equal to all of the bits exepct for the LSB, this allows for all close pairs to be grouped together based on the other bits of the pixel
                //this pre sorts all of the close pixels together making the run time just 2N rather than a logramithmic speed at the cost of memory.
                int index = red+green+blue;

                int leastSignificantBitIndex = redLastBit + greenLastBit + blueLastBit;
                byte leastSignificantBits = pixelArray[index];
                ///this takes the byte inside of the pixel array and changes the bit at the position of the LSB to a 1
                leastSignificantBits = (byte) ((leastSignificantBits  ) | (1) << leastSignificantBitIndex);
                pixelArray[index] = leastSignificantBits;
            }
        }
    }

    //this takes the pixel array and calculates the number of different pixel colors and how many close colors there are
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

    //this is used to find the unique colors used for testing but not for actual use since it would hurt preformance
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

    //same as find unique colors
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

    //this uses the precentange of close colors to deterime wether or not the image was steganographic
    public boolean isImageStegonagraphic () throws IOException{
        this.pixelArraySort();
        double ratioFound = findRatio();
        return  ratioFound> this.ratio;
    }

    //this re draws the image in a new location used to determine if the image was passed on or not.
    public void writeImage(String outputFilePath) throws IOException{
        BufferedImage image = ImageIO.read(new File(this.imageFileName));
        File outputImageFile = new File(outputFilePath);
        boolean isImageStegonagraphic = isImageStegonagraphic();
        if (!isImageStegonagraphic){
            ImageIO.write(image, "png", outputImageFile);
        }
    }

}
