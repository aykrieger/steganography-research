package spatial.GreenBlue;

import lib.BitBuilder;
import lib.BitIterator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;

/*
 This encoding scheme embeds the message within the 2nd to 8th bit in the blue or green
 channel (or both) of a pixel. The embedded bit position is chosen with a hash function. This makes
 the bit selection behave randomly and allows the decoder to select the same randomly selected bits.
 */
public class GreenBlueEncoder {

    public static void encode(String inputImgDir, String outputImgDir, String origMessage,
                              int secretKey) throws IOException{
        if (origMessage.isEmpty()) {
            throw new IllegalArgumentException("Input message must not be empty");
        }
        /*
        Step 1
        Select Secret Key Sk
        Convert message into binary stream M
        */

        /*
        Step 2
        Scramble original message. Replace 1st bit with 8th bit, 2nd with 7th, 3rd with 6th,
        and 4th with 5th. This gives us Mm.
         */
        String scrambledMes = GreenBlueEncoder.scrambleMessage(origMessage);

        BitIterator bitMessage;
        try {
            bitMessage = new BitIterator(scrambledMes);
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException("Could not encode message: " + e.getMessage());
        }

        BufferedImage image = ImageIO.read(new File(inputImgDir));
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int imageSize = imageHeight * imageWidth;

        // Green component of image is only encoded if the message cannot
        // fit in the Blue component
        boolean encodeGreen = true;

        encodingLoopBlue:
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {

                if (!bitMessage.hasNext()) {
                    // The message fits in the Blue component of the image, so
                    // we do not have to encode the Green component
                    encodeGreen = false;
                    break encodingLoopBlue;
                }

                int pixelVal = image.getRGB(x,y);

                /*
                Step 3
                Calculate R0, the number of 0's in the Red channel of the current pixel.
                Calculate R1 = Sk - R0
                 */
                int redChannel = GreenBlueEncoder.getRed(pixelVal);
                int num0inRed = BitIterator.BITS_IN_A_BYTE - Integer.bitCount(redChannel);
                int keyRed = secretKey - num0inRed;

                /*
                Step 4
                Calculate position of bit in Blue channel Pb = ((Iz + R1) % 7) + 1
                 */
                int blueChannel = GreenBlueEncoder.getBlue(pixelVal);
                int bluePos = ((imageSize + keyRed) % 7) + 1;

                /*
                Step 5
                if bit value of Pb position of the current pixel in the Blue channel and current
                bit of Mm are equal,
                    LSB position of Blue component is set to 0
                else
                    LSB position of Blue component is set to 1
                 */

                int bitAtKeyBlue = GreenBlueEncoder.getBitAt(blueChannel, bluePos);
                Byte nextBit = bitMessage.next();

                if (bitAtKeyBlue == nextBit) {
                    // LSB position of Blue component is set to 0
                    // LSB of Blue component in pixel is at position 0
                    pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 0, 0);
                } else {
                    // LSB position of Blue component is set to 1
                    // LSB of Blue component in pixel is at position 0
                    pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 0, 1);
                }
                image.setRGB(x, y, pixelVal);
            }
        }

        /*
        Step 6
        Repeat steps 3 to 5 until Mm is finished or Blue component is finished
         */

        if (encodeGreen) {
            /*
            Step 7
            if Blue component is finished but Mm still remains,
            Select the starting pixel of cover image
            */
            encodingLoopGreen:
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {

                    if (!bitMessage.hasNext()) {
                        break encodingLoopGreen;
                    }

                    int pixelVal = image.getRGB(x,y);

                    /*
                    Step 8
                    Calculate R2, the number of 1s in the Red channel of the current pixel.
                    Calculate R3 = Sk - R2
                     */
                    int redChannel = GreenBlueEncoder.getRed(pixelVal);
                    int num1inRed = Integer.bitCount(redChannel);
                    int keyRed = secretKey - num1inRed;

                    /*
                    Step 9
                    Calculate the position of bit in Green channel Pg = ((Iz + R3) % 7) + 1
                     */
                    int greenChannel = GreenBlueEncoder.getGreen(pixelVal);
                    int greenPos = ((imageSize + keyRed) % 7) + 1;

                    /*
                    Step 10
                    if bit value of Pg position of the current pixel in the Green channel and current
                    bit of Mm are equal,
                        LSB position of Green component is set to 0
                    else
                        LSB position of Green component is set to 1
                     */
                    int bitAtKeyBlue = GreenBlueEncoder.getBitAt(greenChannel, greenPos);
                    Byte nextBit = bitMessage.next();

                    if (bitAtKeyBlue == nextBit) {
                        // LSB position of Green component is set to 0
                        // LSB of Blue component in pixel is at position 8
                        pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 8, 0);
                    } else {
                        // LSB position of Green component is set to 1
                        // LSB of Blue component in pixel is at position 8
                        pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 8, 1);
                    }
                    image.setRGB(x, y, pixelVal);
                }
            }
        }

        if (bitMessage.hasNext()) {
            throw new RuntimeException("Could not fit message in image");
        }

        /*
        Step 11
        Repeat steps 8 to 10 until the bit stream is finished
         */

        // Write the image to a file
        File outputImageFile = new File(outputImgDir);
        ImageIO.write(image, "png", outputImageFile);
    }

    public static String decode(String inputImage, int secretKey) throws IOException {
        BitBuilder bitBuildRes = new BitBuilder();

        BufferedImage image = ImageIO.read(new File(inputImage));
        int imageHeight = image.getHeight();
        int imageWidth = image.getWidth();
        int imageSize = imageHeight * imageWidth;

        // Green component of image is only decoded if the message delimiter has
        // not been reached while decoding the Blue component
        boolean decodeGreen = true;

        decodingLoopBlue:
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {

                /*
                Step 1
                if LSB of Blue component of the current pixel is 0,
                    Calculate R0, number of 0's in Red channel of current pixel
                    Calculate Rz = Sk - R0
                    Calculate Pb = ((Iz + R1) % 7) + 1
                    Select the bit at position Pb and store it into the bit stream
                else
                    Go to next pixel
                 */
                int pixelVal = image.getRGB(x, y);
                int redChannel = GreenBlueEncoder.getRed(pixelVal);
                int num0inRed = BitIterator.BITS_IN_A_BYTE - Integer.bitCount(redChannel);
                int keyRed = secretKey - num0inRed;

                int blueChannel = GreenBlueEncoder.getBlue(pixelVal);
                int bitAtBlueLSB = GreenBlueEncoder.getBitAt(blueChannel, 0);

                int bluePos = ((imageSize + keyRed) % 7) + 1;
                int bitAtBluePos = GreenBlueEncoder.getBitAt(blueChannel, bluePos);
                // We know the original message bit is the same as the bit at the Blue Position
                // in the Blue Channel
                if (bitAtBlueLSB == 0) {
                    if (bitBuildRes.append((byte) bitAtBluePos)) {
                        // Reached the delimiter character
                        decodeGreen = false;
                        break decodingLoopBlue;
                    }
                // We know the original message bit is the same as the bit at the Blue Position
                // in the Blue Channel
                } else {
                    if (bitBuildRes.append((byte) (bitAtBluePos ^ 1))) {
                        // Reached the delimiter character
                        decodeGreen = false;
                        break decodingLoopBlue;
                    }
                }
            }
        }

        /*
        Step 2
        Repeat step 1 until the bit stream is finished or the blue component is finished
         */

        /*
        Step 3
        if Blue component is finished, but bit stream still remains in image,
            Select the starting pixel of the cover image
         */
        if (decodeGreen) {
            decodingLoopGreen:
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {

                /*
                Step 1
                if LSB of Blue component of the current pixel is 0,
                    Calculate R0, number of 0's in Red channel of current pixel
                    Calculate Rz = Sk - R0
                    Calculate Pb = ((Iz + R1) % 7) + 1
                    Select the bit at position Pb and store it into the bit stream
                else
                    Go to next pixel
                 */

                /*
                Step 4
                if the LSB of Green component of the current pixel is 0,
                    Calculate R2, number of 1's in Red channel of the current pixel
                    Calculate R3 = Sk - R2
                    Calculate Pg = ((Iz + R3) % 7) + 1
                    Select the bit at position Pg and store it in the bit stream
                 */
                    int pixelVal = image.getRGB(x, y);
                    int greenChannel = GreenBlueEncoder.getGreen(pixelVal);
                    if (0 == GreenBlueEncoder.getBitAt(greenChannel, 0)) {
                        int redChannel = GreenBlueEncoder.getRed(pixelVal);
                        int num1inRed = Integer.bitCount(redChannel);
                        int keyRed = secretKey - num1inRed;

                        int greenPos = ((imageSize + keyRed) % 7) + 1;
                        int bitAtGreenPos = GreenBlueEncoder.getBitAt(greenChannel, greenPos);
                        if (bitBuildRes.append((byte) bitAtGreenPos)) {
                            break decodingLoopGreen;
                        }
                    }
                }
            }
        }


        /*
        Step 5
        Repeat the previous step until the bit stream is finished
         */

        String scrambledMessage = bitBuildRes.toString();
        // Remove delimiter character
        scrambledMessage = scrambledMessage.substring(0, scrambledMessage.length() - 1);
        /*
        Step 6
        Unscramble the message. Replace 1st bit with 8th bit, 2nd with 7th, 3rd with 6th,
        and 4th with 5th. This gives us Mm.
         */
        return GreenBlueEncoder.unscrambleMessage(scrambledMessage);
    }

    public static String scrambleMessage(String message) {
        BitIterator bitMessage;
        try {
            bitMessage = new BitIterator(message);
        } catch (UnsupportedEncodingException e){
            throw new RuntimeException("Could not encode message: " + e.getMessage());
        }

        StringBuilder scrambledMesBuilder = new StringBuilder();

        while (bitMessage.hasNext()) {

            ArrayList<String> bitList = new ArrayList<>();
            // Convert the binary number to a list of strings where each
            // string is either 0 or 1
            for (int bitCount = 0; bitCount < BitIterator.BITS_IN_A_BYTE; bitCount++) {
                if (bitMessage.hasNext()) {
                    bitList.add(String.valueOf(bitMessage.next()));
                } else {
                    break;
                }
            }
            // Swaps bits in positions 1 and 8, 2 and 7, 3 and 6, and 4 and 5
            // If we do not have a set of 8 bits, do not swap
            if (bitList.size() == 8) {
                Collections.swap(bitList, 0, 7);
                Collections.swap(bitList, 1, 6);
                Collections.swap(bitList, 2, 5);
                Collections.swap(bitList, 3, 4);
            }

            String bitString = String.join("", bitList);
            scrambledMesBuilder.append((char) Integer.parseInt(bitString, 2));
        }
        return scrambledMesBuilder.toString();
    }

    public static String unscrambleMessage(String scrambledMes) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < scrambledMes.length(); i++) {
            int currentByte = (int) scrambledMes.charAt(i);
            currentByte = GreenBlueEncoder.swapBits(currentByte, 0, 7);
            currentByte = GreenBlueEncoder.swapBits(currentByte, 1, 6);
            currentByte = GreenBlueEncoder.swapBits(currentByte, 2, 5);
            currentByte = GreenBlueEncoder.swapBits(currentByte, 3, 4);
            builder.append((char) currentByte);
        }
        String mesWithDelimiter = builder.toString();
        return mesWithDelimiter.substring(0, mesWithDelimiter.length() - 1);
    }

    /**
     * Returns the given number after swapping the two specified bits.
     * The least significant bit is in position 0, second LSB is in position 1, etc.
     * For example, given the decimal number 10, swap bits in positions 0 and 3:
     *     Original: 1 0 1 0 (10 in decimal)
     *      Returns: 0 0 1 1 (3 in decimal)
     * The position numbers should also be less than 64 to prevent overflow.
     * @param num  Original number
     * @param pos1 Position of the first bit to swap
     * @param pos2 Position of the second bit to swap
     * @return     Original number with swapped bits
     */
    public static int swapBits(int num, int pos1, int pos2) {
        // Bit at position 1
        int bit1 = (num >> pos1) & 1;
        // Bit at position 2
        int bit2 = (num >> pos2) & 1;
        // If the bits are the same, no need to swap
        if (bit1 == bit2) {
            return num;
        }
        // Create a mask from the two bit positions
        int mask = (1 << pos1) | (1 << pos2);
        // XOR changes both the bits from 1 to 0 or 0 to 1
        return num ^ mask;
    }

    public static int getRed(int pixelValue) {
        return (pixelValue >> 16) & 0xFF;
    }

    public static int getGreen(int pixelValue) {
        return (pixelValue >> 8) & 0xFF;
    }

    public static int getBlue(int pixelValue) {
        return (pixelValue >> 0) & 0xFF;
    }

    /**
     * Returns the bit at the specified position.
     *
     * @param num Number to check
     * @param pos Position of the bit, where pos = 0 indicates the LSB
     * @return
     */
    public static int getBitAt(int num, int pos) {
        return (num >> pos) & 1;
    }

    /**
     * Sets the bit to either 0 or 1 at the specified position.
     *
     * @param num      Number to check
     * @param pos      Position of the bit, where pos = 0 indicates the LSB
     * @param bitValue Either 0 or 1
     * @return
     */
    public static int setBitAt(int num, int pos, int bitValue) {
        if (bitValue == 0) {
            return num & ~(1 << pos);
        } else {
            return num | (1 << pos);
        }
    }
}
