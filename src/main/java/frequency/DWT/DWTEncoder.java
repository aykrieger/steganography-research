package frequency.DWT;

import lib.BitBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class DWTEncoder {
    private String imageFileName;
    private Optional<BufferedImage> stegoImage = Optional.empty();

    public DWTEncoder(String imageFileName) {
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

    public void Encode(String message) throws IOException {
        BufferedImage image = ImageIO.read(new File(this.imageFileName));
    }

    public String Decode() throws IOException {
        BitBuilder result = new BitBuilder();
        int b = 0x00;
        BufferedImage image = ImageIO.read(new File(this.imageFileName));

        return null;
    }
}
