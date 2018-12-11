import javax.swing.*;
import java.applet.AudioClip;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.net.URL;

/***********************************************************************************************
 * David Frieder's Thomas Game Copyright 2018 David Frieder 11/30/2018
 * Working on collision detection vic 11/12/2018  rev 4.1
 ***********************************************************************************************/
public class ThomasBeatEmUpController extends JComponent implements ActionListener, Runnable, KeyListener
{
    private int widthOfScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize().width;
    private int heightOfScreen = java.awt.Toolkit.getDefaultToolkit().getScreenSize().height;
    private Point mainTrackPosition = new Point(0, 842 * heightOfScreen / 1000);
    private Track mainTrack = new Track(mainTrackPosition, 6);
    private Point thomasHomePosition = new Point(widthOfScreen/3, (2 * heightOfScreen/3)-mainTrack.getTrackSectionHeight());
    private Point upperTrackPosition = new Point(0, 500 * heightOfScreen / 1000);
    private Thomas thomas = new Thomas(new Point(widthOfScreen/3, (2 * heightOfScreen/3)-mainTrack.getTrackSectionHeight()));
    private Track upperTrack = new Track(upperTrackPosition, 3);
    private Road road = new Road();
    private JFrame mainGameWindow = new JFrame("NewGame");// Makes window with
    private AffineTransform identityTx = new AffineTransform();
    private AffineTransform backgroundTx = new AffineTransform();
    private Timer animationTicker = new Timer(30, this);
    private boolean isGoingRight;
    private boolean isGoingLeft;
    private boolean isJumping;
    private Graphics2D g2;
    private URL thomasThemeAddress = getClass().getResource("ThomasThemeSong.wav");
    private AudioClip thomasThemeSong = JApplet.newAudioClip(thomasThemeAddress);
    private int initialJumpingVelocity = -44;
    public int currentJumpingVelocity = initialJumpingVelocity;
    private int gravitationalPull = 2;
    Image thomasSpriteImage = thomas.getLeftThomasSpriteImageArray()[0];


