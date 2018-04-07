package warden;

import org.junit.jupiter.api.Test;
import warden.DST.DiscreteSpringTransform;
import warden.RawQuickPair.RawQuickPair;


import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
public class DSTtest {
    @Test
    public void sixtyFourtyToFiftyFifty() throws IOException {
        DiscreteSpringTransform warden = new DiscreteSpringTransform("InputImages/sixtyFourty.png");
        warden.writeImage("TestImage/sixtyFourtyDST");
    }

    @Test
    public void distortion() throws IOException {
        DiscreteSpringTransform warden = new DiscreteSpringTransform("InputImages/dwt.png");
        warden.writeImage("OutputImages/dwtDST");
    }
}
