import javax.swing.*;
import java.awt.Image;
import java.awt.Toolkit;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Color;

public class ScreenGraphics extends JPanel {
    //default reference ID
    private static final long serialVersionUID = 1L;
    
    //global variables
    private int screenWidth, screenHeight;
    private boolean isSplash = true;
    private int successfulJumps = 0;
    private String message = "F L A P P Y   M I N I O N";
    private Font primaryFont = new Font("Agency FB", Font.BOLD, 80), failFont = new Font("Calibri", Font.BOLD, 56);
    private int messageWidth = 0, scoreWidth = 0;
    private BottomPipe bp1, bp2;
    private TopPipe tp1, tp2;
    private Minion minion;
    Image img = Toolkit.getDefaultToolkit().createImage("background.png");
    

    /**
     * Default constructor for the PlayGameScreen class
     */
    public ScreenGraphics(int screenWidth, int screenHeight, boolean isSplash) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.isSplash = isSplash;
    }
    
    /**
     * Manually control what's drawn on this JPanel by calling the paintComponent method
     * with a graphics object and painting using that object
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        /**g.setColor(new Color(102, 204, 255)); //color for blue sky
        g.fillRect(0, 0, screenWidth, screenHeight*7/8); //create the sky rectangle*/
        g.drawImage(img, 0, 0, null);
        g.setColor(new Color(186, 186, 186)); //color for ground
        g.fillRect(0, screenHeight*7/8, screenWidth, screenHeight/8); //create the ground rectangle
        
        
        g.setColor(Color.BLACK); //dividing line color
        //g.drawLine(0, screenHeight*7/8, screenWidth, screenHeight*7/8); //draw the dividing line
        
        //objects must be instantiated before they're drawn!
        if(bp1 != null && bp2 != null && tp1 != null && tp2 != null) {
            g.drawImage(bp1.getPipe(), bp1.getX(), bp1.getY(), null);
            g.drawImage(bp2.getPipe(), bp2.getX(), bp2.getY(), null);
            g.drawImage(tp1.getPipe(), tp1.getX(), tp1.getY(), null);
            g.drawImage(tp2.getPipe(), tp2.getX(), tp2.getY(), null);
        }
        
        if(!isSplash && minion != null) {
            g.drawImage(minion.getMinion(), minion.getX(), minion.getY(), null);
        }
        
        //needed in case the primary font does not exist
        try {
            g.setFont(primaryFont);
            FontMetrics metric = g.getFontMetrics(primaryFont);
            messageWidth = metric.stringWidth(message);
            scoreWidth = metric.stringWidth(String.format("%d", successfulJumps));
        }
        catch(Exception e) {
            g.setFont(failFont);
            FontMetrics metric = g.getFontMetrics(failFont);
            messageWidth = metric.stringWidth(message);
            scoreWidth = metric.stringWidth(String.format("%d", successfulJumps));
        }
        
        g.drawString(message, screenWidth/2-messageWidth/2, screenHeight/4);
        
        if(!isSplash) {
            g.drawString(String.format("%d", successfulJumps), screenWidth/2-scoreWidth/2, 50);
        }
    }
    
    /**
     * Parsing method for ScreenGraphic's global BottomPipe variables
     * @param bp1 The first BottomPipe
     * @param bp2 The second BottomPipe
     */
    public void setBottomPipe(BottomPipe bp1, BottomPipe bp2) {
        this.bp1 = bp1;
        this.bp2 = bp2;
    }
    
    /**
     * Parsing method for PlayGameScreen's global TopPipe variables
     * @param tp1 The first TopPipe
     * @param tp2 The second TopPipe
     */
    public void setTopPipe(TopPipe tp1, TopPipe tp2) {
        this.tp1 = tp1;
        this.tp2 = tp2;
    }
    
    /**
     * Parsing method for PlayGameScreen's global minion variable
     * @param minion The minion object
     */
    public void setMinion(Minion minion) {
        this.minion = minion;
    }
    
    /**
     * Method called to invoke an increase in the variable tracking the current
     * jump score
     */
    public void incrementJump() {
        successfulJumps++;
    }
    
    /**
     * Method called to return the current jump score
     * @return
     */
    public int getScore() {
        return successfulJumps;
    }
    
    /**
     * Method called to parse a message onto the screen
     * @param message The message to parse
     */
    public void sendText(String message) {
        this.message = message;
    }
}