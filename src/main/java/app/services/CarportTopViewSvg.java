package app.services;

import app.entities.Carport;

public class CarportTopViewSvg
{
    private Carport carport;
    private SvgService svgService;
    private final CalculatorService calculatorService;

    private final String STYLE = "stroke-width:1px; stroke: black; fill: transparent;";
    private final String DASHED_LINE = "stroke-width:1px; stroke: black; stroke-dasharray:5 5;";
    private final String ARROW = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";
    private final String SHED_STYLE_ONE = "stroke-width:2px; stroke: black; stroke-dasharray:10 10;";
    private final String SHED_STYLE_TWO = "stroke-width:2px; stroke: black; stroke-dasharray:10 10; stroke-dashoffset:10;";

    private double TOP_PLATE_WIDTH = 4.5;
    private double POST_WIDTH = 10;
    private double POST_OFFSET_X_SMALL = 40;
    private double POST_OFFSET_X_LARGE = 100; // first post can start 1 meter into the carport
    private double POST_OFFSET_EDGE_BUFFER = 200;
    private double POST_OFFSET_X_WITH_SHED = 15;
    private int MAX_LENGTH_CARPORT_FOR_POST_SPACING = 390;
    private double MAX_LENGTH_BLOCKING = 270;
    private double MAX_LENGTH_BETWEEN_POST = 310;

    private double TOP_PLATE_OFFSET;
    private double MAX_OVERHANG;
    private double POST_OFFSET_Y_TOP;
    private double POST_OFFSET_Y_BOTTOM;

    public CarportTopViewSvg(Carport carport, CalculatorService calculatorService, SvgService svgService)
    {
        this.carport = carport;
        this.calculatorService = calculatorService;
        this.svgService = svgService;

        setDynamicMeasurements();

        addFasciaBoard();
        addTopPlate();
        addCeilingJoist();
        addPosts();
        addPerforatedStrips();
        addShedOutline();
    }

    private void setDynamicMeasurements()
    {
        if (carport.getWidth() >= 330)
        {
            TOP_PLATE_OFFSET = 35;
            MAX_OVERHANG = 70;
        }
        else
        {
            TOP_PLATE_OFFSET = 15;
            MAX_OVERHANG = 30;
        }
        POST_OFFSET_Y_TOP = TOP_PLATE_OFFSET - 2.5;
        POST_OFFSET_Y_BOTTOM = TOP_PLATE_OFFSET + 2.5;
    }

    private void addFasciaBoard()
    {
        svgService.addRectangle(0, 0, carport.getWidth(), carport.getLength(), STYLE);
    }

    private void addTopPlate()
    {
        double width = carport.getWidth();
        double length = carport.getLength();

        svgService.addRectangle(0, TOP_PLATE_OFFSET, TOP_PLATE_WIDTH, length, STYLE);
        svgService.addRectangle(0, width - TOP_PLATE_OFFSET, TOP_PLATE_WIDTH, length, STYLE);
    }

    private void addCeilingJoist()
    {
        int joists = calculatorService.sumHashMapValues(calculatorService.calculateCeilingJoist(carport));

        double spaceBetween = (carport.getLength() / (joists - 1)); //joist-1 is amount of gaps needed for equal spacing
        for (double i = 0; i < joists; i++)
        {
            double x = i * spaceBetween;
            svgService.addRectangle(x, 0, carport.getWidth(), TOP_PLATE_WIDTH, STYLE);
        }
    }

    private void addPosts()
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();
        double shedWidth = carport.getShedWidth();
        double shedLength = carport.getShedLength();

        double maxOverHangLength = 150; //able to most post supporting TopPlate to align with shed Corner
        boolean isFullWidth = shedWidth >= (carportWidth - MAX_OVERHANG);
        boolean isFullLength = shedLength >= (carportLength - maxOverHangLength);

        double postAlignedWithLowerYTopPlate = carportWidth - POST_OFFSET_Y_BOTTOM;
        double shedOuterCornerPostXPosition = carportLength - POST_OFFSET_X_WITH_SHED;
        double shedInnerCornerPostXPosition = carportLength - POST_OFFSET_X_WITH_SHED - shedLength;

