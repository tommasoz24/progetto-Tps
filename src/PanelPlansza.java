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
public class PanelPlansza extends JPanel {
    private final int planszaRozmiar = 8;
    private int rozmX, rozmY, kolumna, rzad;
    //private ClientThread clientThread;
    private boolean tura = false;
    private Color a = Color.WHITE;
    public byte gracz = 1;
    private byte[] stan = new byte[planszaRozmiar * planszaRozmiar]; // 0 - nic, 1 - biały, 2 - czarny
    List<Integer> l = new ArrayList<>(20);

    /**
     * Tworzy obiekt Panelu z pionkami gracza o odpowiednim kolorze
     *
     * @param kolor Kolor gracza tworzącego panel, wartość: false - biały, true - czarny
     */
    public PanelPlansza(boolean kolor) { // false - biały, true - czarny
        System.out.println("Tworzenie panelu planszy");
        setBackground(Color.GREEN);
        if (kolor) {
            a = Color.BLACK;
            gracz = 2;
            tura = true;
        }
        this.setPreferredSize(new Dimension(500, 500));
        this.setMinimumSize(new Dimension(100, 100));
        stan[0] = 1;
        stan[1] = 2;
        stan[2] = 2;
        stan[24] = 2;
        stan[25] = 2;
        stan[27] = 2;
        stan[32] = 2;
        stan[33] = 1;
        stan[34] = 1;
        stan[35] = 1;
        stan[40] = 1;
        stan[48] = 1;
        stan[42] = 1;
        stan[49] = 2;
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tura) {
                    if (e.getX() / rozmX < planszaRozmiar && e.getY() / rozmY < planszaRozmiar) {
                        kolumna = e.getX() / rozmX;
                        rzad = e.getY() / rozmY;
                    }
                    if (stan[planszaRozmiar * rzad + kolumna] != 1 && stan[planszaRozmiar * rzad + kolumna] != 2 && l.size() > 0) {
                        stan[planszaRozmiar * rzad + kolumna] = gracz;
                        byte[] message = {4, (byte) rzad, (byte) kolumna};
                        //try {
                        System.out.println("Kliknięto pole i wysyłanie ruchu");
                        Main.clientThread.send(message);
                        l.clear();
                        //} catch (IOException e1) {
                        // TODO Auto-generated catch block
                        //	e1.printStackTrace();
                        //}
                        tura = false;
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
                if (kolumna != e.getX() / rozmX || rzad != e.getY() / rozmY) {
                    byte gracz2 = 1;
                    if (gracz == 1) gracz2 = 2;

                    if (stan[planszaRozmiar * rzad + kolumna] == 3) {
                        stan[planszaRozmiar * rzad + kolumna] = 0;
                        for (int i = 0; i < l.size(); i++) {
                            stan[l.get(i)] = gracz2;
                        }
                        l.clear();
                        repaint();
                    }

                    if (e.getX() / rozmX < planszaRozmiar && e.getY() / rozmY < planszaRozmiar) {
                        kolumna = e.getX() / rozmX;
                        rzad = e.getY() / rozmY;
                    }
                    if (czySasiaduje(rzad, kolumna)) {
                        if (stan[planszaRozmiar * rzad + kolumna] == 0) {

                            sprawdzMozliwePrzejecia(rzad, kolumna);
                            if (l.size() > 0) {
                                stan[planszaRozmiar * rzad + kolumna] = 3;
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
        tura = true;
        Main.okno.zmienKolorRamki(((gracz == 2) ? true : false));
    }

    /**
     * Zabiera turę graczowi
     */
    public void zabierzTure() {
        tura = false;
        Main.okno.zmienKolorRamki(((gracz == 2) ? false : true));
    }

    /**
     * Aktualizuję aktualny stan planszy zamieniąc go na zawartość tablicy
     *
     * @param stan Tablica z docelowym stanem planszy
     */
    public void updatePlansza(byte[] stan) {
        this.stan = stan;
        repaint();
        byte[] scores = getScores();
        Main.okno.label.setText("" + (int) scores[0]);
        Main.okno.label2.setText("" + (int) scores[1]);
    }

    /**
     * Zwraca tablicę dwuelementową reprezentującą obecny wynik
     *
     * @return Tablica dwulementowa, gdzie wartość pierwszego elementu ilość czarnych pionków na planszy, drugiego ilość białych pionków
     */
    public byte[] getScores() {
        byte[] s = new byte[2];
        for (int i = 0; i < stan.length; i++) {
            if (stan[i] == 1) s[1]++;
            else if (stan[i] == 2) s[0]++;
        }
        return s;
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//wyg�adzanie kraw�dzi

        //rozmX = Math.round(getWidth()/planszaRozmiar);
        rozmX = getWidth() / planszaRozmiar;
        //rozmY = Math.round(getHeight()/planszaRozmiar);
        rozmY = getHeight() / planszaRozmiar;
        g2d.setColor(Color.BLACK);
        for (int i = 0; i <= planszaRozmiar; i++) {
            //g2d.drawLine(0, i*rozmY, getWidth(), i*rozmY);
            //g2d.drawLine(i*rozmX, 0, i*rozmX, getHeight());
            g2d.drawLine(0, i * rozmY, planszaRozmiar * rozmX, i * rozmY);
            g2d.drawLine(i * rozmX, 0, i * rozmX, planszaRozmiar * rozmY);
        }
        for (int i = 0; i < planszaRozmiar; i++) { //wiersze
            for (int j = 0; j < planszaRozmiar; j++) { //kolumny
                if (stan[i * planszaRozmiar + j] == 1) {
                    g2d.setColor(Color.WHITE);
                    //g2d.fillArc(j*rozmX + rozmX/2, i*rozmY + rozmY/2, rozmX, rozmY, 0, 360);
                    g2d.fillArc(j * rozmX + 2, i * rozmY + 2, rozmX - 4, rozmY - 4, 0, 360);
                } else if (stan[i * planszaRozmiar + j] == 2) {
                    g2d.setColor(Color.BLACK);
                    //g2d.fillArc(j*rozmX + rozmX/2, i*rozmY + rozmY/2, rozmX/2, rozmY/2, 0, 360);
                    g2d.fillArc(j * rozmX + 2, i * rozmY + 2, rozmX - 4, rozmY - 4, 0, 360);
                } else if (stan[i * planszaRozmiar + j] == 3) {
                    g2d.setColor(a);
                    g2d.drawArc(j * rozmX + 2, i * rozmY + 2, rozmX - 4, rozmY - 4, 0, 360);
                }
            }
        }
        this.setSize(new Dimension(planszaRozmiar * rozmX + 1, planszaRozmiar * rozmY + 1));
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
            lewo = stan[rzad * planszaRozmiar + kolumna - 1] == 1 || stan[rzad * planszaRozmiar + kolumna - 1] == 2;
        if (kolumna + 1 < planszaRozmiar)
            prawo = stan[rzad * planszaRozmiar + kolumna + 1] == 1 || stan[rzad * planszaRozmiar + kolumna + 1] == 2;
        if (rzad + 1 < planszaRozmiar)
            gora = stan[(rzad + 1) * planszaRozmiar + kolumna] == 1 || stan[(rzad + 1) * planszaRozmiar + kolumna] == 2;
        if (rzad - 1 >= 0)
            dol = stan[(rzad - 1) * planszaRozmiar + kolumna] == 1 || stan[(rzad - 1) * planszaRozmiar + kolumna] == 2;

        if (lewo || prawo || gora || dol) return true;

        // Sprawdzam lewą górną przekątną
        for (int i = planszaRozmiar * rzad + kolumna - (planszaRozmiar + 1); i > planszaRozmiar && (i % planszaRozmiar) < ((i + (planszaRozmiar + 1)) % planszaRozmiar); ) {
            if (stan[i] == 1 || stan[i] == 2) return true;
            else break;
        }

        // Sprawdzam prawą górną przekątną
        for (int i = planszaRozmiar * rzad + kolumna - (planszaRozmiar - 1); i > planszaRozmiar && (i % planszaRozmiar) > ((i + (planszaRozmiar - 1)) % planszaRozmiar); ) {
            if (stan[i] == 1 || stan[i] == 2) return true;
            else break;
        }

        // Sprawdzam lewą dolną przekątną
        for (int i = planszaRozmiar * rzad + kolumna + (planszaRozmiar - 1); i < planszaRozmiar * (planszaRozmiar - 1) && (i % planszaRozmiar) < ((i - (planszaRozmiar - 1)) % planszaRozmiar); ) {
            if (stan[i] == 1 || stan[i] == 2) return true;
            else break;
        }

        // Sprawdzam prawą dolną przekątną
        for (int i = planszaRozmiar * rzad + kolumna + (planszaRozmiar + 1); i < planszaRozmiar * (planszaRozmiar - 1) && (i % planszaRozmiar) > ((i - (planszaRozmiar + 1)) % planszaRozmiar); ) {
            if (stan[i] == 1 || stan[i] == 2) return true;
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
        if (gracz == 1) gracz2 = 2;
        boolean tak = false;
        int i;
        // Sprawdzam lewą stronę
        for (i = kolumna - 1; i > 0; i--) {
            if (stan[planszaRozmiar * rzad + i] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[planszaRozmiar * rzad + i] == gracz) {
            for (i++; i < kolumna; i++) {
                stan[planszaRozmiar * rzad + i] = 3;
                //sprawdzMozliwePrzejecia(rzad,i);
                l.add((planszaRozmiar * rzad + i));
            }
            stan[planszaRozmiar * rzad + i] = 3;
        }
        // Sprawdzam prawą stronę
        tak = false;
        for (i = kolumna + 1; i < planszaRozmiar - 1; i++) {
            if (stan[planszaRozmiar * rzad + i] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[planszaRozmiar * rzad + i] == gracz) {
            for (i--; i > kolumna; i--) {
                stan[planszaRozmiar * rzad + i] = 3;
                //sprawdzMozliwePrzejecia(rzad,i);
                l.add((planszaRozmiar * rzad + i));
            }
            stan[planszaRozmiar * rzad + i] = 3;
        }
        // Sprawdzam górę
        tak = false;
        for (i = rzad - 1; i > 0; i--) {
            if (stan[planszaRozmiar * i + kolumna] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[planszaRozmiar * i + kolumna] == gracz) {
            for (i++; i < rzad; i++) {
                stan[planszaRozmiar * i + kolumna] = 3;
                //sprawdzMozliwePrzejecia(i,kolumna);
                l.add(planszaRozmiar * i + kolumna);
            }
            stan[planszaRozmiar * i + kolumna] = 3;
        }
        // Sprawdzam dół
        tak = false;
        for (i = rzad + 1; i < planszaRozmiar - 1; i++) {
            if (stan[planszaRozmiar * i + kolumna] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[planszaRozmiar * i + kolumna] == gracz) {
            for (i--; i > rzad; i--) {
                stan[planszaRozmiar * i + kolumna] = 3;
                //sprawdzMozliwePrzejecia(i,kolumna);
                l.add(planszaRozmiar * i + kolumna);
            }
            stan[planszaRozmiar * i + kolumna] = 3;
        }
        // Sprawdzam lewą górną przekątną
        tak = false;
        for (i = planszaRozmiar * rzad + kolumna - (planszaRozmiar + 1); i > planszaRozmiar && (i % planszaRozmiar) < ((i + (planszaRozmiar + 1)) % planszaRozmiar); i -= (planszaRozmiar + 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % planszaRozmiar) < (i + (planszaRozmiar + 1)) % planszaRozmiar) {
            if (stan[i] == gracz) {
                for (i += planszaRozmiar + 1; i < planszaRozmiar * rzad + kolumna; i += planszaRozmiar + 1) {
                    stan[i] = 3;
                    //sprawdzMozliwePrzejecia(i/planszaRozmiar, i%planszaRozmiar);
                    l.add(i);
                }
                stan[i] = 3;
            }
        }
        // Sprawdzam prawą górną przekątną
        tak = false;
        for (i = planszaRozmiar * rzad + kolumna - (planszaRozmiar - 1); i > planszaRozmiar && (i % planszaRozmiar) > ((i + (planszaRozmiar - 1)) % planszaRozmiar); i -= (planszaRozmiar - 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % planszaRozmiar) > (i + (planszaRozmiar - 1)) % planszaRozmiar) {
            if (stan[i] == gracz) {
                for (i += planszaRozmiar - 1; i < planszaRozmiar * rzad + kolumna; i += planszaRozmiar - 1) {
                    stan[i] = 3;
                    //sprawdzMozliwePrzejecia(i/planszaRozmiar, i%planszaRozmiar);
                    l.add(i);
                }
                stan[i] = 3;
            }
        }
        // Sprawdzam lewą dolną przekątną
        tak = false;
        for (i = planszaRozmiar * rzad + kolumna + (planszaRozmiar - 1); i < planszaRozmiar * (planszaRozmiar - 1) && (i % planszaRozmiar) < ((i - (planszaRozmiar - 1)) % planszaRozmiar); i += (planszaRozmiar - 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % planszaRozmiar) < (i - (planszaRozmiar - 1)) % planszaRozmiar) {
            if (stan[i] == gracz) {
                for (i -= planszaRozmiar - 1; i > planszaRozmiar * rzad + kolumna; i -= planszaRozmiar - 1) {
                    stan[i] = 3;
                    //sprawdzMozliwePrzejecia(i/planszaRozmiar, i%planszaRozmiar);
                    l.add(i);
                }
                stan[i] = 3;
            }
        }
        // Sprawdzam prawą dolną przekątną
        tak = false;
        for (i = planszaRozmiar * rzad + kolumna + (planszaRozmiar + 1); i < planszaRozmiar * (planszaRozmiar - 1) && (i % planszaRozmiar) > ((i - (planszaRozmiar + 1)) % planszaRozmiar); i += (planszaRozmiar + 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % planszaRozmiar) > (i - (planszaRozmiar + 1)) % planszaRozmiar) {
            if (stan[i] == gracz) {
                for (i -= planszaRozmiar + 1; i > planszaRozmiar * rzad + kolumna; i -= planszaRozmiar + 1) {
                    stan[i] = 3;
                    //sprawdzMozliwePrzejecia(i/planszaRozmiar, i%planszaRozmiar);
                    l.add(i);
                }
                stan[i] = 3;
            }
        }
    }
}
