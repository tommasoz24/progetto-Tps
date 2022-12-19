package controller;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


// Il motore di gioco che calcola le possibili mosse, controlla la correttezza delle mosse dei giocatori, assegna i turni, ecc
public class MotoreDiGioco {


    public Socket socket, socket2;
    private OutputStream out, out2;
    private final String name;
    private final String name2;

    public int gira = 1;
    private final int boardSize = 8;
    private final List<Byte> consenti1 = new ArrayList<>(20);
    private final List<Byte> consenti2 = new ArrayList<>(20);
    private byte[] stato;
    private final byte[] fine = "\r\n".getBytes();
    byte giocatore, giocatore2 = 1;
    public boolean ended = false;
    ClientHandler serverLobbyListener1;
    ClientHandler serverLobbyListener2;

    public MotoreDiGioco(ClientHandler serverLobbyListener1, ClientHandler serverLobbyListener2, Color color1) {
        System.out.println("Engine in funzione");
        this.serverLobbyListener1 = serverLobbyListener1;
        this.serverLobbyListener2 = serverLobbyListener2;
        socket = serverLobbyListener1.client;
        socket2 = serverLobbyListener2.client;
        name = serverLobbyListener1.username;
        name2 = serverLobbyListener2.username;
        System.out.println(name + " vs " + name2);
        if (color1 == Color.BLACK) giocatore = 2;
        else {
            giocatore = 1;
            gira = 2;
        }

        try {
            // inizializzo il gioco
            out = socket.getOutputStream();
            out2 = socket2.getOutputStream();
            System.out.println("START server");
            stato = new byte[boardSize * boardSize];
            stato[27] = 1;
            stato[28] = 2;
            stato[35] = 2;
            stato[36] = 1;
            byte[] message = new byte[stato.length + fine.length + 1];
            message[0] = 1;
            System.arraycopy(stato, 0, message, 1, stato.length);
            for (int i = 0; i < fine.length; i++)
                message[stato.length + 1 + i] = fine[i];
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
            System.out.println("informazione primo round");
            message[0] = 3; // 3 - Invio informazioni sul turno
            out2.write(message);
        } else {
            message[0] = 2; // 2 - Invio informazioni sul turno
            System.arraycopy(fine, 0, message, 1, fine.length);
            out2.write(message);
            System.out.println("informazione primo round");
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
        for (byte b : stato) {
            if (b == giocatore) punti1++;
            else if (b == giocatore2) punti2++;
        }
        if (punti1 > punti2) return giocatore;
        else if (punti1 == punti2) return 0;
        else return giocatore2;
    }

    // verifica se la mossa è corretta
    private boolean isMovimentoCorretto(byte g1, byte pareggio, byte column) {
        if (pareggio >= boardSize || column >= boardSize || stato[pareggio * boardSize + column] != 0)
            return false;

        byte g2 = 1;
        if (g1 == 1) g2 = 2;
        boolean corretto = false;
        int i;
        // Controllo del lato sinistro
        for (i = column - 1; i > 0; i--) {
            if (stato[boardSize * pareggio + i] == g2) corretto = true;
            else break;
        }
        if (corretto && stato[boardSize * pareggio + i] == g1) return true;
        // Controllo del lato destro
        corretto = false;
        for (i = column + 1; i < boardSize - 1; i++) {
            if (stato[boardSize * pareggio + i] == g2) corretto = true;
            else break;
        }
        if (corretto && stato[boardSize * pareggio + i] == g1) return true;
        // Sprawdzam górę
        corretto = false;
        for (i = pareggio - 1; i > 0; i--) {
            if (stato[boardSize * i + column] == g2) corretto = true;
            else break;
        }
        if (corretto && stato[boardSize * i + column] == g1) return true;
        // Controllo del fondo
        corretto = false;
        for (i = pareggio + 1; i < boardSize - 1; i++) {
            if (stato[boardSize * i + column] == g2) corretto = true;
            else break;
        }
        if (corretto && stato[boardSize * i + column] == g1) return true;
        // Controllo la diagonale superiore sinistra
        corretto = false;
        for (i = boardSize * pareggio + column - (boardSize + 1); i > boardSize && (i % boardSize) < ((i + (boardSize + 1)) % boardSize); i -= (boardSize + 1)) {
            if (stato[i] == g2) corretto = true;
            else break;
        }
        if (corretto && (i % boardSize) < (i + (boardSize + 1)) % boardSize) {
            if (stato[i] == g1) return true;
        }
        // Controllo la diagonale superiore destra
        corretto = false;
        for (i = boardSize * pareggio + column - (boardSize - 1); i > boardSize && (i % boardSize) > ((i + (boardSize - 1)) % boardSize); i -= (boardSize - 1)) {
            if (stato[i] == g2) corretto = true;
            else break;
        }
        if (corretto && (i % boardSize) > (i + (boardSize - 1)) % boardSize) {
            if (stato[i] == g1) return true;
        }
        // Controllo la diagonale inferiore sinistra
        corretto = false;
        for (i = boardSize * pareggio + column + (boardSize - 1); i < boardSize * (boardSize - 1) && (i % boardSize) < ((i - (boardSize - 1)) % boardSize); i += (boardSize - 1)) {
            if (stato[i] == g2) corretto = true;
            else break;
        }
        if (corretto && (i % boardSize) < (i - (boardSize - 1)) % boardSize) {
            if (stato[i] == g1) return true;
        }
        // Controllo la diagonale inferiore destra
        corretto = false;
        for (i = boardSize * pareggio + column + (boardSize + 1); i < boardSize * (boardSize - 1) && (i % boardSize) > ((i - (boardSize + 1)) % boardSize); i += (boardSize + 1)) {
            if (stato[i] == g2) corretto = true;
            else break;
        }
        if (corretto && (i % boardSize) > (i - (boardSize + 1)) % boardSize) {
            return stato[i] == g1;
        }
        return false;
    }


    private boolean seMosso(byte player, byte row, byte column) {
        boolean left = false, right = false, top = false, bottom = false;
        byte player2 = 1;
        if (player == 1) player2 = 2;

        if (column - 1 >= 0) left = stato[row * boardSize + column - 1] == player2;
        if (column + 1 < boardSize) right = stato[row * boardSize + column + 1] == player2;
        if (row + 1 < boardSize) top = stato[(row + 1) * boardSize + column] == player2;
        if (row - 1 >= 0) bottom = stato[(row - 1) * boardSize + column] == player2;

        if (left || right || top || bottom) return true;

        // controllo diagonale sinistra
        for (int i = boardSize * row + column - (boardSize + 1); i > boardSize && (i % boardSize) < ((i + (boardSize + 1)) % boardSize); ) {
            if (stato[i] == player2) return true;
            else break;
        }

        // controllo diagonale destra
        for (int i = boardSize * row + column - (boardSize - 1); i > boardSize && (i % boardSize) > ((i + (boardSize - 1)) % boardSize); ) {
            if (stato[i] == player2) return true;
            else break;
        }

        // controllo diagonale inferiore sinistra
        for (int i = boardSize * row + column + (boardSize - 1); i < boardSize * (boardSize - 1) && (i % boardSize) < ((i - (boardSize - 1)) % boardSize); ) {
            if (stato[i] == player2) return true;
            else break;
        }

        // controllo diagonale inferiore destra
        for (int i = boardSize * row + column + (boardSize + 1); i < boardSize * (boardSize - 1) && (i % boardSize) > ((i - (boardSize + 1)) % boardSize); ) {
            if (stato[i] == player2) return true;
            else break;
        }

        return false;
    }


    private void inviaStato() {
        byte[] message = new byte[stato.length + fine.length + 1];
        message[0] = 1;
        System.arraycopy(stato, 0, message, 1, stato.length);
        for (int i = 0; i < fine.length; i++)
            message[stato.length + 1 + i] = fine[i];
        try {
            out.write(message);
            out2.write(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void makeMove(byte player, byte row, byte column) {
        byte player2 = 1;
        if (player == 1) player2 = 2;
        boolean isCorrect = false;
        int i;
        // controllo lato sinistro
        for (i = column - 1; i > 0; i--) {
            if (stato[boardSize * row + i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && stato[boardSize * row + i] == player) {
            for (i++; i <= column; i++) {
                stato[boardSize * row + i] = player;
            }
        }
        // controllo lato destro
        isCorrect = false;
        for (i = column + 1; i < boardSize - 1; i++) {
            if (stato[boardSize * row + i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && stato[boardSize * row + i] == player) {
            for (i--; i >= column; i--) {
                stato[boardSize * row + i] = player;
            }
        }
        // controllo dell'alto
        isCorrect = false;
        for (i = row - 1; i > 0; i--) {
            if (stato[boardSize * i + column] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && stato[boardSize * i + column] == player) {
            for (i++; i <= row; i++) {
                stato[boardSize * i + column] = player;
            }
        }
        // controllo del basso
        isCorrect = false;
        for (i = row + 1; i < boardSize - 1; i++) {
            if (stato[boardSize * i + column] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && stato[boardSize * i + column] == player) {
            for (i--; i >= row; i--) {
                stato[boardSize * i + column] = player;
            }
        }
        // controllo diagonale alta sinistra
        isCorrect = false;
        for (i = boardSize * row + column - (boardSize + 1); i > boardSize && (i % boardSize) < ((i + (boardSize + 1)) % boardSize); i -= (boardSize + 1)) {
            if (stato[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % boardSize) < (i + (boardSize + 1)) % boardSize) {
            if (stato[i] == player) {
                for (i += boardSize + 1; i <= boardSize * row + column; i += boardSize + 1) {
                    stato[i] = player;
                }
            }
        }
        // controllo diagonale alta destra
        isCorrect = false;
        for (i = boardSize * row + column - (boardSize - 1); i > boardSize && (i % boardSize) > ((i + (boardSize - 1)) % boardSize); i -= (boardSize - 1)) {
            if (stato[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % boardSize) > (i + (boardSize - 1)) % boardSize) {
            if (stato[i] == player) {
                for (i += boardSize - 1; i <= boardSize * row + column; i += boardSize - 1) {
                    stato[i] = player;
                }
            }
        }
        // controllo diagonale bassa sinistra
        isCorrect = false;
        for (i = boardSize * row + column + (boardSize - 1); i < boardSize * (boardSize - 1) && (i % boardSize) < ((i - (boardSize - 1)) % boardSize); i += (boardSize - 1)) {
            if (stato[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % boardSize) < (i - (boardSize - 1)) % boardSize) {
            if (stato[i] == player) {
                for (i -= boardSize - 1; i >= boardSize * row + column; i -= boardSize - 1) {
                    stato[i] = player;
                }
            }
        }
        // controllo diagonale bassa destra
        isCorrect = false;
        for (i = boardSize * row + column + (boardSize + 1); i < boardSize * (boardSize - 1) && (i % boardSize) > ((i - (boardSize + 1)) % boardSize); i += (boardSize + 1)) {
            if (stato[i] == player2) isCorrect = true;
            else break;
        }
        if (isCorrect && (i % boardSize) > (i - (boardSize + 1)) % boardSize) {
            if (stato[i] == player) {
                for (i -= boardSize + 1; i >= boardSize * row + column; i -= boardSize + 1) {
                    stato[i] = player;
                }
            }
        }
    }

    public void sendM(Socket s, byte[] message) throws IOException {
        byte[] endOfMessage = "\r\n".getBytes();
        byte[] m;
        if (s == socket) {
            m = new byte[name.length() + 2 + message.length + endOfMessage.length];
            for (int i = 0; i < name.length(); i++)
                m[i] = (byte) name.charAt(i);
            m[name.length()] = ':';
            m[name.length() + 1] = ' ';
            for (int i = 0; i < message.length; i++)
                m[name.length() + 2 + i] = message[i];
            for (int i = 0; i < endOfMessage.length; i++)
                m[name.length() + 2 + message.length + i] = endOfMessage[i];
        } else {
            m = new byte[name2.length() + 2 + message.length + endOfMessage.length];
            for (int i = 0; i < name2.length(); i++)
                m[i] = (byte) name2.charAt(i);
            m[name2.length()] = ':';
            m[name2.length() + 1] = ' ';
            for (int i = 0; i < message.length; i++)
                m[name2.length() + 2 + i] = message[i];
            for (int i = 0; i < endOfMessage.length; i++)
                m[name2.length() + 2 + message.length + i] = endOfMessage[i];
        }

        out.write(m);
        out2.write(m);
        System.out.println("Messaggio inviato correttamente");
    }


    public void close() {
        try {
            out.write(new byte[]{7, '\r', '\n'});
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out2.write(new byte[]{7, '\r', '\n'});
        } catch (IOException e) {
            e.printStackTrace();
        }
        serverLobbyListener1.endMatch();
        serverLobbyListener2.endMatch();
        ended = true;
    }


    public void closeDisc(Socket s) {
        if (socket == s) {
            try {
                out2.write(new byte[]{7, 0, '\r', '\n'});
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                out.write(new byte[]{7, 0, '\r', '\n'});
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        serverLobbyListener1.endMatch();
        serverLobbyListener2.endMatch();
        ended = true;
    }
}
