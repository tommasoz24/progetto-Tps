package model;

import controller.ClientHandler;

import java.net.ServerSocket;
import java.net.Socket;

public class ServerGestioneConnessione extends Thread {

    private final ServerSocket socket;

    // thread gestione connessione
    public ServerGestioneConnessione(ServerSocket server) {
        this.socket = server;
    }

    public void run() {
        while (true) {
            Socket k;
            try {
                k = socket.accept();
                ClientHandler a = new ClientHandler(k);
                a.start();
                System.out.println("Client connesso. Il server funziona...");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
