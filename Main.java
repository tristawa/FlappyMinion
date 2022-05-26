import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Color;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

import javax.swing.*;

public class Main implements ActionListener, KeyListener {
    //global constant variables
    private static final int SCREEN_WIDTH = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
    private static final int SCREEN_HEIGHT = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
    private static final int PIPE_GAP = SCREEN_HEIGHT/9; //distance in pixels between pipes
    private static final int PIPE_WIDTH = SCREEN_WIDTH/8, PIPE_HEIGHT = 4*PIPE_WIDTH-20;
    private static final int MINION_WIDTH = 100, MINION_HEIGHT = 120;
    private static final int UPDATE_DIFFERENCE = 25; //time in ms between updates
    private static final int X_MOVEMENT_DIFFERENCE = 7; //distance the pipes move every update
    private static final int SCREEN_DELAY = 300; //needed because of long load times forcing pipes to pop up mid-screen
    private static final int MINION_X_LOCATION = SCREEN_WIDTH/7;
    private static final int MINION_JUMP_DIFF = 10, MINION_FALL_DIFF = MINION_JUMP_DIFF/2 + 3, MINION_JUMP_HEIGHT = PIPE_GAP*2 - MINION_HEIGHT - MINION_JUMP_DIFF + 25;
    
    //global variables
    private boolean loopVar = true; //false -> don't run loop; true -> run loop for pipes
    private boolean gamePlay = false; //false -> game not being played
    private boolean minionThrust = false; //false -> key has not been pressed to move the minion vertically
    private boolean minionFired = false; //true -> button pressed before jump completes
    private boolean spaceReleased = true; //space bar released; starts as true so first press registers
    private int minionYTracker = SCREEN_HEIGHT/2 - MINION_HEIGHT;
    private Object buildComplete = new Object();
    
    //global swing objects
    private JFrame f = new JFrame("Flappy Minion");
    private JButton startGame;
    private JButton tutorial;
    private JButton replay;
    private JPanel topPanel; //declared globally to accommodate the repaint operation and allow for removeAll(), etc.
    
    //other global objects
    private static Main tc = new Main();
    private static ScreenGraphics screen; //panel that has the moving background at the start of the game
    
    public Main() {
        
    }
    
