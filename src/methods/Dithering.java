package methods;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public class Dithering {

    public static final int[][][] D = {
            {{0,2},{3,1}},
            {{6,8,4},{1,0,3},{5,2,7}},
            {{0,8,2,10},{12,4,14,6},{3,11,1,9},{15,7,13,5}},
            {{0,8,2,10,16},{12,4,14,6,17},{3,11,1,9,18},{15,7,13,5,19},{21,23,22,24,25}},
            {{}},
            {{}},
            {{0,48,12,60,3,51,15,63},{32,16,44,28,35,19,47,31},{8,56,4,52,11,59,7,55},{40,24,36,20,43,27,39,23},{2,50,14,62,1,49,13,61},{34,18,46,30,33,17,45,29},{10,58,6,54,9,57,5,53},{42,26,38,22,41,25,37,21}}
    };

    private static final int threshold = 255/2;

    public static Imagem limiarSimples(Imagem image) {
        for (int y = 0; y < image.getAltura(); y++) {
            for (int x = 0; x < image.getLargura(); x++) {
                if (image.getPixel(y, x, 0) < threshold) {
                    image.setPixel(y, x, 0, 0, 0);
                } else {
                    image.setPixel(y, x, 255, 255, 255);
                }
            }
        }

        return image;
    }

    public  static Imagem modulacaoAleatoria(Imagem image) {
        for (int y = 0; y < image.getAltura(); y++) {
            for (int x = 0; x < image.getLargura(); x++) {
                double temp = image.getPixel(y, x, 0) + random();
                if (temp < threshold) {
                    image.setPixel(y, x, 0, 0, 0);
                } else {
                    image.setPixel(y, x, 255, 255, 255);
                }
            }
        }
        return image;
    }

    public static Imagem periodicoDispersao(Imagem image, int N) {
        Imagem imageOut = quantizar(image, N * N);
        int width = (int) (Math.pow(2, 8) / (N * N));
        int[][] mask = generateBayerMatrix(N);
        for (int y = 0; y < imageOut.getAltura(); y++) {
            for (int x = 0; x < imageOut.getLargura(); x++) {
                if (imageOut.getPixel(y, x, 0) < width * mask[x % N][y % N] ) {
                    imageOut.setPixel(y, x, 0, 0, 0);
                } else {
                    imageOut.setPixel(y, x, 255, 255, 255);
                }
            }
        }
        return imageOut;
    }

    public static Imagem periodicoAglomeracao(Imagem image, int N) {
        Imagem imageIn = quantizar(image, N * N + 1);
        Imagem imageOut = new Imagem(imageIn.getAltura() * N, imageIn.getLargura() * N, imageIn.getBuffer().getType());
        int width = (int) (Math.pow(2, 8) / (N * N + 1));
        int[][] mask = generateBayerMatrix(N);
        //int[][] mask = D[N - 2];
        System.out.println(Arrays.deepToString(mask));
        for (int y = 0; y < imageIn.getAltura(); y++) {
            for (int x = 0; x < imageIn.getLargura(); x++) {
                int intensity = imageIn.getPixel(y, x, 0) / width;
                for (int j = 0; j < mask.length; j++) {
                    for (int i = 0; i < mask[j].length; i++) {
                        if (mask[j][i] < intensity) {
                            imageOut.setPixel(y * mask.length + j, x * mask.length + i, 255, 255, 255);
                        } else {
                            imageOut.setPixel(y * mask.length + j, x * mask.length + i, 0, 0, 0);
                        }
                    }
                }
            }
        }
        return imageOut;
    }

    public static Imagem aperiodicoDispersao(Imagem image) {
        int error;
        for (int y = 0; y < image.getAltura(); y++) {
            for (int x = 0; x < image.getLargura(); x++) {
                if (image.getPixel(y, x, 0) < threshold) {
                    error = image.getPixel(y, x, 0) - 0;
                    image.setPixel(y, x, 0,0,0);
                } else {
                    error = image.getPixel(y, x, 0) - 255;
                    image.setPixel(y, x, 255,255,255);
                }

                if (x + 1 < image.getLargura() && y + 1 < image.getAltura()) {
                    int pxEast = image.getPixel(y, x + 1, 0) + ((int) (3/8.0 * error));
                    int pxSouth = image.getPixel(y + 1, x, 0)+ ((int) (3/8.0 * error));
                    int pxSoutheast = image.getPixel(y + 1, x + 1, 0) + ((int) (1/4.0 * error));

                    image.setPixel(y, x + 1, pxEast, pxEast, pxEast);
                    image.setPixel(y + 1, x, pxSouth, pxSouth, pxSouth);
                    image.setPixel(y + 1, x + 1, pxSoutheast, pxSoutheast, pxSoutheast);
                }

            }
        }
        return image;
    }

    private static Imagem quantizar(Imagem image, int grayscale) {
        int width = (int) (Math.pow(2, 8) / grayscale);
        for (int y = 0; y < image.getAltura(); y++) {
            for (int x = 0; x < image.getLargura(); x++) {
                int px = width * (image.getPixel(y, x, 0) / width);
                image.setPixel(y, x, px, px, px);
            }
        }
        return image;
    }

    private static int random() {
        return (new Random()).nextInt(threshold) * 2 - threshold;
    }


    public static int[][] generateBayerMatrix(int N) {
        int[][] Mask = {{0,2},{3,1}};
        int maskOrder = 2;
        while (maskOrder < N) {
            int[][] U = generateOnesMatrix(maskOrder);
            int[][] M11 = multiply(4, Mask);
            int[][] M12 = sum(multiply(4, Mask), multiply(2, U));
            int[][] M21 = sum(multiply(4, Mask), multiply(3, U));
            int[][] M22 = sum(multiply(4, Mask), U);
            int[][] top = concat(M11, M12, 1);
            int[][] down = concat(M21, M22, 1);
            Mask = concat(top, down, 0);
            maskOrder = 2 * maskOrder;
        }
        return Mask;
    }

    private static int[][] generateOnesMatrix(int N) {
        int[][] U = new int[N][N];
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++)
                U[y][x] = 1;
        }
        return U;
    }

    private static int[][] multiply(int number, int M[][]) {
        int[][] MOut = new int[M.length][M.length];
        for (int y = 0; y < M.length; y++) {
            for (int x = 0; x < M[y].length; x++)
                MOut[y][x] = number * M[y][x];
        }
        return MOut;
    }

    private static int[][] sum(int[][] A, int[][] B) {
        int[][] MOut = new int[A.length][A.length];
        for (int y = 0; y < A.length; y++) {
            for (int x = 0; x < A[y].length; x++)
                MOut[y][x] = A[y][x] + B[y][x];
        }
        return MOut;
    }

    private static int[][] concat(int[][] A, int[][] B, int axis) {
        if (axis == 0) {
            int[][] MOut = new int[A.length + B.length][A[0].length];
            for (int y = 0; y < A.length; y++) { MOut[y] = A[y]; }
            for (int y = 0; y < B.length; y++) { MOut[A.length + y] = B[y]; }
            return MOut;
        } else if (axis == 1) {
            int[][] MOut = new int[A.length][A[0].length + B[0].length];
            for (int y = 0; y < A.length; y++) {
                for (int x = 0; x < A[y].length + B[y].length; x++) {
                    if (x < A[y].length) {  MOut[y][x] = A[y][x]; }
                    else { MOut[y][x] = B[y][x - B[y].length]; }
                }
            }
            return MOut;
        }
        return null;
    }

}
