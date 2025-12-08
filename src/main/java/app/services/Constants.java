package app.services;

public final class Constants
{
    private Constants()
    {
    }

    // Post and spacing constants
    public static final double POST_SIZE = 10.0;
    public static final double POST_OFFSET_X_SMALL = 40.0;
    public static final double POST_OFFSET_X_LARGE = 100.0;
    public static final double POST_OFFSET_X_WITH_SHED = 15.0;
    public static final int MAX_LENGTH_BLOCKING = 270;
    public static final int MAX_LENGTH_BETWEEN_POST = 310;

    // Top plate constants
    public static final double TOP_PLATE_WIDTH = 4.5;
    public static final double TOP_PLATE_SHORT = 480.0;
    public static final double TOP_PLATE_LONG = 600.0;

    // Ceiling joist constants
    public static final double CEILING_JOIST_SHORT = 480.0;
    public static final double CEILING_JOIST_LONG = 600.0;
    public static final int MAX_LENGTH_BTWN_CEILING_JOIST = 60;

    // Overhang and offset constants
    public static final int MIN_OVERHANG = 30;
    public static final int BACKSIDE_OVERHANG = 5;

    // Ceiling joist width
    public static final double CEILING_JOIST_WIDTH = 4.5;

    // Fascia board constants
    public static final double FASCIA_BOARD_SHORT = 360.0;
    public static final double FASCIA_BOARD_LONG = 540.0;

    // Blocking constants
    public static final double BLOCKING_SHORT = 240.0;
    public static final double BLOCKING_LONG = 270.0;

    // Roof plate constants
    public static final double ROOF_PLATE_SHORT = 360.0;
    public static final double ROOF_PLATE_LONG = 600.0;
    public static final int ROOF_PLATE_SCREWS_M2 = 12;

    // Post offset constants
    public static final double POST_OFFSET_LONG = 100.0;
    public static final double POST_OFFSET_EDGE_BUFFER = 200.0;

    // Carport spacing constants
    public static final double MAX_LENGTH_CARPORT_NO_SHED_FEWER_SUPPORTS = 510.0;
    public static final int MAX_LENGTH_CARPORT_FOR_POST_SPACING = 390;

    // SVG Style constants
    public static final String STYLE = "stroke-width:1px; stroke: black; fill: transparent;";
    public static final String DASHED_LINE = "stroke-width:1px; stroke: black; stroke-dasharray:5 5;";
    public static final String ARROW = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";
    public static final String SHED_STYLE_ONE = "stroke-width:2px; stroke: black; stroke-dasharray:10 10;";
    public static final String SHED_STYLE_TWO = "stroke-width:2px; stroke: black; stroke-dasharray:10 10; stroke-dashoffset:10;";
}

