import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

// thread che gestisce la comunicazione che permette connessione client-server
public class ClientThread extends Thread {

	private final Socket socket;
	private BufferedReader in;
	private OutputStream out;
	boolean prossimo = true;

	// socket per la connessione aperta del server
	public ClientThread(Socket socket) {
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		byte[] response;
		byte[] stan;
		try {
			while (prossimo) {
				String line = in.readLine();
				response = line.getBytes();
				System.out.println("Messaggio ricevuto");
				if (response[0] == 0) {
					byte size = response[1];
					System.out.println("Utenti connessi: " + size);
					Object[][] s = new Object[size][3];
					for (byte i = 0; i < size; i++) {
						s[i][0] = i + 1;
						s[i][1] = in.readLine();
						s[i][2] = in.readLine().getBytes()[0];
					}
					Main.pl.updateTable(s);
				} else if (response[0] == 1) {
					stan = new byte[64];
					System.arraycopy(response, 1, stan, 0, stan.length);
					Main.window.getPanel().updatePlansza(stan);
				} else if (response[0] == 2) {
					System.out.println("Messaggio ricevuto - è il mio turno");
					Main.window.getPanel().przydzielTure();
				} else if (response[0] == 3) {
					Main.window.getPanel().zabierzTure();
				} else if (response[0] == 4) {
					Main.username = line.substring(1);
				} else if (response[0] == 5) {
					System.out.println("Richiesta di creazione di una finestra di gioco - Nero");
					Main.window = new GameWindow(true);
					System.out.println("È stata creata una finestra");

				} else if (response[0] == 6) {
					System.out.println("Richiesta di creazione di una finestra di gioco - Bianco");
					Main.window = new GameWindow(false);
					System.out.println("È stata creata una finestra");
				} else if (response[0] == 7) {
					byte[] risultati = Main.window.getPanel().getScores();
					if (response.length == 2) {
						JOptionPane.showMessageDialog(Main.window, "Fine della partita - Avversario eliminato");
					} else if (risultati[0] == risultati[1])
						JOptionPane.showMessageDialog(Main.window, "Fine della partita - pareggio");
					else {
						if (Main.window.getPanel().giocatore == 2) {
							if (risultati[0] > risultati[1])
								JOptionPane.showMessageDialog(Main.window, "Fine della partita - vittoria");
							else
								JOptionPane.showMessageDialog(Main.window, "Fine della partita - perdita");
						} else {
							if (risultati[0] > risultati[1])
								JOptionPane.showMessageDialog(Main.window, "Fine della partita - perdita");
							else
								JOptionPane.showMessageDialog(Main.window, "Fine della partita - vittoria");
						}
					}

					Main.window.close();
				} else {
					System.out.println("Messaggio pronto per la visualizzazione: ");
					char[] a = new char[response.length];
					for (int i = 0; i < response.length; i++)
						a[i] = (char) response[i];
					System.out.println(new String(a));
					Main.window.chat.displayMessage(new String(a));
				}

			}
		} catch (IOException e) {
			prossimo = false;
			System.out.println("Il server si è disconnesso");
		} finally {
			try {
				in.close();
				out.close();
				socket.close();
				System.out.println("La presa è stata chiusa");
				if (Main.windowLobby != null)
					Main.windowLobby.dispatchEvent(new WindowEvent(Main.windowLobby, WindowEvent.WINDOW_CLOSING));
				if (Main.window != null)
					Main.window.dispatchEvent(new WindowEvent(Main.window, WindowEvent.WINDOW_CLOSING));
			} catch (IOException e) {
				System.out.println("Server già chiuso in precedenza");
			}
		}
	}

	// metodo per inviare messaggio al server aggiungendo i caratteri di fine riga
	public void send(byte[] message) {
		byte[] fine = "\r\n".getBytes();
		byte[] m = new byte[message.length + fine.length];
		System.arraycopy(message, 0, m, 0, message.length);
		System.arraycopy(fine, 0, m, message.length, fine.length);
		try {
			out.write(m);
		} catch (IOException e) {
			System.out.println("IOException");
			prossimo = false;
			if (Main.windowLobby != null)
				Main.windowLobby.dispatchEvent(new WindowEvent(Main.windowLobby, WindowEvent.WINDOW_CLOSING));
			if (Main.window != null)
				Main.window.dispatchEvent(new WindowEvent(Main.window, WindowEvent.WINDOW_CLOSING));
		}
		System.out.println("Messaggio inviato");
	}

}