    /**
     * main executable method invoked when running .jar file
     */
    public static void main(String[] args) {
        //build the GUI on a new thread
        
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                tc.buildFrame();
                
                //create a new thread to keep the GUI responsive while the game runs
                Thread t = new Thread() {
                    public void run() {
                        tc.gameScreen(true);
                    }
                };
                t.start();
            }
        });
    }
    
    /**
     * Method to construct the JFrame and add the program content
     */
    private void buildFrame() {
        Image icon = Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("flappy minion graphics/minion.png"));
        
        f.setContentPane(createContentPane());
        f.setResizable(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setAlwaysOnTop(false);
        f.setVisible(true);
        f.setMinimumSize(new Dimension(SCREEN_WIDTH*1/4, SCREEN_HEIGHT*1/4));
        f.setExtendedState(JFrame.MAXIMIZED_BOTH);
        f.setIconImage(icon);
        f.addKeyListener(this);
    }
    
    private JPanel createContentPane() {
        topPanel = new JPanel(); //top-most JPanel in layout hierarchy
        topPanel.setBackground(Color.BLACK);
        //allow us to layer the panels
        LayoutManager overlay = new OverlayLayout(topPanel);
        topPanel.setLayout(overlay);
        
        //Start Game JButton
        startGame = new JButton("Start Playing!");
        startGame.setBackground(Color.BLUE);
        startGame.setForeground(Color.WHITE);
        startGame.setFocusable(false); //rather than just setFocusabled(false)
        startGame.setFont(new Font("Agency FB", Font.BOLD, 40));
        startGame.setAlignmentX(0.5f); //center horizontally on-screen
        startGame.setAlignmentY(0.5f); //center vertically on-screen
        startGame.addActionListener(this);
        topPanel.add(startGame);
        
        /**
        tutorial = new JButton("Tutorial");
        tutorial.setBackground(Color.BLUE);
        tutorial.setForeground(Color.WHITE);
        tutorial.setFocusable(false);
        tutorial.setFont(new Font("Calibri", Font.PLAIN, 30));
        tutorial.setBounds(40, 30, 200, 40);
        tutorial.addActionListener(this);
        topPanel.add(tutorial);*/
        
        //must add last to ensure button's visibility
        screen = new ScreenGraphics(SCREEN_WIDTH, SCREEN_HEIGHT, true); //true --> we want screen to be the splash screen
        topPanel.add(screen);
        
        return topPanel;
    }
    
    /**
     * Implementation for action events
     */
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == startGame) {
            //stop the splash screen
            loopVar = false;
            
            fadeOperation();
        }
        else if(e.getSource() == buildComplete) {
            Thread t = new Thread() {
                public void run() {
                    loopVar = true;
                    gamePlay = true;
                    tc.gameScreen(false);
                }
            };
            t.start();
        }
        else if (e.getSource() == replay)
        {
            fadeOperation(); //maybe make a new fadeOperation
        }
    }
    
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE && gamePlay == true && spaceReleased == true){
            //update a boolean that's tested in game loop to move the minion
            if(minionThrust) { //need this to register the button press and reset the minionYTracker before the jump operation completes
                minionFired = true;
            }
            minionThrust = true;
            spaceReleased = false;
        }
        else if(e.getKeyCode() == KeyEvent.VK_B && gamePlay == false) {
            minionYTracker = SCREEN_HEIGHT/2 - MINION_HEIGHT; //need to reset the minion's starting height
            minionThrust = false; //if user presses SPACE before collision and a collision occurs before reaching max height, you get residual jump, so this is preventative
            actionPerformed(new ActionEvent(startGame, -1, ""));
        }
        if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            System.exit(0);
        }
    }
    
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            spaceReleased = true;
        }
    }
    
    public void keyTyped(KeyEvent e) {
        
    }
    
    /**
     * Perform the fade operation that take place before the start of rounds
     */
    private void fadeOperation() {
        Thread t = new Thread() {
            public void run() {
                topPanel.remove(startGame);
                topPanel.remove(screen);
                topPanel.revalidate();
                topPanel.repaint();
                
                //panel to fade
                JPanel temp = new JPanel();
                int alpha = 0; //alpha channel variable
                temp.setBackground(new Color(0, 0, 0, alpha)); //transparent, black JPanel
                topPanel.add(temp);
                topPanel.add(screen);
                topPanel.revalidate();
                topPanel.repaint();
                
                long currentTime = System.currentTimeMillis();
                
                while(temp.getBackground().getAlpha() != 255) {
                    if((System.currentTimeMillis() - currentTime) > UPDATE_DIFFERENCE/2) {
                        if(alpha < 255 - 10) {
                            alpha += 10;
                        }
                        else {
                            alpha = 255;
                        }
                        
                        temp.setBackground(new Color(0, 0, 0, alpha));
                    
                        topPanel.revalidate();
                        topPanel.repaint();
                        currentTime = System.currentTimeMillis();
                    }
                }
                
                topPanel.removeAll();
                topPanel.add(temp);
                screen = new ScreenGraphics(SCREEN_WIDTH, SCREEN_HEIGHT, false);
                screen.sendText(""); //remove title text
                topPanel.add(screen);
                
                while(temp.getBackground().getAlpha() != 0) {
                    if((System.currentTimeMillis() - currentTime) > UPDATE_DIFFERENCE/2) {
                        if(alpha > 10) {
                            alpha -= 10;
                        }
                        else {
                            alpha = 0;
                        }
                        
                        temp.setBackground(new Color(0, 0, 0, alpha));
                    
                        topPanel.revalidate();
                        topPanel.repaint();
                        currentTime = System.currentTimeMillis();
                    }
                }
                
                actionPerformed(new ActionEvent(buildComplete, -1, "Build Finished"));
            }
        };
        
        t.start();
    }
    
    /**
     * Method that performs the splash screen graphics movements
     */
    private void gameScreen(boolean isSplash) {
        BottomPipe bp1 = new BottomPipe(PIPE_WIDTH, PIPE_HEIGHT);
        BottomPipe bp2 = new BottomPipe(PIPE_WIDTH, PIPE_HEIGHT);
        TopPipe tp1 = new TopPipe(PIPE_WIDTH, PIPE_HEIGHT);
        TopPipe tp2 = new TopPipe(PIPE_WIDTH, PIPE_HEIGHT);
        Minion minion = new Minion(MINION_WIDTH, MINION_HEIGHT);
        
        //variables to track x and y image locations for the bottom pipe
        int xLoc1 = SCREEN_WIDTH+SCREEN_DELAY, xLoc2 = (int) ((double) 3.0/2.0*SCREEN_WIDTH+PIPE_WIDTH/2.0)+SCREEN_DELAY;
        int yLoc1 = bottomPipeLoc(), yLoc2 = bottomPipeLoc();
        int minionX = MINION_X_LOCATION, minionY = minionYTracker;
        
        //variable to hold the loop start time
        long startTime = System.currentTimeMillis();
        
        while(loopVar) {
            if((System.currentTimeMillis() - startTime) > UPDATE_DIFFERENCE) {
                //check if a set of pipes has left the screen
                //if so, reset the pipe's X location and assign a new Y location
                if(xLoc1 < (0-PIPE_WIDTH)) {
                    xLoc1 = SCREEN_WIDTH;
                    yLoc1 = bottomPipeLoc();
                }
                else if(xLoc2 < (0-PIPE_WIDTH)) {
                    xLoc2 = SCREEN_WIDTH;
                    yLoc2 = bottomPipeLoc();
                }
                
                //decrement the pipe locations by the predetermined amount
                xLoc1 -= X_MOVEMENT_DIFFERENCE;
                xLoc2 -= X_MOVEMENT_DIFFERENCE;
                
                if(minionFired && !isSplash) {
                    minionYTracker = minionY;
                    minionFired = false;
                }
                
                if(minionThrust && !isSplash) {
                    //move minion vertically
                    if(minionYTracker - minionY - MINION_JUMP_DIFF < MINION_JUMP_HEIGHT) {
                        if(minionY - MINION_JUMP_DIFF > 0) {
                            minionY -= MINION_JUMP_DIFF; //coordinates different
                        }
                        else {
                            minionY = 0;
                            minionYTracker = minionY;
                            minionThrust = false;
                        }
                    }
                    else {
                        minionYTracker = minionY;
                        minionThrust = false;
                    }
                }
                else if(!isSplash) {
                    minionY += MINION_FALL_DIFF;
                    minionYTracker = minionY;
                }
                
                //update the BottomPipe and TopPipe locations
                bp1.setX(xLoc1);
                bp1.setY(yLoc1);
                bp2.setX(xLoc2);
                bp2.setY(yLoc2);
                tp1.setX(xLoc1);
                tp1.setY(yLoc1-PIPE_GAP-PIPE_HEIGHT); //ensure tp1 placed in proper location
                tp2.setX(xLoc2);
                tp2.setY(yLoc2-PIPE_GAP-PIPE_HEIGHT); //ensure tp2 placed in proper location
                
                if(!isSplash) {
                    minion.setX(minionX);
                    minion.setY(minionY);
                    screen.setMinion(minion);
                }
                
                //set the BottomPipe and TopPipe local variables in PlayGameScreen by parsing the local variables
                screen.setBottomPipe(bp1, bp2);
                screen.setTopPipe(tp1, tp2);
                
                if(!isSplash && minion.getWidth() != -1) { //need the second part because if minion not on-screen, cannot get image width and have cascading error in collision
                    collisionDetection(bp1, bp2, tp1, tp2, minion);
                    updateScore(bp1, bp2, minion);
                }
                
                //update screen's JPanel
                topPanel.revalidate();
                topPanel.repaint();
                
                //update the time-tracking variable after all operations completed
                startTime = System.currentTimeMillis();
            }
        }
    }
    
    /**
     * Calculates a random int for the bottom pipe's placement
     * @return int
     */
    private int bottomPipeLoc() {
        int temp = 0;
        //iterate until temp is a value that allows both pipes to be onscreen
        while(temp <= PIPE_GAP+50 || temp >= SCREEN_HEIGHT-PIPE_GAP) {
            temp = (int) ((double) Math.random()*((double)SCREEN_HEIGHT));
        }
        return temp;
    }
    
    /**
     * Method that checks whether the score needs to be updated
     * @param bp1 First BottomPipe object
     * @param bp2 Second BottomPipe object
     * @param minion Bird object
     */
    private void updateScore(BottomPipe bp1, BottomPipe bp2, Minion minion) {
        if(bp1.getX() + PIPE_WIDTH < minion.getX() && bp1.getX() + PIPE_WIDTH > minion.getX() - X_MOVEMENT_DIFFERENCE) {
            screen.incrementJump();
        }
        else if(bp2.getX() + PIPE_WIDTH < minion.getX() && bp2.getX() + PIPE_WIDTH > minion.getX() - X_MOVEMENT_DIFFERENCE) {
            screen.incrementJump();
        }
    }
    
    /**
     * Method to test whether a collision has occurred
     * @param bp1 First BottomPipe object
     * @param bp2 Second BottomPipe object
     * @param tp1 First TopPipe object
     * @param tp2 Second TopPipe object
     * @param minion Bird object
     */
    private void collisionDetection(BottomPipe bp1, BottomPipe bp2, TopPipe tp1, TopPipe tp2, Minion minion) {
        collisionHelper(minion.getRectangle(), bp1.getRectangle(), minion.getBI(), bp1.getBI());
        collisionHelper(minion.getRectangle(), bp2.getRectangle(), minion.getBI(), bp2.getBI());
        collisionHelper(minion.getRectangle(), tp1.getRectangle(), minion.getBI(), tp1.getBI());
        collisionHelper(minion.getRectangle(), tp2.getRectangle(), minion.getBI(), tp2.getBI());
        
        if(minion.getY() + MINION_HEIGHT > SCREEN_HEIGHT*7/8) { //ground detection
            screen.sendText("Game Over");
            
            replay = new JButton("Play Again");
            replay.setBackground(Color.BLUE);
            replay.setForeground(Color.WHITE);
            replay.setFocusable(false); //rather than just setFocusabled(false)
            replay.setFont(new Font("Agency FB", Font.BOLD, 30));
            replay.setAlignmentX(0.5f); //center horizontally on-screen
            replay.setAlignmentY(0.5f); //center vertically on-screen
            replay.addActionListener(this);
            topPanel.add(replay);
            
            loopVar = false;
            gamePlay = false; //game has ended
            
            
        }
    }
    
    /**
     * Helper method to test the Bird object's potential collision with a pipe object.
     * @param r1 The Bird's rectangle component
     * @param r2 Collision component rectangle
     * @param b1 The Bird's BufferedImage component
     * @param b2 Collision component BufferedImage
     */
    private void collisionHelper(Rectangle r1, Rectangle r2, BufferedImage b1, BufferedImage b2) {
        if(r1.intersects(r2)) {
            Rectangle r = r1.intersection(r2);
            
            int firstI = (int) (r.getMinX() - r1.getMinX()); //firstI is the first x-pixel to iterate from
            int firstJ = (int) (r.getMinY() - r1.getMinY()); //firstJ is the first y-pixel to iterate from
            int bp1XHelper = (int) (r1.getMinX() - r2.getMinX()); //helper variables to use when referring to collision object
            int bp1YHelper = (int) (r1.getMinY() - r2.getMinY());
            
            for(int i = firstI; i < r.getWidth() + firstI; i++) { //
                for(int j = firstJ; j < r.getHeight() + firstJ; j++) {
                    if((b1.getRGB(i, j) & 0xFF000000) != 0x00 && (b2.getRGB(i + bp1XHelper, j + bp1YHelper) & 0xFF000000) != 0x00) {
                        screen.sendText("Game Over");
                        loopVar = false; //stop the game loop
                        gamePlay = false; //game has ended
                        break;
                    }
                }
            }
        }
    }
}