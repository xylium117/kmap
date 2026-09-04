package src;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Set;

public class TruthTableDialog extends JDialog {
    private final int vars;
    private final Set<Integer> minterms;
    private final Set<Integer> dontCares;

    public TruthTableDialog(Frame parent, int vars, Set<Integer> minterms, Set<Integer> dontCares) {
        super(parent, "Truth Table (" + vars + " Variables)", true);
        this.vars = vars;
        this.minterms = minterms;
        this.dontCares = dontCares;
        initializeUI();
    }

    private void initializeUI() {
        setSize(560, 530);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Truth Table Representation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel("Variables: " + getVariableNames() + " | Total Rows: " + (1 << vars));
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subLabel.setForeground(new Color(203, 213, 225));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        String[] columns = getColumnNames();
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        int totalRows = 1 << vars;
        for (int i = 0; i < totalRows; i++) {
            Object[] row = new Object[columns.length];
            row[0] = "m" + i;
            for (int v = 0; v < vars; v++) {
                int bit = (i >> (vars - 1 - v)) & 1;
                row[1 + v] = bit;
            }
            if (minterms.contains(i)) {
                row[1 + vars] = "1";
                row[2 + vars] = "Minterm (1)";
            } else if (dontCares.contains(i)) {
                row[1 + vars] = "X";
                row[2 + vars] = "Don't Care (X)";
            } else {
                row[1 + vars] = "0";
                row[2 + vars] = "Maxterm (0)";
            }
            model.addRow(row);
        }

        JTable table = new JTable(model);
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(226, 232, 240));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(51, 65, 85));
        header.setPreferredSize(new Dimension(0, 32));

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 13));

                if (!isSelected) {
                    if (minterms.contains(row)) {
                        c.setBackground(new Color(236, 253, 245));
                    } else if (dontCares.contains(row)) {
                        c.setBackground(new Color(254, 252, 232));
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 250, 252));
                    }
                }

                if (col == 1 + vars) {
                    setFont(new Font("Segoe UI", Font.BOLD, 14));
                    if ("1".equals(value)) {
                        setForeground(new Color(16, 185, 129));
                    } else if ("X".equals(value)) {
                        setForeground(new Color(217, 119, 6));
                    } else {
                        setForeground(new Color(100, 116, 139));
                    }
                } else if (col == 0) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    setForeground(new Color(71, 85, 105));
                } else {
                    setForeground(new Color(30, 41, 59));
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        bottomPanel.setOpaque(false);

        JLabel summaryLabel = new JLabel("Minterms: " + minterms.size() + " | Don't Cares: " + dontCares.size() + " | 0-Terms: " + (totalRows - minterms.size() - dontCares.size()));
        summaryLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        summaryLabel.setForeground(new Color(100, 116, 139));
        bottomPanel.add(summaryLabel, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        ModernButton copyBtn = new ModernButton("Copy Truth Table", new Color(59, 130, 246), Color.WHITE);
        copyBtn.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append(String.join("\t", columns)).append("\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    sb.append(model.getValueAt(r, c)).append(c == model.getColumnCount() - 1 ? "" : "\t");
                }
                sb.append("\n");
            }
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
            JOptionPane.showMessageDialog(this, "Truth table copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        ModernButton closeBtn = new ModernButton("Close", new Color(100, 116, 139), Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(copyBtn);
        btnPanel.add(closeBtn);
        bottomPanel.add(btnPanel, BorderLayout.EAST);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private String getVariableNames() {
        switch (vars) {
            case 2: return "A, B";
            case 3: return "A, B, C";
            case 4: return "A, B, C, D";
            default: return "A, B, C, D";
        }
    }

    private String[] getColumnNames() {
        String[] cols = new String[vars + 3];
        cols[0] = "Term";
        char vChar = 'A';
        for (int i = 0; i < vars; i++) {
            cols[1 + i] = String.valueOf((char) (vChar + i));
        }
        cols[1 + vars] = "Output (F)";
        cols[2 + vars] = "State";
        return cols;
    }
}
