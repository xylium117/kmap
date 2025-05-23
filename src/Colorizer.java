package src;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * Generates visually distinct, contrasting colors using CIEDE2000 color difference metric.
 */
public class Colorizer {
    // Constants
    private static final float MIN_CONTRAST = 30f; // Minimum perceptible difference
    private static final double PI = Math.PI;
    private static final double TWO_PI = 2 * PI;
    private static final double DEG_TO_RAD = PI / 180;

    // Predefined high-contrast color palette
    private static final List<Color> BASE_PALETTE = List.of(
            new Color( 230, 25, 75 ),   // Red
            new Color( 60, 180, 75 ),   // Green
            new Color( 0, 130, 200 ),   // Blue
            new Color( 245, 130, 48 ),  // Orange
            new Color( 145, 30, 180 ),  // Purple
            new Color( 70, 240, 240 ),  // Cyan
            new Color( 240, 50, 230 ),  // Magenta
            new Color( 210, 245, 60 )   // Lime
    );

    // Instance variables
    private final List<Color> colors = new ArrayList<>( BASE_PALETTE );
    private final Random random = new Random( );

    private int colorIndex = 0;

    /**
     * Gets the next contrasting color in sequence.
     *
     * @return A new color that contrasts with all previously generated colors
     */
    public Color getNextColor ( ) {
        if ( colorIndex < BASE_PALETTE.size( ) ) {
            return BASE_PALETTE.get( colorIndex++ );
        }

        // Generate new colors
        Color newColor;
        int attempts = 0;
        float hueStep = 0.1618f; // Golden ratio conjugate
        do {
            // Predictable Hue selection
            float hue = (colorIndex * hueStep) % 1.0f;
            float saturation = 0.8f + random.nextFloat( ) * 0.3f;
            float brightness = 0.8f + random.nextFloat( ) * 0.2f;
            newColor = Color.getHSBColor( hue, saturation, brightness );

            attempts++;

            if ( attempts > 20 ) { // After 20 tries, approach with aggression
                if ( attempts > 40 ) {
                    newColor = getComplement( );
                } else {
                    newColor = fixColorMap( );
                }
            }
        } while ( (!hasContrast( newColor ) || isDuplicate( newColor ) || isPale( newColor )) && attempts < 100 );

        colors.add( newColor );
        colorIndex++;
        return newColor;
    }

    private Color fixColorMap ( ) {
        Color base = colors.get( random.nextInt( colors.size( ) ) );
        float[] hsb = Color.RGBtoHSB( base.getRed( ), base.getGreen( ), base.getBlue( ), null );

        return Color.getHSBColor(
                (hsb[0] + 0.25f + random.nextFloat( ) * 0.5f) % 1.0f, // Large hue change
                Math.min( 1.0f, hsb[1] * (1.2f + random.nextFloat( ) * 0.3f) ), // Boost saturation
                Math.min( 1.0f, hsb[2] * (1.1f + random.nextFloat( ) * 0.2f) )  // Slightly brighter
        );
    }

    private boolean isDuplicate ( Color color ) {
        for ( Color existing : colors ) {
            if ( ciede2000( existing, color ) < 5.0 ) { // Small threshold for duplicates
                return true;
            }
        }
        return false;
    }

    private boolean isPale ( Color color ) {
        int r = color.getRed( );
        int g = color.getGreen( );
        int b = color.getBlue( );

        return (r > 220 && g > 220 && b > 220) ||
                ((r + g + b) / 3.0 > 230 && Math.abs( r - g ) < 15 && Math.abs( g - b ) < 15);
    }

    private boolean hasContrast ( Color newColor ) {
        for ( Color existing : colors ) {
            if ( ciede2000( existing, newColor ) < MIN_CONTRAST ) {
                return false;
            }
        }
        return true;
    }

    private double ciede2000 ( Color c1, Color c2 ) {
        float[] l1 = rgbToLab( c1 );
        float[] l2 = rgbToLab( c2 );
        return ciede2000( l1[0], l1[1], l1[2], l2[0], l2[1], l2[2] );
    }

