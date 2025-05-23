package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class KMapGUI extends JFrame {
    private final KMapSolver solver;
    private final int variables;
    private final List<String> primeImplicants;
    private KMapPanel kmapPanel;

    public KMapGUI ( int variables, Set<Integer> minterms, Set<Integer> dontCares ) {
        this.variables = variables;
        this.solver = new KMapSolver( variables, minterms, dontCares );
        this.primeImplicants = solver.getImplicants( );
        initializeUI( );
    }

    private void initializeUI ( ) {
        setTitle( "Karnaugh Map" );
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        setLayout( new BorderLayout( ) );

        JPanel mainPanel = new JPanel( new BorderLayout( ) );
        mainPanel.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );

        mainPanel.add( kMapPanel( ), BorderLayout.CENTER );
        mainPanel.add( resultPanel( ), BorderLayout.SOUTH );

        JPanel gPanel = groupPanel();
        mainPanel.add( gPanel, BorderLayout.EAST );

        JPanel cPanel = controlPanel();
        gPanel.addComponentListener( new ComponentAdapter( ) {
            @Override
            public void componentResized ( ComponentEvent e ) {
                cPanel.setPreferredSize(
                        new Dimension( gPanel.getWidth( ), cPanel.getHeight( ) )
                );
                cPanel.revalidate( );
            }
        } );

        JPanel bottomPanel = new JPanel(new BorderLayout());

        bottomPanel.add(resultPanel(), BorderLayout.CENTER);

        bottomPanel.add(cPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);


        add( mainPanel );
        pack( );
        setLocationRelativeTo( null );
    }

    private JPanel kMapPanel () {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createTitledBorder("KARNAUGH MAP"));

        kmapPanel = new KMapPanel(4, 4) {
            @Override
            public Insets getInsets() {
                return new Insets(5, 5, 5, 15);
            }
        };
        kmapPanel.setLayout(new GridLayout(0, getColumnCount() + 1, 5, 5));

        String[] rowLabels = getRowLabels();
        String[] colLabels = getColumnLabels();

        kmapPanel.add( headLabel(""));
        for (String colLabel : colLabels) {
            kmapPanel.add( headLabel(colLabel));
        }

        for (int row = 0; row < rowLabels.length; row++) {
            kmapPanel.add( headLabel(rowLabels[row]));

            for (int col = 0; col < colLabels.length; col++) {
                int decimalValue = getCellValue(row, col);
                JLabel cell = mapCell( decimalValue );
                kmapPanel.addCell(row, col, cell );
            }
        }

        ColorPalette palette = new ColorPalette(variables);

        for (String implicant : solver.getImplicants()) {
            List<Point> groupCells = implicantToCells(implicant);
            Color groupColor = palette.getNextColor();
            GroupBorder border = new GroupBorder(groupCells, groupColor, 5);
            kmapPanel.addGroupBorder( border );
        }

        mainPanel.add(kmapPanel, BorderLayout.CENTER);
        return mainPanel;
    }

    private JLabel headLabel ( String text) {
        JLabel label = new JLabel( text, SwingConstants.CENTER );
        label.setLayout( new OverlayLayout( label ) );

        label.setFont( label.getFont( ).deriveFont( Font.BOLD, 14 ) );
        label.setHorizontalAlignment( SwingConstants.CENTER );
        label.setVerticalAlignment( SwingConstants.CENTER );

        /* JLabel smallLabel = new JLabel( String.valueOf( text ) ) {
            @Override
            public void setBounds ( int x, int y, int width, int height ) {
                super.setBounds( 4, getParent( ).getHeight( ) - 17, width, height );
            }

            @Override
            public void paint ( Graphics g ) {
                setBounds( 4, getParent( ).getHeight( ) - 17, getWidth( ), getHeight( ) );
                super.paint( g );
            }
        };

        smallLabel.setFont( smallLabel.getFont( ).deriveFont( Font.ITALIC, 12f ) );
        smallLabel.setForeground( new Color( 100, 100, 100, 180 ) );
        smallLabel.setHorizontalAlignment( SwingConstants.LEFT );

        label.add( smallLabel ); **/

        label.setBorder( BorderFactory.createLineBorder( Color.BLACK, 1 ) );
        label.setOpaque( true );
        label.setPreferredSize( new Dimension( 50, 50 ) );
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        return label;
    }

    private JLabel mapCell ( int value ) {
        boolean isOne = solver.getMinterms( ).contains( value ) || solver.getDontCares( ).contains( value );
        String mainValue = isOne ? "1" : "0";

        JLabel cell = new JLabel( mainValue, SwingConstants.CENTER );
        cell.setLayout( new OverlayLayout( cell ) );

        cell.setFont( cell.getFont( ).deriveFont( Font.BOLD, 18 ) );
        cell.setHorizontalAlignment( SwingConstants.CENTER );
        cell.setVerticalAlignment( SwingConstants.CENTER );

        JLabel id = new JLabel( String.valueOf( value ) ) {
            @Override
            public void setBounds ( int x, int y, int width, int height ) {
                super.setBounds( 4, getParent( ).getHeight( ) - 17, width, height );
            }

            @Override
            public void paint ( Graphics g ) {
                setBounds( 4, getParent( ).getHeight( ) - 17, getWidth( ), getHeight( ) );
                super.paint( g );
            }
        };

        id.setFont( id.getFont( ).deriveFont( Font.ITALIC, 12f ) );
        id.setForeground( new Color( 100, 100, 100, 180 ) );
        id.setHorizontalAlignment( SwingConstants.LEFT );

        cell.add( id );

        cell.setBorder( BorderFactory.createLineBorder( Color.BLACK, 1 ) );
        cell.setOpaque( true );
        cell.setPreferredSize( new Dimension( 50, 50 ) );

        if ( solver.getMinterms( ).contains( value ) ) {
            cell.setBackground( new Color( 173, 216, 230 ) );
        } else if ( solver.getDontCares( ).contains( value ) ) {
            cell.setBackground( new Color( 255, 255, 150 ) );
        } else {
            cell.setBackground( Color.WHITE );
        }

        return cell;
    }

    private JPanel groupPanel () {
        int minWidth = 180;
        int minHeight = 280;

        JPanel panel = new FixedWidthPanel(minWidth, minHeight);
        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (panel.getWidth() < minWidth || panel.getHeight() < minHeight) {
                    panel.setSize(
                            Math.max(panel.getWidth(), minWidth),
                            Math.max(panel.getHeight(), minHeight)
                    );
                    panel.revalidate();
                }
            }
        });

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createTitledBorder("GROUPS"));
        panel.setBackground(Color.WHITE);

        List<GroupBorder> groupBorders = kmapPanel.getGroupBorders();

        Map<String, List<Pair<String, Color>>> groupedImplicants = new LinkedHashMap<>();
        groupedImplicants.put("Octet", new ArrayList<>());
        groupedImplicants.put("Quad", new ArrayList<>());
        groupedImplicants.put("Pair", new ArrayList<>());

        for ( int i = 0; i < primeImplicants.size(); i++) {
            String implicant = primeImplicants.get(i);
            Color borderColor = groupBorders.get(i).getColor();
            Set<Integer> minterms = implicantToMinterms(implicant);
            int size = minterms.size();

            if (size >= 8) {
                groupedImplicants.get("Octet").add(new Pair<>(implicant, borderColor));
            } else if (size >= 4) {
                groupedImplicants.get("Quad").add(new Pair<>(implicant, borderColor));
            } else if (size >= 2) {
                groupedImplicants.get("Pair").add(new Pair<>(implicant, borderColor));
            }
        }

        for (Map.Entry<String, List<Pair<String, Color>>> entry : groupedImplicants.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                JLabel header = new JLabel(entry.getKey() + ":");
                header.setFont(header.getFont().deriveFont(Font.BOLD));
                header.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(header);
                panel.add(Box.createRigidArea(new Dimension(0, 5)));

                for (Pair<String, Color> pair : entry.getValue()) {
                    String implicant = pair.key();
                    Color borderColor = pair.value();
                    Set<Integer> minterms = implicantToMinterms(implicant);

                    JLabel label = new JLabel("<html><span style='color:gray; font-size:14pt; font-style:italic;'>"
                            + sortedMintermsString(minterms) +
                            "</span></html>");

                    label.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(borderColor, 3),
                            BorderFactory.createEmptyBorder(5, 10, 5, 10)
                    ));
                    label.setOpaque(true);
                    label.setBackground(Color.WHITE);
                    label.setAlignmentX(Component.LEFT_ALIGNMENT);
                    panel.add(label);
                    panel.add(Box.createRigidArea(new Dimension(0, 5)));
                }
            }
        }

        return panel;
    }



    private JPanel controlPanel () {
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 15, 15));

        JButton iBtn = btn("Input");
        JButton TTBtn = btn("Truth Table");
        JButton lDBtn = btn("Logic Diagram");

        iBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        TTBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        lDBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        iBtn.addActionListener(e -> toInput());
        // TTBtn.addActionListener(e -> showTruthTable());
        // lDBtn.addActionListener(e -> showLogicDiagram());

        controlPanel.add(Box.createVerticalStrut(5));
        controlPanel.add(iBtn);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(TTBtn);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(lDBtn);
        controlPanel.add(Box.createVerticalStrut(5));

        return controlPanel;
    }

    private JButton btn(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getBackground());
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                    g2.dispose();
                }
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground().darker());
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD, 12));
        btn.setBackground(new Color(70, 130, 180));
        btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(100, 150, 200));
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(70, 130, 180));
                btn.repaint();
            }
        });

        return btn;
    }

    private JPanel resultPanel ( ) {
        JPanel panel = new JPanel( new BorderLayout( ) );
        panel.setBorder( BorderFactory.createEmptyBorder( 10, 0, 0, 0 ) );

        JTextArea solutionArea = new JTextArea( );
        solutionArea.setEditable( false );
        solutionArea.setFont( new Font( "Monospaced", Font.PLAIN, 14 ) );
        solutionArea.setText( getSolutionText( ) );

        JScrollPane scrollPane = new JScrollPane( solutionArea );
        panel.add( scrollPane, BorderLayout.CENTER );

        return panel;
    }

    private void toInput(){
        int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure to want to close this window?",
                "Exit",
                JOptionPane.YES_NO_OPTION
        );

        if (option == JOptionPane.YES_OPTION) {
            this.dispose();
        }
        SwingUtilities.invokeLater( ( ) -> {
            KMapInput input = new KMapInput( 4 );
            input.setVisible( true );
        } );
    }

    private String getSolutionText ( ) {
        return "Sum of Products (SOP):\n" + solver.getSOP( ) + "\n\n" +
                "Product of Sums (POS):\n" + solver.getPOS( ) + "\n\n";
    }

    private List<Point> implicantToCells ( String implicant ) {
        char[] vars = {'A', 'B', 'C', 'D'};
        int numVars = vars.length;

        Set<Integer> minterms = new HashSet<>( );
        int mask = 0;
        int pattern = 0;

        for ( int i = 0; i < implicant.length( ); i++ ) {
            char c = implicant.charAt( i );

            if ( c == '0' || c == '1' || c == '-' ) {
                int bitPos = numVars - 1 - i;
                if ( c == '0' || c == '1' ) {
                    mask |= (1 << bitPos);
                    if ( c == '1' ) {
                        pattern |= (1 << bitPos);
                    }
                }
            } else if ( c >= 'A' && c <= 'D' ) {
                int varIndex = c - 'A';
                int bitPos = numVars - 1 - varIndex;
                mask |= (1 << bitPos);

                boolean isComplemented = (i + 1 < implicant.length( ) && implicant.charAt( i + 1 ) == '\'');
                if ( !isComplemented ) {
                    pattern |= (1 << bitPos);
                }
            }
        }

        for ( int minterm = 0; minterm < (1 << numVars); minterm++ ) {
            if ( (minterm & mask) == (pattern & mask) ) {
                minterms.add( minterm );
            }
        }


        List<Point> cells = new ArrayList<>();
        for (int minterm : minterms) {
            cells.add(mintermToCellCoordinate(minterm));
        }

        return cells;
    }

    private Set<Integer> implicantToMinterms ( String implicant ) {
        char[] vars = {'A', 'B', 'C', 'D'};
        int numVars = vars.length;

            Set<Integer> minterms = new HashSet<>( );
            int mask = 0;
            int pattern = 0;

            for ( int i = 0; i < implicant.length( ); i++ ) {
                char c = implicant.charAt( i );

                if ( c == '0' || c == '1' || c == '-' ) {
                    int bitPos = numVars - 1 - i;
                    if ( c == '0' || c == '1' ) {
                        mask |= (1 << bitPos);
                        if ( c == '1' ) {
                            pattern |= (1 << bitPos);
                        }
                    }
                } else if ( c >= 'A' && c <= 'D' ) {
                    int varIndex = c - 'A';
                    int bitPos = numVars - 1 - varIndex;
                    mask |= (1 << bitPos);

                    boolean isComplemented = (i + 1 < implicant.length( ) && implicant.charAt( i + 1 ) == '\'');
                    if ( !isComplemented ) {
                        pattern |= (1 << bitPos);
                    }
                }
            }

            for ( int minterm = 0; minterm < (1 << numVars); minterm++ ) {
                if ( (minterm & mask) == (pattern & mask) ) {
                    minterms.add( minterm );
                }
            }


        return minterms;
    }

    private String sortedMintermsString ( Set<Integer> minterms ) {
        return minterms.stream( )
                .sorted( )
                .map( Object::toString )
                .collect( Collectors.joining( ", ", "", "" ) );
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

    private Point mintermToCellCoordinate(int minterm) {
        // Standard 4-variable K-map layout:
        // AB\CD 00 01 11 10
        //   00  0  1  3  2
        //   01  4  5  7  6
        //   11 12 13 15 14
        //   10  8  9 11 10

        int[][] kmapLayout = {
                {0, 1, 3, 2},
                {4, 5, 7, 6},
                {12, 13, 15, 14},
                {8, 9, 11, 10}
        };

        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                if (kmapLayout[row][col] == minterm) {
                    return new Point(row, col);
                }
            }
        }
        return new Point(-1, -1);
    }

}

record Pair<K, V>(K key, V value) {
}