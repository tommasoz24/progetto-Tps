import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

// object for display the chat room
public class ChatWindow extends JTextPane {
	private final Color a = Color.GREEN;
	private final Color b = Color.BLACK;

	public ChatWindow() {
		setBackground(Color.LIGHT_GRAY);
		this.setPreferredSize(new Dimension(100,500));
		this.setMaximumSize(new Dimension(200,500));
		this.setEditable(false);
		activate();
	}

	public void displayMessage(String msg) {
			Color c;
			if (msg.startsWith(Main.username +":"))
				c = a;
			else
				c = b;
			this.setEditable(true);
	        StyleContext sc = StyleContext.getDefaultStyleContext();
	        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

		int len = getDocument().getLength();
	        setCaretPosition(len);
	        setCharacterAttributes(aset, false);
	        replaceSelection(msg+'\n');
	        this.setEditable(false);
	}
	// set the background to white
	public void activate() {
		//active = true;
		setBackground(Color.WHITE);
	}


}
