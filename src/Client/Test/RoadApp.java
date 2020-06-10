package Client.Test;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.Segment;

//import com.javatmz.javaapp.Aknari.MoveAction;

public class RoadApp extends JFrame {

    private static final int D_W = 1500;
    private static final int D_H = 1200;
    int with = 1600;
    int height = 768;
    int roadW = 768;
    int segL = 768;
    double camD = 0.84;
    int N;
    int playerX = 0;
    int pos = 0;

    List<Line> lines = new ArrayList<RoadApp.Line>();

    List<Integer> listValues = new ArrayList<Integer>();

    DrawPanel drawPanel = new DrawPanel();

    public RoadApp() {
        for (int i = 0; i < 1600; i++) {
            Line line = new Line();
            line.z = i * segL;
            if (i > 200 && i < 700) {
                line.curve = 4;
            }
            if (i > 1000) {
                line.y = Math.sin(Math.toRadians(i / 30)) * 1500;
                if (line.y < 0) {
                    line.y = 0;
                }


            }
            if (i % 2 == 0) {
                line.spriteX = -2.5;
                line.drawTree = true;
                System.out.println("drawTree " + i);
            }

            lines.add(line);
        }
        N = lines.size();

        ActionListener listener = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {

                // drawPanel.repaint();

            }
        };
        Timer timer = new Timer(10, listener);
        timer.start();
        add(drawPanel);

        pack();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private class DrawPanel extends JPanel {

        public DrawPanel() {

            String VK_LEFT = "VK_LEFT";
            KeyStroke W = KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0);
            InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
            inputMap.put(W, VK_LEFT);
            ActionMap actionMap = getActionMap();
            actionMap.put(VK_LEFT, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    playerX -= 200;
                    drawPanel.repaint();

                }
            });

            String VK_RIGHT = "VK_RIGHT";
            KeyStroke WVK_RIGHT = KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0);
            inputMap.put(WVK_RIGHT, VK_RIGHT);
            actionMap.put(VK_RIGHT, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    playerX += 200;
                    drawPanel.repaint();

                }
            });

            String VK_UP = "VK_UP";
            KeyStroke WVK_UP = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
            inputMap.put(WVK_UP, VK_UP);
            actionMap.put(VK_UP, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pos += 200;
                    drawPanel.repaint();

                }
            });

            String VK_DOWN = "VK_DOWN";
            KeyStroke WVK_DOWN = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
            inputMap.put(WVK_DOWN, VK_DOWN);
            actionMap.put(VK_DOWN, new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pos -= 200;
                    drawPanel.repaint();

                }
            });

        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            drawValues(g);


        }

        private void drawValues(Graphics g) {
            int startPos = pos / segL;
            double x = 0, dx = 0;
            double maxY = height;
            int camH = 1500 + (int) lines.get(startPos).y;
            for (int n = startPos; n < startPos + 300; n++) {
                Line l = lines.get(n % N);
                l.project(playerX - (int) x, camH, pos);
                x += dx;
                dx += l.curve;
                if (l.Y > 0 && l.Y < maxY) {

                    maxY = l.Y;

                    Color grass = ((n / 2) % 2) == 0 ? new Color(16, 200, 16) : new Color(0, 154, 0);
                    Color rumble = ((n / 2) % 2) == 0 ? new Color(255, 255, 255) : new Color(255, 0, 0);
                    Color road = Color.black;
                    Color midel =  ((n / 2) % 2) == 0 ? new Color(255, 255, 255) : new Color(0, 0, 0);


                    Line p = null;

                    if (n == 0) {
                        p = l;
                    } else {
                        p = lines.get((n - 1) % N);
                    }

                    drawQwad(g, grass, 0, (int) p.Y, with, 0, (int) l.Y, with);

                    drawQwad(g, rumble, (int) p.X, (int) p.Y, (int) (p.W * 1.5), (int) l.X, (int) l.Y,
                            (int) (l.W * 1.5));

                    drawQwad(g, road, (int) p.X, (int) p.Y, (int) (p.W * 1.4), (int) l.X, (int) l.Y,
                            (int) (l.W * 1.4));

                    drawQwad(g, midel, (int) p.X, (int) p.Y, (int) (p.W * 0.8), (int) l.X, (int) l.Y,
                            (int) (l.W * 0.8));

                    drawQwad(g, road, (int) p.X, (int) p.Y, (int) (p.W * 0.7), (int) l.X, (int) l.Y, (int) (l.W* 0.7));


                }
            }
//draw Skye

            Graphics g9d = g;
            g9d.setColor(Color.blue);
            g9d.fillRect(0, 0, 1600, 387);

            try {
                g9d.drawImage(
                        ImageIO.read(new File(
                                "C:/Users/HP/Desktop/paintr/tangarfa/workspace/java-tmz-test/ressource/cropped-sky-web-background.jpg")),
                        0, 0, this);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // draw tree
            for (int n = startPos + 300; n > startPos; n--) {
                if (lines.get(n % N).drawTree) {
                    lines.get(n % N).drawGraphic();
//					System.out.println("OK");
                    Graphics gd = g;
                    //gd.set
					/*gd.setColor(Color.BLACK);
						gd.fillRect((int) lines.get(n % N).destX, (int) lines.get(n % N).destY,23,70);
					*/

                }
            }

        }

        void drawQwad(Graphics g, Color c, int x1, int y1, int w1, int x2, int y2, int w2) {

            Graphics g9d = g;
            int[] x9Points = { x1 - w1, x2 - w2, x2 + w2, x1 + w1 };
            int[] y9Points = { y1, y2, y2, y1 };
            int n9Points = 4;
            g9d.setColor(c);
            g9d.fillPolygon(x9Points, y9Points, n9Points);

        }

        public Dimension getPreferredSize() {
            return new Dimension(D_W, D_H);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RoadApp();
            }
        });
    }

    class Line {
        double x, y, z;
        double X, Y, W;
        double scale, curve, spriteX, clip;
        Graphics graphic;

        double destX;
        double destY;
        double destW;
        double destH;
        boolean drawTree = false;

        public Line() {
            curve = x = y = z = 0;
        }

        void project(int camX, int camY, int camZ) {
            scale = camD / (z - camZ);
            X = (1 + scale * (x - camX)) * with / 2;
            Y = (1 - scale * (y - camY)) * height / 2;
            W = scale * roadW * with / 2;
        }

        void drawGraphic() {
            int W = 100;
            int h = 100;
            destX = X + scale * spriteX * with / 2;
            destY = Y + 4;
            destW = W * W / 266;
            destH = h * W / 266;

            destX += destW * spriteX;
            destY += destH * (-1);

            double clipH = destY + destH - clip;
            if (clipH < 0)
                clipH = 0;
            if (clipH > destH)
                drawTree = false;

        }
    }
}