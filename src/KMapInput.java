package src;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.stream.Collectors;

public class KMapInput extends JFrame {
    private int variables;
    private final Set<Integer> minterms = new TreeSet<>();
    private final Set<Integer> dontCares = new TreeSet<>();
    private final Map<Integer, KMapInputCell> cellMap = new HashMap<>();

    private JPanel mapContainer;
    private JTextField mintermsField;
    private JTextField dontCaresField;
    private JRadioButton var2Btn, var3Btn, var4Btn;
    private boolean updatingFields = false;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            KMapInput input = new KMapInput(4);
            input.setVisible(true);
        });
    }

    public KMapInput(int variables) {
        this(variables, Collections.emptySet(), Collections.emptySet());
    }

    public KMapInput(int variables, Set<Integer> initialMinterms, Set<Integer> initialDontCares) {
        this.variables = (variables >= 2 && variables <= 4) ? variables : 4;
        if (initialMinterms != null) this.minterms.addAll(initialMinterms);
        if (initialDontCares != null) this.dontCares.addAll(initialDontCares);
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Karnaugh Map Input & Configuration");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 41, 59));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel title = new JLabel("Karnaugh Map Solver");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Select variable count, click cells to toggle (0 → 1 → X), or enter minterm lists.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(new Color(203, 213, 225));

        headerPanel.add(title, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new BorderLayout(12, 12));
        mainContent.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        mainContent.setOpaque(false);

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        toolbar.add(buildVariableSelector(), BorderLayout.WEST);
        toolbar.add(buildPresetSelector(), BorderLayout.EAST);
        mainContent.add(toolbar, BorderLayout.NORTH);

        mapContainer = new JPanel(new BorderLayout());
        mapContainer.setOpaque(false);
        rebuildMapGrid();
        mainContent.add(mapContainer, BorderLayout.CENTER);

        mainContent.add(buildSidePanel(), BorderLayout.EAST);

        add(mainContent, BorderLayout.CENTER);

        add(buildBottomPanel(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(860, 620));
        setLocationRelativeTo(null);
    }

    private JPanel buildVariableSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Variables ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(71, 85, 105)
        ));

        var2Btn = new JRadioButton("2 Variables (A, B)", variables == 2);
        var3Btn = new JRadioButton("3 Variables (A, B, C)", variables == 3);
        var4Btn = new JRadioButton("4 Variables (A, B, C, D)", variables == 4);

        ButtonGroup group = new ButtonGroup();
        group.add(var2Btn);
        group.add(var3Btn);
        group.add(var4Btn);

        ItemListener listener = e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                if (var2Btn.isSelected()) switchVariables(2);
                else if (var3Btn.isSelected()) switchVariables(3);
                else if (var4Btn.isSelected()) switchVariables(4);
            }
        };

        var2Btn.addItemListener(listener);
        var3Btn.addItemListener(listener);
        var4Btn.addItemListener(listener);

        var2Btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        var3Btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        var4Btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        panel.add(var2Btn);
        panel.add(var3Btn);
        panel.add(var4Btn);
        return panel;
    }

    private JPanel buildPresetSelector() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225)),
                " Load Preset ",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 12),
                new Color(71, 85, 105)
        ));

        String[] presets = {
                "-- Select Example Preset --",
                "Full Adder (Sum: 3-Var)",
                "Full Adder (Carry: 3-Var)",
                "Majority Voter (3-Var)",
                "4-Corner Wrap (4-Var)",
                "BCD to 7-Seg Segment A (4-Var)",
                "BCD to 7-Seg Segment E (4-Var)",
                "Even Parity Generator (4-Var)"
        };

        JComboBox<String> presetBox = new JComboBox<>(presets);
        presetBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        presetBox.setPreferredSize(new Dimension(210, 28));

        presetBox.addActionListener(e -> {
            int idx = presetBox.getSelectedIndex();
            if (idx == 1) loadPreset(3, setOf(1, 2, 4, 7), setOf());
            else if (idx == 2) loadPreset(3, setOf(3, 5, 6, 7), setOf());
            else if (idx == 3) loadPreset(3, setOf(3, 5, 6, 7), setOf());
            else if (idx == 4) loadPreset(4, setOf(0, 2, 8, 10), setOf());
            else if (idx == 5) loadPreset(4, setOf(0, 2, 3, 5, 6, 7, 8, 9), setOf(10, 11, 12, 13, 14, 15));
            else if (idx == 6) loadPreset(4, setOf(0, 2, 6, 8), setOf(10, 11, 12, 13, 14, 15));
            else if (idx == 7) loadPreset(4, setOf(1, 2, 4, 7, 8, 11, 13, 14), setOf());
        });

        panel.add(presetBox);
        return panel;
    }

    @SafeVarargs
    private static Set<Integer> setOf(Integer... elements) {
        return new HashSet<>(Arrays.asList(elements));
    }

    private void rebuildMapGrid() {
        mapContainer.removeAll();
        cellMap.clear();

        JPanel gridWrapper = new JPanel(new BorderLayout());
        gridWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)), " INTERACTIVE GRID "),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        gridWrapper.setBackground(Color.WHITE);

        int rows = getRowCount();
        int cols = getColumnCount();

        JPanel grid = new JPanel(new GridLayout(rows + 1, cols + 1, 6, 6));
        grid.setBackground(Color.WHITE);

        String[] rowLabels = getRowLabels();
        String[] colLabels = getColumnLabels();

        grid.add(buildHeaderLabel(getCornerLabel(), true));

        for (String cLabel : colLabels) {
            grid.add(buildHeaderLabel(cLabel, false));
        }

        for (int r = 0; r < rows; r++) {
            grid.add(buildHeaderLabel(rowLabels[r], false));

            for (int c = 0; c < cols; c++) {
                int cellId = getCellValue(r, c);
                KMapInputCell cell = new KMapInputCell(this, cellId);

                if (minterms.contains(cellId)) {
                    cell.setState(KMapInputCell.CellState.MINTERM);
                } else if (dontCares.contains(cellId)) {
                    cell.setState(KMapInputCell.CellState.DONT_CARE);
                } else {
                    cell.setState(KMapInputCell.CellState.EMPTY);
                }

                cellMap.put(cellId, cell);
                grid.add(cell);
            }
        }

        gridWrapper.add(grid, BorderLayout.CENTER);
        mapContainer.add(gridWrapper, BorderLayout.CENTER);
        mapContainer.revalidate();
        mapContainer.repaint();

        updateTextFields();
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

    private JPanel buildSidePanel() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)), " CONTROLS & LEGEND "),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        sidePanel.setBackground(Color.WHITE);
        sidePanel.setPreferredSize(new Dimension(210, 320));

        sidePanel.add(createLegendBadge(new Color(219, 234, 254), new Color(30, 64, 175), "Minterm (1)", "Click once"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(createLegendBadge(new Color(254, 240, 138), new Color(161, 98, 7), "Don't Care (X)", "Click twice"));
        sidePanel.add(Box.createRigidArea(new Dimension(0, 8)));
        sidePanel.add(createLegendBadge(Color.WHITE, new Color(100, 116, 139), "Maxterm (0)", "Default / 3rd click"));

        sidePanel.add(Box.createRigidArea(new Dimension(0, 16)));
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        sidePanel.add(sep);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 14)));

        ModernButton clearBtn = new ModernButton("Clear All (0s)", new Color(100, 116, 139), Color.WHITE);
        ModernButton fillBtn = new ModernButton("Fill All (1s)", new Color(59, 130, 246), Color.WHITE);
        ModernButton invertBtn = new ModernButton("Invert Values", new Color(16, 185, 129), Color.WHITE);

        clearBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        fillBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        invertBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        clearBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        fillBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        invertBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        clearBtn.addActionListener(e -> clearGrid());
        fillBtn.addActionListener(e -> fillGrid());
        invertBtn.addActionListener(e -> invertGrid());

        sidePanel.add(clearBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(fillBtn);
        sidePanel.add(Box.createRigidArea(new Dimension(0, 6)));
        sidePanel.add(invertBtn);

        return sidePanel;
    }

    private JPanel createLegendBadge(Color bg, Color fg, String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);

        JLabel swatch = new JLabel(title.contains("1") ? "1" : (title.contains("X") ? "X" : "0"), SwingConstants.CENTER);
        swatch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        swatch.setOpaque(true);
        swatch.setBackground(bg);
        swatch.setForeground(fg);
        swatch.setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225)));
        swatch.setPreferredSize(new Dimension(28, 28));

        JPanel textP = new JPanel(new GridLayout(2, 1));
        textP.setOpaque(false);
        JLabel titleL = new JLabel(title);
        titleL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleL.setForeground(new Color(30, 41, 59));

        JLabel subL = new JLabel(subtitle);
        subL.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        subL.setForeground(new Color(100, 116, 139));

        textP.add(titleL);
        textP.add(subL);

        p.add(swatch, BorderLayout.WEST);
        p.add(textP, BorderLayout.CENTER);
        return p;
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 15, 15, 15));
        panel.setOpaque(false);

        JPanel textFieldsPanel = new JPanel(new GridLayout(2, 1, 6, 6));
        textFieldsPanel.setOpaque(false);

        JPanel mintermRow = new JPanel(new BorderLayout(8, 0));
        mintermRow.setOpaque(false);
        JLabel mLabel = new JLabel("Minterms Σm(...):");
        mLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        mLabel.setPreferredSize(new Dimension(140, 28));
        mintermsField = new JTextField();
        mintermsField.setFont(new Font("Consolas", Font.PLAIN, 13));
        mintermRow.add(mLabel, BorderLayout.WEST);
        mintermRow.add(mintermsField, BorderLayout.CENTER);

        JPanel dontCareRow = new JPanel(new BorderLayout(8, 0));
        dontCareRow.setOpaque(false);
        JLabel dLabel = new JLabel("Don't-Cares d(...):");
        dLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dLabel.setPreferredSize(new Dimension(140, 28));
        dontCaresField = new JTextField();
        dontCaresField.setFont(new Font("Consolas", Font.PLAIN, 13));
        dontCareRow.add(dLabel, BorderLayout.WEST);
        dontCareRow.add(dontCaresField, BorderLayout.CENTER);

        textFieldsPanel.add(mintermRow);
        textFieldsPanel.add(dontCareRow);

        ActionListener applyTextAction = e -> parseAndUpdateFromFields();
        mintermsField.addActionListener(applyTextAction);
        dontCaresField.addActionListener(applyTextAction);
        mintermsField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                parseAndUpdateFromFields();
            }
        });
        dontCaresField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                parseAndUpdateFromFields();
            }
        });

        ModernButton solveBtn = new ModernButton("Solve K-Map →", new Color(79, 70, 229), Color.WHITE);
        solveBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        solveBtn.setPreferredSize(new Dimension(170, 62));
        solveBtn.addActionListener(e -> launchSolution());

        panel.add(textFieldsPanel, BorderLayout.CENTER);
        panel.add(solveBtn, BorderLayout.EAST);
        return panel;
    }

    public void onCellStateChanged(int cellId, KMapInputCell.CellState newState) {
        minterms.remove(cellId);
        dontCares.remove(cellId);

        if (newState == KMapInputCell.CellState.MINTERM) {
            minterms.add(cellId);
        } else if (newState == KMapInputCell.CellState.DONT_CARE) {
            dontCares.add(cellId);
        }

        updateTextFields();
    }

    private void updateTextFields() {
        if (updatingFields) return;
        updatingFields = true;

        if (mintermsField != null) {
            mintermsField.setText(minterms.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }
        if (dontCaresField != null) {
            dontCaresField.setText(dontCares.stream().map(Object::toString).collect(Collectors.joining(", ")));
        }

        updatingFields = false;
    }

    private void parseAndUpdateFromFields() {
        if (updatingFields) return;
        updatingFields = true;

        try {
            int maxVal = (1 << variables) - 1;
            Set<Integer> newMinterms = parseTermString(mintermsField.getText(), maxVal);
            Set<Integer> newDontCares = parseTermString(dontCaresField.getText(), maxVal);

            newDontCares.removeAll(newMinterms);

            minterms.clear();
            minterms.addAll(newMinterms);
            dontCares.clear();
            dontCares.addAll(newDontCares);

            for (Map.Entry<Integer, KMapInputCell> entry : cellMap.entrySet()) {
                int id = entry.getKey();
                if (minterms.contains(id)) {
                    entry.getValue().setState(KMapInputCell.CellState.MINTERM);
                } else if (dontCares.contains(id)) {
                    entry.getValue().setState(KMapInputCell.CellState.DONT_CARE);
                } else {
                    entry.getValue().setState(KMapInputCell.CellState.EMPTY);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid term input: " + ex.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            updatingFields = false;
            updateTextFields();
        }
    }

    private Set<Integer> parseTermString(String text, int maxVal) {
        Set<Integer> terms = new TreeSet<>();
        if (text == null || text.trim().isEmpty()) return terms;

        String cleaned = text.replaceAll("[^0-9, ]", " ");
        String[] parts = cleaned.split("[,\\s]+");
        for (String p : parts) {
            if (!p.trim().isEmpty()) {
                int val = Integer.parseInt(p.trim());
                if (val >= 0 && val <= maxVal) {
                    terms.add(val);
                }
            }
        }
        return terms;
    }

    private void switchVariables(int newVars) {
        if (this.variables == newVars) return;
        this.variables = newVars;
        int maxVal = (1 << variables) - 1;

        minterms.removeIf(m -> m > maxVal);
        dontCares.removeIf(d -> d > maxVal);

        rebuildMapGrid();
    }

    private void loadPreset(int vars, Set<Integer> m, Set<Integer> d) {
        this.variables = vars;
        if (var2Btn != null) var2Btn.setSelected(vars == 2);
        if (var3Btn != null) var3Btn.setSelected(vars == 3);
        if (var4Btn != null) var4Btn.setSelected(vars == 4);

        minterms.clear();
        minterms.addAll(m);
        dontCares.clear();
        dontCares.addAll(d);

        rebuildMapGrid();
    }

    private void clearGrid() {
        minterms.clear();
        dontCares.clear();
        for (KMapInputCell cell : cellMap.values()) {
            cell.setState(KMapInputCell.CellState.EMPTY);
        }
        updateTextFields();
    }

    private void fillGrid() {
        dontCares.clear();
        minterms.clear();
        int maxVal = 1 << variables;
        for (int i = 0; i < maxVal; i++) {
            minterms.add(i);
        }
        for (KMapInputCell cell : cellMap.values()) {
            cell.setState(KMapInputCell.CellState.MINTERM);
        }
        updateTextFields();
    }

    private void invertGrid() {
        int maxVal = 1 << variables;
        Set<Integer> newMinterms = new TreeSet<>();
        for (int i = 0; i < maxVal; i++) {
            if (!minterms.contains(i) && !dontCares.contains(i)) {
                newMinterms.add(i);
            }
        }
        minterms.clear();
        minterms.addAll(newMinterms);

        for (Map.Entry<Integer, KMapInputCell> entry : cellMap.entrySet()) {
            int id = entry.getKey();
            if (minterms.contains(id)) {
                entry.getValue().setState(KMapInputCell.CellState.MINTERM);
            } else if (dontCares.contains(id)) {
                entry.getValue().setState(KMapInputCell.CellState.DONT_CARE);
            } else {
                entry.getValue().setState(KMapInputCell.CellState.EMPTY);
            }
        }
        updateTextFields();
    }

    private void launchSolution() {
        parseAndUpdateFromFields();
        SwingUtilities.invokeLater(() -> {
            KMapGUI gui = new KMapGUI(variables, minterms, dontCares);
            gui.setVisible(true);
        });
        dispose();
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
}