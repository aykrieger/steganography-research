package frequency.DFT;

public class FourierTransformer {

    public static int[][] forward(int[][] input) {
        return input;


        //
    }

    //partially from https://en.wikipedia.org/wiki/Discrete_wavelet_transform#Code_example
    //only performs 1 level
    private static int[][] transform2x2block(int[][] input) {

        int[][] result = new int[4][4];

        result[0][0] = (input[0][0] + input[0][1] + input[1][0] + input[1][1]) / 2;
        result[0][1] = (input[0][0] - input[0][1] + input[1][0] - input[1][1]) / 2;
        result[1][0] = (input[0][0] + input[0][1] - input[1][0] - input[1][1]) / 2;
        result[0][1] = (input[0][0] - input[0][1] - input[1][0] + input[1][1]) / 2;

        return result;
    }

    public static int[][] reverse(int[][] input) {
        return input;
    }

}
