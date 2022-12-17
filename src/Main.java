import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Classe che contiene il metodo main. Esecuzione di un programma lato client.
 * 
 * @autore Bartus
 *
 */
public class Main {

	// static private boolean host = !true;
	/**
	 * Obiekt okna, które używane jest do rozgrywki
	 * 
	 */
	static public OknoGra okno;
	/**
	 * Obiekt okna używanego do wyświetlania lobby
	 * 
	 */
	static public JFrame oknoLobby;
	/**
	 *
	 */
	static public ClientThread clientThread; // thread cliente
	/**
	 * Obiekt wątku menedżera połączeń
	 *
	 */
	static public SerwerLobbyConManager lobby;
	/**
	 * Lista działających wątków komunikacyjnych po stronie serwera
	 *
	 */
	// static public List<SerwerLobbyListener> listeners = new ArrayList<>(10);
	/**
	 * Panel należący do okna lobby
	 */
	static public PanelLobby pl;
	/**
	 * Nick gracza
	 * 
	 */
	static public String name = "Gracz";
	/**
	 * IP/domena serwera
	 * 
	 */
	static public String ip;

	/**
	 * Metoda main programu
	 * 
	 * @param args Brak użycia w programie
	 */
	public static void main(String[] args) {

		try {
			JTextField adres = new JTextField("localhost");
			JTextField nick = new JTextField("Gracz");
			Object[] message = {
					"Adres:", adres,
					"Nick:", nick
			};
			int option = JOptionPane.showConfirmDialog(null, message, "Połącz z serwerem",
					JOptionPane.OK_CANCEL_OPTION);
			if (option == JOptionPane.CANCEL_OPTION)
				System.exit(0);
			else {
				ip = adres.getText();
				name = nick.getText();
			}
			Socket polaczenie = new Socket(ip, 2020);
			clientThread = new ClientThread(polaczenie);
			clientThread.start();
			clientThread.send(name.getBytes());
			JFrame oknoLobby = new JFrame();
			oknoLobby.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			oknoLobby.setTitle("Lobby");
			oknoLobby.setSize(400, 200);
			pl = new PanelLobby();
			oknoLobby.setContentPane(pl);
			oknoLobby.setVisible(true);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Nie udało się połączyć z serwerem");
		}
	}

}
