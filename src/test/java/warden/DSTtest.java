package warden;

import org.junit.jupiter.api.Test;
import warden.DST.DiscreteSpringTransform;


import java.io.IOException;
public class DSTtest {
    @Test
    public void sixtyFourtyToFiftyFifty() throws IOException {
        DiscreteSpringTransform warden = new DiscreteSpringTransform("InputImages/sixtyFourty.png");
        warden.writeImage("OutputImages/sixtyForty.png");

    }

    @Test
    public void distortion() throws IOException {
        DiscreteSpringTransform warden = new DiscreteSpringTransform("InputImages/dwt.png");
        warden.writeImage("OutputImages/dwtDST.png");
    }

    @Test
    public void fruity() throws IOException {
        DiscreteSpringTransform warden = new DiscreteSpringTransform("InputImages/fruit bowl.png");
        warden.writeImage("OutputImages/fruit_bowl_DST.png");
    }
}