    private double ciede2000 ( double L1, double a1, double b1,
                               double L2, double a2, double b2 ) {
        // Weighting factors
        double kl = 1, kc = 1, kh = 1;

        // Compute Chroma
        double C1 = Math.sqrt( a1 * a1 + b1 * b1 );
        double C2 = Math.sqrt( a2 * a2 + b2 * b2 );
        double Cav = (C1 + C2) / 2;

        double G = 0.5 * 1 - 0.5 * Math.sqrt( Math.pow( Cav, 7 ) / (Math.pow( Cav, 7 ) + Math.pow( 25, 7 )) );
        double a1_ = (1 + G) * a1;
        double a2_ = (1 + G) * a2;

        // Compute compensated Chroma
        double C1_ = Math.sqrt( a1_ * a1_ + b1 * b1 );
        double C2_ = Math.sqrt( a2_ * a2_ + b2 * b2 );

        // Compute Hue angle
        double h1_ = (Math.atan2( b1, a1_ ) + TWO_PI) % TWO_PI;
        double h2_ = (Math.atan2( b2, a2_ ) + TWO_PI) % TWO_PI;

        double delL_ = L2 - L1;
        double delC_ = C2_ - C1_;

        // Compute Hue variance
        double delH_;
        if ( C1_ * C2_ == 0 ) delH_ = 0;
        else delH_ = (Math.abs( h1_ - h2_ ) <= PI) ? h2_ - h1_ :
                (h2_ <= h1_) ? h2_ - h1_ + TWO_PI : h2_ - h1_ - TWO_PI;

        double dH_ = 2 * Math.sqrt( C1_ * C2_ ) * Math.sin( delH_ / 2 );

        double Lav_ = (L1 + L2) / 2;
        double Cav_ = (C1_ + C2_) / 2;

        // Compute average Hue
        double Hav_;
        if ( C1_ * C2_ == 0 ) Hav_ = 0;
        else Hav_ = (Math.abs( h1_ - h2_ ) <= PI) ? (h1_ + h2_) / 2 :
                (h1_ + h2_ < TWO_PI) ? (h1_ + h2_ + TWO_PI) / 2 : (h1_ + h2_ - TWO_PI) / 2;

        // Tuning function
        double T = 1 - 0.17 * Math.cos( Hav_ - 30 * DEG_TO_RAD ) + 0.24 * Math.cos( 2 * Hav_ ) +
                0.32 * Math.cos( 3 * Hav_ + 6 * DEG_TO_RAD ) - 0.20 * Math.cos( 4 * Hav_ - 63 * DEG_TO_RAD );

        double dt = 30 * Math.exp( -Math.pow( (Hav_ - 275) / 25, 2 ) ) * DEG_TO_RAD;

        // Apply scaling weights
        double R_C = 2 * Math.sqrt( Math.pow( Cav_, 7 ) / (Math.pow( Cav_, 7 ) + Math.pow( 25, 7 )) );
        double S_L = 1 + (0.015 * Math.pow( Lav_ - 50, 2 )) / Math.sqrt( 20 + Math.pow( Lav_ - 50, 2 ) );
        double S_C = 1 + 0.045 * Cav_;
        double S_H = 1 + 0.015 * Cav_ * T;

        double R_T = -Math.sin( 2 * dt ) * R_C;

        // Final ΔE00
        return Math.sqrt(
                Math.pow( delL_ / (kl * S_L), 2 ) +
                        Math.pow( delC_ / (kc * S_C), 2 ) +
                        Math.pow( dH_ / (kh * S_H), 2 ) +
                        R_T * (delC_ / (kc * S_C)) * (dH_ / (kh * S_H))
        );
    }

    /**
     * Converts an RGB color to CIE Lab color space.
     *
     * @param c RGB {@link Color}
     * @return LAB components as float array [L, a, b]
     */
    private float[] rgbToLab ( Color c ) {
        // Convert sRGB to linear RGB
        double r = c.getRed( ) / 255.0;
        double g = c.getGreen( ) / 255.0;
        double b = c.getBlue( ) / 255.0;

        r = r > 0.04045 ? Math.pow( (r + 0.055) / 1.055, 2.4 ) : r / 12.92;
        g = g > 0.04045 ? Math.pow( (g + 0.055) / 1.055, 2.4 ) : g / 12.92;
        b = b > 0.04045 ? Math.pow( (b + 0.055) / 1.055, 2.4 ) : b / 12.92;

        // Linear RGB to XYZ
        double x = r * 0.4124 + g * 0.3576 + b * 0.1805;
        double y = r * 0.2126 + g * 0.7152 + b * 0.0722;
        double z = r * 0.0193 + g * 0.1192 + b * 0.9505;

        // D65 illuminant
        x /= 0.95047;
        y /= 1.00000;
        z /= 1.08883;

        // XYZ to Lab
        x = x > 0.008856 ? Math.pow( x, 1.0 / 3 ) : (7.787 * x) + (16.0 / 116);
        y = y > 0.008856 ? Math.pow( y, 1.0 / 3 ) : (7.787 * y) + (16.0 / 116);
        z = z > 0.008856 ? Math.pow( z, 1.0 / 3 ) : (7.787 * z) + (16.0 / 116);

        return new float[]{
                (float)((116 * y) - 16),
                (float)(500 * (x - y)),
                (float)(200 * (y - z))
        };
    }

