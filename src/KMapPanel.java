package src;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.*;
import java.util.List;

public class KMapPanel extends JPanel {
    private final List<GroupBorder> groupBorders = new ArrayList<>();
    private final JLabel[][] cells;
    private final int rows;
    private final int cols;
    private int highlightedGroupIndex = -1;

    public KMapPanel(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new JLabel[rows][cols];
        setOpaque(false);
    }

    public void addCell(int row, int col, JLabel cell) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            cells[row][col] = cell;
        }
        add(cell);
    }

    public void addGroupBorder(GroupBorder border) {
        groupBorders.add(border);
        updateCellBackgrounds();
        repaint();
    }

    public void clearGroupBorders() {
        groupBorders.clear();
        highlightedGroupIndex = -1;
        repaint();
    }

    public void setHighlightedGroupIndex(int index) {
        this.highlightedGroupIndex = index;
        repaint();
    }

    private void updateCellBackgrounds() {
        Colorizer colorizer = new Colorizer();
        for (GroupBorder border : groupBorders) {
            Color groupColor = border.getColor();
            for (Point cellPos : border.cells) {
                if (cellPos.x >= 0 && cellPos.x < rows && cellPos.y >= 0 && cellPos.y < cols) {
                    JLabel cell = cells[cellPos.x][cellPos.y];
                    if (cell != null) {
                        Color tinted = colorizer.blendColors(cell.getBackground(), groupColor, 0.15f);
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
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        for (int i = 0; i < groupBorders.size(); i++) {
            GroupBorder border = groupBorders.get(i);
            boolean isHighlighted = (i == highlightedGroupIndex);
            boolean isDimmed = (highlightedGroupIndex >= 0 && !isHighlighted);

            border.drawDecomposed(g2d, cells, rows, cols, isHighlighted, isDimmed);
        }

        g2d.dispose();
    }

    public List<GroupBorder> getGroupBorders() {
        return Collections.unmodifiableList(this.groupBorders);
    }

    public int getGridRows() {
        return rows;
    }

    public int getGridCols() {
        return cols;
    }
}

class GroupBorder {
    final List<Point> cells;
    final Color color;
    final int thickness;

    public GroupBorder(List<Point> cells, Color color, int thickness) {
        this.cells = new ArrayList<>(cells != null ? cells : Collections.<Point>emptyList());
        this.color = color != null ? color : new Color(70, 130, 180);
        this.thickness = thickness > 0 ? thickness : 3;
    }

    public void drawDecomposed(Graphics2D g2d, JLabel[][] cellsGrid, int totalRows, int totalCols, boolean isHighlighted, boolean isDimmed) {
        if (cellsGrid == null || cells.isEmpty()) return;

        List<Rectangle> subRects = calculateSubRectangles(cellsGrid, totalRows, totalCols);
        if (subRects.isEmpty()) return;

        int fillAlpha = isHighlighted ? 130 : (isDimmed ? 30 : 75);
        int borderAlpha = isHighlighted ? 255 : (isDimmed ? 70 : 220);
        int strokeWidth = isHighlighted ? thickness + 2 : thickness;

        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), fillAlpha);
        Color borderColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), borderAlpha);

        for (Rectangle r : subRects) {
            g2d.setColor(fillColor);
            g2d.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));

            if (isHighlighted) {
                g2d.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 90));
                g2d.setStroke(new BasicStroke(strokeWidth + 4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
            }

            g2d.setColor(borderColor);
            g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.draw(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 16, 16));
        }
    }

    private List<Rectangle> calculateSubRectangles(JLabel[][] cellsGrid, int totalRows, int totalCols) {
        List<Rectangle> rects = new ArrayList<>();
        if (cells.isEmpty()) return rects;

        Set<Integer> rowSet = new TreeSet<>();
        Set<Integer> colSet = new TreeSet<>();
        for (Point p : cells) {
            if (p.x >= 0 && p.x < totalRows && p.y >= 0 && p.y < totalCols) {
                rowSet.add(p.x);
                colSet.add(p.y);
            }
        }
        if (rowSet.isEmpty() || colSet.isEmpty()) return rects;

        List<int[]> rowIntervals = partitionIntervals(rowSet, totalRows);
        List<int[]> colIntervals = partitionIntervals(colSet, totalCols);

        int pad = 3;

        for (int[] rInt : rowIntervals) {
            for (int[] cInt : colIntervals) {
                int rMin = rInt[0];
                int rMax = rInt[1];
                int cMin = cInt[0];
                int cMax = cInt[1];

                JLabel topLeft = cellsGrid[rMin][cMin];
                JLabel bottomRight = cellsGrid[rMax][cMax];

                if (topLeft != null && bottomRight != null) {
                    Rectangle b1 = topLeft.getBounds();
                    Rectangle b2 = bottomRight.getBounds();

                    int x = b1.x - pad;
                    int y = b1.y - pad;
                    int w = (b2.x + b2.width + pad) - x;
                    int h = (b2.y + b2.height + pad) - y;

                    rects.add(new Rectangle(x, y, Math.max(1, w), Math.max(1, h)));
                }
            }
        }

        return rects;
    }

    private List<int[]> partitionIntervals(Set<Integer> indices, int totalSize) {
        List<int[]> intervals = new ArrayList<>();
        if (indices.isEmpty()) return intervals;

        boolean wraps = indices.contains(0) && indices.contains(totalSize - 1) && indices.size() < totalSize;

        if (!wraps) {
            int min = Collections.min(indices);
            int max = Collections.max(indices);
            intervals.add(new int[]{min, max});
        } else {
            int topEnd = 0;
            while (indices.contains(topEnd + 1)) {
                topEnd++;
            }
            int botStart = totalSize - 1;
            while (indices.contains(botStart - 1)) {
                botStart--;
            }

            intervals.add(new int[]{0, topEnd});
            intervals.add(new int[]{botStart, totalSize - 1});
        }

        return intervals;
    }

    public Color getColor() {
        return color;
    }

    public List<Point> getCells() {
        return Collections.unmodifiableList(cells);
    }
}