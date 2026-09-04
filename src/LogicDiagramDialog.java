package src;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LogicDiagramDialog extends JDialog {
    private final int vars;
    private final KMapSolver solver;

    public enum ExpressionForm {
        SOP, POS
    }

    public enum GateFamily {
        BASIC, ONLY_NAND, ONLY_NOR
    }

    private ExpressionForm currentForm = ExpressionForm.SOP;
    private GateFamily currentFamily = GateFamily.BASIC;

    private JLabel formulaLabel;
    private JLabel convertedFormulaLabel;
    private JLabel statsLabel;
    private CircuitPanel circuitPanel;

    public LogicDiagramDialog(Frame parent, KMapSolver solver) {
        super(parent, "Logic Circuit Diagram", true);
        this.solver = solver;
        this.vars = solver.getVariables();
        initializeUI();
    }

    private void initializeUI() {
        setSize(980, 700);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(248, 250, 252));

        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(new Color(15, 23, 42));
        topContainer.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setOpaque(false);

        JLabel titleLabel = new JLabel("Logic Circuit Implementation");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 19));
        titleLabel.setForeground(Color.WHITE);
        titleRow.add(titleLabel, BorderLayout.WEST);

        topContainer.add(titleRow, BorderLayout.NORTH);

        JPanel optionsBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 6));
        optionsBar.setOpaque(false);

        JPanel formBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        formBox.setOpaque(false);
        JLabel formLbl = new JLabel("Form:");
        formLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        formLbl.setForeground(new Color(226, 232, 240));

        JRadioButton sopRadio = new JRadioButton("SOP (Sum of Products)", true);
        JRadioButton posRadio = new JRadioButton("POS (Product of Sums)", false);
        styleRadio(sopRadio);
        styleRadio(posRadio);

        ButtonGroup formGroup = new ButtonGroup();
        formGroup.add(sopRadio);
        formGroup.add(posRadio);

        formBox.add(formLbl);
        formBox.add(sopRadio);
        formBox.add(posRadio);

        JPanel gateBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        gateBox.setOpaque(false);
        JLabel gateLbl = new JLabel("Gate Logic:");
        gateLbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        gateLbl.setForeground(new Color(226, 232, 240));

        JRadioButton basicRadio = new JRadioButton("Basic (AND, OR, NOT)", true);
        JRadioButton nandRadio = new JRadioButton("Universal (Only NAND)", false);
        JRadioButton norRadio = new JRadioButton("Universal (Only NOR)", false);
        styleRadio(basicRadio);
        styleRadio(nandRadio);
        styleRadio(norRadio);

        ButtonGroup gateGroup = new ButtonGroup();
        gateGroup.add(basicRadio);
        gateGroup.add(nandRadio);
        gateGroup.add(norRadio);

        gateBox.add(gateLbl);
        gateBox.add(basicRadio);
        gateBox.add(nandRadio);
        gateBox.add(norRadio);

        ItemListener updateListener = new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    currentForm = sopRadio.isSelected() ? ExpressionForm.SOP : ExpressionForm.POS;
                    if (basicRadio.isSelected())
                        currentFamily = GateFamily.BASIC;
                    else if (nandRadio.isSelected())
                        currentFamily = GateFamily.ONLY_NAND;
                    else if (norRadio.isSelected())
                        currentFamily = GateFamily.ONLY_NOR;

                    refreshCircuit();
                }
            }
        };

        sopRadio.addItemListener(updateListener);
        posRadio.addItemListener(updateListener);
        basicRadio.addItemListener(updateListener);
        nandRadio.addItemListener(updateListener);
        norRadio.addItemListener(updateListener);

        optionsBar.add(formBox);
        optionsBar.add(new JSeparator(SwingConstants.VERTICAL));
        optionsBar.add(gateBox);
        topContainer.add(optionsBar, BorderLayout.CENTER);

        JPanel banner = new JPanel(new GridLayout(3, 1, 2, 2));
        banner.setOpaque(false);

        formulaLabel = new JLabel("Original Expression: ");
        formulaLabel.setFont(new Font("Consolas", Font.BOLD, 14));
        formulaLabel.setForeground(new Color(56, 189, 248));

        convertedFormulaLabel = new JLabel("Gate Transformed: ");
        convertedFormulaLabel.setFont(new Font("Consolas", Font.BOLD, 13));
        convertedFormulaLabel.setForeground(new Color(167, 243, 208));

        statsLabel = new JLabel("Circuit Complexity: ");
        statsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statsLabel.setForeground(new Color(203, 213, 225));

        banner.add(formulaLabel);
        banner.add(convertedFormulaLabel);
        banner.add(statsLabel);
        topContainer.add(banner, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        circuitPanel = new CircuitPanel();
        JScrollPane scrollPane = new JScrollPane(circuitPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(new Color(241, 245, 249));

        ModernButton copyBtn = new ModernButton("Copy Expression", new Color(59, 130, 246), Color.WHITE);
        ModernButton closeBtn = new ModernButton("Close", new Color(100, 116, 139), Color.WHITE);

        copyBtn.addActionListener(e -> {
            String txt = convertedFormulaLabel.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(txt), null);
            JOptionPane.showMessageDialog(this, "Expression copied to clipboard!", "Copied",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        closeBtn.addActionListener(e -> dispose());

        bottomBar.add(copyBtn);
        bottomBar.add(closeBtn);
        add(bottomBar, BorderLayout.SOUTH);

        refreshCircuit();
    }

    private void styleRadio(JRadioButton radio) {
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        radio.setForeground(Color.WHITE);
        radio.setOpaque(false);
        radio.setFocusPainted(false);
    }

    private void refreshCircuit() {
        String origSop = solver.getSOP();
        String origPos = solver.getPOS();

        List<String> activeTerms = (currentForm == ExpressionForm.SOP) ? solver.getImplicants()
                : solver.getPOSImplicants();
        String baseExpr = (currentForm == ExpressionForm.SOP) ? origSop : origPos;

        formulaLabel.setText("Selected Expression (" + currentForm + "): F = " + baseExpr);

        String transformed = buildTransformedExpression(baseExpr, activeTerms, currentForm, currentFamily);
        convertedFormulaLabel.setText("Logic Implementation (" + currentFamily + "): " + transformed);

        int totalGates = calculateGateCount(activeTerms, currentForm, currentFamily);
        statsLabel.setText("Gate Count: " + totalGates + " gates | Level 1: " + activeTerms.size()
                + " gates | Level 2: " + (activeTerms.size() > 1 ? "1 gate" : "0 gates"));

        circuitPanel.updateCircuit(vars, activeTerms, currentForm, currentFamily, baseExpr);
    }

    private String buildTransformedExpression(String base, List<String> terms, ExpressionForm form, GateFamily family) {
        if (base.equals("0") || base.equals("1"))
            return "F = " + base;
        if (terms.isEmpty())
            return "F = 0";

        if (family == GateFamily.BASIC) {
            return "F = " + base;
        } else if (family == GateFamily.ONLY_NAND) {
            if (form == ExpressionForm.SOP) {
                List<String> nandTerms = new ArrayList<>();
                for (String t : terms) {
                    nandTerms.add("(" + solver.toAlgebraic(t) + ")'");
                }
                return "F = (" + String.join(" · ", nandTerms) + ")'";
            } else {
                return "F = [NAND-NAND implementation of (" + base + ")]";
            }
        } else {
            if (form == ExpressionForm.POS) {
                List<String> norTerms = new ArrayList<>();
                for (String t : terms) {
                    norTerms.add("(" + solver.toPOSAlgebraic(t) + ")'");
                }
                return "F = (" + String.join(" + ", norTerms) + ")'";
            } else {
                return "F = [NOR-NOR implementation of (" + base + ")]";
            }
        }
    }

    private int calculateGateCount(List<String> terms, ExpressionForm form, GateFamily family) {
        if (terms.isEmpty())
            return 0;
        int inverters = 0;
        for (String t : terms) {
            for (char c : t.toCharArray()) {
                if (c == '0' || c == '1')
                    inverters++;
            }
        }
        int level1Gates = terms.size();
        int level2Gates = (terms.size() > 1) ? 1 : 0;
        return inverters + level1Gates + level2Gates;
    }

    static class CircuitPanel extends JPanel {
        private int vars;
        private List<String> terms = new ArrayList<>();
        private ExpressionForm form = ExpressionForm.SOP;
        private GateFamily family = GateFamily.BASIC;
        private String rawExpr = "";

        public CircuitPanel() {
            setBackground(Color.WHITE);
        }

        public void updateCircuit(int vars, List<String> terms, ExpressionForm form, GateFamily family,
                String rawExpr) {
            this.vars = vars;
            this.terms = terms != null ? new ArrayList<>(terms) : new ArrayList<String>();
            this.form = form;
            this.family = family;
            this.rawExpr = rawExpr;

            int requiredHeight = Math.max(500, 150 + this.terms.size() * 90);
            setPreferredSize(new Dimension(940, requiredHeight));
            revalidate();
            repaint();
        }

        private static String dashString(int count) {
            char[] chars = new char[count];
            Arrays.fill(chars, '-');
            return new String(chars);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();

            g2.setColor(new Color(248, 250, 252));
            for (int x = 0; x < w; x += 20)
                g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 20)
                g2.drawLine(0, y, w, y);

            if (rawExpr.equals("0") || terms.isEmpty()) {
                drawConstant(g2, "0 (GND / Logic Low)", new Color(239, 68, 68));
                g2.dispose();
                return;
            }
            if (rawExpr.equals("1") || (terms.size() == 1 && terms.get(0).equals(dashString(vars)))) {
                drawConstant(g2, "1 (VCC / Logic High)", new Color(16, 185, 129));
                g2.dispose();
                return;
            }

            int startX = 75;
            int railSpacing = 34;
            int totalRails = vars * 2;
            int railsEndX = startX + (totalRails - 1) * railSpacing;

            int level1GateX = railsEndX + 95;
            int gateWidth = 56;
            int gateHeight = 42;

            int numTerms = terms.size();
            int startY = 70;
            int termSpacing = Math.max(75, (h - 150) / Math.max(1, numTerms));

            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            char varChar = 'A';

            for (int v = 0; v < vars; v++) {
                int normX = startX + (v * 2) * railSpacing;
                int invX = startX + (v * 2 + 1) * railSpacing;
                String varName = String.valueOf((char) (varChar + v));

                g2.setColor(new Color(30, 41, 59));
                g2.drawString(varName, normX - 5, startY - 34);

                g2.setColor(new Color(100, 116, 139));
                g2.setStroke(new BasicStroke(2));
                g2.drawLine(normX, startY - 24, normX, h - 30);

                g2.drawLine(normX, startY - 24, invX, startY - 24);
                drawConnectionDot(g2, normX, startY - 24);

                drawInverterGate(g2, invX, startY - 24);

                g2.setColor(new Color(148, 163, 184));
                g2.drawLine(invX, startY + 12, invX, h - 30);
            }

            List<Point> level1Outputs = new ArrayList<>();

            for (int i = 0; i < numTerms; i++) {
                String term = terms.get(i);
                int gateY = startY + 34 + i * termSpacing;

                List<Integer> connectedRailsX = new ArrayList<>();
                for (int v = 0; v < vars; v++) {
                    if (v < term.length()) {
                        char c = term.charAt(v);
                        if (form == ExpressionForm.SOP) {
                            if (c == '1')
                                connectedRailsX.add(startX + (v * 2) * railSpacing);
                            else if (c == '0')
                                connectedRailsX.add(startX + (v * 2 + 1) * railSpacing);
                        } else {
                            if (c == '0')
                                connectedRailsX.add(startX + (v * 2) * railSpacing);
                            else if (c == '1')
                                connectedRailsX.add(startX + (v * 2 + 1) * railSpacing);
                        }
                    }
                }

                if (connectedRailsX.size() >= 1) {
                    boolean isLevel1Nand = (family == GateFamily.ONLY_NAND);
                    boolean isLevel1Nor = (family == GateFamily.ONLY_NOR);
                    boolean isOrLike = (family == GateFamily.ONLY_NOR)
                            || (family == GateFamily.BASIC && form == ExpressionForm.POS);

                    int inCount = connectedRailsX.size();

                    for (int k = 0; k < inCount; k++) {
                        int rX = connectedRailsX.get(k);
                        int inY = gateY - gateHeight / 2 + 8 + (k * (gateHeight - 16)) / Math.max(1, inCount - 1);

                        g2.setColor(new Color(71, 85, 105));
                        g2.setStroke(new BasicStroke(2));
                        int enterX = isOrLike ? level1GateX + 6 : level1GateX + 3;
                        g2.drawLine(rX, inY, enterX, inY);
                        drawConnectionDot(g2, rX, inY);
                    }

                    drawLevel1Gate(g2, level1GateX, gateY, gateWidth, gateHeight, form, family);

                    int bubbleOffset = (isLevel1Nand || isLevel1Nor) ? 8 : 0;
                    int gateTipX = level1GateX + gateWidth + bubbleOffset;
                    int outPinEndX = gateTipX + 22;

                    g2.setColor(new Color(30, 41, 59));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(level1GateX + gateWidth - 2, gateY, outPinEndX, gateY);

                    level1Outputs.add(new Point(outPinEndX, gateY));
                }
            }

            if (level1Outputs.size() > 1) {
                int level2GateX = level1GateX + gateWidth + 130;
                int level2GateY = (level1Outputs.get(0).y + level1Outputs.get(level1Outputs.size() - 1).y) / 2;
                int level2Width = 62;
                int level2Height = Math.max(54, Math.min(240, level1Outputs.size() * 28));

                boolean isLevel2Nand = (family == GateFamily.ONLY_NAND);
                boolean isLevel2Nor = (family == GateFamily.ONLY_NOR);
                boolean isLevel2OrLike = (family == GateFamily.ONLY_NOR)
                        || (family == GateFamily.BASIC && form == ExpressionForm.SOP);

                int busX = level2GateX - 45;

                for (int k = 0; k < level1Outputs.size(); k++) {
                    Point p = level1Outputs.get(k);
                    int inY = level2GateY - level2Height / 2 + 10
                            + (k * (level2Height - 20)) / (level1Outputs.size() - 1);

                    g2.setColor(new Color(71, 85, 105));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawLine(p.x, p.y, busX, p.y);
                    g2.drawLine(busX, p.y, busX, inY);

                    int enterX = isLevel2OrLike ? level2GateX + 6 : level2GateX + 3;
                    g2.drawLine(busX, inY, enterX, inY);
                }

                drawLevel2Gate(g2, level2GateX, level2GateY, level2Width, level2Height, form, family);

                int level2BubbleOffset = (isLevel2Nand || isLevel2Nor) ? 8 : 0;
                int l2TipX = level2GateX + level2Width + level2BubbleOffset;
                int finalOutX = l2TipX + 55;

                g2.setColor(new Color(16, 185, 129));
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(level2GateX + level2Width - 2, level2GateY, finalOutX, level2GateY);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("F", finalOutX + 8, level2GateY + 6);
            } else if (level1Outputs.size() == 1) {
                Point p = level1Outputs.get(0);
                int finalOutX = p.x + 85;

                g2.setColor(new Color(16, 185, 129));
                g2.setStroke(new BasicStroke(3));
                g2.drawLine(p.x, p.y, finalOutX, p.y);

                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                g2.drawString("F", finalOutX + 8, p.y + 6);
            }

            g2.dispose();
        }

        private void drawConstant(Graphics2D g2, String text, Color c) {
            g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
            g2.setColor(c);
            g2.drawString("Output: F = " + text, 100, 160);
        }

        private void drawConnectionDot(Graphics2D g2, int x, int y) {
            g2.setColor(new Color(30, 41, 59));
            g2.fillOval(x - 4, y - 4, 8, 8);
        }

        private void drawInverterGate(Graphics2D g2, int x, int bendY) {
            int w = 14;
            int h = 16;
            int triTopY = bendY + 12;

            g2.setColor(new Color(71, 85, 105));
            g2.setStroke(new BasicStroke(2));

            g2.drawLine(x, bendY, x, triTopY);

            Path2D triangle = new Path2D.Double();
            triangle.moveTo(x - w / 2.0, triTopY);
            triangle.lineTo(x + w / 2.0, triTopY);
            triangle.lineTo(x, triTopY + h);
            triangle.closePath();

            g2.setColor(new Color(241, 245, 249));
            g2.fill(triangle);
            g2.setColor(new Color(71, 85, 105));
            g2.draw(triangle);

            g2.setColor(Color.WHITE);
            g2.fillOval(x - 3, triTopY + h, 6, 6);
            g2.setColor(new Color(71, 85, 105));
            g2.drawOval(x - 3, triTopY + h, 6, 6);

            g2.drawLine(x, triTopY + h + 6, x, triTopY + h + 12);
        }

        private void drawLevel1Gate(Graphics2D g2, int x, int y, int w, int h, ExpressionForm form, GateFamily fam) {
            if (fam == GateFamily.ONLY_NAND || (fam == GateFamily.BASIC && form == ExpressionForm.SOP)) {
                boolean isNand = (fam == GateFamily.ONLY_NAND);
                drawAndLikeGate(g2, x, y, w, h, isNand);
            } else {
                boolean isNor = (fam == GateFamily.ONLY_NOR);
                drawOrLikeGate(g2, x, y, w, h, isNor);
            }
        }

        private void drawLevel2Gate(Graphics2D g2, int x, int y, int w, int h, ExpressionForm form, GateFamily fam) {
            if (fam == GateFamily.ONLY_NAND || (fam == GateFamily.BASIC && form == ExpressionForm.POS)) {
                boolean isNand = (fam == GateFamily.ONLY_NAND);
                drawAndLikeGate(g2, x, y, w, h, isNand);
            } else {
                boolean isNor = (fam == GateFamily.ONLY_NOR);
                drawOrLikeGate(g2, x, y, w, h, isNor);
            }
        }

        private void drawAndLikeGate(Graphics2D g2, int x, int y, int w, int h, boolean inverted) {
            Path2D path = new Path2D.Double();
            path.moveTo(x, y - h / 2.0);
            path.lineTo(x + w * 0.45, y - h / 2.0);
            path.curveTo(x + w * 0.85, y - h / 2.0, x + w, y - h * 0.25, x + w, y);
            path.curveTo(x + w, y + h * 0.25, x + w * 0.85, y + h / 2.0, x + w * 0.45, y + h / 2.0);
            path.lineTo(x, y + h / 2.0);
            path.closePath();

            g2.setColor(new Color(241, 245, 249));
            g2.fill(path);
            g2.setColor(new Color(30, 41, 59));
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(path);

            if (inverted) {
                g2.setColor(Color.WHITE);
                g2.fillOval(x + w, y - 4, 8, 8);
                g2.setColor(new Color(30, 41, 59));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(x + w, y - 4, 8, 8);
            }
        }

        private void drawOrLikeGate(Graphics2D g2, int x, int y, int w, int h, boolean inverted) {
            Path2D path = new Path2D.Double();
            path.moveTo(x, y - h / 2.0);
            path.quadTo(x + w * 0.25, y, x, y + h / 2.0);
            path.quadTo(x + w * 0.65, y + h / 2.0, x + w, y);
            path.quadTo(x + w * 0.65, y - h / 2.0, x, y - h / 2.0);
            path.closePath();

            g2.setColor(new Color(241, 245, 249));
            g2.fill(path);
            g2.setColor(new Color(30, 41, 59));
            g2.setStroke(new BasicStroke(2.2f));
            g2.draw(path);

            if (inverted) {
                g2.setColor(Color.WHITE);
                g2.fillOval(x + w, y - 4, 8, 8);
                g2.setColor(new Color(30, 41, 59));
                g2.setStroke(new BasicStroke(2));
                g2.drawOval(x + w, y - 4, 8, 8);
            }
        }
    }
}
