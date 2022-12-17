import java.net.ServerSocket;
import java.net.Socket;

/** Menedżer połączeń serwera z klientami tworzący nowy wątek SerwerLobbyListener'a dla każdego nowo połączonego klienta
 * @author Bartosz Ruta
 *
 */
public class SerwerLobbyConManager extends Thread {
	
	private ServerSocket socket;
	
	/** Tworzy wątek menedżera połączeń
	 * @param serwer Gniazdo otwartego serwera
	 */
	public SerwerLobbyConManager(ServerSocket serwer) {
		this.socket = serwer;
	}
	
	public void run() {
		while(true) {
				Socket k = null;
			try {
				k = socket.accept();
				SerwerLobbyListener a = new SerwerLobbyListener(k);
				//System.out.println("Dodano klienta do listy");
				a.start();
				System.out.println("Tutaj działa");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
