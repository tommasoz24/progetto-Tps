package view;

import main.Main;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class FinestraDiGioco extends JFrame {

    public JPanel contentPane;
    public SchermataGrigliaGioco panel;    // pannello con la scacchiera
    public FinestraChat chat;       // pannello con la chat
    public JLabel label, label2;    // etichette punteggio

    // creo la finestra di gioco
    public FinestraDiGioco(boolean nero) {
        System.out.println("Creazione finestra di gioco");
        setTitle("Othello");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        contentPane.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        label = new JLabel("22");
        label.setBorder(new CompoundBorder( // setta i due bordi
                BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK), // bordo esterno
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
        label2.setBorder(new CompoundBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK), // bordo interno
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

        panel = new SchermataGrigliaGioco(nero);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.75;
        c.weighty = 1;
        c.gridwidth = 1;
        c.gridheight = 3;
        c.gridx = 1;
        c.gridy = 0;
        contentPane.add(panel, c);

        chat = new FinestraChat();
        JScrollPane chatScrollbar = new JScrollPane(chat, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.2;
        c.weighty = 0.9;
        c.gridwidth = 1;
        c.gridheight = 2;
        c.gridx = 4;
        c.gridy = 0;
        contentPane.add(chatScrollbar, c);

        JTextArea tA = new JTextArea();
        tA.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    System.out.println("Testo: " + tA.getText());
                    Main.clientThread.send((tA.getText().replace('\n', ' ')).getBytes());
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

        setContentPane(contentPane);
        setSize(600, 500);
        setVisible(true);

    }

    // aggiorna il punteggio
    public SchermataGrigliaGioco getPanel() {
        return panel;
    }

    // cambia il colore dei bordi, la dimensione dei caratteri delle etichette che mostrano il risultato corrente per informare sul risultato della ricerca

    public void changeColorFrame(boolean czarny) {
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

    public void close() {
        setVisible(false);
        this.dispose();
    }
}
