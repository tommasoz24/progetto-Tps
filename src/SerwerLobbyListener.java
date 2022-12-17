import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/** Wątek obsługujący komunikację z klientem po stronie serwera
 * @author Bartosz Ruta
 *
 */
public class SerwerLobbyListener extends Thread {

	public Socket klient;
	private BufferedReader in;
	private OutputStream out;
	public boolean dalej = true;
	byte busy = 0;
	private Silnik s;
	public String nick;
	
	/** Tworzy wątek nasłuchujący wiadomości od klienta
	 * @param klient Gniazdo nawiązanego połączenia z klientem
	 */
	public SerwerLobbyListener(Socket klient/*, List<socketzNickiem> users*/) {
		this.klient = klient;
		try {
			in = new BufferedReader(new InputStreamReader(klient.getInputStream()));
			out = klient.getOutputStream();
			nick = in.readLine();
			String nickT = nick;
			int a = 0;
			for (int i=0; i<MainSerwer.listeners.size(); i++) {
				if (MainSerwer.listeners.get(i).nick.equals(nickT)) {
					System.out.println(nickT);
					a++;
					nickT = nick + "(" + a + ")";
					i=0;
				}
			}
			nick = nickT;
			byte[] nb = nick.getBytes();
			byte[] temp = new byte[nick.length()+1];
			for (int i=0; i<nb.length; i++)
				temp[i+1] = nb[i];
			temp[0] = 4;
			send(temp);
			/*
			for (int j=0; j<Main.listeners.size(); j++) {
				Main.listeners.get(j).send(new byte[] {0,(byte) Main.listeners.size()});
				
				for (byte i = 0; i<Main.listeners.size(); i++) {
					Main.listeners.get(j).send(Main.listeners.get(i).nick.getBytes());
					Main.listeners.get(j).send(new byte[] {Main.listeners.get(i).busy});
				}
			}*/
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		MainSerwer.listeners.add(this);
		//Main.listeners.add(this);
	}
	
	public void run() {
		try {
			for (int j=0; j<MainSerwer.listeners.size(); j++) {
				if (MainSerwer.listeners.get(j) == this)
					continue;
				MainSerwer.listeners.get(j).send(new byte[] {0,(byte) MainSerwer.listeners.size()});
				
				for (byte i = 0; i<MainSerwer.listeners.size(); i++) {
					MainSerwer.listeners.get(j).send(MainSerwer.listeners.get(i).nick.getBytes());
					MainSerwer.listeners.get(j).send(new byte[] {MainSerwer.listeners.get(i).busy});
				}
			}
			while (dalej) {
				String message;
				System.out.println("Listener czeka na readLine");
				message = in.readLine();
				if (message.getBytes()[0] == 0) { // 0 - updateLista dla klienta
					System.out.println("Żądanie update'u listy");
					send(new byte[] {0,(byte) MainSerwer.listeners.size()});
					
					for (byte i = 0; i<MainSerwer.listeners.size(); i++) {
						send(MainSerwer.listeners.get(i).nick.getBytes());
						send(new byte[] {MainSerwer.listeners.get(i).busy});
					}
					System.out.println("Wysłano całość");
				}
				else if (message.getBytes()[0] == 1) { // 1 - rozpocznij rozgrywkę
					System.out.println("Odebrano prosbe o gre");
					SerwerLobbyListener s2 = null;
					for (int i=0; i<MainSerwer.listeners.size(); i++) {
						if (MainSerwer.listeners.get(i).nick.equals(message.substring(1)))
							s2 = MainSerwer.listeners.get(i);
					}
					if (busy == 1 || s2 == null || s2.busy == 1)
						continue;
					busy = 1;
					s2.busy = 1;
					System.out.println("Przed stworzeniem silnika");
					
					send(new byte[] {5});
					s2.send(new byte[] {6});
					s = new Silnik(this,s2,Color.BLACK);
					s2.s = this.s;
				}
				else if (message.getBytes()[0] == 2){
					//wait = true;
				}
				else if (message.getBytes()[0] == 4) {
					if ((s.socket == klient && s.tura == 1) || (s.socket2 == klient && s.tura == 2)) {
						System.out.println("Odebrano ruch od gracza");
						//Main.okno.plansza.zabierzTure();
						//Main.s.response = message.getBytes();
						//Main.s.czekaj = false;
						this.s.run(message.getBytes());
					}
					//s.notify();
				}
				else {
					System.out.println("Odebrano wiadomość na czat");
					//Main.s.sendM(klient,message.getBytes());
					this.s.sendM(klient,message.getBytes());
					
				}
			} 
		}
		catch (IOException e) {
			System.out.println("IOException");
			dalej = false;
		}
		finally {
			System.out.println("Klient " + nick + " się rozłączył.");
			MainSerwer.listeners.remove(this);
			try {
				dalej = false;
				if (s != null || busy == 1)
					s.closeDisc(klient);
				out.close();
				in.close();
				klient.close();
			} catch (IOException e) {
				
			}
		}
	}
	
	/** Kończy mecz jeśli taki obecnie trwa oraz ustawia flagę busy na 0
	 * 
	 */
	public void zakonczMecz() {
		s = null;
		busy = 0;
	}
	
	/** Wysyła wiadomość do klienta dopisując znaki końca linii
	 * @param message Wiadomość od serwera do klienta
	 * @throws IOException
	 */
	public void send(byte[] message) throws IOException {
		byte[] koniec = "\r\n".getBytes();
		byte[] m = new byte[message.length+koniec.length];
		for (int i=0; i<message.length; i++)
			m[i] = message[i];
		for (int i=0; i<koniec.length; i++)
			m[message.length+i] = koniec[i];
		out.write(m);
	}
}
