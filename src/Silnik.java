import java.awt.Color;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
/** Silnik gry wyliczający możliwe ruchy, sprawdzający poprawność ruchów graczy, przydzielający tury itp.
 * @author Bartosz Ruta
 */
public class Silnik {
	
	/** Gniazda graczy 
	*/
	public Socket socket, socket2;
	private OutputStream out, out2;
	private String name = "Gracz1", name2 = "Gracz2";
	/** Informacja o aktualnie wykonywanej turze 
	*/
	public int tura = 1;
	private final int planszaRozmiar = 8;
	private List<Byte> dozwolone1 = new ArrayList<>(20), dozwolone2 = new ArrayList<>(20);
	private byte[] stan, koniec = "\r\n".getBytes();
	//public byte[] response;
	byte gracz = 1, gracz2 = 1;
	public boolean czekaj = true, ended = false;
	SerwerLobbyListener as,bs;
	
	/** Tworzy slinik gry
	 * @param a Referencja do Listenera dla klienta będącego pierwszym graczem
	 * @param b Referencja do Listenera dla klienta będącego drugim graczem
	 * @param kolor1 Kolor pionków pierwszego gracza - Color.Black zaczyna, Color.White - wykonuje ruch jako drugi
	 */
	public Silnik(SerwerLobbyListener a, SerwerLobbyListener b, Color kolor1) {
		System.out.println("Działa silnik");
		this.as = a;
		this.bs = b;
		socket = a.klient;
		socket2 = b.klient;
		name = a.nick;
		name2 = b.nick;
		System.out.println(name + " vs " + name2);
		if (kolor1 == Color.BLACK)
			gracz = 2;
		else {
			gracz = 1;
			tura = 2;
		}
		
		try {
			out = socket.getOutputStream(); out2 = socket2.getOutputStream();
			System.out.println("Serwer START");
			stan = new byte[planszaRozmiar*planszaRozmiar];
			for(int i=0 ;i<50; i++)
				stan[i] = 1;
			stan[27] = 1; stan[28] = 2; stan[35] = 2; stan[36] = 1;
			byte[] message = new byte[stan.length+koniec.length+1];
			message[0] = 1;
			for (int i=0; i<stan.length; i++)
				message[i+1] = stan[i];
			for (int i=0; i<koniec.length; i++)
				message[stan.length+1+i] = koniec[i];
			out.write(message); out2.write(message);
			turaInfo();
			dozwoloneRuchy();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/** Wykonanie kodu odpowiedzialnego za logikę gry w zależności od ruchu gracza
	 * @param response Wiadomość od gracza do silnika gry. W zależności od wartości pierwszego bajta tablicy:
	 * 0 - brak akcji
	 * 4 - ruch gracza
	 */
	public void run(byte[] response) {
		try {
			if (!ended) {
				if (tura==1) {
					System.out.println("Tura pierwszego okienka");
					byte[] message = new byte[koniec.length+1];
					
					//else {
						System.out.println("Są ruchy do zrobienia dla lewego:");
						for (int i=0; i<dozwolone1.size(); i++)
							System.out.print(dozwolone1.get(i)+" ");
						System.out.println();
						//line = in.readLine(); // Czekam na przesłanie ruchu przez gracza1
						//this.wait();
						//while (czekaj)
						//	Thread.sleep(10);
						//byte[] response = line.getBytes();
						//czekaj = true;
						switch(response[0]) {
						case 0:
							break;
						case 4:
							byte rzad = response[1], kolumna = response[2];
							if (dozwolone1.indexOf((byte) (rzad*planszaRozmiar+kolumna)) == -1) {
								message[0] = -1;
								out.write(message);
								System.out.println("Przesłano info, że tura nie dozwolona");
								wyslijStan();
								turaInfo();
							}
							else {
								wykonajRuch(gracz,rzad,kolumna);
								wyslijStan();
								tura = 2;
								turaInfo();
								System.out.println("Wysłano stany po wyk ruchu");
							}
							break;
						default:
							System.out.println("Coś się popsuło");
							break;
						}
						dozwoloneRuchy();
						if (dozwolone1.size() + dozwolone2.size() == 0) {
							System.out.println("Gra zakończona");
							if (ktoWygral() == gracz) 
								System.out.println("Wygrywa " + name);
							else if (ktoWygral() == gracz2)
								System.out.println("Wygrywa " + name2);
							else
								System.out.println("Remis");
							close();
						}
						else if (dozwolone2.size() == 0) {
							tura = 1; 
							turaInfo();
							System.out.println("Brak dozwolonych ruchów dla lewego okna");
						}
					//}
				}
				else {
					System.out.println("Tura drugiego okienka");
					byte[] message = new byte[koniec.length+1];
					if (dozwolone1.size() + dozwolone2.size() == 0) {
						System.out.println("Gra zakończona");
						if (ktoWygral() == gracz)
							System.out.println("Wygrywa " + name);
						else if (ktoWygral() == gracz2)
							System.out.println("Wygrywa " + name2);
						else
							System.out.println("Remis");
						//send(new byte[] {7});
						close();
					}
					else if (dozwolone2.size() == 0) {
						tura = 1;
						System.out.println("Brak dozwolonych ruchów dla prawego okna");
					}
					else {
						System.out.println("Są ruchy do zrobienia dla drugiego okna: ");
						for (int i=0; i<dozwolone2.size(); i++)
							System.out.print(dozwolone2.get(i)+" ");
						System.out.println();
						//line = in2.readLine(); // Czekam na przesłanie ruchu przez gracza2
						
						//while (czekaj)
							//Thread.sleep(10);
						//czekaj = true;
						//this.wait();
						//byte[] response = line.getBytes();
						switch(response[0]) {
						case 0:
							break;
						case 4:
							byte rzad = response[1], kolumna = response[2];
							if (dozwolone2.indexOf((byte) (rzad*planszaRozmiar+kolumna)) == -1) {
								message[0] = -1;
								out2.write(message);
								wyslijStan();
								turaInfo();
								System.out.println("Przesłano info, że tura nie dozwolona");
							}
							else {
								wykonajRuch(gracz2,rzad,kolumna);
								wyslijStan();
								tura = 1;
								turaInfo();
								System.out.println("Wysłano stany po wyk ruchu");
							}
							break;
						default:
							System.out.println("Coś się popsuło");
							break;
						}
						dozwoloneRuchy();
						if (dozwolone1.size() + dozwolone2.size() == 0) {
							System.out.println("Gra zakończona");
							if (ktoWygral() == gracz) 
								System.out.println("Wygrywa " + name);
							else if (ktoWygral() == gracz2)
								System.out.println("Wygrywa " + name2);
							else
								System.out.println("Remis");
							close();
						}
						else if (dozwolone1.size() == 0) {
							tura = 2; 
							turaInfo();
							System.out.println("Brak dozwolonych ruchów dla lewego okna");
						}
					}
				}
					
			}
		} catch (Exception e) {
			close();
			e.printStackTrace();
		} finally {
			//close();
		}
	}
	
	/** Wysłanie do klientów informacji do kogo należy aktualna tura
	 * @throws IOException
	 */
	public void turaInfo() throws IOException {
		byte[] message = new byte[koniec.length+1];
		if (tura == 1 ) {
			message[0] = 2; // 2 - Przesyłam info że twoja tura
			for (int i=0; i<koniec.length; i++)
				message[i+1] = koniec[i];
			out.write(message);
			System.out.println("Przesłano info, że tura pierwszego");
			message[0] = 3; // 3 - Przesyłam info że tura przeciwnika
			out2.write(message);
		}
		else {
			message[0] = 2; // 2 - Przesyłam info że twoja tura
			for (int i=0; i<koniec.length; i++)
				message[i+1] = koniec[i];
			out2.write(message);
			System.out.println("Przesłano info, że tura pierwszego");
			message[0] = 3; // 3 - Przesyłam info że tura przeciwnika
			out.write(message);
		}
	}
	
	/** Wypełnienie list dozwolonych ruchów
	 * 
	 */
	private void dozwoloneRuchy() {
		dozwolone1.clear(); dozwolone2.clear();
		for (byte i=planszaRozmiar/2; i>=0; i--) {
			boolean czySasiadowal = false;
			for (byte j=0; j<planszaRozmiar; j++) {
				boolean t = czySasiaduje(gracz,i,j);
				czySasiadowal |= t;
				if (t) {
					if (czyRuchPoprawny(gracz,i,j))
						dozwolone1.add((byte) (i*planszaRozmiar+j));
				}
			}
			if (!czySasiadowal)
				break;
		}
		for (byte i=planszaRozmiar/2 +1; i<planszaRozmiar; i++) {
			boolean czySasiadowal = false;
			for (byte j=0; j<planszaRozmiar; j++) {
				boolean t = czySasiaduje(gracz,i,j);
				czySasiadowal |= t;
				if (t) {
					if (czyRuchPoprawny(gracz,i,j))
						dozwolone1.add((byte) (i*planszaRozmiar+j));
				}
			}
			if (!czySasiadowal)
				break;
		}
		
		
		for (byte i=planszaRozmiar/2; i>=0; i--) {
			boolean czySasiadowal = false;
			for (byte j=0; j<planszaRozmiar; j++) {
				boolean t = czySasiaduje(gracz2,i,j);
				czySasiadowal |= t;
				if (t) {
					if (czyRuchPoprawny(gracz2,i,j))
						dozwolone2.add((byte) (i*planszaRozmiar+j));
				}
			}
			if (!czySasiadowal)
				break;
		}
		for (byte i=planszaRozmiar/2 +1; i<planszaRozmiar; i++) {
			boolean czySasiadowal = false;
			for (byte j=0; j<planszaRozmiar; j++) {
				boolean t = czySasiaduje(gracz2,i,j);
				czySasiadowal |= t;
				if (t) {
					if (czyRuchPoprawny(gracz2,i,j))
						dozwolone2.add((byte) (i*planszaRozmiar+j));
				}
			}
			if (!czySasiadowal)
				break;
		}
	}
	
	/** Dostarcza informacji o graczu wygrywającym
	 * @return Wartość zmienej gracz,gracz2 (oznaczającej kolor pionków zwycięzcy) lub 0 gdy jest remis
	 */
	public byte ktoWygral() {
		int punkty1 = 0, punkty2 = 0;
		for (int i=0; i<stan.length; i++) {
			if (stan[i] == gracz)
				punkty1++;
			else if (stan[i] == gracz2)
				punkty2++;
		}
		if (punkty1>punkty2)
			return gracz;
		else if (punkty1 == punkty2)
			return 0;
		else 
			return gracz2;
	}
	
	/** Zwraca true gdy ruch jest poprawny, false gdy nie jest zgodny z regułami gry
	 * @param g1 Kolor pionków gracza wykonującego ruch (1 - biały, 2 - czarny)
	 * @param rzad Numer rzędu wybranego pola na planszy
	 * @param kolumna Numer kolumny wybranego pola na planszy
	 * @return true gdy ruch poprawny, false w przeciwnym wypadku
	 */
	private boolean czyRuchPoprawny(byte g1, byte rzad, byte kolumna) {
		if (rzad >= planszaRozmiar || kolumna >= planszaRozmiar || stan[rzad*planszaRozmiar+kolumna] != 0)
			return false;
		
		byte g2 = 1;
		if (g1 == 1)
			g2 = 2;
		boolean tak = false;
		int i;
		// Sprawdzam lewą stronę
		for (i=kolumna-1; i>0; i--) {
			if (stan[planszaRozmiar*rzad+i]==g2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*rzad+i] == g1)
			return true;
		// Sprawdzam prawą stronę
		tak = false;
		for (i=kolumna+1; i<planszaRozmiar-1; i++) {
			if (stan[planszaRozmiar*rzad+i]==g2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*rzad+i] == g1) 
			return true;
		// Sprawdzam górę
		tak = false;
		for (i=rzad-1; i>0; i--) {
			if (stan[planszaRozmiar*i+kolumna]==g2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*i+kolumna] == g1) 
			return true;
		// Sprawdzam dół
		tak = false;
		for (i=rzad+1; i<planszaRozmiar-1; i++) {
			if (stan[planszaRozmiar*i+kolumna]==g2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*i+kolumna] == g1) 
			return true;
		// Sprawdzam lewą górną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna-(planszaRozmiar+1); i>planszaRozmiar 
				&& (i%planszaRozmiar) < ((i+(planszaRozmiar+1))%planszaRozmiar); i-=(planszaRozmiar+1)) {
			if (stan[i] == g2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) < (i+(planszaRozmiar+1))%planszaRozmiar) {
			if (stan[i] == g1) 
				return true;
		}
		// Sprawdzam prawą górną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna-(planszaRozmiar-1); i>planszaRozmiar 
				&& (i%planszaRozmiar) > ((i+(planszaRozmiar-1))%planszaRozmiar); i-=(planszaRozmiar-1)) {
			if (stan[i] == g2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) > (i+(planszaRozmiar-1))%planszaRozmiar) {
			if (stan[i] == g1)
				return true;
		}
		// Sprawdzam lewą dolną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna+(planszaRozmiar-1); i<planszaRozmiar*(planszaRozmiar-1)
				&& (i%planszaRozmiar) < ((i-(planszaRozmiar-1))%planszaRozmiar); i+=(planszaRozmiar-1)) {
			if (stan[i] == g2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) < (i-(planszaRozmiar-1))%planszaRozmiar) {
			if (stan[i] == g1) 
				return true;
		}
		// Sprawdzam prawą dolną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna+(planszaRozmiar+1); i<planszaRozmiar*(planszaRozmiar-1)
				&& (i%planszaRozmiar) > ((i-(planszaRozmiar+1))%planszaRozmiar); i+=(planszaRozmiar+1)) {
			if (stan[i] == g2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) > (i-(planszaRozmiar+1))%planszaRozmiar) {
			if (stan[i] == g1)
				return true;
		}
		return false;
	}
	
	/** Zwraca true, gdy pole sąsiaduje z pionkiem przeciwnika, false gdy sąsiaduje z własnym pionkiem lub nie sąsiaduje z żadnym pionkiem
	 * @param gracz Kolor pionków gracza wykonującego ruch (1 - biały, 2 - czarny)
	 * @param rzad Numer rzędu wybranego pola na planszy
	 * @param kolumna Numer kolumny wybranego pola na planszy
	 * @return true gdy pole sąsiaduje z pionkiem przeciwnika, false w przeciwnym wypadku
	 */
	private boolean czySasiaduje(byte gracz, byte rzad, byte kolumna) {
		boolean lewo = false, prawo = false, gora = false, dol = false;
		byte gracz2 = 1;
		if (gracz == 1)
			gracz2 = 2;
		
		if (kolumna-1>=0)
			lewo = stan[rzad*planszaRozmiar+kolumna-1] == gracz2;
		if (kolumna+1<planszaRozmiar)
			prawo = stan[rzad*planszaRozmiar+kolumna+1] == gracz2;
		if (rzad+1<planszaRozmiar)
			gora = stan[(rzad+1)*planszaRozmiar+kolumna] == gracz2;
		if (rzad-1>=0)
			dol = stan[(rzad-1)*planszaRozmiar+kolumna] == gracz2;
		
		if (lewo || prawo || gora || dol)
			return true;
		
		// Sprawdzam lewą górną przekątną
		for (int i=planszaRozmiar*rzad+kolumna-(planszaRozmiar+1); i>planszaRozmiar 
				&& (i%planszaRozmiar) < ((i+(planszaRozmiar+1))%planszaRozmiar);) {
			if (stan[i] == gracz2)
				return true;
			else 
				break;
		}
		
		// Sprawdzam prawą górną przekątną
		for (int i=planszaRozmiar*rzad+kolumna-(planszaRozmiar-1); i>planszaRozmiar 
				&& (i%planszaRozmiar) > ((i+(planszaRozmiar-1))%planszaRozmiar);) {
			if (stan[i] == gracz2)
				return true;
			else 
				break;
		}
		
		// Sprawdzam lewą dolną przekątną
		for (int i=planszaRozmiar*rzad+kolumna+(planszaRozmiar-1); i<planszaRozmiar*(planszaRozmiar-1)
				&& (i%planszaRozmiar) < ((i-(planszaRozmiar-1))%planszaRozmiar);) {
			if (stan[i] == gracz2)
				return true;
			else 
				break;
		}
		
		// Sprawdzam prawą dolną przekątną
		for (int i=planszaRozmiar*rzad+kolumna+(planszaRozmiar+1); i<planszaRozmiar*(planszaRozmiar-1)
				&& (i%planszaRozmiar) > ((i-(planszaRozmiar+1))%planszaRozmiar);) {
			if (stan[i] == gracz2)
				return true;
			else 
				break;
		}
		
		return false;
	}
	/*
	private boolean czyPlanszaZapelniona() {
		for (int i=0; i<stan.length; i++) {
			if (stan[i] == 0)
				return false;
		}
		return true;
	}
	*/
	/** Wysyła do klientów aktualny stan planszy
	 * 
	 */
	private void wyslijStan() {
		byte[] message = new byte[stan.length+koniec.length+1];
		message[0] = 1;
		for (int i=0; i<stan.length; i++)
			message[i+1] = stan[i];
		for (int i=0; i<koniec.length; i++)
			message[stan.length+1+i] = koniec[i];
		try {
			out.write(message);out2.write(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	/** Zmiana stanu planszy po wykonaniu ruchu
	 * @param gracz Kolor pionków gracza wykonującego ruch (1 - biały, 2 - czarny)
	 * @param rzad Numer rzędu wybranego pola na planszy
	 * @param kolumna Numer kolumny wybranego pola na planszy
	 */
	private void wykonajRuch(byte gracz, byte rzad, byte kolumna) {
		byte gracz2 = 1;
		if (gracz == 1)
			gracz2 = 2;
		boolean tak = false;
		int i;
		// Sprawdzam lewą stronę
		for (i=kolumna-1; i>0; i--) {
			if (stan[planszaRozmiar*rzad+i]==gracz2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*rzad+i] == gracz) {
			for (i++;i<=kolumna;i++) {
				stan[planszaRozmiar*rzad+i] = gracz;
				//wykonajRuch(gracz, rzad, (byte) i);
			}
		}
		// Sprawdzam prawą stronę
		tak = false;
		for (i=kolumna+1; i<planszaRozmiar-1; i++) {
			if (stan[planszaRozmiar*rzad+i]==gracz2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*rzad+i] == gracz) {
			for (i--;i>=kolumna;i--) {
				stan[planszaRozmiar*rzad+i] = gracz;
				//wykonajRuch(gracz, rzad, (byte) i);
			}
		}
		// Sprawdzam górę
		tak = false;
		for (i=rzad-1; i>0; i--) {
			if (stan[planszaRozmiar*i+kolumna]==gracz2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*i+kolumna] == gracz) {
			for (i++;i<=rzad;i++) {
				stan[planszaRozmiar*i+kolumna] = gracz;
				//wykonajRuch(gracz, (byte) i, kolumna);
			}
		}
		// Sprawdzam dół
		tak = false;
		for (i=rzad+1; i<planszaRozmiar-1; i++) {
			if (stan[planszaRozmiar*i+kolumna]==gracz2)
				tak = true;
			else
				break;
		}
		if (tak && stan[planszaRozmiar*i+kolumna] == gracz) {
			for (i--;i>=rzad;i--) {
				stan[planszaRozmiar*i+kolumna] = gracz;
				//wykonajRuch(gracz, (byte) i, kolumna);
			}
		}
		// Sprawdzam lewą górną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna-(planszaRozmiar+1); i>planszaRozmiar 
				&& (i%planszaRozmiar) < ((i+(planszaRozmiar+1))%planszaRozmiar); i-=(planszaRozmiar+1)) {
			if (stan[i] == gracz2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) < (i+(planszaRozmiar+1))%planszaRozmiar) {
			if (stan[i] == gracz) {
				for (i+=planszaRozmiar+1; i<=planszaRozmiar*rzad+kolumna; i+=planszaRozmiar+1) {
					stan[i] = gracz;
					//wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
				}
			}
		}
		// Sprawdzam prawą górną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna-(planszaRozmiar-1); i>planszaRozmiar 
				&& (i%planszaRozmiar) > ((i+(planszaRozmiar-1))%planszaRozmiar); i-=(planszaRozmiar-1)) {
			if (stan[i] == gracz2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) > (i+(planszaRozmiar-1))%planszaRozmiar) {
			if (stan[i] == gracz) {
				for (i+=planszaRozmiar-1; i<=planszaRozmiar*rzad+kolumna; i+=planszaRozmiar-1) {
					stan[i] = gracz;
					//wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
				}
			}
		}
		// Sprawdzam lewą dolną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna+(planszaRozmiar-1); i<planszaRozmiar*(planszaRozmiar-1)
				&& (i%planszaRozmiar) < ((i-(planszaRozmiar-1))%planszaRozmiar); i+=(planszaRozmiar-1)) {
			if (stan[i] == gracz2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) < (i-(planszaRozmiar-1))%planszaRozmiar) {
			if (stan[i] == gracz) {
				for (i-=planszaRozmiar-1; i>=planszaRozmiar*rzad+kolumna; i-=planszaRozmiar-1) {
					stan[i] = gracz;
					//wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
				}
			}
		}
		// Sprawdzam prawą dolną przekątną
		tak = false;
		for (i=planszaRozmiar*rzad+kolumna+(planszaRozmiar+1); i<planszaRozmiar*(planszaRozmiar-1)
				&& (i%planszaRozmiar) > ((i-(planszaRozmiar+1))%planszaRozmiar); i+=(planszaRozmiar+1)) {
			if (stan[i] == gracz2)
				tak = true;
			else
				break;
		}
		if (tak && (i%planszaRozmiar) > (i-(planszaRozmiar+1))%planszaRozmiar) {
			if (stan[i] == gracz) {
				for (i-=planszaRozmiar+1; i>=planszaRozmiar*rzad+kolumna; i-=planszaRozmiar+1) {
					stan[i] = gracz;
					//wykonajRuch(gracz, (byte) (i/planszaRozmiar), (byte) (i%planszaRozmiar));
				}
			}
		}
	}
	
	/** Wysyła wiadomość do każdego z graczy dopisując znaki końca linii
	 * @param message Wiadomość do wysłania 
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
		out2.write(m);
		System.out.println("Wysłano wiadomość");
	}
	
	/** Wysyła wiadomość na czat do każdego z graczy dopisując prawidłowy prefiks (nick gracza) i dodając znaki końca linii
	 * @param s Socket gracza wysyłającego wiadomość
	 * @param message Wiadomość do wysłania na czat
	 * @throws IOException
	 */
	public void sendM(Socket s,byte[] message) throws IOException {
		byte[] koniec = "\r\n".getBytes();
		byte[] m;
		if (s==socket) {
			m = new byte[name.length()+2+message.length+koniec.length];
			for (int i=0; i<name.length();i++)
				m[i] = (byte) name.charAt(i);
			m[name.length()] = ':'; m[name.length()+1] = ' ';
			for (int i=0; i<message.length; i++)
				m[name.length()+2+i] = message[i];
			for (int i=0; i<koniec.length; i++)
				m[name.length()+2+message.length+i] = koniec[i];
		} 
		else {
			m = new byte[name2.length()+2+message.length+koniec.length];
			for (int i=0; i<name2.length();i++)
				m[i] = (byte) name2.charAt(i);
			m[name2.length()] = ':'; m[name2.length()+1] = ' ';
			for (int i=0; i<message.length; i++)
				m[name2.length()+2+i] = message[i];
			for (int i=0; i<koniec.length; i++)
				m[name2.length()+2+message.length+i] = koniec[i];
		}
			
		out.write(m);
		out2.write(m);
		System.out.println("Wysłano wiadomość");
	}
	
	/** Wysłanie informacji o zakończeniu gry 
	 * 
	 */
	public void close() {
		boolean rozlaczony = false;
		try {
			out.write(new byte[] {7,'\r','\n'});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		try {
			out2.write(new byte[] {7,'\r','\n'});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		as.zakonczMecz();
		bs.zakonczMecz();
		ended = true;
	}
	
	/** Wysłanie informacji o zakończeniu gry z powodu wyjścia przeciwnika
	 * @param s Gniazdo gracza, który opuścił rozgrywkę
	 */
	public void closeDisc(Socket s) {
		if (socket == s) {
			try {
				out2.write(new byte[] {7,0,'\r','\n'});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			as.zakonczMecz();
			bs.zakonczMecz();
			ended = true;
		}
		else {
			try {
				out.write(new byte[] {7,0,'\r','\n'});
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
			as.zakonczMecz();
			bs.zakonczMecz();
			ended = true;
		}
	}
}
