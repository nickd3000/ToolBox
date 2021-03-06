package com.physmo.minvio;

// Import the basic graphics classes.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.Map;

/**
 * A basic display module implemented using AWT.
 *
 * @author nickd3000
 */
// TODO: add filled and outline versions of circle
// TODO: change all drawing operations to take int position values.
// TODO: should we use Graphics2D more?
// TODO: add mouse functions to BPanel
// TODO: move bpanel to it's own file as it's going to do more.
// TODO: how to send mouse data to basicdisplay without lots of duplicate functions.
// This might help add some key/mouse input.
public class BasicDisplayAwt implements BasicDisplay {

    private static final int MAX_BUTTONS = 4;
    private static long timerStart = 0;
    private final JFrame mainFrame;
    private final BPanel panel;
    private final int width;
    private final int height;
    Map<Integer, Font> builtInFonts = new HashMap<>();
    private Color drawColor;

    /**
     * Default constructor - creates display with default size
     */
    public BasicDisplayAwt() {
        this(400, 400);
    }

    /**
     * Constructor with user defined window size.
     *
     * @param width  Width of window
     * @param height Height of window
     */
    public BasicDisplayAwt(int width, int height) {
        this.width = width;
        this.height = height;
        panel = new BPanel(width, height);

        mainFrame = new JFrame("...");
        mainFrame.getContentPane().add(panel);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
        mainFrame.setVisible(true);
        mainFrame.setResizable(false);

        drawColor = Color.BLACK;
    }

    /**
     * Close display
     */
    @Override
    public void close() {
        //panel.dispose();
    }

    @Override
    public void refresh() {
        panel.paintImmediately(0, 0, width, height);
    }

