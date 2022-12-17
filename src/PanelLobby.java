import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.Color.*;

/**
 * Panel wyświetlający w tabeli listę wszystkich klientów będących na serwerze, wraz z przyciskami Graj i Odśwież
 *
 * @author Bartosz Ruta
 */
public class PanelLobby extends JPanel {

    private final JTable table;
    DefaultTableModel dtm;
    Object[][] s;

    /**
     * Tworzy obiekt PanelLobby
     */
    public PanelLobby() {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        dtm = new DefaultTableModel() {
            public boolean isCellEditable(int row, int column) {
                //all cells false
                return false;
            }
        };
        dtm.addColumn("Nr");
        dtm.addColumn("Nick");
        dtm.addColumn("W grze");
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
                    if (table.getSelectedRow() == row)
                        c.setBackground(GREEN);
                    else
                        c.setBackground(WHITE);
                    c.setForeground(BLACK);
                }

                return c;
            }
        });
        table.setRowSelectionAllowed(true);

        JPanel a = new JPanel();
        a.setLayout(new FlowLayout());
        JButton button = new JButton("Odśwież");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                requestTableUpdate();
            }
        });

        JButton button2 = new JButton("Graj");
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int row = table.getSelectedRow();
                if (row != -1 && ((String) table.getValueAt(row, 1)).compareTo(Main.name) != 0 && (byte) table.getValueAt(row, 2) != 1) {
                    try {
                        String z = table.getValueAt(row, 1).toString();
                        System.out.println("Wywołanie graj() z arg " + z);
                        graj(z);
                    } catch (NumberFormatException nfe) {
                    }
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
        for (int i = 0; i < s.length; i++)
            dtm.addRow(s[i]);
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
        for (int i = 0; i < a.length(); i++)
            m[i + 1] = b[i];
        m[0] = 1;
        //try {
        Main.clientThread.send(m);
        //} catch (IOException e) {
        // TODO Auto-generated catch block
        //	e.printStackTrace();
        //}
    }
}
