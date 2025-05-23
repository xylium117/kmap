package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class KMapInputCell extends JLabel {
    private final int id;
    private final Color mintermColor = new Color( 173, 216, 230 );
    private final Color dontcareColor = new Color( 255, 255, 150 );
    private final Color falseColor = Color.WHITE;
    private final KMapInput parent;
    private CellState state = CellState.EMPTY;

    public KMapInputCell ( KMapInput parent, int id ) {
        this.parent = parent;
        this.id = id;

        initializeCell( );
        updateAppearance( );
    }

    private void initializeCell ( ) {
        setText( " " );
        setLayout( new OverlayLayout( this ) );
        setFont( getFont( ).deriveFont( Font.BOLD, 18 ) );
        setHorizontalAlignment( SwingConstants.CENTER );
        setVerticalAlignment( SwingConstants.CENTER );
        setBorder( BorderFactory.createLineBorder( Color.BLACK, 1 ) );
        setOpaque( true );
        setPreferredSize( new Dimension( 50, 50 ) );
        repaint( );
        addMouseListener( new MouseAdapter( ) {
            @Override
            public void mouseClicked ( MouseEvent e ) {
                toggleState( );
            }
        } );
    }

    @Override
    protected void paintComponent ( Graphics g ) {
        super.paintComponent( g );

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON );

        g2.setFont( getFont( ).deriveFont( Font.ITALIC, 10 ) );
        g2.setColor( new Color( 100, 100, 100 ) );
        g2.drawString( String.valueOf( id ), 5, getHeight( ) - 5 );

        int centerX = getWidth( ) / 2;
        int centerY = getHeight( ) / 2;
        int symbolSize = 20;
        int strokeWidth = 3;

        switch (state) {
            case MINTERM -> {
                g2.setColor( Color.BLACK );
                g2.setStroke( new BasicStroke( strokeWidth ) );
                g2.drawOval( centerX - symbolSize / 2, centerY - symbolSize / 2,
                        symbolSize, symbolSize );
            }

            case DONT_CARE -> {
                g2.setColor( Color.BLACK );
                g2.setStroke( new BasicStroke( strokeWidth ) );
                g2.drawLine( centerX - symbolSize / 2, centerY - symbolSize / 2,
                        centerX + symbolSize / 2, centerY + symbolSize / 2 );
                g2.drawLine( centerX + symbolSize / 2, centerY - symbolSize / 2,
                        centerX - symbolSize / 2, centerY + symbolSize / 2 );
            }
        }
    }

    public void toggleState ( ) {
        switch (state) {
            case EMPTY -> {
                state = CellState.MINTERM;
                parent.addMinterm( id );
            }
            case MINTERM -> {
                state = CellState.DONT_CARE;
                parent.removeMinterm( id );
                parent.addDontCare( id );
            }
            case DONT_CARE -> {
                state = CellState.EMPTY;
                parent.removeDontCare( id );
            }
        }
        repaint( );
        updateAppearance( );
    }

    private void updateAppearance ( ) {
        Color bgColor = switch (state) {
            case MINTERM -> mintermColor;
            case DONT_CARE -> dontcareColor;
            default -> falseColor;
        };
        setBackground( bgColor );
    }

    public enum CellState {
        EMPTY, MINTERM, DONT_CARE
    }
}