    // Refresh variant that delays to keep refresh rate at fps frames per second.
    @Override
    public void refresh(int fps) {


        int msPerFrame = 1000 / fps; // e.g.g 33.3 for 30fps
        while (getEllapsedTime() < msPerFrame) {

            int remainingTime = (int) (msPerFrame - getEllapsedTime());

            if (remainingTime < 5) continue;
            try {
                Thread.sleep(5);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

        //panel.repaint(); // <-- Old method
        panel.paintImmediately(0, 0, width, height);

        startTimer();
    }

    @Override
    public void setTitle(String str) {
        mainFrame.setTitle(str);
    }

    /**
     * Clear the display to supplied color.
     *
     * @param c Color to fill display with.
     */
    @Override
    public void cls(Color c) {
        Color colOld = this.setDrawColor(c);

        panel.g.fillRect(0, 0, width, height);

        this.setDrawColor(colOld);
    }

    @Override
    public Color setDrawColor(Color newCol) {
        Color oldCol = drawColor;
        drawColor = newCol;
        panel.g.setColor(newCol);
        return oldCol;
    }

    @Override
    public int[] getKeyState() {
        return panel.keyDown;
    }

    @Override
    public int[] getKeyStatePrevious() {
        return panel.keyDownPrevious;
    }

    // Update previous keys with current keys so we can tell what changed next time.
    @Override
    public void tickInput() {
        System.arraycopy(panel.keyDown, 0, panel.keyDownPrevious, 0, panel.keyDown.length);
    }

    @Override
    public Image getDrawBuffer() {
        return panel.drawBuffer;
    }

    @Override
    public void drawImage(BufferedImage sourceImage, int x, int y) {
        panel.g.drawImage(sourceImage, x, y, null);
    }

    @Override
    public void drawImage(BufferedImage sourceImage, int x, int y, int w, int h) {
        panel.g.drawImage(sourceImage, x, y, w, h, null);
    }

    @Override
    public Color getColorAtPoint(int x, int y) {
        int rgb = panel.drawBuffer.getRGB(x, y);
        return new Color(rgb);
    }

    @Override
    public int getRGBAtPoint(int x, int y) {
        return panel.drawBuffer1.getRGB(x, y);
    }

    @Override
    public void drawPoint(int x, int y) {
        panel.g.drawLine(x, y, x, y);
        //panel.g.drawLine(x1, y1, x2, y2);

    }

    /**
     * Draw Line
     *
     * @param x1 Start X
     * @param y1 Start Y
     * @param x2 End X
     * @param y2 End Y
     */
    @Override
    public void drawLine(int x1, int y1, int x2, int y2) {
        panel.g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawLine(double x1, double y1, double x2, double y2, double thickness) {
        //Graphics2D g2d = (Graphics2D) panel.g;

        panel.g2d.setStroke(new BasicStroke((float) thickness));
        panel.g2d.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
    }

    @Override
    public void drawFilledRect(int x, int y, int width, int height) {
        panel.g.fillRect(x, y, width, height);
    }

    /**
     * Draw rectangle outline
     *
     * @param x1 Start X
     * @param y1 Start Y
     * @param x2 End X
     * @param y2 End Y
     */
    @Override
    public void drawRect(int x1, int y1, int x2, int y2) {
        drawLine(x1, y1, x2, y1);
        drawLine(x2, y1, x2, y2);
        drawLine(x2, y2, x1, y2);
        drawLine(x1, y1, x1, y2);
    }

    /**
     * Draw a centered circle.
     *
     * @param x x position
     * @param y y position
     * @param r diameter
     */
    @Override
    public void drawFilledCircle(double x, double y, double r) {
        //panel.g2d.fillOval((int) (x - (d / 2)), (int) (y - (d / 2)), (int) (d), (int) (d));
        //double r_2 = r/2.0;
        // This new method does correct sub-pixel float coords.
        panel.g2d.fill(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }

    @Override
    public void drawCircle(double x, double y, double r) {
        //panel.g.drawOval((int) (x - (d / 2)), (int) (y - (d / 2)), (int) (d), (int) (d));
        //double r_2 = r/2.0;
        panel.g2d.draw(new Ellipse2D.Double(x - r, y - r, r * 2, r * 2));
    }

    @Override
    public void drawFilledPolygon(int[] xPoints, int[] yPoints, int numPoints) {
        panel.g2d.fillPolygon(xPoints, yPoints, numPoints);
    }

    @Override
    public void drawText(String str, int x, int y) {

        panel.g.drawString(str, x, y);

    }

    /* TEXT ---------------------------------------------------------------*/

    @Override
    public void setFont(Font font) {
        panel.g.setFont(font);
    }

    @Override
    public void setFont(int size) {
        String builtInFontName = "Verdana";
        if (!builtInFonts.containsKey(size)) {
            Font newFont = new Font(builtInFontName, Font.PLAIN, size);
            builtInFonts.put(size, newFont);
        }

        if (builtInFonts.containsKey(size)) {
            setFont(builtInFonts.get(size));
        }
    }

    @Override
    public int[] getTextSize(String str) {
        FontMetrics metrics = panel.g.getFontMetrics(panel.g.getFont());

        return new int[]{metrics.stringWidth(str), metrics.getAscent(), metrics.getDescent()};

    }

    @Override
    public void startTimer() {
        timerStart = System.nanoTime();
    }

    /* TIMING ---------------------------------------------------------------*/

    // Returns milliseconds since startTimr() was called.
    @Override
    public long getEllapsedTime() {
        return (System.nanoTime() - timerStart) / 1_000_000;
    }

    // Returns a new distinct colour for each supplied index.
    @Override
    public Color getDistinctColor(int index, double saturation) {

        return new Color(Color.HSBtoRGB(((float) index) * 0.6180339887f, (float) saturation, 1.0f));
    }

    /* COLOR ----------------------------------------------------------------*/

    @Override
    public int getMouseX() {
        return panel.mouseX;
    }

    @Override
    public int getMouseY() {
        return panel.mouseY;
    }

    @Override
    public boolean getMouseButtonLeft() {
        int MOUSE_BUTTON_ID_LEFT = 1;
        return panel.mouseButtonStates[MOUSE_BUTTON_ID_LEFT];
    }

    @Override
    public boolean getMouseButtonMiddle() {
        int MOUSE_BUTTON_ID_MIDDLE = 2;
        return panel.mouseButtonStates[MOUSE_BUTTON_ID_MIDDLE];
    }

    @Override
    public boolean getMouseButtonRight() {
        int MOUSE_BUTTON_ID_RIGHT = 3;
        return panel.mouseButtonStates[MOUSE_BUTTON_ID_RIGHT];
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    static class BPanel extends JPanel implements MouseMotionListener, KeyListener, MouseListener {
        private static final long serialVersionUID = 3096588689174149256L;
        BufferedImage drawBuffer;
        BufferedImage drawBuffer1;
        BufferedImage drawBuffer2;
        Graphics g;
        Graphics2D g2d;

        private Graphics graphics_1;
        private Graphics2D graphics2D_1;
        private Graphics graphics_2;
        private Graphics2D graphics2D_2;
        private int activeBuffer = 1;

        final int numKeys = 1000;
        final int[] keyDown = new int[numKeys];
        final int[] keyDownPrevious = new int[numKeys];
        final boolean[] mouseButtonStates = new boolean[MAX_BUTTONS];
        int mouseX = 0;
        int mouseY = 0;

        // Switch between our two drawing buffer interfaces,
        private void flipBuffers() {

            if (activeBuffer == 1) activeBuffer = 2;
            else activeBuffer = 1;

            if (activeBuffer == 1) {
                g = graphics_1;
                g2d = graphics2D_1;
                drawBuffer = drawBuffer1;
            } else {
                g = graphics_2;
                g2d = graphics2D_2;
                drawBuffer = drawBuffer2;
            }


        }

        BPanel(int width, int height) {
            setSize(width, height);
            setVisible(true);
            drawBuffer1 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            drawBuffer2 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            setPreferredSize(new Dimension(width, height));

            graphics_1 = drawBuffer1.getGraphics();
            graphics_2 = drawBuffer2.getGraphics();
            graphics2D_1 = (Graphics2D) graphics_1;
            graphics2D_2 = (Graphics2D) graphics_2;
            graphics2D_1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics2D_2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g = graphics_1;
            g2d = graphics2D_1;
            drawBuffer = drawBuffer1;

            Font fnt = new Font("window", Font.BOLD, 20);
            g.setFont(fnt);

            for (int i = 0; i < numKeys; i++) keyDown[i] = 0;

            this.addKeyListener(this);
            this.addMouseMotionListener(this);
            this.addMouseListener(this);
            this.setFocusable(true);
            this.requestFocusInWindow();
            this.setDoubleBuffered(true);
        }

        @Override
        public void paintComponent(Graphics g) {
            g.drawImage(drawBuffer, 0, 0, null);
            flipBuffers();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            //System.out.println("Key Pressed:" + e.getKeyCode());
            keyDown[e.getKeyCode()] = 1;
        }

        @Override
        public void keyReleased(KeyEvent e) {
            //System.out.println("keyReleased "+e.getKeyCode());
            keyDown[e.getKeyCode()] = 0;
        }


        @Override
        public void mouseClicked(MouseEvent e) {
//            int bid = e.getButton();
//            if (bid<MAX_BUTTONS) {
//                mouseButtonStates[bid]=true;
//            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            int bid = e.getButton();
            if (bid < MAX_BUTTONS) {
                mouseButtonStates[bid] = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int bid = e.getButton();
            if (bid < MAX_BUTTONS) {
                mouseButtonStates[bid] = false;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }
}
