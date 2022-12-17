import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static java.awt.Color.*;

// Un pannello che mostra un elenco di tutti i client sul server in una tabella, insieme ai pulsanti Riproduci e Aggiorna

public class PanelLobby extends JPanel {

    private final JTable table;
    DefaultTableModel dtm;
    Object[][] s;

    // costruttore
    public PanelLobby() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        dtm = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        dtm.addColumn("Numero");
        dtm.addColumn("Nickname");
        dtm.addColumn("In gioco"); // 0 - no, 1 - si
        table = new JTable(dtm);
        JScrollPane tab = new JScrollPane(table, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                final Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                // c.setFocusable(true);

                if ((table.getValueAt(row, 1)).toString().compareTo(Main.name) == 0) {
                    c.setBackground(LIGHT_GRAY);
                    c.setForeground(YELLOW);
                } else if ((byte) table.getValueAt(row, 2) == 1) {
                    c.setBackground(LIGHT_GRAY);
                    c.setForeground(RED);
                } else {
                    if (table.getSelectedRow() == row) c.setBackground(GREEN);
                    else c.setBackground(WHITE);
                    c.setForeground(BLACK);
                }

                return c;
            }
        });
        table.setRowSelectionAllowed(true);

        JPanel a = new JPanel();
        a.setLayout(new FlowLayout());
        JButton button = new JButton("Odśwież");
        button.addActionListener(e -> requestTableUpdate());

        JButton button2 = new JButton("Graj");
        button2.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1 && ((String) table.getValueAt(row, 1)).compareTo(Main.name) != 0 && (byte) table.getValueAt(row, 2) != 1) {
                try {
                    String z = table.getValueAt(row, 1).toString();
                    System.out.println("Wywołanie graj() z arg " + z);
                    graj(z);
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            }
        });
        a.add(button);
        a.add(button2);
        //button2.setEnabled(false);
        this.add(tab);
        this.add(a);
        requestTableUpdate();
    }

    /**
     * Usuwa obecną zawartość tabeli zastępując ją nową zawartością
     *
     * @param s Tablica dwuwymiarowa (rzędy i kolumny) zawierająca zawartość tabeli
     */
    public void updateTable(Object[][] s) {
        this.s = s;
        int size = dtm.getRowCount();
        for (int i = 0; i < size; i++)
            dtm.removeRow(0);
        for (Object[] objects : s) dtm.addRow(objects);
        repaint();
    }

    /**
     * Wysyła, za pośrednictwem wątku ClientThread, prośbę o zaaktualizowanie tabeli połączonych użytkowników
     */
    public void requestTableUpdate() {
        byte[] m = {0};
        //try {
        Main.clientThread.send(m);
        //} catch (IOException e) {
        // TODO Auto-generated catch block
        //	e.printStackTrace();
        //}
    }

    /**
     * Wysyła, za pośrednictwem wątku ClientThread, prośbę o rozpoczęcie gry z odpowiednim graczem
     *
     * @param a Nick gracza, z którym ma rozpocząć się gra
     */
    public void graj(String a) {
        byte[] m = new byte[a.length() + 1];
        byte[] b = a.getBytes();
        System.arraycopy(b, 0, m, 1, a.length());
        m[0] = 1;
        //try {
        Main.clientThread.send(m);
        //} catch (IOException e) {
        // TODO Auto-generated catch block
        //	e.printStackTrace();
        //}
    }
}
