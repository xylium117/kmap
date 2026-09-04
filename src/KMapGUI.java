package src;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class KMapGUI extends JFrame {
    private final KMapSolver solver;
    private final int variables;
    private final List<String> primeImplicants;
    private KMapPanel kmapPanel;

    public KMapGUI(int variables, Set<Integer> minterms, Set<Integer> dontCares) {
        this.variables = variables;
        this.solver = new KMapSolver(variables, minterms, dontCares);
        this.primeImplicants = solver.getImplicants();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Karnaugh Map Solution (" + variables + " Variables)");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(new Color(30, 41, 59));
        topHeader.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JLabel title = new JLabel("Karnaugh Map Simplification");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel(variables + "-Variable Map | Minimal SOP & POS Solution");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(203, 213, 225));

        topHeader.add(title, BorderLayout.NORTH);
        topHeader.add(subtitle, BorderLayout.SOUTH);
        add(topHeader, BorderLayout.NORTH);

        JPanel centerContainer = new JPanel(new BorderLayout(12, 12));
        centerContainer.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        centerContainer.setOpaque(false);

        JPanel mapWrapper = new JPanel(new BorderLayout());
        mapWrapper.setOpaque(false);
        mapWrapper.add(buildKMapPanel(), BorderLayout.CENTER);
        centerContainer.add(mapWrapper, BorderLayout.CENTER);

        JPanel groupWrapper = buildGroupPanel();
        centerContainer.add(groupWrapper, BorderLayout.EAST);

        add(centerContainer, BorderLayout.CENTER);

        JPanel bottomContainer = new JPanel(new BorderLayout(12, 12));
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(0, 12, 12, 12));
        bottomContainer.setOpaque(false);

        bottomContainer.add(buildResultPanel(), BorderLayout.CENTER);
        bottomContainer.add(buildControlPanel(), BorderLayout.EAST);

        add(bottomContainer, BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(890, 650));
        setLocationRelativeTo(null);
    }

    private JPanel buildKMapPanel() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)), " KARNAUGH MAP "),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        container.setBackground(Color.WHITE);

        int rows = getRowCount();
        int cols = getColumnCount();

        kmapPanel = new KMapPanel(rows, cols);
        kmapPanel.setLayout(new GridLayout(rows + 1, cols + 1, 6, 6));

        String[] rowLabels = getRowLabels();
        String[] colLabels = getColumnLabels();

        kmapPanel.add(buildHeaderLabel(getCornerLabel(), true));

        for (String colLabel : colLabels) {
            kmapPanel.add(buildHeaderLabel(colLabel, false));
        }

        for (int row = 0; row < rows; row++) {
            kmapPanel.add(buildHeaderLabel(rowLabels[row], false));

            for (int col = 0; col < cols; col++) {
                int decimalValue = getCellValue(row, col);
                JLabel cell = buildMapCell(decimalValue);
                kmapPanel.addCell(row, col, cell);
            }
        }

        ColorPalette palette = new ColorPalette(variables);
        for (String implicant : primeImplicants) {
            List<Point> groupCells = implicantToCells(implicant);
            Color groupColor = palette.getNextColor();
            GroupBorder border = new GroupBorder(groupCells, groupColor, 4);
            kmapPanel.addGroupBorder(border);
        }

        container.add(kmapPanel, BorderLayout.CENTER);
        return container;
    }

    private JLabel buildHeaderLabel(String text, boolean isCorner) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, isCorner ? 13 : 14));
        label.setForeground(new Color(51, 65, 85));
        label.setBackground(new Color(241, 245, 249));
        label.setOpaque(true);
        label.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240), 1));
        label.setPreferredSize(new Dimension(54, 46));
        return label;
    }

    private JLabel buildMapCell(int value) {
        boolean isMinterm = solver.getMinterms().contains(value);
        boolean isDontCare = solver.getDontCares().contains(value);

        String mainValue = isMinterm ? "1" : (isDontCare ? "X" : "0");

        JLabel cell = new JLabel(mainValue, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                g2.setColor(new Color(100, 116, 139, 200));
                g2.drawString(String.valueOf(value), 5, getHeight() - 5);
                g2.dispose();
            }
        };

        cell.setFont(new Font("Segoe UI", Font.BOLD, 18));
        cell.setHorizontalAlignment(SwingConstants.CENTER);
        cell.setVerticalAlignment(SwingConstants.CENTER);
        cell.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        cell.setOpaque(true);
        cell.setPreferredSize(new Dimension(54, 54));

        if (isMinterm) {
            cell.setBackground(new Color(219, 234, 254));
            cell.setForeground(new Color(30, 64, 175));
        } else if (isDontCare) {
            cell.setBackground(new Color(254, 240, 138));
            cell.setForeground(new Color(161, 98, 7));
        } else {
            cell.setBackground(Color.WHITE);
            cell.setForeground(new Color(148, 163, 184));
        }

        return cell;
    }

    private JPanel buildGroupPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)), " IMPLICANT GROUPS "),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(240, 320));

        List<GroupBorder> groupBorders = kmapPanel.getGroupBorders();

        if (primeImplicants.isEmpty()) {
            JLabel emptyLabel = new JLabel("<html><i>No prime implicants (Function is 0)</i></html>");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            emptyLabel.setForeground(new Color(100, 116, 139));
            panel.add(emptyLabel);
        } else {
            for (int i = 0; i < primeImplicants.size(); i++) {
                final int groupIndex = i;
                String implicant = primeImplicants.get(i);
                Color groupColor = (i < groupBorders.size()) ? groupBorders.get(i).getColor() : new Color(70, 130, 180);
                Set<Integer> terms = implicantToMinterms(implicant);
                String algTerm = solver.toAlgebraic(implicant);
                String groupType = getGroupTypeName(terms.size());

                JPanel card = new JPanel(new BorderLayout(6, 4));
                card.setBackground(new Color(248, 250, 252));
                card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(groupColor, 2, true),
                        BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
                card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
                card.setAlignmentX(Component.LEFT_ALIGNMENT);

                JLabel termLabel = new JLabel(algTerm + "  (" + groupType + ")");
                termLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                termLabel.setForeground(new Color(30, 41, 59));

                JLabel mintermsLabel = new JLabel("m(" + sortedMintermsString(terms) + ")");
                mintermsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
                mintermsLabel.setForeground(new Color(100, 116, 139));

                card.add(termLabel, BorderLayout.NORTH);
                card.add(mintermsLabel, BorderLayout.SOUTH);

                card.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        card.setBackground(new Color(241, 245, 249));
                        kmapPanel.setHighlightedGroupIndex(groupIndex);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        card.setBackground(new Color(248, 250, 252));
                        kmapPanel.setHighlightedGroupIndex(-1);
                    }
                });

                panel.add(card);
                panel.add(Box.createRigidArea(new Dimension(0, 6)));
            }
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.add(scroll, BorderLayout.CENTER);
        return outer;
    }

    private String getGroupTypeName(int size) {
        if (size >= 16) return "Hexadecet";
        if (size >= 8) return "Octet";
        if (size >= 4) return "Quad";
        if (size >= 2) return "Pair";
        return "Singleton";
    }

    private JPanel buildResultPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 6, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)), " SIMPLIFIED BOOLEAN EXPRESSIONS "),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        panel.setBackground(Color.WHITE);

        String sop = solver.getSOP();
        String pos = solver.getPOS();

        JPanel sopRow = createExpressionRow("SOP (Sum of Products):", sop, new Color(37, 99, 235));
        JPanel posRow = createExpressionRow("POS (Product of Sums):", pos, new Color(5, 150, 105));

        panel.add(sopRow);
        panel.add(posRow);
        return panel;
    }

    private JPanel createExpressionRow(String title, String expr, Color badgeColor) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(new Color(71, 85, 105));
        titleLabel.setPreferredSize(new Dimension(170, 26));

        JTextField exprField = new JTextField("F = " + expr);
        exprField.setEditable(false);
        exprField.setFont(new Font("Consolas", Font.BOLD, 14));
        exprField.setForeground(badgeColor);
        exprField.setBackground(new Color(248, 250, 252));
        exprField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)
        ));

        ModernButton copyBtn = new ModernButton("Copy", new Color(100, 116, 139), Color.WHITE);
        copyBtn.addActionListener(e -> {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(expr), null);
            JOptionPane.showMessageDialog(this, "Copied: " + expr, "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        row.add(titleLabel, BorderLayout.WEST);
        row.add(exprField, BorderLayout.CENTER);
        row.add(copyBtn, BorderLayout.EAST);
        return row;
    }

    private JPanel buildControlPanel() {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(3, 1, 0, 8));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        controlPanel.setOpaque(false);

        ModernButton inputBtn = new ModernButton("Edit / New Map", new Color(79, 70, 229), Color.WHITE);
        ModernButton ttBtn = new ModernButton("Truth Table", new Color(14, 165, 233), Color.WHITE);
        ModernButton ldBtn = new ModernButton("Logic Diagram (Gates)", new Color(16, 185, 129), Color.WHITE);

        inputBtn.addActionListener(e -> returnToInput());
        ttBtn.addActionListener(e -> showTruthTable());
        ldBtn.addActionListener(e -> showLogicDiagram());

        controlPanel.add(inputBtn);
        controlPanel.add(ttBtn);
        controlPanel.add(ldBtn);

        return controlPanel;
    }

    private void returnToInput() {
        SwingUtilities.invokeLater(() -> {
            KMapInput input = new KMapInput(variables, solver.getMinterms(), solver.getDontCares());
            input.setVisible(true);
        });
        dispose();
    }

    private void showTruthTable() {
        TruthTableDialog dialog = new TruthTableDialog(this, variables, solver.getMinterms(), solver.getDontCares());
        dialog.setVisible(true);
    }

    private void showLogicDiagram() {
        LogicDiagramDialog dialog = new LogicDiagramDialog(this, solver);
        dialog.setVisible(true);
    }

    private List<Point> implicantToCells(String implicant) {
        Set<Integer> minterms = implicantToMinterms(implicant);
        List<Point> cells = new ArrayList<>();
        for (int m : minterms) {
            cells.add(mintermToCellCoordinate(m));
        }
        return cells;
    }

    private Set<Integer> implicantToMinterms(String implicant) {
        Set<Integer> minterms = new HashSet<>();
        int totalStates = 1 << variables;

        for (int m = 0; m < totalStates; m++) {
            boolean match = true;
            for (int i = 0; i < variables; i++) {
                char impChar = (i < implicant.length()) ? implicant.charAt(i) : '-';
                if (impChar != '-') {
                    int bit = (m >> (variables - 1 - i)) & 1;
                    if ((impChar == '1' && bit != 1) || (impChar == '0' && bit != 0)) {
                        match = false;
                        break;
                    }
                }
            }
            if (match) {
                minterms.add(m);
            }
        }
        return minterms;
    }

    private String sortedMintermsString(Set<Integer> minterms) {
        return minterms.stream()
                .sorted()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    private int getRowCount() {
        switch (variables) {
            case 2:
            case 3:
                return 2;
            case 4:
            default:
                return 4;
        }
    }

    private int getColumnCount() {
        switch (variables) {
            case 2:
                return 2;
            case 3:
            case 4:
            default:
                return 4;
        }
    }

    private String getCornerLabel() {
        switch (variables) {
            case 2:
                return "A \\ B";
            case 3:
                return "A \\ BC";
            case 4:
            default:
                return "AB \\ CD";
        }
    }

    private String[] getRowLabels() {
        switch (variables) {
            case 2:
            case 3:
                return new String[]{"0", "1"};
            case 4:
            default:
                return new String[]{"00", "01", "11", "10"};
        }
    }

    private String[] getColumnLabels() {
        switch (variables) {
            case 2:
                return new String[]{"0", "1"};
            case 3:
            case 4:
            default:
                return new String[]{"00", "01", "11", "10"};
        }
    }

    private int getCellValue(int row, int col) {
        switch (variables) {
            case 2: {
                return (row << 1) | col;
            }
            case 3: {
                int[] colOrder = {0, 1, 3, 2};
                return (row << 2) | colOrder[col];
            }
            case 4:
            default: {
                int[] rowOrder = {0, 1, 3, 2};
                int[] colOrder = {0, 1, 3, 2};
                return (rowOrder[row] << 2) | colOrder[col];
            }
        }
    }

    private Point mintermToCellCoordinate(int minterm) {
        int rows = getRowCount();
        int cols = getColumnCount();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (getCellValue(r, c) == minterm) {
                    return new Point(r, c);
                }
            }
        }
        return new Point(-1, -1);
    }
}