import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;


/** Klasa zawierająca metodę main. Uruchamianie programu po stronie serwera.
 * @author Bartus
 *
 */
public class MainSerwer {

	/** Lista działających wątków komunikacyjnych po stronie serwera
	 * 
	 */
	static public List<SerwerLobbyListener> listeners = new ArrayList<>(10);
	
	/** Metoda main po stronie serwera
	 * @param args Brak użycia w programie
	 */
	public static void main(String[] args) {
		try {
			ServerSocket serwer = new ServerSocket(2020);
			Thread lobby = new SerwerLobbyConManager(serwer);
			//Thread s = new Silnik(serwer,Color.BLACK);
			lobby.start();
			//s.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
