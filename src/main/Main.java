package main;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import model.ClientThread;
import view.FinestraDiGioco;
import view.SchermataLobby;


// Client main.Main
public class Main {

    static public FinestraDiGioco window;

    static public JFrame windowLobby;

    static public ClientThread clientThread; // thread cliente

    static public SchermataLobby pl;

    static public String username = "Giocatore";

    static public String ip;


    public static void main(String[] args) {

        try {
            JTextField localhost = new JTextField("localhost");
            JTextField username = new JTextField("Giocatore");
            Object[] message = {"address:", localhost, "username:", username};
            int option = JOptionPane.showConfirmDialog(null, message, "Connessione al server", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.CANCEL_OPTION) System.exit(0);
            else {
                ip = localhost.getText();
                Main.username = username.getText();
            }
            Socket collegamento = new Socket(ip, 4455);
            clientThread = new ClientThread(collegamento);
            clientThread.start();
            clientThread.send(Main.username.getBytes());
            JFrame lobbyFrame = new JFrame();
            lobbyFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            lobbyFrame.setTitle("Lobby");
            lobbyFrame.setSize(400, 200);
            pl = new SchermataLobby();
            lobbyFrame.setContentPane(pl);
            lobbyFrame.setVisible(true);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Impossibile effettuare la connessione");
        }
    }

}
