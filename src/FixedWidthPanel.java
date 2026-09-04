package src;

import javax.swing.*;
import java.awt.*;

class FixedWidthPanel extends JPanel {
    private final int minWidth;
    private final int minHeight;

    public FixedWidthPanel(int minWidth, int minHeight) {
        this.minWidth = minWidth;
        this.minHeight = minHeight;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(minWidth, minHeight);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        return new Dimension(Math.max(d.width, minWidth), Math.max(d.height, minHeight));
    }
}
