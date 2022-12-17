import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class MainServer {

    // questa è la classe che avvia il server socket
    static public List<ServerLobbyListener> listeners = new ArrayList<>(10);

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(2020);
            Thread lobby = new ServerLobbyConManager(server);
            lobby.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
