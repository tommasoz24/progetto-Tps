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

    public Engine(ServerLobbyListener a, ServerLobbyListener b, Color color1) {
        System.out.println("Motore in funzione");
        this.as = a;
        this.bs = b;
        socket = a.client;
        socket2 = b.client;
        name = a.username;
        name2 = b.username;
        System.out.println(name + " vs " + name2);
        if (color1 == Color.BLACK) giocatore = 2;
        else {
            giocatore = 1;
            gira = 2;
        }

        try {
            out = socket.getOutputStream();
            out2 = socket2.getOutputStream();
            System.out.println("START server");
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
            turnoInfo();
            mossePermesse();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // metodo che calcola le mosse possibili per il giocatore corrente
    public void run(byte[] response) {
        try {
            if (!ended) {
                if (gira == 1) {
                    System.out.println("finestra rotonda");
                    byte[] message = new byte[fine.length + 1];

                    System.out.println("Ci sono movimenti da fare per la sinistra:");
                    for (Byte aByte : consenti1) System.out.print(aByte + " ");
                    System.out.println();
                    switch (response[0]) {
                        case 0:
                            break;
                        case 4:
                            byte b = response[1], column = response[2];
                            if (!consenti1.contains((byte) (b * boardSize + column))) {
                                message[0] = -1;
                                out.write(message);
                                System.out.println("Svolta non consentita");
                                inviaStato();
                                turnoInfo();
                            } else {
                                makeMove(giocatore, b, column);
                                inviaStato();
                                gira = 2;
                                turnoInfo();
                                System.out.println("stato dopo rilevamento movimento");
                            }
                            break;
                        default:
                            System.out.println("errore di comunicazione");
                            break;
                    }
                    mossePermesse();
                    if (consenti1.size() + consenti2.size() == 0) {
                        System.out.println("Il gioco è finito");
                        if (trovaVincitore() == giocatore) System.out.println("Vittoria " + name);
                        else if (trovaVincitore() == giocatore2) System.out.println("Vittoria " + name2);
                        else System.out.println("Legare");
                        close();
                    } else if (consenti2.size() == 0) {
                        gira = 1;
                        turnoInfo();
                        System.out.println("Nessun movimento consentito per la finestra sinistra");
                    }
                } else {
                    System.out.println("Secondo round");
                    byte[] message = new byte[fine.length + 1];
                    if (consenti1.size() + consenti2.size() == 0) {
                        System.out.println("Il gioco è finito");
                        if (trovaVincitore() == giocatore) System.out.println("Vittorie " + name);
                        else if (trovaVincitore() == giocatore2) System.out.println("Vittorie " + name2);
                        else System.out.println("Pareggio");
                        close();
                    } else if (consenti2.size() == 0) {
                        gira = 1;
                        System.out.println("Nessun movimento consentito per la finestra di destra");
                    } else {
                        System.out.println("Ci sono mosse da fare per la seconda finestra: ");
                        for (Byte aByte : consenti2) System.out.print(aByte + " ");
                        System.out.println();

                        switch (response[0]) {
                            case 0:
                                break;
                            case 4:
                                byte b = response[1], columns = response[2];
                                if (!consenti2.contains((byte) (b * boardSize + columns))) {
                                    message[0] = -1;
                                    out2.write(message);
                                    inviaStato();
                                    turnoInfo();
                                    System.out.println("svolta non consentita");
                                } else {
                                    makeMove(giocatore2, b, columns);
                                    inviaStato();
                                    gira = 1;
                                    turnoInfo();
                                    System.out.println("stato dopo rilevamento movimento");
                                }
                                break;
                            default:
                                System.out.println("Coś się popsuło");
                                break;
                        }
                        mossePermesse();
                        if (consenti1.size() + consenti2.size() == 0) {
                            System.out.println("Il gioco è finito");
                            if (trovaVincitore() == giocatore) System.out.println(" " + name);
                            else if (trovaVincitore() == giocatore2) System.out.println("Vittorie " + name2);
                            else System.out.println("Pareggio");
                            close();
                        } else if (consenti1.size() == 0) {
                            gira = 2;
                            turnoInfo();
                            System.out.println("Nessun movimento consentito per la finestra sinistra");
                        }
                    }
                }

            }
        } catch (Exception e) {
            close();
            e.printStackTrace();
        }
    }

    //Invio di informazioni ai clienti a cui appartiene il turno corrente
    public void turnoInfo() throws IOException {
        byte[] message = new byte[fine.length + 1];
        if (gira == 1) {
            message[0] = 2; // 2 - Invio informazioni sul turno
            System.arraycopy(fine, 0, message, 1, fine.length);
            out.write(message);
            System.out.println("Przesłano info, że tura pierwszego");
            message[0] = 3; // 3 - Invio informazioni sul turno
            out2.write(message);
        } else {
            message[0] = 2; // 2 - Invio informazioni sul turno
            System.arraycopy(fine, 0, message, 1, fine.length);
            out2.write(message);
            System.out.println("Przesłano info, że tura pierwszego");
            message[0] = 3; // 3 - Invio informazioni sul turno
            out.write(message);
        }
    }

    // elenco informazioni dei movimenti consentiti
    private void mossePermesse() {
        consenti1.clear();
        consenti2.clear();
        for (byte i = boardSize / 2; i >= 0; i--) {
            boolean DidSat = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = seMosso(giocatore, i, j);
                DidSat |= t;
                if (t) {
                    if (isMovimentoCorretto(giocatore, i, j)) consenti1.add((byte) (i * boardSize + j));
                }
            }
            if (!DidSat) break;
        }
        for (byte i = boardSize / 2 + 1; i < boardSize; i++) {
            boolean didStat = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = seMosso(giocatore, i, j);
                didStat |= t;
                if (t) {
                    if (isMovimentoCorretto(giocatore, i, j)) consenti1.add((byte) (i * boardSize + j));
                }
            }
            if (!didStat) break;
        }


        for (byte i = boardSize / 2; i >= 0; i--) {
            boolean didStat = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = seMosso(giocatore2, i, j);
                didStat |= t;
                if (t) {
                    if (isMovimentoCorretto(giocatore2, i, j)) consenti2.add((byte) (i * boardSize + j));
                }
            }
            if (!didStat) break;
        }
        for (byte i = boardSize / 2 + 1; i < boardSize; i++) {
            boolean didStat = false;
            for (byte j = 0; j < boardSize; j++) {
                boolean t = seMosso(giocatore2, i, j);
                didStat |= t;
                if (t) {
                    if (isMovimentoCorretto(giocatore2, i, j)) consenti2.add((byte) (i * boardSize + j));
                }
            }
            if (!didStat) break;
        }
    }

    // fornisce informazioni sul giocatore vincente
    public byte trovaVincitore() {
        int punti1 = 0, punti2 = 0;
        for (byte b : stan) {
            if (b == giocatore) punti1++;
            else if (b == giocatore2) punti2++;
        }
        if (punti1 > punti2) return giocatore;
        else if (punti1 == punti2) return 0;
        else return giocatore2;
    }

    // verifica se la mossa è corretta
    private boolean isMovimentoCorretto(byte g1, byte pareggio, byte column) {
        if (pareggio >= boardSize || column >= boardSize || stan[pareggio * boardSize + column] != 0)
            return false;

        byte g2 = 1;
        if (g1 == 1) g2 = 2;
        boolean tak = false;
        int i;
        // Sprawdzam lewą stronę
        for (i = column - 1; i > 0; i--) {
            if (stan[boardSize * pareggio + i] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * pareggio + i] == g1) return true;
        // Sprawdzam prawą stronę
        tak = false;
        for (i = column + 1; i < boardSize - 1; i++) {
            if (stan[boardSize * pareggio + i] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * pareggio + i] == g1) return true;
        // Sprawdzam górę
        tak = false;
        for (i = pareggio - 1; i > 0; i--) {
            if (stan[boardSize * i + column] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * i + column] == g1) return true;
        // Sprawdzam dół
        tak = false;
        for (i = pareggio + 1; i < boardSize - 1; i++) {
            if (stan[boardSize * i + column] == g2) tak = true;
            else break;
        }
        if (tak && stan[boardSize * i + column] == g1) return true;
        // Sprawdzam lewą górną przekątną
        tak = false;
        for (i = boardSize * pareggio + column - (boardSize + 1); i > boardSize && (i % boardSize) < ((i + (boardSize + 1)) % boardSize); i -= (boardSize + 1)) {
            if (stan[i] == g2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) < (i + (boardSize + 1)) % boardSize) {
            if (stan[i] == g1) return true;
        }
        // Sprawdzam prawą górną przekątną
        tak = false;
        for (i = boardSize * pareggio + column - (boardSize - 1); i > boardSize && (i % boardSize) > ((i + (boardSize - 1)) % boardSize); i -= (boardSize - 1)) {
            if (stan[i] == g2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) > (i + (boardSize - 1)) % boardSize) {
            if (stan[i] == g1) return true;
        }
        // Sprawdzam lewą dolną przekątną
        tak = false;
        for (i = boardSize * pareggio + column + (boardSize - 1); i < boardSize * (boardSize - 1) && (i % boardSize) < ((i - (boardSize - 1)) % boardSize); i += (boardSize - 1)) {
            if (stan[i] == g2) tak = true;
            else break;
        }
        if (tak && (i % boardSize) < (i - (boardSize - 1)) % boardSize) {
            if (stan[i] == g1) return true;
        }
        // Sprawdzam prawą dolną przekątną
        tak = false;
        for (i = boardSize * pareggio + column + (boardSize + 1); i < boardSize * (boardSize - 1) && (i % boardSize) > ((i - (boardSize + 1)) % boardSize); i += (boardSize + 1)) {
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
    private boolean seMosso(byte gracz, byte rzad, byte kolumna) {
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
    private void inviaStato() {
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
    private void makeMove(byte gracz, byte rzad, byte kolumna) {
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
        as.endMatch();
        bs.endMatch();
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
        as.endMatch();
        bs.endMatch();
        ended = true;
    }
}
