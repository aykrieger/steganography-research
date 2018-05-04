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
    private BufferedImage image;

    // ## Temporary fix so output image is the same size as input image
    private int ORIGINAL_WIDTH;
    private int ORIGINAL_HEIGHT;

    public DiscreteSpringTransform(String imageFileName) throws IOException {
        this.image = ImageIO.read(new File(imageFileName));

        // ## Temporary fix so output image is the same size as input image
        this.ORIGINAL_WIDTH = this.image.getWidth();
        this.ORIGINAL_HEIGHT = this.image.getHeight();
    }

    private BufferedImage breakImageUp() throws IOException {
         int border = (int) (this.image.getWidth() * 0.6);
         BufferedImage larger = this.image.getSubimage(0,0, border, this.image.getHeight());
         BufferedImage smaller = this.image.getSubimage(border+1,0, (this.image.getWidth() - border-1), this.image.getHeight());
         if (border > 100){
           breakImageUp(larger, smaller );
        }
        BufferedImage returnImage = new BufferedImage(2*((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)), smaller.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = returnImage.getGraphics();
        graphics.drawImage(larger,0,0,((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(),null);
        graphics.drawImage(smaller,((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),0,((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(),null);
        graphics.dispose();
        return returnImage;
    }

    private BufferedImage breakImageUp(BufferedImage larger, BufferedImage smaller) throws IOException {

        int largerBorder = (int) (larger.getWidth() * 0.6);
        int smallerBorder = (int) (smaller.getWidth() * 0.6);

        BufferedImage largerOfLarger = larger.getSubimage(0,0, largerBorder, larger.getHeight());
        BufferedImage smallerOfLarger = larger.getSubimage(largerBorder+1,0, (larger.getWidth() - largerBorder-1), larger.getHeight());
        BufferedImage largerOfSmaller = smaller.getSubimage(0,0, smallerBorder, smaller.getHeight());
        BufferedImage smallerOfSmaller = smaller.getSubimage(smallerBorder+1,0, (smaller.getWidth() - smallerBorder-1), smaller.getHeight());

        if (smallerBorder > 100){
            larger = breakImageUp(largerOfLarger, smallerOfLarger);
            smaller = breakImageUp(largerOfSmaller,smallerOfSmaller );
        }
        BufferedImage returnImage = new BufferedImage(2*((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)), smaller.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = returnImage.getGraphics();
        graphics.drawImage(larger,0,0,((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(),null);
        graphics.drawImage(smaller,((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),0,((int) ((larger.getWidth()+smaller.getWidth()) * 0.5)),smaller.getHeight(),null);
        graphics.dispose();
        return returnImage;
    }

    public void writeImage(String outputFilePath) throws IOException{
        BufferedImage changedImage = breakImageUp();

        // ## Temporary fix so output image is the same size as input image
        Image tempImage = changedImage.getScaledInstance(ORIGINAL_WIDTH, ORIGINAL_HEIGHT,
                Image.SCALE_DEFAULT);
        BufferedImage tempBuff = new BufferedImage(ORIGINAL_WIDTH, ORIGINAL_HEIGHT,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = tempBuff.createGraphics();
        graphics.drawImage(tempImage, 0, 0, null);
        graphics.dispose();
        changedImage = tempBuff;

        File outputImageFile = new File(outputFilePath);
        ImageIO.write(changedImage, "png", outputImageFile);
    }
}