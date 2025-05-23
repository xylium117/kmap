package src;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A subclass of Colorizer that generates a fixed number of unique, contrasting colors.
 */
public class ColorPalette extends Colorizer {
    private final List<Color> palette;
    private int index = 0;

    public ColorPalette ( int n ) {
        int count = (1 << n) - 1;
        palette = new ArrayList<>( count );

        for ( int i = 0; i < count; i++ ) {
            palette.add( super.getNextColor( ) );
        }
    }
    public Color getNextColor ( ) {
        if ( index < palette.size( ) ) {
            return palette.get( index++ );
        }
        return null;
    }
}
