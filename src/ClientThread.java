import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

// thread che gestisce la comunicazione che permette conessione client-server
public class ClientThread extends Thread {

	private Socket socket;
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
		byte[] response = new byte[64];
		byte[] stan = new byte[64];
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
					for (int i = 0; i < stan.length; i++)
						stan[i] = response[i + 1];
					Main.okno.getPlansza().updatePlansza(stan);
				} else if (response[0] == 2) {
					System.out.println("Messaggio ricevuto - è il mio turno");
					Main.okno.getPlansza().przydzielTure();
				} else if (response[0] == 3) {
					Main.okno.getPlansza().zabierzTure();
				} else if (response[0] == 4) {
					Main.name = line.substring(1);
				} else if (response[0] == 5) {
					System.out.println("Richiesta di creazione di una finestra di gioco - Nero");
					Main.okno = new OknoGra(true);
					System.out.println("È stata creata una finestra");

				} else if (response[0] == 6) {
					System.out.println("Richiesta di creazione di una finestra di gioco - Bianco");
					Main.okno = new OknoGra(false);
					System.out.println("È stata creata una finestra");
				} else if (response[0] == 7) {
					byte[] wyniki = Main.okno.getPlansza().getScores();
					if (response.length == 2) {
						JOptionPane.showMessageDialog(Main.okno, "Fine della partita - Avversario eliminato");
					} else if (wyniki[0] == wyniki[1])
						JOptionPane.showMessageDialog(Main.okno, "Fine della partita - pareggio");
					else {
						if (Main.okno.getPlansza().gracz == 2) {
							if (wyniki[0] > wyniki[1])
								JOptionPane.showMessageDialog(Main.okno, "Fine della partita - vittoria");
							else
								JOptionPane.showMessageDialog(Main.okno, "Fine della partita - perdita");
						} else {
							if (wyniki[0] > wyniki[1])
								JOptionPane.showMessageDialog(Main.okno, "Fine della partita - perdita");
							else
								JOptionPane.showMessageDialog(Main.okno, "Fine della partita - vittoria");
						}
					}

					Main.okno.close();
				} else {
					System.out.println("Messaggio pronto per la visualizzazione: ");
					char[] a = new char[response.length];
					for (int i = 0; i < response.length; i++)
						a[i] = (char) response[i];
					System.out.println(new String(a));
					Main.okno.czat.wyswietlWiadomosc(new String(a));
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
				if (Main.oknoLobby != null)
					Main.oknoLobby.dispatchEvent(new WindowEvent(Main.oknoLobby, WindowEvent.WINDOW_CLOSING));
				if (Main.okno != null)
					Main.okno.dispatchEvent(new WindowEvent(Main.okno, WindowEvent.WINDOW_CLOSING));
			} catch (IOException e) {
				System.out.println("Server già chiuso in precedenza");
			}
		}
	}

	// metodo per inviare messaggio al server aggiungendo i caratteri di fine riga
	public void send(byte[] message) {
		byte[] koniec = "\r\n".getBytes();
		byte[] m = new byte[message.length + koniec.length];
		for (int i = 0; i < message.length; i++)
			m[i] = message[i];
		for (int i = 0; i < koniec.length; i++)
			m[message.length + i] = koniec[i];
		try {
			out.write(m);
		} catch (IOException e) {
			System.out.println("IOException");
			prossimo = false;
			if (Main.oknoLobby != null)
				Main.oknoLobby.dispatchEvent(new WindowEvent(Main.oknoLobby, WindowEvent.WINDOW_CLOSING));
			if (Main.okno != null)
				Main.okno.dispatchEvent(new WindowEvent(Main.okno, WindowEvent.WINDOW_CLOSING));
		}
		System.out.println("Messaggio inviato");
	}

}
