import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One-off generator for the foecollab:tags font. Each glyph is drawn to match the
 * in-game FishOnMC crew tag (assets/minecraft/textures/font/crew.png): a 9x7 square
 * with a two-tone vertical shade (lighter top, darker bottom), and a 5x5 rounded
 * white letter with a 1px drop-shadow on its right edge.
 *
 * Run from the project root:
 *   javac -d tools/out tools/TagGlyphGen.java && java -cp tools/out TagGlyphGen
 */
public class TagGlyphGen {
    static final int W = 9;             // texture width  (matches crew.png)
    static final int H = 7;             // texture height (matches crew.png)
    static final int LETTER_X = 2;      // 5x5 letter box offset
    static final int LETTER_Y = 1;
    static final int LIGHT_ROWS = 3;    // top rows use the lighter shade, the rest the darker

    static final double DARK_K = 0.72;  // bottom shade multiplier
    static final double SHADOW_K = 0.50;// letter shadow multiplier (right edge)

    // 5x5 rounded pixel font, styled after the crew glyph's "C".
    static final Map<Character, String[]> FONT = new LinkedHashMap<>();
    static {
        FONT.put('A', new String[]{".###.", "#...#", "#####", "#...#", "#...#"});
        FONT.put('B', new String[]{"####.", "#...#", "####.", "#...#", "####."});
        FONT.put('C', new String[]{".###.", "#...#", "#....", "#...#", ".###."});
        FONT.put('D', new String[]{"####.", "#...#", "#...#", "#...#", "####."});
        FONT.put('E', new String[]{"#####", "#....", "####.", "#....", "#####"});
        FONT.put('F', new String[]{"#####", "#....", "####.", "#....", "#...."});
        FONT.put('L', new String[]{"#....", "#....", "#....", "#....", "#####"});
        FONT.put('M', new String[]{"#...#", "##.##", "#.#.#", "#...#", "#...#"});
        FONT.put('O', new String[]{".###.", "#...#", "#...#", "#...#", ".###."});
        FONT.put('R', new String[]{"####.", "#...#", "####.", "#..#.", "#...#"});
        FONT.put('S', new String[]{".####", "#....", ".###.", "....#", "####."});
        FONT.put('T', new String[]{"#####", "..#..", "..#..", "..#..", "..#.."});
    }

    record Tag(int cp, String name, char letter, int rgb) {}

    static final Tag[] TAGS = new Tag[]{
            // Ranks
            new Tag(0xF021, "owner", 'O', 0xE03C3C),
            new Tag(0xF022, "admin", 'A', 0xFF4500),
            new Tag(0xF023, "manager", 'M', 0xFFFFFF),
            new Tag(0xF024, "staff", 'S', 0x3D7CE0),
            new Tag(0xF026, "designer", 'D', 0xF15BB5),
            new Tag(0xF027, "builder", 'B', 0x2F6BE3),
            new Tag(0xF028, "admiral", 'A', 0xAE5AF6),
            new Tag(0xF029, "captain", 'C', 0xFCA307),
            new Tag(0xF030, "mariner", 'M', 0x66F8AE),
            new Tag(0xF031, "sailor", 'S', 0x96F564),
            new Tag(0xF032, "angler", 'A', 0x20BBD7),
            new Tag(0xF066, "media", 'M', 0xC85127),
            new Tag(0xF088, "communitymanager", 'C', 0x2EB84B),
            // Rarities
            new Tag(0xF033, "common", 'C', 0xFFFFFF),
            new Tag(0xF034, "rare", 'R', 0x2B85C4),
            new Tag(0xF035, "epic", 'E', 0x1CD832),
            new Tag(0xF036, "legendary", 'L', 0xD98103),
            new Tag(0xF037, "mythical", 'M', 0xC93832),
            new Tag(0xF092, "special", 'S', 0xDD7ACF),
            // Variants — albino / melanistic / trophy / fabled are intentionally omitted so they
            // keep the server's original glyph (their simplified squares were removed).
            new Tag(0xF098, "alternate", 'A', 0x9CB4FC),
            new Tag(0xF102, "spooky", 'S', 0x2E2E8F),
            new Tag(0xF179, "frozen", 'F', 0x7FB0E7),
    };

    public static void main(String[] args) throws Exception {
        File texDir = new File("src/main/resources/assets/foecollab/textures/font/tag");
        File fontDir = new File("src/main/resources/assets/foecollab/font");
        texDir.mkdirs();
        fontDir.mkdirs();

        StringBuilder json = new StringBuilder("{\"providers\":[\n");
        for (int t = 0; t < TAGS.length; t++) {
            Tag tag = TAGS[t];
            int light = ensureContrast(tag.rgb());
            int dark = scale(light, DARK_K);
            int shadow = scale(light, SHADOW_K);

            BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
            // Two-tone background: lighter top rows, darker bottom rows (matches crew.png).
            for (int y = 0; y < H; y++) {
                int bg = y < LIGHT_ROWS ? light : dark;
                for (int x = 0; x < W; x++) {
                    img.setRGB(x, y, 0xFF000000 | bg);
                }
            }

            String[] glyph = FONT.get(Character.toUpperCase(tag.letter()));
            if (glyph != null) {
                // 1px right-edge shadow first, then the white letter on top.
                for (int r = 0; r < glyph.length; r++) {
                    for (int c = 0; c < glyph[r].length(); c++) {
                        if (glyph[r].charAt(c) == '#') {
                            img.setRGB(LETTER_X + c + 1, LETTER_Y + r, 0xFF000000 | shadow);
                        }
                    }
                }
                for (int r = 0; r < glyph.length; r++) {
                    for (int c = 0; c < glyph[r].length(); c++) {
                        if (glyph[r].charAt(c) == '#') {
                            img.setRGB(LETTER_X + c, LETTER_Y + r, 0xFFFFFFFF);
                        }
                    }
                }
            }
            ImageIO.write(img, "png", new File(texDir, tag.name() + ".png"));

            json.append(String.format(
                    "    {\"type\":\"bitmap\",\"file\":\"foecollab:font/tag/%s.png\",\"ascent\":7,\"height\":7,\"chars\":[\"\\u%04X\"]}%s\n",
                    tag.name(), tag.cp(), t == TAGS.length - 1 ? "" : ","));
        }
        json.append("  ]\n}\n");

        try (Writer w = new FileWriter(new File(fontDir, "tags.json"))) {
            w.write(json.toString());
        }
        System.out.println("Wrote " + TAGS.length + " glyphs + tags.json");
    }

    // White letter + its shadow read on most colours; only knock back the very
    // lightest squares (e.g. pure-white common) so the letter still shows.
    static int ensureContrast(int rgb) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        if (lum > 200) {
            return scale(rgb, 0.68);
        }
        return (r << 16) | (g << 8) | b;
    }

    // Multiply an rgb by k, clamped to [0,255] per channel.
    static int scale(int rgb, double k) {
        int r = Math.min(255, (int) (((rgb >> 16) & 0xFF) * k));
        int g = Math.min(255, (int) (((rgb >> 8) & 0xFF) * k));
        int b = Math.min(255, (int) ((rgb & 0xFF) * k));
        return (r << 16) | (g << 8) | b;
    }
}
