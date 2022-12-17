import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

// Il thread che gestisce la comunicazione con il client sul lato server


public class ServerLobbyListener extends Thread {

    public Socket client;
    private BufferedReader in;
    private OutputStream out;
    public boolean prossimo = true;
    byte busy = 0;
    private Engine e;
    public String nick;

    // client thread
    public ServerLobbyListener(Socket client) {

        this.client = client;

        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            out = client.getOutputStream();
            nick = in.readLine();
            String nickT = nick;
            int a = 0;
            for (int i = 0; i < MainServer.listeners.size(); i++) {
                if (MainServer.listeners.get(i).nick.equals(nickT)) {
                    System.out.println(nickT);
                    a++;
                    nickT = nick + "(" + a + ")";
                    i = 0;
                }
            }
            nick = nickT;
            byte[] nb = nick.getBytes();
            byte[] temp = new byte[nick.length() + 1];
            System.arraycopy(nb, 0, temp, 1, nb.length);
            temp[0] = 4;
            send(temp);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        MainServer.listeners.add(this);
        //Main.listeners.add(this);
    }

    public void run() {
        try {
            for (int j = 0; j < MainServer.listeners.size(); j++) {
                if (MainServer.listeners.get(j) == this) continue;
                MainServer.listeners.get(j).send(new byte[]{0, (byte) MainServer.listeners.size()});

                for (byte i = 0; i < MainServer.listeners.size(); i++) {
                    MainServer.listeners.get(j).send(MainServer.listeners.get(i).nick.getBytes());
                    MainServer.listeners.get(j).send(new byte[]{MainServer.listeners.get(i).busy});
                }
            }
            while (prossimo) {
                String message;
                System.out.println("Listener czeka na readLine");
                message = in.readLine();
                if (message.getBytes()[0] == 0) { // 0 - updateLista dla klienta
                    System.out.println("Żądanie update'u listy");
                    send(new byte[]{0, (byte) MainServer.listeners.size()});

                    for (byte i = 0; i < MainServer.listeners.size(); i++) {
                        send(MainServer.listeners.get(i).nick.getBytes());
                        send(new byte[]{MainServer.listeners.get(i).busy});
                    }
                    System.out.println("Wysłano całość");
                } else if (message.getBytes()[0] == 1) { // 1 - rozpocznij rozgrywkę
                    System.out.println("Odebrano prosbe o gre");
                    ServerLobbyListener s2 = null;
                    for (int i = 0; i < MainServer.listeners.size(); i++) {
                        if (MainServer.listeners.get(i).nick.equals(message.substring(1)))
                            s2 = MainServer.listeners.get(i);
                    }
                    if (busy == 1 || s2 == null || s2.busy == 1) continue;
                    busy = 1;
                    s2.busy = 1;
                    System.out.println("Przed stworzeniem silnika");

                    send(new byte[]{5});
                    s2.send(new byte[]{6});
                    e = new Engine(this, s2, Color.BLACK);
                    s2.e = this.e;
                } else if (message.getBytes()[0] == 2) {
                    //wait = true;
                } else if (message.getBytes()[0] == 4) {
                    if ((e.socket == client && e.gira == 1) || (e.socket2 == client && e.gira == 2)) {
                        System.out.println("Odebrano ruch od gracza");
                        this.e.run(message.getBytes());
                    }
                    //e.notify();
                } else {
                    System.out.println("Odebrano wiadomość na chat");
                    //Main.e.sendM(client,message.getBytes());
                    this.e.sendM(client, message.getBytes());

                }
            }
        } catch (IOException e) {
            System.out.println("IOException");
            prossimo = false;
        } finally {
            System.out.println("Klient " + nick + " się rozłączył.");
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

    /**
     * Termina la partita se ne è in corso una e imposta a 0 il flag di occupato
     */
    public void zakonczMecz() {
        e = null;
        busy = 0;
    }

    // invia messaggio al client con i caratteri di fine linea
    public void send(byte[] message) throws IOException {
        byte[] koniec = "\r\n".getBytes();
        byte[] m = new byte[message.length + koniec.length];
        System.arraycopy(message, 0, m, 0, message.length);
        System.arraycopy(koniec, 0, m, message.length, koniec.length);
        out.write(m);
    }
}
