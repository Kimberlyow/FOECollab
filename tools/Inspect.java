import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Inspect {
    public static void main(String[] a) throws Exception {
        BufferedImage img = ImageIO.read(new File(a[0]));
        int w = img.getWidth(), h = img.getHeight();
        System.out.println("size=" + w + "x" + h);
        for (int y = 0; y < h; y++) {
            StringBuilder sb = new StringBuilder();
            for (int x = 0; x < w; x++) {
                int p = img.getRGB(x, y);
                int al = (p >>> 24); int r = (p >> 16) & 0xFF, g = (p >> 8) & 0xFF, b = p & 0xFF;
                if (al < 40) sb.append(".....  ");
                else sb.append(String.format("%02X%02X%02X ", r, g, b));
            }
            System.out.println(sb.toString());
        }
        int s = 22;
        BufferedImage out = new BufferedImage(w * s, h * s, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
            int p = img.getRGB(x, y);
            for (int sy = 0; sy < s; sy++) for (int sx = 0; sx < s; sx++) out.setRGB(x * s + sx, y * s + sy, p);
        }
        ImageIO.write(out, "png", new File("tools/ref_zoom.png"));
    }
}
