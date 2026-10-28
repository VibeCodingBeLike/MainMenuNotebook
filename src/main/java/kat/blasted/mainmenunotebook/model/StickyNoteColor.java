package kat.blasted.mainmenunotebook.model;

public enum StickyNoteColor {
    YELLOW(
        "Yellow",
        0xFFFFF9A6, // Soft bright yellow
        0xFFF5E16C, // Header yellow
        0xFFE0C950, // Border yellow
        0xFF212121  // Charcoal text
    ),
    PINK(
        "Pink",
        0xFFFFD1D5, // Pastel pink
        0xFFFFA8B0, // Header pink
        0xFFF08C96, // Border pink
        0xFF212121
    ),
    MINT(
        "Mint",
        0xFFD2F5D7, // Pastel mint green
        0xFFA6E8AE, // Header mint
        0xFF8FD697, // Border mint
        0xFF212121
    ),
    BLUE(
        "Blue",
        0xFFCDEEFE, // Pastel sky blue
        0xFFA3DBF8, // Header blue
        0xFF86C6E6, // Border blue
        0xFF212121
    ),
    LAVENDER(
        "Lavender",
        0xFFEADBFC, // Pastel lavender
        0xFFD3B7F5, // Header lavender
        0xFFBBA0E0, // Border lavender
        0xFF212121
    ),
    PEACH(
        "Peach",
        0xFFFFE3C7, // Pastel peach
        0xFFFFC999, // Header peach
        0xFFF2B079, // Border peach
        0xFF212121
    );

    private final String name;
    private final int background;
    private final int header;
    private final int border;
    private final int textColor;

    StickyNoteColor(String name, int background, int header, int border, int textColor) {
        this.name = name;
        this.background = background;
        this.header = header;
        this.border = border;
        this.textColor = textColor;
    }

    public String getName() {
        return name;
    }

    public int getBackground() {
        return background;
    }

    public int getHeader() {
        return header;
    }

    public int getBorder() {
        return border;
    }

    public int getTextColor() {
        return textColor;
    }

    public static StickyNoteColor fromIndex(int index) {
        StickyNoteColor[] values = values();
        int safeIndex = Math.floorMod(index, values.length);
        return values[safeIndex];
    }
}
