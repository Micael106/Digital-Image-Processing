package methods;

import java.util.ArrayList;

public class Pixel {
    public final int x;
    public final int y;
    public final int R;
    public final int G;
    public final int B;
    public final int gray;

    public Pixel(int y, int x, int R, int G, int B) {
        this.y = y;
        this.x = x;
        this.R = R;
        this.G = G;
        this.B = B;
        this.gray = R;
    }

    public ArrayList<Pixel> getNeighbors(Imagem image) {
        var pixels = new ArrayList<Pixel>();
        if (y - 1 >= 0) {
            pixels.add(image.getPixel(y - 1, x));
        }

        if (x + 1 < image.getLargura()) {
            pixels.add(image.getPixel(y, x + 1));
        }

        if (y + 1 < image.getAltura()) {
            pixels.add(image.getPixel(y + 1, x));
        }

        if (x - 1 >= 0) {
            pixels.add(image.getPixel(y, x - 1));
        }

        return pixels;
    }

    public boolean equals(Object px) {
        return (y == ((Pixel) px).y && x == ((Pixel) px).x && gray == ((Pixel) px).gray);
    }

}