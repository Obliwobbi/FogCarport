package app.util;

public final class Constants
{
    private Constants()
    {
    }
    
    // Measurement constants
    public static final double MIN_CARPORT_WIDTH = 240;
    public static final double MAX_CARPORT_WIDTH = 600;
    public static final double MIN_CARPORT_LENGTH = 240;
    public static final double MAX_CARPORT_LENGTH = 780;
    public static final double MIN_SHED_LENGTH = 120;
    public static final double MAX_SHED_LENGTH = 690;
    public static final double MIN_SHED_WIDTH = 120;
    public static final double MAX_SHED_WIDTH = 510;
    public static final double CARPORT_MEASUREMENT_INTERVAL = 30;

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

    // Components needed and sizing constants
    public static final int SHORT_SCREWS_NEEDED_PER_BOARD = 3;
    public static final int PACK_SIZE_LONG_SCREW = 400;
    public static final int PACK_SIZE_ROOF_PLATE_SCREW = 200;
    public static final int PACK_SIZE_SHORT_SCREW = 300;
    public static final int PACK_SIZE_FITTING_SCREW = 250;
    public static final int LONG_SCREWS_NEEDED_PER_BOARD = 6;
    public static final int SCREWS_PER_BLOCKING_FITTING = 4;
    public static final int SCREWS_PER_CEILING_JOIST_FITTING = 9;

    // SVG Style constants
    public static final String STYLE = "stroke-width:1px; stroke: black; fill: transparent;";
    public static final String DASHED_LINE = "stroke-width:1px; stroke: black; stroke-dasharray:5 5;";
    public static final String ARROW = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";
    public static final String SHED_STYLE_ONE = "stroke-width:2px; stroke: black; stroke-dasharray:10 10;";
    public static final String SHED_STYLE_TWO = "stroke-width:2px; stroke: black; stroke-dasharray:10 10; stroke-dashoffset:10;";

    // Material ID constants
    public static final int POST_MATERIAL_ID = 12;

    public static final int TOP_PLATE_SHORT_MATERIAL_ID = 9;
    public static final int TOP_PLATE_LONG_MATERIAL_ID = 8;

    public static final int BOLT_MATERIAL_ID = 22;
    public static final int WASHER_MATERIAL_ID = 23;

    public static final int CEILING_JOIST_SHORT_MATERIAL_ID = 10;
    public static final int CEILING_JOIST_LONG_MATERIAL_ID = 11;
    public static final int CEILING_JOIST_FITTING_RIGHT_MATERIAL_ID = 20;
    public static final int CEILING_JOIST_FITTING_LEFT_MATERIAL_ID = 21;

    public static final int PERFORATED_STRIP_MATERIAL_ID = 19;

    public static final int FASCIA_BOARD_SHORT_MATERIAL_ID = 3;
    public static final int SUB_FASCIA_BOARD_SHORT_MATERIAL_ID = 1;
    public static final int WEATHER_BOARD_SHORT_MATERIAL_ID = 15;

    public static final int FASCIA_BOARD_LONG_MATERIAL_ID = 4;
    public static final int SUB_FASCIA_BOARD_LONG_MATERIAL_ID = 2;
    public static final int WEATHER_BOARD_LONG_MATERIAL_ID = 14;

    public static final int ROOF_PLATE_SHORT_MATERIAL_ID = 17;
    public static final int ROOF_PLATE_LONG_MATERIAL_ID = 16;
    public static final int ROOF_PLATE_SCREW_MATERIAL_ID = 18;

    public static final int BLOCKING_SHORT_MATERIAL_ID = 7;
    public static final int BLOCKING_LONG_MATERIAL_ID = 6;

    public static final int BLOCKING_FITTING_MATERIAL_ID = 30;

    public static final int SIDE_BOARD_MATERIAL_ID = 13;

    public static final int SHED_DOOR_STRIP_MATERIAL_ID = 5;
    public static final int SHED_DOOR_GRIP_MATERIAL_ID = 28;
    public static final int SHED_DOOR_HINGE_MATERIAL_ID = 29;

    public static final int SCREW_SHORT_MATERIAL_ID = 25;
    public static final int SCREW_LONG_MATERIAL_ID = 27;
    public static final int SCREW_UNIVERSAL_MATERIAL_ID = 24;
}

