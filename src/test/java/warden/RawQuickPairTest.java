package warden;

import org.junit.jupiter.api.Test;
import warden.RawQuickPair.RawQuickPair;


import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;

public class RawQuickPairTest {
    @Test
    public void oneColor() throws IOException {
        RawQuickPair warden = new RawQuickPair("InputImages/blackPixelArt.png");
        int uniqueColors = warden.findUniqueColors();
        int closeColors = warden.findCloseColors();
        double ratio = warden.findRatio();
        assertEquals(uniqueColors, 1);
        assertEquals(closeColors, 0);
        assertEquals(ratio, 0.0);
    }

    @Test
    public void elevenColors() throws IOException {
        RawQuickPair warden = new RawQuickPair("InputImages/elevenColorPixelArt.png");
        int uniqueColors = warden.findUniqueColors();
        int closeColors = warden.findCloseColors();
        double ratio = warden.findRatio();
        assertEquals(uniqueColors, 11);
        assertEquals(closeColors, 0);
        assertEquals(ratio, 0.0);
    }

    @Test
    public void onlyClosePairs() throws IOException {
        RawQuickPair warden = new RawQuickPair("InputImages/onlyClosePairs.png");
        int uniqueColors = warden.findUniqueColors();
        int closeColors = warden.findCloseColors();
        double ratio = warden.findRatio();
        assertEquals(uniqueColors, 4);
        assertEquals(closeColors, 4);
        assertEquals(ratio, 1.0);
    }
}
