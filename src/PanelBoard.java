import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel zawierający planszę do gry
 *
 * @author Bartosz Ruta
 */
public class PanelBoard extends JPanel {
    private final int panelDimension = 8;
    private int dimX, dimY, colonna, riga;
    private boolean t = false;      // t è true se il mouse è dentro il pannello
    private Color a = Color.WHITE;
    public byte giocatore = 1;
    private byte[] stato = new byte[panelDimension * panelDimension]; // 0 - niente, 1 - bianco, 2 - nero
    List<Integer> l = new ArrayList<>(20);

    /**
     * Tworzy obiekt Panelu z pionkami gracza o odpowiednim kolorze
     *
     * @param colore Kolor gracza tworzącego panel, wartość: false - biały, true - czarny
     */
    public PanelBoard(boolean colore) { // false - bianco, true - nero
        System.out.println("Tworzenie panelu planszy");
        setBackground(Color.GREEN);
        if (colore) {
            a = Color.BLACK;
            giocatore = 2;
            t = true;
        }
        this.setPreferredSize(new Dimension(500, 500));
        this.setMinimumSize(new Dimension(100, 100));
        stato[0] = 1;
        stato[1] = 2;
        stato[2] = 2;
        stato[24] = 2;
        stato[25] = 2;
        stato[27] = 2;
        stato[32] = 2;
        stato[33] = 1;
        stato[34] = 1;
        stato[35] = 1;
        stato[40] = 1;
        stato[48] = 1;
        stato[42] = 1;
        stato[49] = 2;
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (t) {
                    if (e.getX() / dimX < panelDimension && e.getY() / dimY < panelDimension) {
                        colonna = e.getX() / dimX;
                        riga = e.getY() / dimY;
                    }
                    if (stato[panelDimension * riga + colonna] != 1 && stato[panelDimension * riga + colonna] != 2 && l.size() > 0) {
                        stato[panelDimension * riga + colonna] = giocatore;
                        byte[] message = {4, (byte) riga, (byte) colonna};
                        //try {
                        System.out.println("Kliknięto pole i wysyłanie ruchu");
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
                if (colonna != e.getX() / dimX || riga != e.getY() / dimY) {
                    byte gracz2 = 1;
                    if (giocatore == 1) gracz2 = 2;

                    if (stato[panelDimension * riga + colonna] == 3) {
                        stato[panelDimension * riga + colonna] = 0;
                        for (Integer integer : l) {
                            stato[integer] = gracz2;
                        }
                        l.clear();
                        repaint();
                    }

                    if (e.getX() / dimX < panelDimension && e.getY() / dimY < panelDimension) {
                        colonna = e.getX() / dimX;
                        riga = e.getY() / dimY;
                    }
                    if (czySasiaduje(riga, colonna)) {
                        if (stato[panelDimension * riga + colonna] == 0) {

                            sprawdzMozliwePrzejecia(riga, colonna);
                            if (l.size() > 0) {
                                stato[panelDimension * riga + colonna] = 3;
                                repaint();
                            }
                        }
                    }
                }
            }

        });
    }

    /**
     * Przydziela turę graczowi
     */
    public void przydzielTure() {
        t = true;
        Main.window.zmienKolorRamki(((giocatore == 2) ? true : false));
    }

    /**
     * Zabiera turę graczowi
     */
    public void zabierzTure() {
        t = false;
        Main.window.zmienKolorRamki(((giocatore == 2) ? false : true));
    }

    /**
     * Aktualizuję aktualny stato planszy zamieniąc go na zawartość tablicy
     *
     * @param stan Tablica z docelowym stanem planszy
     */
    public void updatePlansza(byte[] stan) {
        this.stato = stan;
        repaint();
        byte[] scores = getScores();
        Main.window.label.setText("" + (int) scores[0]);
        Main.window.label2.setText("" + (int) scores[1]);
    }

    /**
     * Zwraca tablicę dwuelementową reprezentującą obecny wynik
     *
     * @return Tablica dwulementowa, gdzie wartość pierwszego elementu ilość czarnych pionków na planszy, drugiego ilość białych pionków
     */
    public byte[] getScores() {
        byte[] s = new byte[2];
        for (int i = 0; i < stato.length; i++) {
            if (stato[i] == 1) s[1]++;
            else if (stato[i] == 2) s[0]++;
        }
        return s;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//wyg�adzanie kraw�dzi

        //dimX = Math.round(getWidth()/panelDimension);
        dimX = getWidth() / panelDimension;
        //dimY = Math.round(getHeight()/panelDimension);
        dimY = getHeight() / panelDimension;
        g2d.setColor(Color.BLACK);
        for (int i = 0; i <= panelDimension; i++) {
            //g2d.drawLine(0, i*dimY, getWidth(), i*dimY);
            //g2d.drawLine(i*dimX, 0, i*dimX, getHeight());
            g2d.drawLine(0, i * dimY, panelDimension * dimX, i * dimY);
            g2d.drawLine(i * dimX, 0, i * dimX, panelDimension * dimY);
        }
        for (int i = 0; i < panelDimension; i++) { //wiersze
            for (int j = 0; j < panelDimension; j++) { //kolumny
                if (stato[i * panelDimension + j] == 1) {
                    g2d.setColor(Color.WHITE);
                    //g2d.fillArc(j*dimX + dimX/2, i*dimY + dimY/2, dimX, dimY, 0, 360);
                    g2d.fillArc(j * dimX + 2, i * dimY + 2, dimX - 4, dimY - 4, 0, 360);
                } else if (stato[i * panelDimension + j] == 2) {
                    g2d.setColor(Color.BLACK);
                    //g2d.fillArc(j*dimX + dimX/2, i*dimY + dimY/2, dimX/2, dimY/2, 0, 360);
                    g2d.fillArc(j * dimX + 2, i * dimY + 2, dimX - 4, dimY - 4, 0, 360);
                } else if (stato[i * panelDimension + j] == 3) {
                    g2d.setColor(a);
                    g2d.drawArc(j * dimX + 2, i * dimY + 2, dimX - 4, dimY - 4, 0, 360);
                }
            }
        }
        this.setSize(new Dimension(panelDimension * dimX + 1, panelDimension * dimY + 1));
    }

    /**
     * Zwraca true, gdy ewentualnie postawiony pionek sąsiaduje z pionkiem przeciwnika
     *
     * @param rzad    Numer rzędu wykonywanego ruchu
     * @param kolumna Numer kolumny wykonywanego ruchu
     * @return true, gdy ewentualnie postawiony pionek sąsiaduje z pionkiem przeciwnika, false w przeciwnym wypadku
     */
    public boolean czySasiaduje(int rzad, int kolumna) {
        boolean lewo = false, prawo = false, gora = false, dol = false;

        if (kolumna - 1 >= 0)
            lewo = stato[rzad * panelDimension + kolumna - 1] == 1 || stato[rzad * panelDimension + kolumna - 1] == 2;
        if (kolumna + 1 < panelDimension)
            prawo = stato[rzad * panelDimension + kolumna + 1] == 1 || stato[rzad * panelDimension + kolumna + 1] == 2;
        if (rzad + 1 < panelDimension)
            gora = stato[(rzad + 1) * panelDimension + kolumna] == 1 || stato[(rzad + 1) * panelDimension + kolumna] == 2;
        if (rzad - 1 >= 0)
            dol = stato[(rzad - 1) * panelDimension + kolumna] == 1 || stato[(rzad - 1) * panelDimension + kolumna] == 2;

        if (lewo || prawo || gora || dol) return true;

        // Sprawdzam lewą górną przekątną
        for (int i = panelDimension * rzad + kolumna - (panelDimension + 1); i > panelDimension && (i % panelDimension) < ((i + (panelDimension + 1)) % panelDimension); ) {
            if (stato[i] == 1 || stato[i] == 2) return true;
            else break;
        }

        // Sprawdzam prawą górną przekątną
        for (int i = panelDimension * rzad + kolumna - (panelDimension - 1); i > panelDimension && (i % panelDimension) > ((i + (panelDimension - 1)) % panelDimension); ) {
            if (stato[i] == 1 || stato[i] == 2) return true;
            else break;
        }

        // Sprawdzam lewą dolną przekątną
        for (int i = panelDimension * rzad + kolumna + (panelDimension - 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) < ((i - (panelDimension - 1)) % panelDimension); ) {
            if (stato[i] == 1 || stato[i] == 2) return true;
            else break;
        }

        // Sprawdzam prawą dolną przekątną
        for (int i = panelDimension * rzad + kolumna + (panelDimension + 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) > ((i - (panelDimension + 1)) % panelDimension); ) {
            if (stato[i] == 1 || stato[i] == 2) return true;
            else break;
        }

        return false;

    }

    /**
     * Wypełnia listę pól, które zostałyby przejęte po wykonaniu ruchu
     *
     * @param rzad    Numer rzędu wykonywanego ruchu
     * @param kolumna Numer rzędu wykonywanego ruchu
     */
    public void sprawdzMozliwePrzejecia(int rzad, int kolumna) {
        byte gracz2 = 1;
        if (giocatore == 1) gracz2 = 2;
        boolean tak = false;
        int i;
        // Sprawdzam lewą stronę
        for (i = kolumna - 1; i > 0; i--) {
            if (stato[panelDimension * rzad + i] == gracz2) tak = true;
            else break;
        }
        if (tak && stato[panelDimension * rzad + i] == giocatore) {
            for (i++; i < kolumna; i++) {
                stato[panelDimension * rzad + i] = 3;
                //sprawdzMozliwePrzejecia(riga,i);
                l.add((panelDimension * rzad + i));
            }
            stato[panelDimension * rzad + i] = 3;
        }
        // Sprawdzam prawą stronę
        tak = false;
        for (i = kolumna + 1; i < panelDimension - 1; i++) {
            if (stato[panelDimension * rzad + i] == gracz2) tak = true;
            else break;
        }
        if (tak && stato[panelDimension * rzad + i] == giocatore) {
            for (i--; i > kolumna; i--) {
                stato[panelDimension * rzad + i] = 3;
                //sprawdzMozliwePrzejecia(riga,i);
                l.add((panelDimension * rzad + i));
            }
            stato[panelDimension * rzad + i] = 3;
        }
        // Sprawdzam górę
        tak = false;
        for (i = rzad - 1; i > 0; i--) {
            if (stato[panelDimension * i + kolumna] == gracz2) tak = true;
            else break;
        }
        if (tak && stato[panelDimension * i + kolumna] == giocatore) {
            for (i++; i < rzad; i++) {
                stato[panelDimension * i + kolumna] = 3;
                //sprawdzMozliwePrzejecia(i,colonna);
                l.add(panelDimension * i + kolumna);
            }
            stato[panelDimension * i + kolumna] = 3;
        }
        // Sprawdzam dół
        tak = false;
        for (i = rzad + 1; i < panelDimension - 1; i++) {
            if (stato[panelDimension * i + kolumna] == gracz2) tak = true;
            else break;
        }
        if (tak && stato[panelDimension * i + kolumna] == giocatore) {
            for (i--; i > rzad; i--) {
                stato[panelDimension * i + kolumna] = 3;
                //sprawdzMozliwePrzejecia(i,colonna);
                l.add(panelDimension * i + kolumna);
            }
            stato[panelDimension * i + kolumna] = 3;
        }
        // Sprawdzam lewą górną przekątną
        tak = false;
        for (i = panelDimension * rzad + kolumna - (panelDimension + 1); i > panelDimension && (i % panelDimension) < ((i + (panelDimension + 1)) % panelDimension); i -= (panelDimension + 1)) {
            if (stato[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % panelDimension) < (i + (panelDimension + 1)) % panelDimension) {
            if (stato[i] == giocatore) {
                for (i += panelDimension + 1; i < panelDimension * rzad + kolumna; i += panelDimension + 1) {
                    stato[i] = 3;
                    //sprawdzMozliwePrzejecia(i/panelDimension, i%panelDimension);
                    l.add(i);
                }
                stato[i] = 3;
            }
        }
        // Sprawdzam prawą górną przekątną
        tak = false;
        for (i = panelDimension * rzad + kolumna - (panelDimension - 1); i > panelDimension && (i % panelDimension) > ((i + (panelDimension - 1)) % panelDimension); i -= (panelDimension - 1)) {
            if (stato[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % panelDimension) > (i + (panelDimension - 1)) % panelDimension) {
            if (stato[i] == giocatore) {
                for (i += panelDimension - 1; i < panelDimension * rzad + kolumna; i += panelDimension - 1) {
                    stato[i] = 3;
                    //sprawdzMozliwePrzejecia(i/panelDimension, i%panelDimension);
                    l.add(i);
                }
                stato[i] = 3;
            }
        }
        // Sprawdzam lewą dolną przekątną
        tak = false;
        for (i = panelDimension * rzad + kolumna + (panelDimension - 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) < ((i - (panelDimension - 1)) % panelDimension); i += (panelDimension - 1)) {
            if (stato[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % panelDimension) < (i - (panelDimension - 1)) % panelDimension) {
            if (stato[i] == giocatore) {
                for (i -= panelDimension - 1; i > panelDimension * rzad + kolumna; i -= panelDimension - 1) {
                    stato[i] = 3;
                    //sprawdzMozliwePrzejecia(i/panelDimension, i%panelDimension);
                    l.add(i);
                }
                stato[i] = 3;
            }
        }
        // Sprawdzam prawą dolną przekątną
        tak = false;
        for (i = panelDimension * rzad + kolumna + (panelDimension + 1); i < panelDimension * (panelDimension - 1) && (i % panelDimension) > ((i - (panelDimension + 1)) % panelDimension); i += (panelDimension + 1)) {
            if (stato[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % panelDimension) > (i - (panelDimension + 1)) % panelDimension) {
            if (stato[i] == giocatore) {
                for (i -= panelDimension + 1; i > panelDimension * rzad + kolumna; i -= panelDimension + 1) {
                    stato[i] = 3;
                    //sprawdzMozliwePrzejecia(i/panelDimension, i%panelDimension);
                    l.add(i);
                }
                stato[i] = 3;
            }
        }
    }
}
