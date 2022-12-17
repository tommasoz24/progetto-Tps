import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import javax.swing.JOptionPane;

/** Wątek obsługujący komunikację z serwerem po stronie klienta
 * @author Bartosz Ruta
 *
 */
public class ClientThread extends Thread {
	
	private Socket socket;
	private BufferedReader in;
	private OutputStream out;
	boolean dalej = true;
	
	/** Tworzy wątek komunikacji z serwerem
	 * @param socket Gniazdo otwartego połączenia z serwerem
	 */
	public ClientThread(Socket socket) {
		this.socket = socket;
		try {
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run() {
		byte[] response = new byte[64];
		byte[] stan = new byte[64];
		try {
		while (dalej) {
			
				String line = in.readLine();
				response = line.getBytes();
				System.out.println("Odebrano wiadomość");
				if (response[0]==0) {
					byte size = response[1];
					System.out.println("Odbieranie zaw. tabeli users");
					Object[][] s = new Object[size][3];
					for (byte i=0; i<size; i++) {
						s[i][0] = i+1;
						s[i][1] = in.readLine();
						s[i][2] = in.readLine().getBytes()[0];
					}
					Main.pl.updateTable(s);
				}
				else if (response[0]==1) {
					stan = new byte[64];
					for (int i=0; i<stan.length; i++)
						stan[i] = response[i+1];
					Main.okno.getPlansza().updatePlansza(stan);
				}
				else if (response[0]==2) {
					System.out.println("Odebrano wiadomość - moja tura");
					Main.okno.getPlansza().przydzielTure();
				}
				else if (response[0]==3) {
					Main.okno.getPlansza().zabierzTure();
				}
				else if (response[0]==4) {
					Main.name = line.substring(1);
				}
				else if (response[0]==5) {
					System.out.println("Prośba o stworzenie okna gry - Czarne");
					//send(new byte[] {2});
					//EventQueue.invokeLater(new Runnable() {
					//	public void run() {
							Main.okno = new OknoGra(true);
							/*Main.okno.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
							Main.okno.addWindowListener(new WindowAdapter() {
					            @Override
					            public void windowClosing(WindowEvent e) {
					                System.out.println("WindowClosingDemo.windowClosing");
					                //System.exit(0);
					            }
					        });*/
					//	}
					//});
					System.out.println("Stworzono okno");
					//sleep(1000);
					
				}
				else if (response[0]==6) {
					System.out.println("Prośba o stworzenie okna gry - Białe");
					//send(new byte[] {2});
					//EventQueue.invokeLater(new Runnable() {
					//	public void run() {
							Main.okno = new OknoGra(false);
					//	}
					//});
					System.out.println("Stworzono okno");
					//sleep(1000);
				}
				else if (response[0] == 7) {
					byte[] wyniki = Main.okno.getPlansza().getScores();
					if (response.length == 2) {
						JOptionPane.showMessageDialog(Main.okno,"Koniec gry - Przeciwnik wyszedł");
					}
					else if (wyniki[0] == wyniki[1])
						JOptionPane.showMessageDialog(Main.okno,"Koniec gry - remis");
					else {
						if (Main.okno.getPlansza().gracz == 2) {
							if (wyniki[0] > wyniki[1])
								JOptionPane.showMessageDialog(Main.okno,"Koniec gry - wygrana");
							else 
								JOptionPane.showMessageDialog(Main.okno,"Koniec gry - przegrana");
						}
						else {
							if (wyniki[0] > wyniki[1])
								JOptionPane.showMessageDialog(Main.okno,"Koniec gry - przegrana");
							else 
								JOptionPane.showMessageDialog(Main.okno,"Koniec gry - wygrana");
						}
					}
					
					Main.okno.close();
				}
				else {
					System.out.println("Wiadomość gotowa do wyświetlenia: ");
					char[] a = new char[response.length];
					for (int i=0; i<response.length; i++)
						a[i] = (char) response[i];
					System.out.println(new String(a));
					Main.okno.czat.wyswietlWiadomosc(new String(a));
				}
				
			
		}
		} catch (IOException e) {
			//e.printStackTrace();
			dalej = false;
			System.out.println("Serwer się rozłączył");
		} finally {
			try {
				in.close();
				out.close();
				socket.close();
				System.out.println("Zamknięto socket");
				if (Main.oknoLobby != null)
					Main.oknoLobby.dispatchEvent(new WindowEvent(Main.oknoLobby, WindowEvent.WINDOW_CLOSING));
				if (Main.okno != null)
					Main.okno.dispatchEvent(new WindowEvent(Main.okno, WindowEvent.WINDOW_CLOSING));
			} catch (IOException e) {
				System.out.println("Już wcześniej zamknięty");
				//e.printStackTrace();
			}
		}
	}
	
	/** Wysyła wiadomość do serwera dopisując znaki końca linii
	 * @param message Wiadomość od klienta do serwera
	 */
	public void send(byte[] message) {
		byte[] koniec = "\r\n".getBytes();
		byte[] m = new byte[message.length+koniec.length];
		for (int i=0; i<message.length; i++)
			m[i] = message[i];
		for (int i=0; i<koniec.length; i++)
			m[message.length+i] = koniec[i];
		try {
			out.write(m);
		} catch (IOException e) {
			//e.printStackTrace();
			System.out.println("IOException");
			dalej = false;
			if (Main.oknoLobby != null)
				Main.oknoLobby.dispatchEvent(new WindowEvent(Main.oknoLobby, WindowEvent.WINDOW_CLOSING));
			if (Main.okno != null)
				Main.okno.dispatchEvent(new WindowEvent(Main.okno, WindowEvent.WINDOW_CLOSING));
		}
		//if (message.length>2)
			System.out.println("Wysłano wiadomość"); //+ (char) message[0] + (char) message[1] + (char) message[2]);
	}

}
