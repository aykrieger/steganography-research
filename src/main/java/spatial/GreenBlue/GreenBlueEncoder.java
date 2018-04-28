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

 This encoder is based on the algorithm in "An Improved Color Image Steganography Technique in
 Spatial Domain by Saikat Mondal, Rameswar Debnath, and Borun Kumar Mondal.
 (DOI: 10.1109/ICECE.2016.7853987)
 */
public class GreenBlueEncoder {

    public static void encode(String inputImgDir, String outputImgDir, String origMessage,
                              int secretKey) throws IOException {
        if (origMessage.isEmpty()) {
            throw new IllegalArgumentException("Input message must not be empty");
        }

        String scrambledMes = GreenBlueEncoder.scrambleMessage(origMessage) +
                              BitIterator.END_DELIMITER;

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
                pixelVal = GreenBlueEncoder.encodePixel(pixelVal, bitMessage, secretKey,
                        imageSize, GBencoderType.BLUE);
                image.setRGB(x, y, pixelVal);
            }
        }

        if (encodeGreen) {
            encodingLoopGreen:
            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {

                    if (!bitMessage.hasNext()) {
                        break encodingLoopGreen;
                    }
                    int pixelVal = image.getRGB(x,y);
                    pixelVal = GreenBlueEncoder.encodePixel(pixelVal, bitMessage, secretKey,
                            imageSize, GBencoderType.GREEN);
                    image.setRGB(x, y, pixelVal);
                }
            }
        }

        if (bitMessage.hasNext()) {
            throw new RuntimeException("Could not fit message in image");
        }

        File outputImageFile = new File(outputImgDir);
        ImageIO.write(image, "png", outputImageFile);
    }

    public static int encodePixel(int pixelVal, BitIterator bitMessage, int secretKey,
                                  int imageSize, GBencoderType encodeType) {

        int redChannel = GreenBlueEncoder.getRed(pixelVal);

        int numBitsInRed;
        if (encodeType == GBencoderType.BLUE) {
            // Uses the number of 0 bits in Red channel for Blue encoding
            numBitsInRed = BitIterator.BITS_IN_A_BYTE - Integer.bitCount(redChannel);
        } else {
            // Uses the number of 1 bits in Red channel for Green encoding
            numBitsInRed = Integer.bitCount(redChannel);
        }

        int keyRed = secretKey - numBitsInRed;

        int colorByte;
        if (encodeType == GBencoderType.BLUE) {
            colorByte = GreenBlueEncoder.getBlue(pixelVal);
        } else {
            colorByte = GreenBlueEncoder.getGreen(pixelVal);
        }

        // This hash function is used so the encoder and decoder must share the same secret key
        int bitPos = ((imageSize + keyRed) % 7) + 1;

        int bitFromColorByte = GreenBlueEncoder.getBitAt(colorByte, bitPos);
        Byte nextBit = bitMessage.next();

        // If the next bit of the message and the bit at the hashed position match,
        // set the LSB of the specified color component to 0, else set it to 1
        if (encodeType == GBencoderType.BLUE) {
            // LSB position of Blue component is at bit position 0
            if (nextBit == bitFromColorByte) {
                pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 0, 0);
            } else {
                pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 0, 1);
            }
        } else {
            // LSB position of Green component is at bit position 8
            if (nextBit == bitFromColorByte) {
                pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 8, 0);
            } else {
                pixelVal = GreenBlueEncoder.setBitAt(pixelVal, 8, 1);
            }
        }
        return pixelVal;
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

                int pixelVal = image.getRGB(x, y);
                int bitAtColorByte = GreenBlueEncoder.decodePixel(pixelVal, secretKey,
                        imageSize, GBencoderType.BLUE);
                int bitAtBlueLSB = GreenBlueEncoder.getBitAt(pixelVal, 0);
                // We know the original message bit is the same as the bit at the hashed position
                // in the Blue component
                if (bitAtBlueLSB == 0) {
                    if (bitBuildRes.append((byte) bitAtColorByte)) {
                        // Reached the delimiter character
                        decodeGreen = false;
                        break decodingLoopBlue;
                    }
                // We know the original message bit is the same as the bit at the hashed position
                // in the Blue component
                } else {
                    if (bitBuildRes.append((byte) (bitAtColorByte ^ 1))) {
                        // Reached the delimiter character
                        decodeGreen = false;
                        break decodingLoopBlue;
                    }
                }
            }
        }

        if (decodeGreen) {
            decodingLoopGreen:
            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {

                    int pixelVal = image.getRGB(x, y);
                    int bitAtColorByte = GreenBlueEncoder.decodePixel(pixelVal, secretKey,
                            imageSize, GBencoderType.GREEN);
                    // LSB position of Green component is at bit position 8
                    int bitAtGreenLSB = GreenBlueEncoder.getBitAt(pixelVal, 8);
                    // We know the original message bit is the same as the bit at the hashed
                    // position in the Green component
                    if (bitAtGreenLSB == 0) {
                        if (bitBuildRes.append((byte) bitAtColorByte)) {
                            // Reached the delimiter character
                            break decodingLoopGreen;
                        }
                    // We know the original message bit is the complement the bit at the
                    // hashed position in the Green component
                    } else {
                        if (bitBuildRes.append((byte) (bitAtColorByte ^ 1))) {
                            // Reached the delimiter character
                            break decodingLoopGreen;
                        }
                    }
                }
            }
        }
        String scrambledMessage = bitBuildRes.toString();
        return GreenBlueEncoder.unscrambleMessage(scrambledMessage);
    }

    public static int decodePixel(int pixelVal, int secretKey, int imageSize,
                                  GBencoderType encodeType) {

        int redChannel = GreenBlueEncoder.getRed(pixelVal);
        int numBitsInRed;
        if (encodeType == GBencoderType.BLUE) {
            // Uses the number of 0 bits in Red channel for Blue encoding
            numBitsInRed = BitIterator.BITS_IN_A_BYTE - Integer.bitCount(redChannel);
        } else {
            // Uses the number of 1 bits in Red channel for Green encoding
            numBitsInRed = Integer.bitCount(redChannel);
        }
        int keyRed = secretKey - numBitsInRed;

        int colorByte;
        if (encodeType == GBencoderType.BLUE) {
            colorByte = GreenBlueEncoder.getBlue(pixelVal);
        } else {
            colorByte = GreenBlueEncoder.getGreen(pixelVal);
        }
        // This hash function is used so the encoder and decoder must share the same secret key
        int bitPos = ((imageSize + keyRed) % 7) + 1;
        return GreenBlueEncoder.getBitAt(colorByte, bitPos);
    }

    public static String scrambleMessage(String message) {
        message += BitIterator.END_DELIMITER;
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
            if (bitList.size() == BitIterator.BITS_IN_A_BYTE) {
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
        // Remove the delimiter character
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
