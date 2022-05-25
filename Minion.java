import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Image;
import java.awt.image.BufferedImage;

public class Minion {
    //global variables
    private Image minion;
    private int xLoc = 0, yLoc = 0;
    
    /** constructor
    */
    public Minion(int initialWidth, int initialHeight) {
        minion = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("flappy minion graphics/minion.png"));
        scaleMinion(initialWidth, initialHeight);
    }
    
    /**
     * scale the minion sprite into the desired dimensions
     */
    public void scaleMinion(int width, int height) {
        minion = minion.getScaledInstance(width, height, Image.SCALE_SMOOTH);        
    }
    
    /**
     * getter method for the minion object
     */
    public Image getMinion() {
        return minion;
    }
    
    public int getWidth() {
        try {
            return minion.getWidth(null);
        }
        catch(Exception e) {
            return -1;
        }
    }
    
    public int getHeight() {
        try {
            return minion.getHeight(null);
        }
        catch(Exception e) {
            return -1;
        }
    }
    
    public void setX(int x) {
        xLoc = x;
    }
    
    public int getX() {
        return xLoc;
    }
    
    public void setY(int y) {
        yLoc = y;
    }
    
    public int getY() {
        return yLoc;
    }
    
    /**
     * method used to acquire a Rectangle that outlines the minion's image
     * @return Rectangle outlining the minion's position on screen
     */
    public Rectangle getRectangle() {
        return (new Rectangle(xLoc, yLoc, minion.getWidth(null), minion.getHeight(null)));
    }
    
    /**
     * method to acquire a BufferedImage that represents the minion's image object
     * @return Minion's BufferedImage object
     */
    public BufferedImage getBI() {
        BufferedImage bi = new BufferedImage(minion.getWidth(null), minion.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bi.getGraphics();
        g.drawImage(minion, 0, 0, null);
        g.dispose();
        return bi;
    }
}