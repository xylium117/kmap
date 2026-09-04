package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class KMapInputCell extends JLabel {
    private final int id;
    private final KMapInput parent;
    private CellState state = CellState.EMPTY;
    private boolean isHovered = false;

    private static final Color MINTERM_BG = new Color(219, 234, 254);
    private static final Color MINTERM_FG = new Color(30, 64, 175);
    private static final Color DONTCARE_BG = new Color(254, 240, 138);
    private static final Color DONTCARE_FG = new Color(161, 98, 7);
    private static final Color EMPTY_BG = Color.WHITE;
    private static final Color EMPTY_FG = new Color(148, 163, 184);

    public KMapInputCell(KMapInput parent, int id) {
        this.parent = parent;
        this.id = id;
        initializeCell();
        updateAppearance();
    }

    private void initializeCell() {
        setFont(new Font("Segoe UI", Font.BOLD, 18));
        setHorizontalAlignment(SwingConstants.CENTER);
        setVerticalAlignment(SwingConstants.CENTER);
        setBorder(BorderFactory.createLineBorder(new Color(203, 213, 225), 1));
        setOpaque(true);
        setPreferredSize(new Dimension(54, 54));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                toggleState();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isHovered) {
            g2.setColor(new Color(59, 130, 246, 30));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(59, 130, 246));
            g2.setStroke(new BasicStroke(2));
            g2.drawRect(1, 1, getWidth() - 2, getHeight() - 2);
        }

        g2.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        g2.setColor(new Color(100, 116, 139, 200));
        g2.drawString(String.valueOf(id), 5, getHeight() - 5);

        g2.dispose();
    }

    public void toggleState() {
        switch (state) {
            case EMPTY:
                setState(CellState.MINTERM);
                break;
            case MINTERM:
                setState(CellState.DONT_CARE);
                break;
            case DONT_CARE:
                setState(CellState.EMPTY);
                break;
        }
        parent.onCellStateChanged(id, state);
    }

    public void setState(CellState newState) {
        this.state = newState;
        updateAppearance();
        repaint();
    }

    public CellState getState() {
        return state;
    }

    public int getCellId() {
        return id;
    }

    private void updateAppearance() {
        switch (state) {
            case MINTERM:
                setText("1");
                setBackground(MINTERM_BG);
                setForeground(MINTERM_FG);
                break;
            case DONT_CARE:
                setText("X");
                setBackground(DONTCARE_BG);
                setForeground(DONTCARE_FG);
                break;
            default:
                setText("0");
                setBackground(EMPTY_BG);
                setForeground(EMPTY_FG);
                break;
        }
    }

    public enum CellState {
        EMPTY, MINTERM, DONT_CARE
    }
}