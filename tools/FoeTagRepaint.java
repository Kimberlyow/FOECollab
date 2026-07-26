import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Repaints minecraft:font/foe_purple.png so its background matches the shortened ADMIRAL tag
 * (foecollab:textures/font/tag/admiral.png): the same two-tone purple square (lighter top rows,
 * darker bottom rows) sampled straight from admiral.png, with a per-letter right-edge drop shadow,
 * keeping the existing white "FOE" lettering. The green foe.png is left untouched.
 *
 *   javac -d tools/out tools/FoeTagRepaint.java && java -cp tools/out FoeTagRepaint [write]
 *
 * Without the "write" arg it only prints diagnostics (dry run).
 */
public class FoeTagRepaint {
    public static void main(String[] args) throws Exception {
        boolean write = args.length > 0 && args[0].equals("write");
        File admiralF = new File("src/main/resources/assets/foecollab/textures/font/tag/admiral.png");
        File foeF = new File("src/main/resources/assets/minecraft/textures/font/foe_purple.png");

        BufferedImage adm = ImageIO.read(admiralF);
        BufferedImage foe = ImageIO.read(foeF);
        int W = foe.getWidth(), H = foe.getHeight();
        System.out.println("admiral " + adm.getWidth() + "x" + adm.getHeight() + "   foe_purple " + W + "x" + H);

        // Sample admiral's two-tone purple straight from the texture: light from the top row, dark
        // from the bottom row, and count how many leading rows use the light shade. Sample the
        // left margin column (x=0), which is pure background — the centre column passes through the
        // "A" letter.
        int bx = 0;
        int light = adm.getRGB(bx, 0) & 0xFFFFFF;
        int dark = adm.getRGB(bx, adm.getHeight() - 1) & 0xFFFFFF;
        int lightRows = 0;
        for (int y = 0; y < adm.getHeight(); y++) {
            if ((adm.getRGB(bx, y) & 0xFFFFFF) == light) lightRows++;
            else break;
        }
        int shadow = scale(light, 0.50);
        System.out.printf("light=%06X dark=%06X shadow=%06X lightRows=%d%n", light, dark, shadow, lightRows);

        // "FOE" letter mask = the near-white pixels of the current glyph.
        boolean[][] text = new boolean[H][W];
        StringBuilder mask = new StringBuilder();
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                int argb = foe.getRGB(x, y);
                int a = argb >>> 24, r = (argb >> 16) & 0xFF, g = (argb >> 8) & 0xFF, b = argb & 0xFF;
                boolean t = a > 128 && r > 170 && g > 170 && b > 170;
                text[y][x] = t;
                mask.append(t ? '#' : (a > 128 ? '.' : ' '));
            }
            mask.append('\n');
        }
        System.out.println("text mask (#=letter, .=opaque bg, space=transparent):\n" + mask);

        // Rebuild: opaque two-tone bg, then 1px right-edge letter shadow, then white letters on top.
        BufferedImage out = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < H; y++) {
            int bg = y < lightRows ? light : dark;
            for (int x = 0; x < W; x++) out.setRGB(x, y, 0xFF000000 | bg);
        }
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                if (text[y][x] && x + 1 < W && !text[y][x + 1]) out.setRGB(x + 1, y, 0xFF000000 | shadow);
        for (int y = 0; y < H; y++)
            for (int x = 0; x < W; x++)
                if (text[y][x]) out.setRGB(x, y, 0xFFFFFFFF);

        if (write) {
            ImageIO.write(out, "png", foeF);
            System.out.println("Wrote " + foeF);
        } else {
            System.out.println("(dry run — pass 'write' to overwrite)");
        }
    }

    static int scale(int rgb, double k) {
        int r = Math.min(255, (int) (((rgb >> 16) & 0xFF) * k));
        int g = Math.min(255, (int) (((rgb >> 8) & 0xFF) * k));
        int b = Math.min(255, (int) ((rgb & 0xFF) * k));
        return (r << 16) | (g << 8) | b;
    }
}
