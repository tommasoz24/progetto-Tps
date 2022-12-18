package controller;

import main.MainServer;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

// Il thread che gestisce la comunicazione con il client sul lato server


public class ClientHandler extends Thread {

    public Socket client;
    public boolean prossimo = true;
    public String username;
    byte busy = 0;
    private BufferedReader in;
    private OutputStream out;
    private MotoreDiGioco e;

    // client thread
    public ClientHandler(Socket client) {

        this.client = client;

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = client.getOutputStream();
            username = in.readLine();
            String username = this.username;
            int a = 0;
            for (int i = 0; i < MainServer.listeners.size(); i++) {
                if (MainServer.listeners.get(i).username.equals(username)) {
                    System.out.println(username);
                    a++;
                    username = this.username + "(" + a + ")";
                    i = 0;
                }
            }
            this.username = username;
            byte[] nb = this.username.getBytes();
            byte[] temp = new byte[this.username.length() + 1];
            System.arraycopy(nb, 0, temp, 1, nb.length);
            temp[0] = 4;
            send(temp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MainServer.listeners.add(this);
    }

    public void run() {
        try {
            for (int j = 0; j < MainServer.listeners.size(); j++) {
                if (MainServer.listeners.get(j) == this) continue;
                MainServer.listeners.get(j).send(new byte[]{0, (byte) MainServer.listeners.size()});

                for (byte i = 0; i < MainServer.listeners.size(); i++) {
                    MainServer.listeners.get(j).send(MainServer.listeners.get(i).username.getBytes());
                    MainServer.listeners.get(j).send(new byte[]{MainServer.listeners.get(i).busy});
                }
            }
            while (prossimo) {
                String message;
                System.out.println("in attesa readLine");
                message = in.readLine();
                if (message.getBytes()[0] == 0) { // 0 - lista di aggiornamento per il client
                    System.out.println("Richiesta di aggiornamento dell'elenco");
                    send(new byte[]{0, (byte) MainServer.listeners.size()});

                    for (byte i = 0; i < MainServer.listeners.size(); i++) {
                        send(MainServer.listeners.get(i).username.getBytes());
                        send(new byte[]{MainServer.listeners.get(i).busy});
                    }
                    System.out.println("Invio completato");
                } else if (message.getBytes()[0] == 1) { // 1 - avviare il gioco
                    System.out.println("Odebrano prosbe o gre");
                    ClientHandler s2 = null;
                    for (int i = 0; i < MainServer.listeners.size(); i++) {
                        if (MainServer.listeners.get(i).username.equals(message.substring(1)))
                            s2 = MainServer.listeners.get(i);
                    }
                    if (busy == 1 || s2 == null || s2.busy == 1) continue;
                    busy = 1;
                    s2.busy = 1;
                    System.out.println("prima che l'engine venga creato");

                    send(new byte[]{5});
                    s2.send(new byte[]{6});
                    e = new MotoreDiGioco(this, s2, Color.BLACK);
                    s2.e = this.e;
                } else if (message.getBytes()[0] == 4) {
                    if ((e.socket == client && e.gira == 1) || (e.socket2 == client && e.gira == 2)) {
                        System.out.println("mossa ricevuta dal giocatore " + e.gira);
                        this.e.run(message.getBytes());
                    }
                } else {
                    System.out.println("ricevuto messaggio: " + message);
                    this.e.sendM(client, message.getBytes());

                }
            }
        } catch (IOException e) {
            System.out.println("IOException");
            prossimo = false;
        } finally {
            System.out.println("Client " + username + " scollegato.");
            MainServer.listeners.remove(this);
            try {
                prossimo = false;
                if (e != null || busy == 1) {
                    assert e != null;
                    e.closeDisc(client);
                }
                out.close();
                in.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

     //Termina la partita se ne è in corso una e imposta a 0 il flag di occupato
    public void endMatch() {
        e = null;
        busy = 0;
    }

    // invia messaggio al client con i caratteri di fine linea
    public void send(byte[] message) throws IOException {
        byte[] fine = "\r\n".getBytes();
        byte[] m = new byte[message.length + fine.length];
        System.arraycopy(message, 0, m, 0, message.length);
        System.arraycopy(fine, 0, m, message.length, fine.length);
        out.write(m);
    }
}
