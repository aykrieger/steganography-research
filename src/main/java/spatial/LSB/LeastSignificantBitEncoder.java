package spatial.LSB;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class LeastSignificantBitEncoder {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();

    public LeastSignificantBitEncoder(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public void Encode(String message) throws IOException {
        message += Message.END_DELIMITER;
        Message bitMessage;

        try {
            bitMessage = new Message(message);

        } catch (UnsupportedEncodingException e){
            throw new RuntimeException("Could not encode message: " + e.getMessage());
        }

        BufferedImage image = ImageIO.read(new File(this.imageFileName));
        for (int y = 0; y < image.getHeight(); y ++) {
            for (int x = 0; x < image.getWidth(); x ++) {
                int pixelColor = image.getRGB(x,y);

                // Set the red value
                if(bitMessage.hasNext()) {
                    pixelColor = (pixelColor & 0xfffeffff) | (bitMessage.next() << 16);
                }

                // Set the green value
                if(bitMessage.hasNext()) {
                    pixelColor = (pixelColor & 0xfffffeff) | (bitMessage.next() << 8);
                }

                // Set the blue value
                if(bitMessage.hasNext()) {
                    pixelColor = (pixelColor & 0xfffffffe) | bitMessage.next();
                }

                image.setRGB(x, y, pixelColor);

                if(bitMessage.hasNext() == false) {
                    this.stegoImage = Optional.of(image);
                    return;
                }
            }
        }
    }

    public boolean WriteImage(String outputFileName) throws IOException {
        if(this.stegoImage.isPresent() == false) {
            return false;
        }

        File outputImageFile = new File(outputFileName);
        ImageIO.write(this.stegoImage.get(), "png", outputImageFile);

        return true;
    }

    public String Decode() throws IOException {
        Message result = new Message();

        int b = 0x00;
        int i = 0;

        BufferedImage image = ImageIO.read(new File(this.imageFileName));
        for (int y = 0; y < image.getHeight(); y ++) {
            for (int x = 0; x < image.getWidth(); x ++) {
                int pixelColor = image.getRGB(x, y);

                if (i >= 8) {
                    if (b == Message.END_DELIMITER) {
                        return result.toString();
                    }

                    result.append((byte) b);
                    i = 0;
                    b = 0x00;
                }

                // Pull out the red value's bit
                b = (b << 1) | ((pixelColor & 0x00010000) >> 16);
                i ++;


                if (i >= 8) {
                    if (b == Message.END_DELIMITER) {
                        return result.toString();
                    }

                    result.append((byte) b);
                    i = 0;
                    b = 0x00;
                }

                // Pull out the green value's bit
                b = (b << 1) | ((pixelColor & 0x00000100) >> 8);
                i ++;

                if (i >= 8) {
                    if (b == Message.END_DELIMITER) {
                        return result.toString();
                    }

                    result.append((byte) b);
                    i = 0;
                    b = 0x00;
                }

                // Pull out the blue value's bit
                b = (b << 1) | (pixelColor & 0x00000001);
                i ++;
            }
        }

        return result.toString();
    }

    private class Message implements Iterator {
        private List<Byte> message;
        private Byte currentByte;
        private int bitsIteratedInByte = 0;
        private final int BITS_IN_A_BYTE = 8;
        private static final char END_DELIMITER = '\0';

        private Message() {
            message = new ArrayList<>();
        }

        private Message(String message) throws UnsupportedEncodingException {
            this.message = new ArrayList<Byte>();

            for(byte b : message.getBytes("ASCII")) {
                this.message.add(b);
            }

            if(this.message.size() > 0) {
                this.currentByte = this.message.remove(0);
            }
        }

        public void append(Byte b) {
            message.add(b);
        }

        @Override
        public boolean hasNext() {
            return message.isEmpty() == false || bitsIteratedInByte < 7;
        }

        //Returns the next bit in the message
        //Returns in the form of a byte due to necessity
        @Override
        public Byte next() {
            int b = currentByte >> (BITS_IN_A_BYTE - (bitsIteratedInByte + 1));
            b = b & 0x01;

            bitsIteratedInByte ++;

            if (bitsIteratedInByte >= 8) {
                currentByte = message.remove(0);
                bitsIteratedInByte = 0;
            }

            return (byte) b;
        }

        public String toString() {
            StringBuilder msg = new StringBuilder();

            for(byte b : message) {
                msg.append((char)b);
            }
            return msg.toString();
        }
    }
}
