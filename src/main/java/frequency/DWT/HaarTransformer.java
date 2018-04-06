package frequency.DWT;

public class HaarTransformer {

    public static int[][] forward(int[][] input) {
        if(isPowerOfTwo(input.length) == false || isPowerOfTwo(input[0].length) == false) {
            throw new IllegalArgumentException("Input matrix to Haar transform must be a power of 2");
        }

        int[][] output = new int[input.length][input[0].length];

        for(int i = 0; i < input.length; i++) {
            output[i] = forward(input[i]);
        }

        for(int i = 0; i < output.length; i++) {

            int[] column = new int[output[0].length];
            for (int j = 0; j < output[0].length; j++){
                column[j] = output[j][i];
            }

            column = forward(column);

            for (int j = 0; j < output[0].length; j++){
                output[j][i] = column[j];
            }
        }

        return output;
    }

    //partially from https://en.wikipedia.org/wiki/Discrete_wavelet_transform#Code_example
    //only performs 1 level
    private static int[] forward(int[] input) {
        int[] output = new int[input.length];

        int length = input.length / 2;

        for (int i = 0; i < length; i++) {
            int sum = input[i * 2] + input[i * 2 + 1];
            int difference = input[i * 2] - input[i * 2 + 1];
            output[i] = sum;
            output[length + i] = difference;
        }

        return output;
    }

    public static int[][] reverse(int[][] input) {
        if(isPowerOfTwo(input.length) == false || isPowerOfTwo(input[0].length) == false) {
            throw new IllegalArgumentException("Input matrix to Haar transform must be a power of 2");
        }

        int[][] output = new int[input.length][input[0].length];

        for(int i = 0; i < output.length; i++) {
            int[] column = new int[output[0].length];
            for (int j = 0; j < output[0].length; j++){
                column[j] = input[j][i];
            }

            column = reverse(column);

            for (int j = 0; j < output[0].length; j++){
                output[j][i] = column[j];
            }
        }

        for(int i = 0; i < input.length; i++) {
            output[i] = reverse(output[i]);
        }

        return output;
    }

    //only performs 1 level
    private static int[] reverse(int[] input) {
        int[] output = new int[input.length];

        int length = input.length / 2;

        for (int i = 0; i < length; i++) {
            //have a + b, a - b
            // (a + b) + (a - b) = 2a

            int sum = input[i];
            int difference = input[i + length];


            int originalA = (sum + difference) / 2;
            int originalB = originalA - difference;

            output[2*i] = originalA;
            output[(2*i)+1] = originalB;
        }

        return output;
    }

    private static boolean isPowerOfTwo(int num) {
        double power = Math.floor(Math.log((double)num) / Math.log(2.0));
        double result = Math.pow(2.0, power);
        return (result == (double)num);
    }
}
