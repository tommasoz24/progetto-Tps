import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;



// Client Main
public class Main {

	static public OknoGra okno;

	static public JFrame oknoLobby;

	static public ClientThread clientThread; // thread cliente

	static public ServerLobbyConManager lobby;

	static public PanelLobby pl;

	static public String name = "Gracz";

	static public String ip;


	public static void main(String[] args) {

		try {
			JTextField localhost = new JTextField("localhost");
			JTextField username = new JTextField("Gracz");
			Object[] message = {
					"address:", localhost,
					"username:", username
			};
			int option = JOptionPane.showConfirmDialog(null, message, "Connessione al server",
					JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.CANCEL_OPTION)
				System.exit(0);
			else {
				ip = localhost.getText();
				name = username.getText();
			}
			Socket collegamento = new Socket(ip, 2020);
			clientThread = new ClientThread(collegamento);
			clientThread.start();
			clientThread.send(name.getBytes());
			JFrame lobbyFrame = new JFrame();
			lobbyFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			lobbyFrame.setTitle("Lobby");
			lobbyFrame.setSize(400, 200);
			pl = new PanelLobby();
			lobbyFrame.setContentPane(pl);
			lobbyFrame.setVisible(true);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Impossibile effettuare la connessione");
		}
	}

}
