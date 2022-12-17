import java.net.ServerSocket;
import java.net.Socket;

public class ServerLobbyConManager extends Thread {

    private final ServerSocket socket;

    // thread gestione connessione
    public ServerLobbyConManager(ServerSocket server) {
        this.socket = server;
    }

    public void run() {
        while (true) {
            Socket k;
            try {
                k = socket.accept();
                ServerLobbyListener a = new ServerLobbyListener(k);
                a.start();
                System.out.println("Client connesso. Il server funziona...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
