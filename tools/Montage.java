import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Arrays;

public class Montage {
    public static void main(String[] a) throws Exception {
        File dir = new File("src/main/resources/assets/foecollab/textures/font/tag");
        File[] files = dir.listFiles((d,n)->n.endsWith(".png"));
        Arrays.sort(files, (x,y)->x.getName().compareTo(y.getName()));
        int scale=14, pad=6, cols=5, gw=9, gh=7;
        int cellW=gw*scale+pad, cellH=gh*scale+pad;
        int rows=(files.length+cols-1)/cols;
        BufferedImage out=new BufferedImage(cols*cellW+pad, rows*cellH+pad, BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<out.getHeight();y++)for(int x=0;x<out.getWidth();x++)out.setRGB(x,y,0xFF303030);
        for(int i=0;i<files.length;i++){
            BufferedImage img=ImageIO.read(files[i]);
            int gx=(i%cols)*cellW+pad, gy=(i/cols)*cellH+pad;
            for(int y=0;y<img.getHeight();y++)for(int x=0;x<img.getWidth();x++){
                int rgb=img.getRGB(x,y);
                for(int sy=0;sy<scale;sy++)for(int sx=0;sx<scale;sx++)
                    out.setRGB(gx+x*scale+sx, gy+y*scale+sy, rgb);
            }
        }
        ImageIO.write(out,"png",new File("tools/montage.png"));
        System.out.println("montage: "+files.length+" tiles");
        for(int i=0;i<files.length;i++) System.out.print(files[i].getName().replace(".png","")+(i%cols==cols-1?"\n":"  "));
    }
}
