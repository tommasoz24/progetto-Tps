import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/** Obiekt odpowiadający za wyświetlanie czatu
 * @author Bartosz Ruta
 *
 */
public class ChatWindow extends JTextPane {
	private Color a = Color.GREEN, b = Color.BLACK;
	//private boolean aktywny = false;
	//private JScrollPane scrollbar;
	
	/** Tworzy obiekt wyświetlający chat
	 * 
	 */
	public ChatWindow() {
		setBackground(Color.LIGHT_GRAY);
		/*for (int i=0; i<100; i++)
			wyswietlWiadomosc("Przykładowy tekst coś tam coś tam",b);*/
		this.setPreferredSize(new Dimension(100,500));
		this.setMaximumSize(new Dimension(200,500));
		this.setEditable(false);
		//this.setMargin(new Insets(5,5,5,5));
		aktywuj();
	}
	
	/** Wyświetla wiadomość na chat zmieniając kolor na:
	 * Zielony, gdy wiadomość poprzedza nick klienta, u którego otwarte jest window
	 * Czarny w przeciwnym wypadku
	 * @param msg Treść wiadomości
	 */
	public void wyswietlWiadomosc(String msg) {
			Color c;
			if (msg.startsWith(Main.name+":"))
				c = a;
			else
				c = b;
			this.setEditable(true);
	        StyleContext sc = StyleContext.getDefaultStyleContext();
	        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

	        //aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
	        //aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

	        int len = getDocument().getLength();
	        setCaretPosition(len);
	        setCharacterAttributes(aset, false);
	        replaceSelection(msg+'\n');
	        this.setEditable(false);
	}
	
	/** Ustawio tło na białe
	 * 
	 */
	public void aktywuj() {
		//aktywny = true;
		setBackground(Color.WHITE);
	}
	
	/** Ustawia tło na szare
	 * 
	 */
	public void dezaktywuj() {
		//aktywny = false;
		setBackground(Color.LIGHT_GRAY);
	}
	
	
	
	
}
