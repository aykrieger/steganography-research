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

    public DiscreteSpringTransform(String imageFileName) throws IOException {
        this.image = ImageIO.read(new File(imageFileName));
    }

    //break image up takes an image and splits it into an image at 60 precent and 40 precent of the original width
    //and than changes them into images with half the width of original image and combines them
    private BufferedImage breakImageUp() throws IOException {
        int width = image.getWidth();
        int border = (int) (width * 0.6);
        BufferedImage larger = this.image.getSubimage(0,0, border, this.image.getHeight());
        BufferedImage smaller = this.image.getSubimage(border+1,0, (this.image.getWidth() - border-1), this.image.getHeight());
        if (border > 100){
            breakImageUp(larger, smaller );
        }
        BufferedImage returnImage = new BufferedImage(width, smaller.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = returnImage.getGraphics();
        graphics.drawImage(larger,0,0,width/2,smaller.getHeight(),null);
        graphics.drawImage(smaller,width/2,0,width-(width/2),smaller.getHeight(),null);
        graphics.dispose();
        return returnImage;
    }

    //recursive call for breakImageUp
    private BufferedImage breakImageUp(BufferedImage larger, BufferedImage smaller) throws IOException {

        int largerBorder = (int) (larger.getWidth() * 0.6);
        int smallerBorder = (int) (smaller.getWidth() * 0.6);

        BufferedImage largerOfLarger = larger.getSubimage(0,0, largerBorder, larger.getHeight());
        BufferedImage smallerOfLarger = larger.getSubimage(largerBorder,0, (larger.getWidth() - largerBorder), larger.getHeight());
        BufferedImage largerOfSmaller = smaller.getSubimage(0,0, smallerBorder, smaller.getHeight());
        BufferedImage smallerOfSmaller = smaller.getSubimage(smallerBorder,0, (smaller.getWidth() - smallerBorder), smaller.getHeight());

        if (smallerBorder > 100){
            larger = breakImageUp(largerOfLarger, smallerOfLarger);
            smaller = breakImageUp(largerOfSmaller,smallerOfSmaller );
        }
        BufferedImage returnImage = new BufferedImage(larger.getWidth()+smaller.getWidth(), smaller.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = returnImage.getGraphics();
        int halfway = (int) ((larger.getWidth()+smaller.getWidth()) * 0.5);
        graphics.drawImage(larger,0,0,halfway,smaller.getHeight(),null);
        graphics.drawImage(smaller,halfway,0,larger.getWidth()+smaller.getWidth()-halfway,smaller.getHeight(),null);
        graphics.dispose();
        return returnImage;
    }

    public void writeImage(String outputFilePath) throws IOException{
        BufferedImage changedImage = breakImageUp();
        File outputImageFile = new File(outputFilePath);
        ImageIO.write(changedImage, "png", outputImageFile);
    }
}