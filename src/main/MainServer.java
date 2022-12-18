package main;

import controller.ClientHandler;
import model.ServerGestioneConnessione;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class MainServer {

    // questa è la classe che avvia il server socket
    static public List<ClientHandler> listeners = new ArrayList<>(10);

    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8888);
            Thread lobby = new ServerGestioneConnessione(server);
            lobby.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
