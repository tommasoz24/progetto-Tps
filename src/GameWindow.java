import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GameWindow extends JFrame {

    public JPanel contentPane;
    public PanelPlansza plansza;        // pannello con la scacchiera
    public ChatWindow chat;       // pannello con la chat
    /**
     * Etykiety wyświetlające aktualny wynik
     */
    public JLabel label, label2;

    /**
     * Tworzy okno gry Reversi
     *
     * @param czarny Kolor pionków gracza tworzącego okno: true - czarny, false - biały
     */
    public GameWindow(boolean czarny) {
        System.out.println("Tworzenie okna gry");
        setTitle("Reversi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setBounds(200,200,500,500);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        label = new JLabel("22");
        //label.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        label.setBorder(new CompoundBorder( // sets two borders
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK), // outer border
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        label.setForeground(Color.BLACK);
        label.setBackground(Color.GREEN);
        label.setOpaque(true);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Serif", Font.PLAIN, 14));
        label.setMinimumSize(new Dimension(30, 30));
        label.setMaximumSize(new Dimension(30, 30));
        label2 = new JLabel("22");
        //label2.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
        label2.setBorder(new CompoundBorder( // sets two borders
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK), // outer border
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        label2.setForeground(Color.WHITE);
        label2.setBackground(Color.GREEN);
        label2.setOpaque(true);
        label2.setVerticalAlignment(SwingConstants.CENTER);
        label2.setHorizontalAlignment(SwingConstants.CENTER);
        label2.setMinimumSize(new Dimension(30, 30));
        label2.setMaximumSize(new Dimension(30, 30));
        label2.setFont(new Font("Serif", Font.PLAIN, 14));
        c.fill = GridBagConstraints.NONE;
        c.weightx = 0.025;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        contentPane.add(label, c);
        c.gridx = 0;
        c.gridy = 1;
        contentPane.add(label2, c);

        plansza = new PanelPlansza(czarny);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.75;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.gridx = 1;
        c.gridy = 0;
        contentPane.add(plansza, c);

        chat = new ChatWindow();
        JScrollPane czatIscrollbar = new JScrollPane(chat, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.2;
        c.weighty = 0.9;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.gridx = 4;
        c.gridy = 0;
        contentPane.add(czatIscrollbar, c);
        //c.anchor = GridBagConstraints.FIRST_LINE_END;

        JTextArea tA = new JTextArea();/*{
			public void keyPressed(KeyEvent e) {
	            System.out.println("test");
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                	e.consume();
                	try {
						Main.clientThread.send(this.getText().getBytes());
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} 
                }
			}
		};*/
        tA.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    System.out.println("Tekst: " + tA.getText());
                    //try {
                    Main.clientThread.send((tA.getText().replace('\n', ' ')).getBytes());
                    //} catch (IOException e1) {
                    //	e1.printStackTrace();
                    //}
                    //tA.setText("");
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tA.setText("");
                }
            }
        });
        tA.setLineWrap(true);
        //tA.setPreferredSize(new Dimension(100,50));
        //tA.setMaximumSize(new Dimension(200,100));
        JScrollPane scrollPane = new JScrollPane(tA, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(100, 50));
        scrollPane.setMaximumSize(new Dimension(200, 100));
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.2;
        c.weighty = 0.1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 4;
        c.gridy = 2;
        contentPane.add(scrollPane, c);

        //c.anchor = GridBagConstraints.FIRST_LINE_END;

        setContentPane(contentPane);
        setSize(600, 500);
        setVisible(true);

        //scrollbar.setViewportBorder(new EmptyBorder(5,5,5,5));
        //scrollbar.setViewportView(chat);
        //chat.wyswietlWiadomosc("ASDASDADS");
    }

    /**
     * Zwraca referencję do planszy aktualnej gry
     *
     * @return referencję do planszy aktualnej gry
     */
    public PanelPlansza getPlansza() {
        return plansza;
    }

    /**
     * Zmienia kolor ramek, wielkość czcionek etykiet pokazujących aktualny wynik, co ma informować o
     * aktualnej turze
     *
     * @param czarny Informacja czy obecna tura należy do pionków czarnych
     */
    public void zmienKolorRamki(boolean czarny) {
        if (czarny) {
            label.setFont(new Font("Serif", Font.BOLD, 16));
            label.setBorder(new CompoundBorder( // sets two borders
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE), // outer border
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            label2.setFont(new Font("Serif", Font.PLAIN, 14));
            label2.setBorder(new CompoundBorder( // sets two borders
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK), // outer border
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        } else {
            label.setFont(new Font("Serif", Font.PLAIN, 14));
            label.setBorder(new CompoundBorder( // sets two borders
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK), // outer border
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));
            label2.setFont(new Font("Serif", Font.BOLD, 16));
            label2.setBorder(new CompoundBorder( // sets two borders
                    BorderFactory.createMatteBorder(1, 1, 1, 1, Color.ORANGE), // outer border
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        }
    }

    /**
     * Zamyka okno
     */
    public void close() {
        setVisible(false);
        this.dispose();
    }
}
