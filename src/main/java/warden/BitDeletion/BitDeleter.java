package warden.BitDeletion;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Performs bit deletion on a stego image as described by Smith and Agaian in "Denoising and the Active Warden"
 * It sets the last 4 bit planes of each pixel's RGB values to 0, thus removing any messages hidden in the noise
 */
public class BitDeleter {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();

    public BitDeleter(String imageFileName) {
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

    // TODO add in ability to select how many LSB planes to delete
    public void ScrubImage() throws IOException {
        BufferedImage image = ImageIO.read(new File(this.imageFileName));

        for (int y = 0; y < image.getHeight(); y ++) {
            for (int x = 0; x < image.getWidth(); x ++) {
                int pixelColor = image.getRGB(x,y);

                // Remove the red last 4 LSBs
                pixelColor = (pixelColor & 0xfff0ffff);

                // Remove the green last 4 LSBs
                pixelColor = (pixelColor & 0xfffff0ff);

                // Remove the blue last 4 LSBs
                pixelColor = (pixelColor & 0xfffffff0);

                // Save the change
                image.setRGB(x, y, pixelColor);
            }
        }

        this.stegoImage = Optional.of(image);
    }
}
