package md.vnastasi.vtp;

import java.security.SecureRandom;

public final class Sampler {

    static int[][] randomMatrix(int size) {
        var randomizer = new SecureRandom();
        var matrix = new int[size][size];
        for (var i = 0; i < size; i++) {
            for (var j = 0; j < size; j++) {
                matrix[i][j] = randomizer.nextInt(-999, 1000);
            }
        }
        return matrix;
    }
}
