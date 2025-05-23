import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class KMapPanel extends JPanel {
    private final List<GroupBorder> groupBorders = new ArrayList<>();
    private final JLabel[][] cells;

    public KMapPanel(int rows, int cols) {
        cells = new JLabel[rows][cols];
        setLayout(new GridLayout(rows, cols));
    }

    public void addCell(int row, int col, JLabel cell) {
        cells[row][col] = cell;
        add(cell);
    }

    public void addGroupBorder(GroupBorder border) {
        groupBorders.add(border);
        updateCellBackgrounds();
        repaint();
    }

    private void updateCellBackgrounds() {
        for (GroupBorder border : groupBorders) {
            Color groupColor = border.getColor();
            for (Point cellPos : border.cells) {
                if (cellPos.x < cells.length && cellPos.y < cells[0].length) {
                    JLabel cell = cells[cellPos.x][cellPos.y];
                    if (cell != null) {
                        Color tinted = new Colorizer().blendColors(cell.getBackground(), groupColor, 0.2f);
                        cell.setBackground(tinted);
                        cell.setOpaque(true);
                    }
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        for (GroupBorder border : groupBorders) {
            Rectangle bounds = border.calculateBounds(cells);

            Color fillColor = new Color(
                    border.color.getRed(),
                    border.color.getGreen(),
                    border.color.getBlue(),
                    80);
            g2d.setColor(fillColor);
            g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15);

            g2d.setStroke(new BasicStroke(7));
            g2d.setColor(new Color(border.color.getRed(),
                    border.color.getGreen(),
                    border.color.getBlue(), 50));
            border.draw(g2d, cells);

            g2d.setStroke(new BasicStroke(5));
            g2d.setColor(border.color);
            border.draw(g2d, cells);
        }
    }

    public List<GroupBorder> getGroupBorders() {
        return this.groupBorders;
    }
}

class GroupBorder {
    final List<Point> cells;
    final Color color;
    final int thickness;

    public GroupBorder(List<Point> cells, Color color, int thickness) {
        this.cells = cells;
        this.color = color;
        this.thickness = thickness;
    }

    public void draw(Graphics2D g2d, JLabel[][] cells) {
        if (cells == null || this.cells.isEmpty()) return;

        Rectangle bounds = calculateBounds(cells);
        g2d.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 15, 15);
    }

    protected Rectangle calculateBounds(JLabel[][] cells) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = 0, maxY = 0;

        for (Point cell : this.cells) {
            if (cell.x < cells.length && cell.y < cells[0].length) {
                Rectangle cellBounds = cells[cell.x][cell.y].getBounds();
                minX = Math.min(minX, cellBounds.x - 2);
                minY = Math.min(minY, cellBounds.y - 2);
                maxX = Math.max(maxX, cellBounds.x + cellBounds.width + 2);
                maxY = Math.max(maxY, cellBounds.y + cellBounds.height + 2);
            }
        }

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    public Color getColor() {
        return color;
    }
}