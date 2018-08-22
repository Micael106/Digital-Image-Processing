package methods;

public class MathematicalMorphology {

    public static class Binary {
        public static Imagem erode(Imagem image, int[][] B) {
            Imagem erodedImage = new Imagem(image.getAltura(), image.getLargura(), image.getBuffer().getType());
            for (int y = 0; y < image.getAltura(); y++) {
                if (y + B.length > image.getAltura()) {
                    continue;
                }
                for (int x = 0; x < image.getLargura(); x++) {
                    if (x + B[0].length > image.getLargura()) {
                        continue;
                    }
                    int count = 0;
                    for (int j = 0; j < B.length; j++) {
                        for (int i = 0; i < B[j].length; i++)
                            if (image.getPixel(y + j, x + i, 0) / 255 == B[j][i])
                                count += 1;
                    }
                    if (count == B.length * B[0].length) {
                        int pxValue = 255;
                        erodedImage.setPixel(y + B.length / 2, x + B[0].length / 2, pxValue, pxValue, pxValue);
                    }
                }
            }
            return erodedImage;
        }

        public static Imagem dilate(Imagem image, int[][] B) {
            Imagem dilatedImage = new Imagem(image.getAltura(), image.getLargura(), image.getBuffer().getType());
            for (int y = 0; y < image.getAltura(); y++) {
                if (y + B.length > image.getAltura()) {
                    continue;
                }
                for (int x = 0; x < image.getLargura(); x++) {
                    if (x + B[0].length > image.getLargura()) {
                        continue;
                    }
                    int count = 0;
                    for (int j = 0; j < B.length; j++) {
                        for (int i = 0; i < B[j].length; i++)
                            if (image.getPixel(y + j, x + i, 0) / 255 == B[j][i])
                                count += 1;
                    }
                    if (count > 0) {
                        int pxValue = 255;
                        dilatedImage.setPixel(y + B.length / 2, x + B[0].length / 2, pxValue, pxValue, pxValue);
                    }
                }
            }
            return dilatedImage;
        }

        public static Imagem opening(Imagem image, int[][] B) {
            return dilate(erode(image, B), B);
        }

        public static Imagem closing(Imagem image, int[][] B) {
            return erode(dilate(image, B), B);
        }

        public static Imagem innerEdge(Imagem image, int[][] B) {
            Imagem imgOut = new Imagem(image.getAltura(), image.getLargura(), image.getBuffer().getType());
            Imagem erodedImg = erode(image, B);
            for (int y = 0; y < image.getAltura(); y++) {
                for (int x = 0; x < image.getLargura(); x++)
                    if (image.getPixel(y, x, 0) != erodedImg.getPixel(y, x, 0))
                        imgOut.setPixel(y, x, 255, 255, 255);
            }
            return imgOut;
        }

        public static Imagem outerEdge(Imagem image, int[][] B) {
            Imagem imgOut = new Imagem(image.getAltura(), image.getLargura(), image.getBuffer().getType());
            Imagem dilatedImd = dilate(image, B);
            for (int y = 0; y < image.getAltura(); y++) {
                for (int x = 0; x < image.getLargura(); x++) {
                    if (image.getPixel(y, x, 0) != dilatedImd.getPixel(y, x, 0)) {
                        imgOut.setPixel(y, x, 255, 255, 255);
                    }
                }
            }
            return imgOut;
        }
    }

    public static class Monochrome {
        public static Imagem erode(Imagem image, int[][] B) {
            Imagem erodedImage = new Imagem(image.getAltura(), image.getLargura(), image.getBuffer().getType());
            for (int y = 0; y < image.getAltura(); y++) {
                if (y + B.length > image.getAltura()) { continue; }
                for (int x = 0; x < image.getLargura(); x++) {
                    if (x + B[0].length > image.getLargura()) { continue; }
                    int minPx = 255;
                    for (int j = 0; j < B.length; j++) {
                        for (int i = 0; i < B[j].length; i++)
                            if (B[j][i] != 0 && image.getPixel(y + j, x + i, 0) < minPx)
                                minPx = image.getPixel(y + j, x + i, 0);
                    }
                    erodedImage.setPixel(y + B.length/2, x + B[0].length/2, minPx, minPx, minPx);
                }
            }
            return erodedImage;
        }

        public static Imagem dilate(Imagem image, int[][] B) {
            Imagem dilatedImage = new Imagem(image.getAltura(), image.getLargura(), image.getBuffer().getType());
            for (int y = 0; y < image.getAltura(); y++) {
                if (y + B.length > image.getAltura()) { continue; }
                for (int x = 0; x < image.getLargura(); x++) {
                    if (x + B[0].length > image.getLargura()) { continue; }
                    int maxPx = 0;
                    for (int j = 0; j < B.length; j++) {
                        for (int i = 0; i < B[j].length; i++)
                            if (B[j][i] != 0 && image.getPixel(y + j, x + i, 0) > maxPx)
                                maxPx = image.getPixel(y + j, x + i, 0);
                    }
                    dilatedImage.setPixel(y + B.length/2, x + B[0].length/2, maxPx, maxPx, maxPx);
                }
            }
            return dilatedImage;
        }

        public static Imagem opening(Imagem image, int[][] B) {
            return dilate(erode(image, B), B);
        }

        public static Imagem closing(Imagem image, int[][] B) {
            return erode(dilate(image, B), B);
        }

        public static Imagem smoothing(Imagem image, int[][] B) {
            return closing(opening(image, B), B);
        }

        public static Imagem gradient(Imagem image, int[][] B) {
            Imagem imgOut = new Imagem(image.getAltura(), image.getLargura(), image.getBuffer().getType());
            Imagem erodedImg = erode(image.clone(), B);
            Imagem dilatedImg = dilate(image.clone(), B);
            for (int y = 0; y < image.getAltura(); y++) {
                for (int x = 0; x < image.getLargura(); x++) {
                    int pxValue = dilatedImg.getPixel(y, x, 0) - erodedImg.getPixel(y, x, 0);
                    imgOut.setPixel(y, x, pxValue, pxValue, pxValue);
                }
            }
            return imgOut;
        }
    }
}