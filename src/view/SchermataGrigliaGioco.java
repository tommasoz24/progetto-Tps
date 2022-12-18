package view;

import main.Main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

// pannello che contiene la griglia di gioco
public class SchermataGrigliaGioco extends JPanel {
    private final int panelDimension = 8;
    private int dimX, dimY, column, row;
    private boolean t = false;      // t è true se il mouse è dentro il pannello
    private Color a = Color.WHITE;
    public byte player = 1;
    private byte[] status = new byte[panelDimension * panelDimension]; // 0 - niente, 1 - bianco, 2 - nero
    List<Integer> l = new ArrayList<>(20);


    public SchermataGrigliaGioco(boolean colore) { // false - bianco, true - nero
        System.out.println("Creazione pannello di gioco");
        setBackground(Color.GREEN);
        if (colore) {
            a = Color.BLACK;
            player = 2;
            t = true;
        }
        this.setPreferredSize(new Dimension(500, 500));
        this.setMinimumSize(new Dimension(100, 100));
        status[0] = 1;
        status[1] = 2;
        status[2] = 2;
        status[24] = 2;
        status[25] = 2;
        status[27] = 2;
        status[32] = 2;
        status[33] = 1;
        status[34] = 1;
        status[35] = 1;
        status[40] = 1;
        status[48] = 1;
        status[42] = 1;
        status[49] = 2;
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (t) {
                    if (e.getX() / dimX < panelDimension && e.getY() / dimY < panelDimension) {
                        column = e.getX() / dimX;
                        row = e.getY() / dimY;
                    }
                    if (status[panelDimension * row + column] != 1 && status[panelDimension * row + column] != 2 && l.size() > 0) {
                        status[panelDimension * row + column] = player;
                        byte[] message = {4, (byte) row, (byte) column};
                        System.out.println("Box cliccato e invio movimento");
                        Main.clientThread.send(message);
                        l.clear();
                        t = false;
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }
        });
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (column != e.getX() / dimX || row != e.getY() / dimY) {
                    byte player2 = 1;
                    if (player == 1) player2 = 2;

                    if (status[panelDimension * row + column] == 3) {
                        status[panelDimension * row + column] = 0;
                        for (Integer integer : l) {
                            status[integer] = player2;
                        }
                        l.clear();
                        repaint();
                    }

                    if (e.getX() / dimX < panelDimension && e.getY() / dimY < panelDimension) {
                        column = e.getX() / dimX;
                        row = e.getY() / dimY;
                    }
                    if (seVicino(row, column)) {
                        if (status[panelDimension * row + column] == 0) {

                            controllaMovimentiPossibili(row, column);
                            if (l.size() > 0) {
                                status[panelDimension * row + column] = 3;
                                repaint();
                            }
                        }
                    }
                }
            }

        });
    }


    public void assegnaCorretto() {
        t = true;
        Main.window.changeColorFrame((player == 2));
    }


    public void turnoGiocatore() {
        t = false;
        Main.window.changeColorFrame((player != 2));
    }


    public void aggiornamentoGriglia(byte[] stan) {
        this.status = stan;
        repaint();
        byte[] scores = getScores();
        Main.window.label.setText("" + (int) scores[0]);
        Main.window.label2.setText("" + (int) scores[1]);
    }


    public byte[] getScores() {
        byte[] s = new byte[2];
        for (byte b : status) {
            if (b == 1) s[1]++;
            else if (b == 2) s[0]++;
        }
        return s;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //dimX = Math.round(getWidth()/panelDimension);
        dimX = getWidth() / panelDimension;
        //dimY = Math.round(getHeight()/panelDimension);
        dimY = getHeight() / panelDimension;
        g2d.setColor(Color.BLACK);
        for (int i = 0; i <= panelDimension; i++) {
            g2d.drawLine(0, i * dimY, panelDimension * dimX, i * dimY);
            g2d.drawLine(i * dimX, 0, i * dimX, panelDimension * dimY);
        }
        for (int i = 0; i < panelDimension; i++) {
            for (int j = 0; j < panelDimension; j++) {
                if (status[i * panelDimension + j] == 1) {
                    g2d.setColor(Color.WHITE);
                    g2d.fillArc(j * dimX + 2, i * dimY + 2, dimX - 4, dimY - 4, 0, 360);
                } else if (status[i * panelDimension + j] == 2) {
                    g2d.setColor(Color.BLACK);
                    g2d.fillArc(j * dimX + 2, i * dimY + 2, dimX - 4, dimY - 4, 0, 360);
                } else if (status[i * panelDimension + j] == 3) {
                    g2d.setColor(a);
                    g2d.drawArc(j * dimX + 2, i * dimY + 2, dimX - 4, dimY - 4, 0, 360);
                }
            }
        }
        this.setSize(new Dimension(panelDimension * dimX + 1, panelDimension * dimY + 1));
    }


    public boolean seVicino(int row, int column) {
        boolean left = false, right = false, top = false, bottom = false;

        if (column - 1 >= 0)
            left = status[row * panelDimension + column - 1] == 1 || status[row * panelDimension + column - 1] == 2;
        if (column + 1 < panelDimension)
            right = status[row * panelDimension + column + 1] == 1 || status[row * panelDimension + column + 1] == 2;
        if (row + 1 < panelDimension)
            top = status[(row + 1) * panelDimension + column] == 1 || status[(row + 1) * panelDimension + column] == 2;
        if (row - 1 >= 0)
            bottom = status[(row - 1) * panelDimension + column] == 1 || status[(row - 1) * panelDimension + column] == 2;

        if (left || right || top || bottom) return true;

        // diagonale in alto a sinistra
        for (int i = panelDimension * row + column - (panelDimension + 1); i > panelDimension && (i % panelDimension) < ((i + (panelDimension + 1)) % panelDimension); ) {
            if (status[i] == 1 || status[i] == 2) return true;
            else break;
        }

        // diagonale in alto a destra
        for (int i = panelDimension * row + column - (panelDimension - 1); i > panelDimension && (i % panelDimension) > ((i + (panelDimension - 1)) % panelDimension); ) {
            if (status[i] == 1 || status[i] == 2) return true;
            else break;
        }

        // diagonale in basso a sinistra
        for (int i = panelDimension * row + column + (panelDimension - 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) < ((i - (panelDimension - 1)) % panelDimension); ) {
            if (status[i] == 1 || status[i] == 2) return true;
            else break;
        }

        // diagonale in basso a destra
        for (int i = panelDimension * row + column + (panelDimension + 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) > ((i - (panelDimension + 1)) % panelDimension); ) {
            if (status[i] == 1 || status[i] == 2) return true;
            else break;
        }

        return false;

    }

    public void controllaMovimentiPossibili(int row, int column) {
        byte player2 = 1;
        if (player == 1) player2 = 2;
        boolean isCorrect = false;
        int i;
        // controllo a sinistra
        for (i = column - 1; i > 0; i--) {
            if (status[panelDimension * row + i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && status[panelDimension * row + i] == player) {
            for (i++; i < column; i++) {
                status[panelDimension * row + i] = 3;
                l.add((panelDimension * row + i));
            }
            status[panelDimension * row + i] = 3;
        }
        // controllo a destra
        isCorrect = false;
        for (i = column + 1; i < panelDimension - 1; i++) {
            if (status[panelDimension * row + i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && status[panelDimension * row + i] == player) {
            for (i--; i > column; i--) {
                status[panelDimension * row + i] = 3;
                l.add((panelDimension * row + i));
            }
            status[panelDimension * row + i] = 3;
        }
        // controllo in alto
        isCorrect = false;
        for (i = row - 1; i > 0; i--) {
            if (status[panelDimension * i + column] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && status[panelDimension * i + column] == player) {
            for (i++; i < row; i++) {
                status[panelDimension * i + column] = 3;
                l.add(panelDimension * i + column);
            }
            status[panelDimension * i + column] = 3;
        }
        // controllo in basso
        isCorrect = false;
        for (i = row + 1; i < panelDimension - 1; i++) {
            if (status[panelDimension * i + column] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && status[panelDimension * i + column] == player) {
            for (i--; i > row; i--) {
                status[panelDimension * i + column] = 3;
                l.add(panelDimension * i + column);
            }
            status[panelDimension * i + column] = 3;
        }
        // controllo diagonale in alto a sinistra
        isCorrect = false;
        for (i = panelDimension * row + column - (panelDimension + 1); i > panelDimension && (i % panelDimension) < ((i + (panelDimension + 1)) % panelDimension); i -= (panelDimension + 1)) {
            if (status[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % panelDimension) < (i + (panelDimension + 1)) % panelDimension) {
            if (status[i] == player) {
                for (i += panelDimension + 1; i < panelDimension * row + column; i += panelDimension + 1) {
                    status[i] = 3;
                    l.add(i);
                }
                status[i] = 3;
            }
        }
        // controllo diagonale in alto a destra
        isCorrect = false;
        for (i = panelDimension * row + column - (panelDimension - 1); i > panelDimension && (i % panelDimension) > ((i + (panelDimension - 1)) % panelDimension); i -= (panelDimension - 1)) {
            if (status[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % panelDimension) > (i + (panelDimension - 1)) % panelDimension) {
            if (status[i] == player) {
                for (i += panelDimension - 1; i < panelDimension * row + column; i += panelDimension - 1) {
                    status[i] = 3;
                    //sprawdzMozliwePzejecia(i/panelDimension, i%panelDimension);
                    l.add(i);
                }
                status[i] = 3;
            }
        }
        // controllo diagonale in basso a sinistra
        isCorrect = false;
        for (i = panelDimension * row + column + (panelDimension - 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) < ((i - (panelDimension - 1)) % panelDimension); i += (panelDimension - 1)) {
            if (status[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % panelDimension) < (i - (panelDimension - 1)) % panelDimension) {
            if (status[i] == player) {
                for (i -= panelDimension - 1; i > panelDimension * row + column; i -= panelDimension - 1) {
                    status[i] = 3;
                    l.add(i);
                }
                status[i] = 3;
            }
        }
        // controllo diagonale in basso a destra
        isCorrect = false;
        for (i = panelDimension * row + column + (panelDimension + 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) > ((i - (panelDimension + 1)) % panelDimension); i += (panelDimension + 1)) {
            if (status[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % panelDimension) > (i - (panelDimension + 1)) % panelDimension) {
            if (status[i] == player) {
                for (i -= panelDimension + 1; i > panelDimension * row + column; i -= panelDimension + 1) {
                    status[i] = 3;
                    l.add(i);
                }
                status[i] = 3;
            }
        }
    }
}
