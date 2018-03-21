package frequency.DWT;



/**
 * Implementation of the 2D Haar Wavelet transform
 * Based on https://en.wikipedia.org/wiki/Haar_wavelet
 * Check out the JWave library: https://github.com/cscheiblich/JWave/wiki/HowTo
 */
public class HaarTransform {

    // TODO
    public static double[][] TwoDimensional(double[][] input) {
        Transform t = new Transform( new FastWaveletTransform( new Haar02( )  );
        return t.forward(input);
    }


    // TODO
    public static int[][] InvertedTwoDimensional(int[][] input) {
        return input;
    }
}
