package spatial.LSB;

import lib.BitBuilder;
import lib.BitIterator;
import lib.Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

public class LeastSignificantBitEncoder implements Encoder {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();

    public LeastSignificantBitEncoder() {
        this.imageFileName = null;
    }
    public LeastSignificantBitEncoder(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public double GetCapacityFactor() {
        return 3/8;
    }

    public void SetImage(String path) {
        this.imageFileName = path;
    }

    public String GetName(){
        return "LSB";
    }

    public void Encode(String message) throws IOException {
        message += BitIterator.END_DELIMITER;
        BitIterator bitMessage;

        try {
            bitMessage = new BitIterator(message);

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

                if(!bitMessage.hasNext()) {
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
        BitBuilder result = new BitBuilder();
        int b = 0x00;
        BufferedImage image = ImageIO.read(new File(this.imageFileName));

        for (int y = 0; y < image.getHeight(); y ++) {
            for (int x = 0; x < image.getWidth(); x ++) {
                int pixelColor = image.getRGB(x, y);

                // Pull out the red value's bit
                b = 0x00;
                b = (b << 1) | ((pixelColor & 0x00010000) >> 16);

                if (result.append((byte) b)) {
                    return result.toString();
                }

                // Pull out the green value's bit
                b = 0x00;
                b = (b << 1) | ((pixelColor & 0x00000100) >> 8);

                if (result.append((byte) b)) {
                    return result.toString();
                }

                // Pull out the blue value's bit
                b = 0x00;
                b = (b << 1) | (pixelColor & 0x00000001);

                if (result.append((byte) b)) {
                    return result.toString();
                }
            }
        }

        //return whatever we have if no endpoint was reached
        return result.toString();
    }
}
