package lib;

/**
 * Created by james on 3/30/18.
 */
public class TypeConverter {

    public static int[][] doubleMatrixToIntMatrix(double[][] mat) {
        int[][] retMat = new int[mat.length][mat[0].length];

        for (int row = 0; row < mat.length; row++) {
            for (int col = 0; col < mat[0].length; col++) {
                retMat[col][row] = (int)mat[col][row];
            }
        }

        return retMat;
    }

    public static double[][] intMatrixToDoubleMatrix(int[][] mat) {
        double[][] retMat = new double[mat.length][mat[0].length];

        for (int row = 0; row < mat.length; row++) {
            for (int col = 0; col < mat[0].length; col++) {
                retMat[col][row] = (double)mat[col][row];
            }
        }

        return retMat;
    }
}
