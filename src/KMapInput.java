package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;

public class KMapInput extends JFrame {
    private final int variables;
    private final Set<Integer> minterms = new HashSet<>();
    private final Set<Integer> dontCares = new HashSet<>();

    public static void main ( String[] args ) {
        SwingUtilities.invokeLater( ( ) -> {
            KMapInput input = new KMapInput( 4 );
            input.setVisible( true );
        } );
    }

    public KMapInput ( int variables ) {
        this.variables = variables;
        initializeUI( );
    }


    private void initializeUI ( ) {
        setTitle( "Karnaugh Map" );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setLayout( new BorderLayout( ) );

        JPanel mainPanel = new JPanel( new BorderLayout( ) );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

        mainPanel.add( kMapPanel( ), BorderLayout.CENTER );
        mainPanel.add( legendPanel(), BorderLayout.EAST);
        add( mainPanel );
        pack( );
        setLocationRelativeTo( null );
    }

    private JPanel kMapPanel ( ) {
        JPanel mainPanel = new JPanel( new BorderLayout( ) );
        mainPanel.setBorder( BorderFactory.createTitledBorder( "KARNAUGH MAP" ) );

        KMapInputPanel kmapPanel = new KMapInputPanel( 4, 4 ) {
            @Override
            public Insets getInsets ( ) {
                return new Insets( 5, 5, 5, 15 );
            }
        };
        kmapPanel.setLayout( new GridLayout( 0, getColumnCount( ) + 1, 5, 5 ) );

        String[] rowLabels = getRowLabels( );
        String[] colLabels = getColumnLabels( );

        kmapPanel.add( headLabel( "" ) );
        for ( String colLabel : colLabels ) {
            kmapPanel.add( headLabel( colLabel ) );
        }

        for ( int row = 0; row < rowLabels.length; row++ ) {
            kmapPanel.add( headLabel( rowLabels[row] ) );

            for ( int col = 0; col < colLabels.length; col++ ) {
                int decimalValue = getCellValue( row, col );
                KMapInputCell cell = new KMapInputCell(this, decimalValue);
                kmapPanel.addCell( row, col, cell );
            }
        }

        mainPanel.add( kmapPanel, BorderLayout.CENTER );
        return mainPanel;
    }

    private JLabel headLabel ( String text ) {
        JLabel label = new JLabel( text, SwingConstants.CENTER );
        label.setLayout( new OverlayLayout( label ) );
        label.setFont( label.getFont( ).deriveFont( Font.BOLD, 14 ) );
        label.setHorizontalAlignment( SwingConstants.CENTER );
        label.setVerticalAlignment( SwingConstants.CENTER );
        label.setBorder( BorderFactory.createLineBorder( Color.BLACK, 1 ) );
        label.setOpaque( true );
        label.setPreferredSize( new Dimension( 50, 50 ) );
        label.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
        return label;
    }

    private JPanel legendPanel () {
        JPanel legendPanel = new JPanel();
        legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
        legendPanel.setBorder(BorderFactory.createTitledBorder("LEGEND"));

        JPanel legendItems = new JPanel();
        legendItems.setLayout(new GridLayout(2, 1, 5, 5));
        legendItems.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 5));

        JPanel mintermPanel = legendItem(
                new Color(173, 216, 230),
                "Minterm (○)",
                new Color(50, 50, 50)
        );
        legendItems.add(mintermPanel);

        JPanel dontCarePanel = legendItem(
                new Color(255, 255, 150),
                "Don't Care (✕)",
                new Color(50, 50, 50)
        );
        legendItems.add(dontCarePanel);

        legendPanel.add(legendItems);

        JButton btn = new JButton("Solve K-Map");
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(120, 40));
        btn.setPreferredSize(new Dimension(120, 40));

        btn.setBackground(new Color(70, 130, 180));
        btn.setForeground(Color.WHITE);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 100, 150)),
                BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(100, 150, 200));
            }

            @Override
            public void mouseExited( MouseEvent e) {
                btn.setBackground( new Color( 70, 130, 180 ) );
            }
        });

        btn.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                    this,
                    "Confirm K-MAP?",
                    "Confirm Map",
                    JOptionPane.YES_NO_OPTION
            );

            if (option == JOptionPane.YES_OPTION) {
                KMapGUI gui = new KMapGUI( 4, minterms, dontCares );
                gui.setVisible( true );

                Window win = SwingUtilities.getWindowAncestor(btn);
                if (win != null) {
                    win.dispose();
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        buttonPanel.add(btn);

        legendPanel.add(Box.createVerticalGlue());
        legendPanel.add(buttonPanel);

        return legendPanel;
    }

    private JPanel legendItem ( Color color, String text, Color textColor) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setOpaque(false);

        // Color swatch
        JLabel colorLabel = new JLabel("   ");
        colorLabel.setOpaque(true);
        colorLabel.setBackground(color);
        colorLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK),
                BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));

        // Text label
        JLabel textLabel = new JLabel(text);
        textLabel.setForeground(textColor);
        textLabel.setFont(textLabel.getFont().deriveFont(Font.BOLD, 12));

        panel.add(colorLabel);
        panel.add(textLabel);

        return panel;
    }

    private int getColumnCount ( ) {
        if ( variables <= 2 ) return 2;
        else if ( variables == 3 ) return 2;
        else return 4;
    }

    private String[] getRowLabels ( ) {
        return switch (variables) {
            case 2, 3 -> new String[]{"0", "1"};
            case 4 -> new String[]{"00", "01", "11", "10"};
            default -> throw new IllegalArgumentException( "Unsupported number of variables" );
        };
    }

    private String[] getColumnLabels ( ) {
        return switch (variables) {
            case 2, 3 -> new String[]{"0", "1"};
            case 4 -> new String[]{"00", "01", "11", "10"};
            default -> throw new IllegalArgumentException( "Unsupported number of variables" );
        };
    }

    private int getCellValue ( int row, int col ) {
        switch (variables) {
            case 2 -> {
                return (row << 1) | col;
            }
            case 3 -> {
                int[] rowOrder = {0, 1, 3, 2};
                return (rowOrder[row] << 1) | col;
            }
            case 4 -> {
                int[] rowOrder4 = {0, 1, 3, 2};
                int[] colOrder4 = {0, 1, 3, 2};
                return (rowOrder4[row] << 2) | colOrder4[col];
            }
            default -> throw new IllegalArgumentException( "Unsupported number of variables" );
        }
    }

    // Methods to manage the sets
    public void addMinterm(int id) {
        minterms.add(id);
        dontCares.remove(id);
    }

    public void removeMinterm(int id) {
        minterms.remove(id);
    }

    public void addDontCare(int id) {
        dontCares.add(id);
        minterms.remove(id);
    }

    public void removeDontCare(int id) {
        dontCares.remove(id);
    }

    // Inner classes
    static class KMapInputPanel extends JPanel {
        private final KMapInputCell[][] cells;

        public KMapInputPanel ( int rows, int cols ) {
            this.cells = new KMapInputCell[rows][cols];
        }

        public void addCell ( int row, int col, KMapInputCell cell ) {
            cells[row][col] = cell;
            add( cell );
        }

    }
}