import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


// Il motore di gioco che calcola le possibili mosse, controlla la correttezza delle mosse dei giocatori, assegna i turni, ecc
public class Engine {


    public Socket socket, socket2;
    private OutputStream out, out2;
    private final String name;
    private final String name2;

    public int gira = 1;
    private final int boardSize = 8;
    private final List<Byte> consenti1 = new ArrayList<>(20);
    private final List<Byte> consenti2 = new ArrayList<>(20);
    private byte[] stan;
    private final byte[] fine = "\r\n".getBytes();
    byte giocatore, giocatore2 = 1;
    public boolean ended = false;
    ServerLobbyListener as;
    ServerLobbyListener bs;

    public Engine(ServerLobbyListener a, ServerLobbyListener b, Color kolor1) {
        System.out.println("Motore in funzione");
        this.as = a;
        this.bs = b;
        socket = a.client;
        socket2 = b.client;
        name = a.nick;
        name2 = b.nick;
        System.out.println(name + " vs " + name2);
        if (kolor1 == Color.BLACK) giocatore = 2;
        else {
            giocatore = 1;
            gira = 2;
        }

        try {
            out = socket.getOutputStream();
            out2 = socket2.getOutputStream();
            System.out.println("Serwer START");
            stan = new byte[boardSize * boardSize];
            for (int i = 0; i < 50; i++)
                stan[i] = 1;
            stan[27] = 1;
            stan[28] = 2;
            stan[35] = 2;
            stan[36] = 1;
            byte[] message = new byte[stan.length + fine.length + 1];
            message[0] = 1;
            System.arraycopy(stan, 0, message, 1, stan.length);
            for (int i = 0; i < fine.length; i++)
                message[stan.length + 1 + i] = fine[i];
            out.write(message);
            out2.write(message);
            turaInfo();
            dozwoloneRuchy();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Wykonanie kodu odpowiedzialnego za logikę gry w zależności od ruchu gracza
     *
     * @param response Wiadomość od gracza do silnika gry. W zależności od wartości pierwszego bajta tablicy:
     *                 0 - brak akcji
     *                 4 - ruch gracza
     */
    public void run(byte[] response) {
        try {
            if (!ended) {
                if (gira == 1) {
                    System.out.println("Tura pierwszego okienka");
                    byte[] message = new byte[fine.length + 1];

                    //else {
                    System.out.println("Są ruchy do zrobienia dla lewego:");
                    for (Byte aByte : consenti1) System.out.print(aByte + " ");
                    System.out.println();
                    switch (response[0]) {
                        case 0:
                            break;
                        case 4:
                            byte rzad = response[1], kolumna = response[2];
                            if (!consenti1.contains((byte) (rzad * boardSize + kolumna))) {
                                message[0] = -1;
                                out.write(message);
                                System.out.println("Przesłano info, że tura nie dozwolona");
                                wyslijStan();
                                turaInfo();
                            } else {
                                wykonajRuch(giocatore, rzad, kolumna);
                                wyslijStan();
                                gira = 2;
                                turaInfo();
                                System.out.println("Wysłano stany po wyk ruchu");
                            }
                            break;
                        default:
                            System.out.println("Coś się popsuło");
                            break;
                    }
                    dozwoloneRuchy();
                    if (consenti1.size() + consenti2.size() == 0) {
                        System.out.println("Gra zakończona");
                        if (ktoWygral() == giocatore) System.out.println("Wygrywa " + name);
                        else if (ktoWygral() == giocatore2) System.out.println("Wygrywa " + name2);
                        else System.out.println("Remis");
                        close();
                    } else if (consenti2.size() == 0) {
                        gira = 1;
                        turaInfo();
                        System.out.println("Brak dozwolonych ruchów dla lewego okna");
                    }
                    //}
                } else {
                    System.out.println("Tura drugiego okienka");
                    byte[] message = new byte[fine.length + 1];
                    if (consenti1.size() + consenti2.size() == 0) {
                        System.out.println("Gra zakończona");
                        if (ktoWygral() == giocatore) System.out.println("Wygrywa " + name);
                        else if (ktoWygral() == giocatore2) System.out.println("Wygrywa " + name2);
                        else System.out.println("Remis");
                        //send(new byte[] {7});
                        close();
                    } else if (consenti2.size() == 0) {
                        gira = 1;
                        System.out.println("Brak dozwolonych ruchów dla prawego okna");
                    } else {
                        System.out.println("Są ruchy do zrobienia dla drugiego okna: ");
                        for (Byte aByte : consenti2) System.out.print(aByte + " ");
                        System.out.println();

                        switch (response[0]) {
                            case 0:
                                break;
                            case 4:
                                byte rzad = response[1], kolumna = response[2];
                                if (!consenti2.contains((byte) (rzad * boardSize + kolumna))) {
                                    message[0] = -1;
                                    out2.write(message);
                                    wyslijStan();
                                    turaInfo();
                                    System.out.println("Przesłano info, że tura nie dozwolona");
                                } else {
                                    wykonajRuch(giocatore2, rzad, kolumna);
                                    wyslijStan();
                                    gira = 1;
                                    turaInfo();
                                    System.out.println("Wysłano stany po wyk ruchu");
                                }
                                break;
                            default:
                                System.out.println("Coś się popsuło");
                                break;
                        }
                        dozwoloneRuchy();
                        if (consenti1.size() + consenti2.size() == 0) {
                            System.out.println("Gra zakończona");
                            if (ktoWygral() == giocatore) System.out.println("Wygrywa " + name);
                            else if (ktoWygral() == giocatore2) System.out.println("Wygrywa " + name2);
                            else System.out.println("Remis");
                            close();
                        } else if (consenti1.size() == 0) {
                            gira = 2;
                            turaInfo();
                            System.out.println("Brak dozwolonych ruchów dla lewego okna");
                        }
                    }
                }

            }
        } catch (Exception e) {
            close();
            e.printStackTrace();
        }
    }

    /**
     * Wysłanie do klientów informacji do kogo należy aktualna tura
     *
     */
    public void turaInfo() throws IOException {
        byte[] message = new byte[fine.length + 1];
        if (gira == 1) {
            message[0] = 2; // 2 - Przesyłam info że twoja tura
            System.arraycopy(fine, 0, message, 1, fine.length);
            out.write(message);
            System.out.println("Przesłano info, że tura pierwszego");
            message[0] = 3; // 3 - Przesyłam info że tura przeciwnika
            out2.write(message);
        } else {
            message[0] = 2; // 2 - Przesyłam info że twoja tura
            System.arraycopy(fine, 0, message, 1, fine.length);
            out2.write(message);
            System.out.println("Przesłano info, że tura pierwszego");
            message[0] = 3; // 3 - Przesyłam info że tura przeciwnika
            out.write(message);
        }
    }

    /**
     * Wypełnienie list dozwolonych ruchów
     */
    private void dozwoloneRuchy() {
        consenti1.clear();
        consenti2.clear();
        for (byte i = boardSize / 2; i >= 0; i--) {
            boolean czySasiadowal = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = czySasiaduje(giocatore, i, j);
                czySasiadowal |= t;
                if (t) {
                    if (czyRuchPoprawny(giocatore, i, j)) consenti1.add((byte) (i * boardSize + j));
                }
            }
            if (!czySasiadowal) break;
        }
        for (byte i = boardSize / 2 + 1; i < boardSize; i++) {
            boolean czySasiadowal = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = czySasiaduje(giocatore, i, j);
                czySasiadowal |= t;
                if (t) {
                    if (czyRuchPoprawny(giocatore, i, j)) consenti1.add((byte) (i * boardSize + j));
                }
            }
            if (!czySasiadowal) break;
        }


        for (byte i = boardSize / 2; i >= 0; i--) {
            boolean czySasiadowal = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = czySasiaduje(giocatore2, i, j);
                czySasiadowal |= t;
                if (t) {
                    if (czyRuchPoprawny(giocatore2, i, j)) consenti2.add((byte) (i * boardSize + j));
                }
            }
            if (!czySasiadowal) break;
        }
        for (byte i = boardSize / 2 + 1; i < boardSize; i++) {
            boolean czySasiadowal = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = czySasiaduje(giocatore2, i, j);
                czySasiadowal |= t;
                if (t) {
                    if (czyRuchPoprawny(giocatore2, i, j)) consenti2.add((byte) (i * boardSize + j));
                }
            }
            if (!czySasiadowal) break;
        }
    }

    /**
     * Dostarcza informacji o graczu wygrywającym
     *
     * @return Wartość zmienej gracz,gracz2 (oznaczającej kolor pionków zwycięzcy) lub 0 gdy jest remis
     */
    public byte ktoWygral() {
        int punkty1 = 0, punkty2 = 0;
        for (byte b : stan) {
            if (b == giocatore) punkty1++;
            else if (b == giocatore2) punkty2++;
        }
        if (punkty1 > punkty2) return giocatore;
        else if (punkty1 == punkty2) return 0;
        else return giocatore2;
    }

    /**
     * Zwraca true gdy ruch jest poprawny, false gdy nie jest zgodny z regułami gry
     *
     * @param g1      Kolor pionków gracza wykonującego ruch (1 - biały, 2 - czarny)
     * @param rzad    Numer rzędu wybranego pola na planszy
     * @param kolumna Numer kolumny wybranego pola na planszy
     * @return true gdy ruch poprawny, false w przeciwnym wypadku
     */
    private boolean czyRuchPoprawny(byte g1, byte rzad, byte kolumna) {
        if (rzad >= boardSize || kolumna >= boardSize || stan[rzad * boardSize + kolumna] != 0)
            return false;

        byte g2 = 1;
        if (g1 == 1) g2 = 2;
        boolean tak = false;
        int i;
        // Sprawdzam lewą stronę
        for (i = kolumna - 1; i > 0; i--) {
            if (stan[boardSize * rzad + i] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * rzad + i] == g1) return true;
        // Sprawdzam prawą stronę
        tak = false;
        for (i = kolumna + 1; i < boardSize - 1; i++) {
            if (stan[boardSize * rzad + i] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * rzad + i] == g1) return true;
        // Sprawdzam górę
        tak = false;
        for (i = rzad - 1; i > 0; i--) {
            if (stan[boardSize * i + kolumna] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * i + kolumna] == g1) return true;
        // Sprawdzam dół
        tak = false;
        for (i = rzad + 1; i < boardSize - 1; i++) {
            if (stan[boardSize * i + kolumna] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * i + kolumna] == g1) return true;
        // Sprawdzam lewą górną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna - (boardSize + 1); i > boardSize && (i % boardSize) < ((i + (boardSize + 1)) % boardSize); i -= (boardSize + 1)) {
            if (stan[i] == g2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) < (i + (boardSize + 1)) % boardSize) {
            if (stan[i] == g1) return true;
        }
        // Sprawdzam prawą górną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna - (boardSize - 1); i > boardSize && (i % boardSize) > ((i + (boardSize - 1)) % boardSize); i -= (boardSize - 1)) {
            if (stan[i] == g2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) > (i + (boardSize - 1)) % boardSize) {
            if (stan[i] == g1) return true;
        }
        // Sprawdzam lewą dolną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna + (boardSize - 1); i < boardSize * (boardSize - 1) && (i % boardSize) < ((i - (boardSize - 1)) % boardSize); i += (boardSize - 1)) {
            if (stan[i] == g2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) < (i - (boardSize - 1)) % boardSize) {
            if (stan[i] == g1) return true;
        }
        // Sprawdzam prawą dolną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna + (boardSize + 1); i < boardSize * (boardSize - 1) && (i % boardSize) > ((i - (boardSize + 1)) % boardSize); i += (boardSize + 1)) {
            if (stan[i] == g2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) > (i - (boardSize + 1)) % boardSize) {
            return stan[i] == g1;
        }
        return false;
    }

    /**
     * Zwraca true, gdy pole sąsiaduje z pionkiem przeciwnika, false gdy sąsiaduje z własnym pionkiem lub nie sąsiaduje z żadnym pionkiem
     *
     * @param gracz   Kolor pionków gracza wykonującego ruch (1 - biały, 2 - czarny)
     * @param rzad    Numer rzędu wybranego pola na planszy
     * @param kolumna Numer kolumny wybranego pola na planszy
     * @return true gdy pole sąsiaduje z pionkiem przeciwnika, false w przeciwnym wypadku
     */
    private boolean czySasiaduje(byte gracz, byte rzad, byte kolumna) {
        boolean lewo = false, prawo = false, gora = false, dol = false;
        byte gracz2 = 1;
        if (gracz == 1) gracz2 = 2;

        if (kolumna - 1 >= 0) lewo = stan[rzad * boardSize + kolumna - 1] == gracz2;
        if (kolumna + 1 < boardSize) prawo = stan[rzad * boardSize + kolumna + 1] == gracz2;
        if (rzad + 1 < boardSize) gora = stan[(rzad + 1) * boardSize + kolumna] == gracz2;
        if (rzad - 1 >= 0) dol = stan[(rzad - 1) * boardSize + kolumna] == gracz2;

        if (lewo || prawo || gora || dol) return true;

        // Sprawdzam lewą górną przekątną
        for (int i = boardSize * rzad + kolumna - (boardSize + 1); i > boardSize && (i % boardSize) < ((i + (boardSize + 1)) % boardSize); ) {
            if (stan[i] == gracz2) return true;
            else break;
        }

        // Sprawdzam prawą górną przekątną
        for (int i = boardSize * rzad + kolumna - (boardSize - 1); i > boardSize && (i % boardSize) > ((i + (boardSize - 1)) % boardSize); ) {
            if (stan[i] == gracz2) return true;
            else break;
        }

        // Sprawdzam lewą dolną przekątną
        for (int i = boardSize * rzad + kolumna + (boardSize - 1); i < boardSize * (boardSize - 1) && (i % boardSize) < ((i - (boardSize - 1)) % boardSize); ) {
            if (stan[i] == gracz2) return true;
            else break;
        }

        // Sprawdzam prawą dolną przekątną
        for (int i = boardSize * rzad + kolumna + (boardSize + 1); i < boardSize * (boardSize - 1) && (i % boardSize) > ((i - (boardSize + 1)) % boardSize); ) {
            if (stan[i] == gracz2) return true;
            else break;
        }

        return false;
    }

    /**
     * Wysyła do klientów aktualny stan planszy
     */
    private void wyslijStan() {
        byte[] message = new byte[stan.length + fine.length + 1];
        message[0] = 1;
        System.arraycopy(stan, 0, message, 1, stan.length);
        for (int i = 0; i < fine.length; i++)
            message[stan.length + 1 + i] = fine[i];
        try {
            out.write(message);
            out2.write(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Zmiana stanu planszy po wykonaniu ruchu
     *
     * @param gracz   Kolor pionków gracza wykonującego ruch (1 - biały, 2 - czarny)
     * @param rzad    Numer rzędu wybranego pola na planszy
     * @param kolumna Numer kolumny wybranego pola na planszy
     */
    private void wykonajRuch(byte gracz, byte rzad, byte kolumna) {
        byte gracz2 = 1;
        if (gracz == 1) gracz2 = 2;
        boolean tak = false;
        int i;
        // Sprawdzam lewą stronę
        for (i = kolumna - 1; i > 0; i--) {
            if (stan[boardSize * rzad + i] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * rzad + i] == gracz) {
            for (i++; i <= kolumna; i++) {
                stan[boardSize * rzad + i] = gracz;
                //wykonajRuch(gracz, rzad, (byte) i);
            }
        }
        // Sprawdzam prawą stronę
        tak = false;
        for (i = kolumna + 1; i < boardSize - 1; i++) {
            if (stan[boardSize * rzad + i] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * rzad + i] == gracz) {
            for (i--; i >= kolumna; i--) {
                stan[boardSize * rzad + i] = gracz;
                //wykonajRuch(gracz, rzad, (byte) i);
            }
        }
        // Sprawdzam górę
        tak = false;
        for (i = rzad - 1; i > 0; i--) {
            if (stan[boardSize * i + kolumna] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * i + kolumna] == gracz) {
            for (i++; i <= rzad; i++) {
                stan[boardSize * i + kolumna] = gracz;
                //wykonajRuch(gracz, (byte) i, kolumna);
            }
        }
        // Sprawdzam dół
        tak = false;
        for (i = rzad + 1; i < boardSize - 1; i++) {
            if (stan[boardSize * i + kolumna] == gracz2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * i + kolumna] == gracz) {
            for (i--; i >= rzad; i--) {
                stan[boardSize * i + kolumna] = gracz;
                //wykonajRuch(gracz, (byte) i, kolumna);
            }
        }
        // Sprawdzam lewą górną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna - (boardSize + 1); i > boardSize && (i % boardSize) < ((i + (boardSize + 1)) % boardSize); i -= (boardSize + 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) < (i + (boardSize + 1)) % boardSize) {
            if (stan[i] == gracz) {
                for (i += boardSize + 1; i <= boardSize * rzad + kolumna; i += boardSize + 1) {
                    stan[i] = gracz;
                    //wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
                }
            }
        }
        // Sprawdzam prawą górną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna - (boardSize - 1); i > boardSize && (i % boardSize) > ((i + (boardSize - 1)) % boardSize); i -= (boardSize - 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) > (i + (boardSize - 1)) % boardSize) {
            if (stan[i] == gracz) {
                for (i += boardSize - 1; i <= boardSize * rzad + kolumna; i += boardSize - 1) {
                    stan[i] = gracz;
                    //wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
                }
            }
        }
        // Sprawdzam lewą dolną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna + (boardSize - 1); i < boardSize * (boardSize - 1) && (i % boardSize) < ((i - (boardSize - 1)) % boardSize); i += (boardSize - 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) < (i - (boardSize - 1)) % boardSize) {
            if (stan[i] == gracz) {
                for (i -= boardSize - 1; i >= boardSize * rzad + kolumna; i -= boardSize - 1) {
                    stan[i] = gracz;
                    //wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
                }
            }
        }
        // Sprawdzam prawą dolną przekątną
        tak = false;
        for (i = boardSize * rzad + kolumna + (boardSize + 1); i < boardSize * (boardSize - 1) && (i % boardSize) > ((i - (boardSize + 1)) % boardSize); i += (boardSize + 1)) {
            if (stan[i] == gracz2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) > (i - (boardSize + 1)) % boardSize) {
            if (stan[i] == gracz) {
                for (i -= boardSize + 1; i >= boardSize * rzad + kolumna; i -= boardSize + 1) {
                    stan[i] = gracz;
                    //wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
                }
            }
        }
    }

    /**
     * Wysyła wiadomość na chat do każdego z graczy dopisując prawidłowy prefiks (nick gracza) i dodając znaki końca linii
     *
     * @param s       Socket gracza wysyłającego wiadomość
     * @param message Wiadomość do wysłania na chat
     */
    public void sendM(Socket s, byte[] message) throws IOException {
        byte[] koniec = "\r\n".getBytes();
        byte[] m;
        if (s == socket) {
            m = new byte[name.length() + 2 + message.length + koniec.length];
            for (int i = 0; i < name.length(); i++)
                m[i] = (byte) name.charAt(i);
            m[name.length()] = ':';
            m[name.length() + 1] = ' ';
            for (int i = 0; i < message.length; i++)
                m[name.length() + 2 + i] = message[i];
            for (int i = 0; i < koniec.length; i++)
                m[name.length() + 2 + message.length + i] = koniec[i];
        } else {
            m = new byte[name2.length() + 2 + message.length + koniec.length];
            for (int i = 0; i < name2.length(); i++)
                m[i] = (byte) name2.charAt(i);
            m[name2.length()] = ':';
            m[name2.length() + 1] = ' ';
            for (int i = 0; i < message.length; i++)
                m[name2.length() + 2 + i] = message[i];
            for (int i = 0; i < koniec.length; i++)
                m[name2.length() + 2 + message.length + i] = koniec[i];
        }

        out.write(m);
        out2.write(m);
        System.out.println("Wysłano wiadomość");
    }

    /**
     * Wysłanie informacji o zakończeniu gry
     */
    public void close() {
        try {
            out.write(new byte[]{7, '\r', '\n'});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        try {
            out2.write(new byte[]{7, '\r', '\n'});
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        }
        as.zakonczMecz();
        bs.zakonczMecz();
        ended = true;
    }

    /**
     * Wysłanie informacji o zakończeniu gry z powodu wyjścia przeciwnika
     *
     * @param s Gniazdo gracza, który opuścił rozgrywkę
     */
    public void closeDisc(Socket s) {
        if (socket == s) {
            try {
                out2.write(new byte[]{7, 0, '\r', '\n'});
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
        } else {
            try {
                out.write(new byte[]{7, 0, '\r', '\n'});
            } catch (IOException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
            }
        }
        as.zakonczMecz();
        bs.zakonczMecz();
        ended = true;
    }
}