        if (carport.isWithShed())
        {
            if (!isFullLength)
            {
                double frontPost = (carportLength > (MAX_LENGTH_BETWEEN_POST + POST_OFFSET_X_WITH_SHED)) ? POST_OFFSET_X_LARGE : POST_OFFSET_X_SMALL;
                svgService.addRectangle(frontPost, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // FRONT UPPER LEFT
            }

            if (isFullWidth)
            {
                svgService.addRectangle(POST_OFFSET_X_LARGE, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); //FRONT LOWER LEFT
            }

            //Always build a shed from upper right corner
            svgService.addRectangle(shedInnerCornerPostXPosition, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER SHED LEFT CORNER
            svgService.addRectangle(shedOuterCornerPostXPosition, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER SHED RIGHT CORNER

            double lowerYPosition = (isFullWidth) ? postAlignedWithLowerYTopPlate : shedWidth + POST_OFFSET_Y_TOP;

            svgService.addRectangle(shedInnerCornerPostXPosition, lowerYPosition, POST_WIDTH, POST_WIDTH, STYLE); // LOWER SHED LEFT CORNER
            svgService.addRectangle(shedOuterCornerPostXPosition, lowerYPosition, POST_WIDTH, POST_WIDTH, STYLE); // LOWER SHED RIGHT CORNER

            if (shedWidth > MAX_LENGTH_BLOCKING) //POST FOR BETWEEN SHED CORNER ON WIDTH/Y IF NEEDED
            {
                double middlePostOfShed = (isFullWidth) ? (POST_OFFSET_Y_TOP + (lowerYPosition)) / 2 : (POST_OFFSET_Y_TOP + shedWidth + POST_OFFSET_Y_BOTTOM) / 2;

                svgService.addRectangle(shedOuterCornerPostXPosition, middlePostOfShed, POST_WIDTH, POST_WIDTH, STYLE); // MIDDLE SHED RIGHT
                svgService.addRectangle(shedInnerCornerPostXPosition, middlePostOfShed, POST_WIDTH, POST_WIDTH, STYLE); // MIDDLE SHED LEFT
            }

            if (shedLength > MAX_LENGTH_BLOCKING) //POST FOR BETWEEN SHED CORNER ON WIDTH/X IF NEEDED
            {
                int numberOfSegments = (int) Math.ceil(shedLength / MAX_LENGTH_BLOCKING);

                double spaceBetweenPosts = shedLength / numberOfSegments;

                // Add intermediate posts (numberOfSegments - 1 posts between the corners)
                for (int i = 1; i < numberOfSegments; i++)
                {
                    double postX = shedInnerCornerPostXPosition + (i * spaceBetweenPosts);

                    svgService.addRectangle(postX, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE);
                    svgService.addRectangle(postX, lowerYPosition, POST_WIDTH, POST_WIDTH, STYLE);
                }
            }

            double spaceBetweenShedInnerPostAndFirstCarportPost = shedInnerCornerPostXPosition - POST_OFFSET_X_LARGE;
            if (spaceBetweenShedInnerPostAndFirstCarportPost > MAX_LENGTH_BLOCKING)
            {
                double middlePostUnderTopPlatesX = POST_OFFSET_X_LARGE + (spaceBetweenShedInnerPostAndFirstCarportPost / 2);
                svgService.addRectangle(middlePostUnderTopPlatesX, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER POST BETWEEN FRONT AND SHED CORNER

                if (isFullWidth)
                {
                    svgService.addRectangle(middlePostUnderTopPlatesX, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER POST BETWEEN FRONT AND SHED CORNER
                }
            }

            if (!isFullWidth)
            {
                int stnCarportLengthMedium = 350;
                int stnCarportLengthMediumLarge = 450;
                int stnCarportLengthLarge = 510;

                if (carportLength > stnCarportLengthLarge)
                {
                    int numberOfSegments = Math.max((int) Math.ceil(carportLength / MAX_LENGTH_BETWEEN_POST), 3);
                    double spaceBetweenPosts = (carportLength - POST_OFFSET_EDGE_BUFFER) / (2);

                    for (int i = 0; i < numberOfSegments; i++)
                    {
                        double postX = POST_OFFSET_X_LARGE + (i * spaceBetweenPosts);
                        svgService.addRectangle(postX, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // ALL POSTS UNDER LOWER TOP PLATE SHED !FULLWIDTH
                    }
                }
                else if (carportLength >= stnCarportLengthMediumLarge)
                {
                    svgService.addRectangle(POST_OFFSET_X_LARGE, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER LEFT
                    svgService.addRectangle(carportLength - POST_OFFSET_X_LARGE, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER RIGHT
                }
                else if (carportLength > stnCarportLengthMedium)
                {
                    svgService.addRectangle(POST_OFFSET_X_LARGE, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER LEFT
                    svgService.addRectangle(carportLength - POST_OFFSET_X_WITH_SHED, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER RIGHT
                }
                else
                {
                    svgService.addRectangle(POST_OFFSET_X_SMALL, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER LEFT
                    svgService.addRectangle(carportLength - POST_OFFSET_X_WITH_SHED, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER RIGHT
                }
            }
        }
        else // NO SHED
        {
            if (carport.getLength() <= MAX_LENGTH_CARPORT_FOR_POST_SPACING)
            {
                svgService.addRectangle(POST_OFFSET_X_SMALL, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER LEFT
                svgService.addRectangle(carportLength - POST_OFFSET_X_SMALL, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER RIGHT
                svgService.addRectangle(POST_OFFSET_X_SMALL, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER LEFT
                svgService.addRectangle(carportLength - POST_OFFSET_X_SMALL, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER RIGHT
            }
            if (carport.getLength() > MAX_LENGTH_CARPORT_FOR_POST_SPACING)
            {
                double posts = (double) calculatorService.calculatePosts(carport) / 2;
                double spaceBetween = (carportLength - POST_OFFSET_EDGE_BUFFER) / (posts - 1); // Calculate spacing: posts - 1 gives number of gaps

                for (int i = 0; i < posts; i++)
                {
                    double x = POST_OFFSET_X_LARGE + (i * spaceBetween);
                    svgService.addRectangle(x, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER POSTS UNDER TOP PLATE
                    svgService.addRectangle(x, postAlignedWithLowerYTopPlate, POST_WIDTH, POST_WIDTH, STYLE); // LOWER POSTS UNDER TOP PLATE
                }
            }
        }
    }

    private void addPerforatedStrips()
    {
        int joists = calculatorService.sumHashMapValues(calculatorService.calculateCeilingJoist(carport));

        double startX = (carport.getLength() / (joists - 1)); //joist-1 is amount of gaps needed for equal spacing
        double endJoist = Math.min(10, joists - 2);

        double endX = endJoist * startX;

        svgService.addLine(startX, TOP_PLATE_OFFSET, endX, carport.getWidth() - TOP_PLATE_OFFSET, DASHED_LINE); //uses TOP-PLATE_OFFSET due to needing to be connected to the ceilingjoist atop that;
        svgService.addLine(startX, carport.getWidth() - TOP_PLATE_OFFSET, endX, TOP_PLATE_OFFSET, DASHED_LINE);
    }

    private void addShedOutline()
    {
        if (!carport.isWithShed())
        {
            return;
        }

        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();
        double shedWidth = carport.getShedWidth();
        double shedLength = carport.getShedLength();

        boolean isFullWidth = shedWidth >= (carportWidth - MAX_OVERHANG);

        // Position lines on OUTER edge of posts
        double shedOuterCornerPostX = carportLength - POST_OFFSET_X_WITH_SHED + POST_WIDTH;
        double shedInnerCornerPostX = carportLength - POST_OFFSET_X_WITH_SHED - shedLength;
        double upperY = POST_OFFSET_Y_TOP;
        double lowerY = (isFullWidth) ? (carportWidth - POST_OFFSET_Y_BOTTOM + POST_WIDTH) : (shedWidth + POST_OFFSET_Y_TOP + POST_WIDTH);

        double lineOffset = 2; // Distance between the two dashed lines

        // Top line (double-dashed)
        svgService.addLine(shedInnerCornerPostX, upperY, shedOuterCornerPostX, upperY, SHED_STYLE_ONE);
        svgService.addLine(shedInnerCornerPostX, upperY - lineOffset, shedOuterCornerPostX, upperY - lineOffset, SHED_STYLE_TWO);

        // Bottom line (double-dashed)
        svgService.addLine(shedInnerCornerPostX, lowerY, shedOuterCornerPostX, lowerY, SHED_STYLE_ONE);
        svgService.addLine(shedInnerCornerPostX, lowerY + lineOffset, shedOuterCornerPostX, lowerY + lineOffset, SHED_STYLE_TWO);

        // Left line (double-dashed)
        svgService.addLine(shedInnerCornerPostX, upperY, shedInnerCornerPostX, lowerY, SHED_STYLE_ONE);
        svgService.addLine(shedInnerCornerPostX - lineOffset, upperY, shedInnerCornerPostX - lineOffset, lowerY, SHED_STYLE_TWO);

        // Right line (double-dashed)
        svgService.addLine(shedOuterCornerPostX, upperY, shedOuterCornerPostX, lowerY, SHED_STYLE_ONE);
        svgService.addLine(shedOuterCornerPostX + lineOffset, upperY, shedOuterCornerPostX + lineOffset, lowerY, SHED_STYLE_TWO);
    }

    public String createMeasuredCarportSvg()
    {
        double carportWidth = carport.getWidth();
        double carportLength = carport.getLength();

        double leftMargin = 50;
        double topMargin = 50;
        double bottomMargin = 40;
        double rightMargin = 50;

        double totalWidth = leftMargin + carportLength + rightMargin;
        double totalHeight = topMargin + carportWidth + bottomMargin;
        double minOverhang = 30; //15 each side

        SvgServiceImpl outerSvg = new SvgServiceImpl(0, 0, String.format("0 0 %.1f %.1f", totalWidth, totalHeight), "100%", "auto");
        SvgServiceImpl innerSvg = new SvgServiceImpl((int) leftMargin, (int) topMargin, String.format("0 0 %.1f %.1f", carportLength + 5, carportWidth), String.format("%.1f", carportLength), String.format("%.1f", carportWidth));

        innerSvg.addSvg((SvgServiceImpl) svgService);
        outerSvg.addSvg(innerSvg);

        // Width measurement (left side) with arrows
        outerSvg.addArrow(20, 52.5, 20, 32.5 + carportWidth, STYLE + ARROW);
        outerSvg.addText(12, (int) (topMargin + carportWidth / 2), 270, String.format("%.0f cm", carportWidth));

        // Length measurement (bottom) with arrows
        outerSvg.addArrow(leftMargin, topMargin + carportWidth + 10, leftMargin + carportLength, topMargin + carportWidth + 10, STYLE + ARROW);
        outerSvg.addText((int) (leftMargin + carportLength / 2), (int) (topMargin + carportWidth + 35), 0, String.format("%.0f cm", carportLength));

        // Post spacing measurement on width (left side)
        double postSpacingOnWidth = (carportWidth < 340) ? carportWidth - minOverhang : carportWidth - (2 * TOP_PLATE_OFFSET);
        outerSvg.addArrow(40, topMargin + TOP_PLATE_OFFSET, 40, 40 + carportWidth - TOP_PLATE_OFFSET, STYLE + ARROW);
        outerSvg.addText(35, (int) (topMargin + carportWidth / 2), 270, String.format("%.0f cm", postSpacingOnWidth));


        // Ceiling joist spacing measurements (top)
        int joists = calculatorService.sumHashMapValues(calculatorService.calculateCeilingJoist(carport));
        double joistSpacing = carportLength / (joists - 1);

        for (int i = 0; i < joists - 1; i++)
        {
            double x1 = leftMargin + (i * joistSpacing);
            double x2 = leftMargin + ((i + 1) * joistSpacing);
            double y = topMargin - 8;

            outerSvg.addArrow(x1, y, x2, y, STYLE + ARROW);
            outerSvg.addText((int) ((x1 + x2) / 2), (int) (y - 10), 0, String.format("%.02f", joistSpacing / 100));
        }

        return outerSvg.toString();
    }

    @Override
    public String toString()
    {
        return svgService.toString();
    }
}