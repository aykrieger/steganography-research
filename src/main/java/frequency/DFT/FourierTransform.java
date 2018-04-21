package frequency.DFT;

public class FourierTransform {

    public static int[][] transformColorPlane(int[][] input) {

        int inputLength = input.length;
        int inputWidth = input[0].length;

        if ( (input.length % 2 != 0) || (input[0].length % 2 != 0)) {
            throw new IllegalArgumentException("Input matrix must have an even length and width,");
        }

        int[][] result = new int[inputLength][inputWidth];
        for(int i = 0; i < inputLength; i += 2) {
            for(int j = 0; j < inputWidth; j += 2) {
                int[][] temp = {{input[i][j], input[i][j + 1]},
                                {input[i + 1][j], input[i + 1][j + 1]}};
                temp = transform2x2block(temp);
                result[i][j] = temp[0][0];
                result[i][j + 1] = temp[0][1];
                result[i + 1][j] = temp[1][0];
                result[i + 1][j + 1] = temp[1][1];
            }
        }

        return result;
    }

    private static int[][] transform2x2block(int[][] input) {

        int[][] result = new int[2][2];

        result[0][0] = (input[0][0] + input[0][1] + input[1][0] + input[1][1]) / 2;
        result[0][1] = (input[0][0] - input[0][1] + input[1][0] - input[1][1]) / 2;
        result[1][0] = (input[0][0] + input[0][1] - input[1][0] - input[1][1]) / 2;
        result[1][1] = (input[0][0] - input[0][1] - input[1][0] + input[1][1]) / 2;

        return result;
    }

}
