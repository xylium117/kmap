package src;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ColorPalette extends Colorizer {
    private final List<Color> palette;
    private int index = 0;

    public ColorPalette(int n) {
        int count = Math.max(16, (1 << n));
        palette = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            palette.add(super.getNextColor());
        }
    }

    @Override
    public Color getNextColor() {
        if (index < palette.size()) {
            return palette.get(index++);
        }
        Color newColor = super.getNextColor();
        if (newColor != null) {
            palette.add(newColor);
            index++;
            return newColor;
        }
        return palette.get((index++) % palette.size());
    }

    public void reset() {
        index = 0;
    }
}