    private Color labToRgb ( float l, float a, float b ) {
        // Convert LAB to XYZ
        double y = (l + 16.0) / 116.0;
        double x = y + (a / 500.0);
        double z = y - (b / 200.0);

        // Apply reverse nonlinearity
        x = (x > 0.206897) ? Math.pow( x, 3 ) : (x - 16.0 / 116.0) / 7.787;
        y = (y > 0.206897) ? Math.pow( y, 3 ) : (y - 16.0 / 116.0) / 7.787;
        z = (z > 0.206897) ? Math.pow( z, 3 ) : (z - 16.0 / 116.0) / 7.787;

        // D65 illuminant
        x *= 0.95047;
        y *= 1.00000;
        z *= 1.08883;

        // Convert XYZ to RGB
        double r = x * 3.2406 + y * -1.5372 + z * -0.4986;
        double g = x * -0.9689 + y * 1.8758 + z * 0.0415;
        double blue = x * 0.0557 + y * -0.2040 + z * 1.0570;

        // Gamma correction
        r = (r > 0.0031308) ? 1.055 * Math.pow( r, 1.0 / 2.4 ) - 0.055 : 12.92 * r;
        g = (g > 0.0031308) ? 1.055 * Math.pow( g, 1.0 / 2.4 ) - 0.055 : 12.92 * g;
        blue = (blue > 0.0031308) ? 1.055 * Math.pow( blue, 1.0 / 2.4 ) - 0.055 : 12.92 * blue;

        // Clamp and create Color
        return new Color(
                clamp( (float)r, 0.0F, 1.0F ),
                clamp( (float)g, 0.0F, 1.0F ),
                clamp( (float)blue, 0.0F, 1.0F )
        );
    }

    private float clamp ( float value, float min, float max ) {
        return Math.max( min, Math.min( max, value ) );
    }


    /**
     * Generates a complementary color when normal generation fails.
     */
    private Color getComplement ( ) {
        Color mostSimilar = getCloseMatch( colors.get( 0 ) );
        float[] lab = rgbToLab( mostSimilar );
        // Create complementary color in LAB space
        return labToRgb(
                clamp( 100 - lab[0], 30, 90 ),  // Opposite lightness
                clamp( -lab[1], -100, 100 ),     // Opposite a*
                clamp( -lab[2], -100, 100 )      // Opposite b*
        );
    }

    /**
     * Finds the most similar color to the reference in the palette.
     */
    private Color getCloseMatch ( Color reference ) {
        Color mostSimilar = colors.get( 0 );
        double minDifference = Double.MAX_VALUE;

        for ( Color color : colors ) {
            double difference = ciede2000( reference, color );
            if ( difference < minDifference ) {
                minDifference = difference;
                mostSimilar = color;
            }
        }
        return mostSimilar;
    }

    /**
     * Blend an overlay with a base color in a fixed ratio
     *
     * @param base Base Color on which the Overlay is applied (present in major proportion)
     * @param overlay Overlay Color which is applied (present in min proportion)
     * @param ratio Ratio of mixing
     * @return Blended color in ratio
     */
    public Color blendColors ( Color base, Color overlay, float ratio ) {
        float inverseRatio = 1.0f - ratio;
        float red = overlay.getRed( ) * ratio + base.getRed( ) * inverseRatio;
        float green = overlay.getGreen( ) * ratio + base.getGreen( ) * inverseRatio;
        float blue = overlay.getBlue( ) * ratio + base.getBlue( ) * inverseRatio;
        return new Color( (int)red, (int)green, (int)blue );
    }
}

