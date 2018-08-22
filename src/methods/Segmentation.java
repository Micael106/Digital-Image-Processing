package methods;

import java.util.ArrayList;

public class Segmentation {
    public static Imagem regionGrowing(Imagem image, Pixel seedPixel, int range) {
        var region = new Imagem(image.getAltura(), image.getLargura(), Imagem.RGB);
        var neighborsPx = new ArrayList<Pixel>();
        neighborsPx.add(seedPixel);

        while (!neighborsPx.isEmpty()) {
            var px = neighborsPx.remove(0);
            region.setPixel(px);
            //System.out.println("Pixels: " + neighborsPx.size());
            //System.out.println("(" + px.y + "," + px.x + ")" + " -> " + px.getNeighbors(image).size());
            for (var nearPx : px.getNeighbors(image)) {
                var counter = 0;
                for (var previousPx : neighborsPx) { if (nearPx.equals(previousPx)) counter += 1; } // Para se pixel vizinho já tiver sido analizado
                var req = counter == 0 && (Math.abs(nearPx.R - seedPixel.R) < range && Math.abs(nearPx.G - seedPixel.G) < range && Math.abs(nearPx.B - seedPixel.B) < range) && !region.getPixel(nearPx.y, nearPx.x).equals(nearPx);
                if (req) { neighborsPx.add(nearPx); }
            }
        }

        return region;
    }
}
