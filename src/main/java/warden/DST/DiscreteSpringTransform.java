package warden.DST;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DiscreteSpringTransform {

    /*
     two different ways it seems to do discrete spring transform, way number 1 is to use the formula
    A'(x,y)=A(((3 sin(π x) sin(3 π x))/(π^2 x^2)),((3 sin(π y) sin(3 π y))/(π^2 y^2))
    or use the pinch attack which is a form of DST which is where you split the image into a area which is compressed and an area which is expanded.

     */
    private String imageFileName;

    public DiscreteSpringTransform(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    private BufferedImage breakImageUp() throws IOException {
        BufferedImage image = ImageIO.read(new File(this.imageFileName));
         int border = (int) (image.getWidth() * 0.4);
         BufferedImage larger = image.getSubimage(0,0, border, image.getHeight());
         BufferedImage smaller = image.getSubimage(border+1,0, (image.getWidth() - border), image.getHeight());
         if (border > 200){
           breakImageUp(larger, smaller );
        }
        larger.getScaledInstance(((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(), Image.SCALE_SMOOTH);
        smaller.getScaledInstance(((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(), Image.SCALE_SMOOTH);

        BufferedImage returnImage = new BufferedImage((larger.getWidth()+smaller.getWidth()), smaller.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = returnImage.getGraphics();
        graphics.drawImage(larger,0,0,null);
        graphics.drawImage(smaller, larger.getWidth(), 0, null);
        graphics.dispose();
        return returnImage;
    }

    private BufferedImage breakImageUp(BufferedImage larger, BufferedImage smaller) throws IOException {

        int largerBorder = (int) (larger.getWidth() * 0.4);
        int smallerBorder = (int) (smaller.getWidth() * 0.4);

        BufferedImage largerOfLarger = larger.getSubimage(0,0, largerBorder, larger.getHeight());
        BufferedImage smallerOfLarger = larger.getSubimage(largerBorder+1,0, (larger.getWidth() - largerBorder), larger.getHeight());
        BufferedImage largerOfSmaller = smaller.getSubimage(0,0, smallerBorder, smaller.getHeight());
        BufferedImage smallerOfSmaller = smaller.getSubimage(smallerBorder+1,0, (smaller.getWidth() - smallerBorder), smaller.getHeight());

        if (smallerBorder > 200){
            larger = breakImageUp(largerOfLarger, smallerOfLarger);
            smaller = breakImageUp(largerOfSmaller,smallerOfSmaller );
        }

        larger.getScaledInstance(((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(), Image.SCALE_SMOOTH);
        smaller.getScaledInstance(((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(), Image.SCALE_SMOOTH);

        BufferedImage returnImage = new BufferedImage((larger.getWidth()+smaller.getWidth()), smaller.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = returnImage.getGraphics();
        graphics.drawImage(larger,0,0,null);
        graphics.drawImage(smaller, larger.getWidth(), 0, null);
        graphics.dispose();
        return returnImage;
    }

    public void writeImage(String outputFilePath) throws IOException{
        File outputImageFile = new File(outputFilePath);
        ImageIO.write(breakImageUp(), "png", outputImageFile);
    }
}