    /***********************************************************************************************
     * Main
     ***********************************************************************************************/
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new ThomasBeatEmUpController());
    }

    /***********************************************************************************************
     * Run
     ***********************************************************************************************/
    @Override
    public void run()
    {
        setUpMainGameWindow();
        thomasThemeSong.loop(); //UN-COMMENT THIS TO MAKE THE THEME MUSIC PLAY
        animationTicker.start();
    }

    /***********************************************************************************************
     * Set up main JFrame
     ***********************************************************************************************/
    private void setUpMainGameWindow()
    {
        mainGameWindow.setTitle("Thomas the tank");
        mainGameWindow.setSize(widthOfScreen, heightOfScreen);
        mainGameWindow.add(this);// Adds the paint method to the JFrame
        mainGameWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainGameWindow.getContentPane().setBackground(new Color(200, 235, 255));
        mainGameWindow.setVisible(true);
        mainGameWindow.addKeyListener(this);
    }

    /***********************************************************************************************
     * Paint
     ***********************************************************************************************/
    public void paint(Graphics g)
    {
        g2 = (Graphics2D) g;
        g2.setTransform(identityTx);
        drawThomas();
        g2.setTransform(backgroundTx);
        drawRoad();
        drawObstacle();
        drawTracks(mainTrack);
        drawTracks(upperTrack);
        if (thomas.getThomasBoundingBox().intersects(upperTrack.getTrackBoundingBox()))
        {
            g2.fillOval(500, 500, 50, 50);
        }
    }
    private void drawObstacle()
    {
        g2.setTransform(backgroundTx);
        g2.translate(-widthOfScreen, heightOfScreen - 400);
        g2.fillRect(0, 0, 500, 300);
    }
    /***********************************************************************************************
     * Draw road
     ***********************************************************************************************/
    private void drawRoad()
    {
        Image roadImage = road.getRoadImage();
        int roadImageWidth = roadImage.getWidth(null);
        for (int i = 0; i < 1 + widthOfScreen / roadImageWidth; i++)
        {
            g2.drawImage(roadImage, i * roadImageWidth, 85 * heightOfScreen / 100, null);
        }
    }

    /***********************************************************************************************
     * Draw tracks
     ***********************************************************************************************/
    private void drawTracks(Track track)
    {
        Image trackSectionImage = track.getTrackSectionImage();
        int trackSectionWidth = trackSectionImage.getWidth(null);
        int trackSectionHeight = trackSectionImage.getHeight(null);
        Point trackPosition = track.getTrackPosition();
        int trackYposition = trackPosition.y;
        Rectangle2D.Double trackBoundingBox = new Rectangle2D.Double(trackPosition.x, trackPosition.y, trackSectionWidth, trackSectionHeight);
        g2.setTransform(backgroundTx);
        g2.setColor(Color.green);
        for (int i = 0; i < track.getNumberOfTrackSections(); i++)
        {
            g2.drawImage(trackSectionImage, i * trackSectionWidth, trackYposition, null);
            g2.draw(trackBoundingBox);
        }
    }

    /***********************************************************************************************
     * Draw Thomas
     ***********************************************************************************************/
    public void drawThomas()
    {
        g2.setTransform(identityTx);
        g2.setColor(Color.GREEN);
        try
        {
            if (isGoingLeft)// Thomas going left
            {
                thomasSpriteImage = thomas.nextThomasSpriteImage(false, isGoingLeft);
            }
            if (isGoingRight)// Thomas going right
            {
                thomasSpriteImage = thomas.nextThomasSpriteImage(true, isGoingLeft);
            }
            if (isJumping)
            {
                thomas.getThomasPosition().translate(0, currentJumpingVelocity); // Move Thomas up
                currentJumpingVelocity += gravitationalPull;
//                System.out.println(thomasHomePosition);
                thomas.setThomasBoundingBox(new Rectangle2D.Double(thomas.getThomasBoundingBox().x, thomas.getThomasBoundingBox().y - 30, thomas.getThomasBoundingBox().width, thomas.getThomasBoundingBox().height));
                if(thomas.getThomasPosition().y > thomasHomePosition.getY())
                {
                	//TODO: find out what is changing ThomasHomePosition
                	thomas.setThomasPosition(thomasHomePosition);
//                	System.out.println("isJumping = false");
                	currentJumpingVelocity = initialJumpingVelocity;
                	isJumping = false;
                }
            }
            if (!isJumping && thomas.getThomasPosition().y < (mainTrackPosition.getY()-thomasSpriteImage.getHeight(mainGameWindow)))
            {
            	currentJumpingVelocity = 0;
            	thomas.getThomasPosition().translate(0, currentJumpingVelocity); // Move Thomas up
                currentJumpingVelocity += gravitationalPull;
	                thomas.getThomasPosition().translate(0, +30);  // Move Thomas down
	                thomas.setThomasBoundingBox(new Rectangle2D.Double(thomas.getThomasBoundingBox().x, thomas.getThomasBoundingBox().y, thomas.getThomasBoundingBox().width, thomas.getThomasBoundingBox().height));
            }
            g2.drawImage(thomasSpriteImage, thomas.getThomasPosition().x, thomas.getThomasPosition().y, null);
            g2.draw(thomas.getThomasBoundingBox());
        } catch (Exception ex)
        {
            System.out.println("error reading thomas thomasSpriteImage from thomas sprite thomasSpriteImage array");
        }
    }

    /***********************************************************************************************
     * Respond to key typed...Not being used
     ***********************************************************************************************/
    @Override
    public void keyTyped(KeyEvent e)
    {
    }

    /***********************************************************************************************
     * Respond to key pressed
     ***********************************************************************************************/
    @Override
    public void keyPressed(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) // going right
        {
            isGoingRight = true;
            isGoingLeft = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) // going left
        {
            isGoingLeft = true;
            isGoingRight = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP)
        {
            isJumping = true;
        }
    }

    /***********************************************************************************************
     * Respond to key released
     ***********************************************************************************************/
    @Override
    public void keyReleased(KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) // going right
        {
            isGoingRight = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_LEFT) // going left
        {
            isGoingLeft = false;
        }
        if (e.getKeyCode() == KeyEvent.VK_UP)
        {
//            isJumping = false;
        }
    }

    /***********************************************************************************************
     * Action Performed.....Respond to animation ticker and paint ticker
     ***********************************************************************************************/
    @Override
    public void actionPerformed(ActionEvent e)
    {
        if (isGoingLeft)
        {
            backgroundTx.translate(+30, 0); // Move background right
        }
        if (isGoingRight)
        {
            backgroundTx.translate(-30, 0); // Move backgound left
        }

        repaint();
    }
}