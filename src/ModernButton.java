package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ModernButton extends JButton {
    private Color baseColor;
    private Color hoverColor;
    private Color pressedColor;
    private Color textColor;
    private int cornerRadius = 10;

    public ModernButton(String text) {
        this(text, new Color(79, 70, 229), Color.WHITE);
    }

    public ModernButton(String text, Color bg, Color fg) {
        super(text);
        this.baseColor = bg;
        this.textColor = fg;
        this.hoverColor = calculateHover(bg);
        this.pressedColor = calculatePressed(bg);

        setFont(new Font("Segoe UI", Font.BOLD, 12));
        setForeground(textColor);
        setContentAreaFilled(false);
        setOpaque(false);
        setFocusPainted(false);
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        setCursor(new Cursor(Cursor.HAND_CURSOR));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                repaint();
            }
        });
    }

    public void setColors(Color bg, Color fg) {
        this.baseColor = bg;
        this.textColor = fg;
        this.hoverColor = calculateHover(bg);
        this.pressedColor = calculatePressed(bg);
        setForeground(textColor);
        repaint();
    }

    public void setCornerRadius(int radius) {
        this.cornerRadius = radius;
        repaint();
    }

    private Color calculateHover(Color c) {
        int r = Math.min(255, (int)(c.getRed() * 1.15) + 15);
        int g = Math.min(255, (int)(c.getGreen() * 1.15) + 15);
        int b = Math.min(255, (int)(c.getBlue() * 1.15) + 15);
        return new Color(r, g, b);
    }

    private Color calculatePressed(Color c) {
        int r = Math.max(0, (int)(c.getRed() * 0.85));
        int g = Math.max(0, (int)(c.getGreen() * 0.85));
        int b = Math.max(0, (int)(c.getBlue() * 0.85));
        return new Color(r, g, b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Color fill = baseColor;
        if (!isEnabled()) {
            fill = new Color(203, 213, 225);
        } else if (getModel().isPressed()) {
            fill = pressedColor;
        } else if (getModel().isRollover()) {
            fill = hoverColor;
        }

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        Color border = isEnabled() ? pressedColor : new Color(148, 163, 184);
        g2.setColor(border);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        g2.dispose();
        super.paintComponent(g);
    }
}
