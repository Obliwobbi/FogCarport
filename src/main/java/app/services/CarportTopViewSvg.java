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


    double TOP_PLATE_WIDTH = 4.5;
    double POST_WIDTH = 10;
    double TOP_PLATE_OFFSET = 50;
    double POST_OFFSET_Y_TOP = TOP_PLATE_OFFSET - 2.5;
    double POST_OFFSET_Y_BOTTOM = TOP_PLATE_OFFSET + 2.5;
    double POST_OFFSET_X_SMALL = 40;
    double POST_OFFSET_X_LARGE = 100; // first post can start 1 meter into the carport
    double POST_OFFSET_EDGE_BUFFER = 200;
    double POST_OFFSET_X_WITH_SHED = 30;
    int MAX_LENGTH_CARPORT_FOR_POST_SPACING = 390;
    private final double MAX_LENGTH_BLOCKING = 270;

    public CarportTopViewSvg(Carport carport, CalculatorService calculatorService, SvgService svgService)
    {
        this.carport = carport;
        this.calculatorService = calculatorService;
        this.svgService = svgService;

        addFasciaBoard();
        addTopPlate();
        addCeilingJoist();
        addPosts();
        addPerforatedStrips();

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
        int joists = calculatorService.calculateCeilingJoist(carport)
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

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

        double maxOverhang = 120; //your able to move top plate further in if you have slightly larger overhang
        boolean isFullWidth = shedWidth >= (carportWidth - maxOverhang);

        double partialShedLowerCornerPostY = shedWidth + POST_OFFSET_Y_TOP;


        if (carport.isWithShed())
        {
            double shedOuterCornerPostXPosition = carportLength - POST_OFFSET_X_WITH_SHED;
            double shedInnerCornerPostXPosition = carportLength - POST_OFFSET_X_WITH_SHED - shedLength;

            svgService.addRectangle(POST_OFFSET_X_LARGE, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // FRONT UPPER LEFT

            if (isFullWidth)
            {
                svgService.addRectangle(POST_OFFSET_X_LARGE, carportWidth - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE); //FRONT LOWER LEFT
            }

            //Always build a shed from upper right corner
            svgService.addRectangle(shedInnerCornerPostXPosition, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER SHED LEFT CORNER
            svgService.addRectangle(shedOuterCornerPostXPosition, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER SHED RIGHT CORNER

            double loverYPosition = (isFullWidth) ? carportWidth - POST_OFFSET_Y_BOTTOM : shedWidth + POST_OFFSET_Y_TOP;

            svgService.addRectangle(shedInnerCornerPostXPosition, loverYPosition, POST_WIDTH, POST_WIDTH, STYLE); // LOWER SHED LEFT CORNER
            svgService.addRectangle(shedOuterCornerPostXPosition, loverYPosition, POST_WIDTH, POST_WIDTH, STYLE); // LOWER SHED RIGHT CORNER


            if (shedWidth > MAX_LENGTH_BLOCKING)
            {
                double middlePostOfShed = (isFullWidth) ? (POST_OFFSET_Y_TOP + (loverYPosition)) / 2 : (POST_OFFSET_Y_TOP + shedWidth + POST_OFFSET_Y_BOTTOM) / 2;

                svgService.addRectangle(shedOuterCornerPostXPosition, middlePostOfShed, POST_WIDTH, POST_WIDTH, STYLE); // MIDDLE SHED RIGHT
                svgService.addRectangle(shedInnerCornerPostXPosition, middlePostOfShed, POST_WIDTH, POST_WIDTH, STYLE); // MIDDLE SHED LEFT
            }

            if (shedLength > MAX_LENGTH_BLOCKING)
            {
                double spaceBetweenShedCornerPostUnderTopPlate = shedOuterCornerPostXPosition - shedInnerCornerPostXPosition;
                double postBetweenShedPostsOnLengthX = shedOuterCornerPostXPosition - (spaceBetweenShedCornerPostUnderTopPlate / 2);

                svgService.addRectangle(postBetweenShedPostsOnLengthX, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE);
                svgService.addRectangle(postBetweenShedPostsOnLengthX, loverYPosition, POST_WIDTH, POST_WIDTH, STYLE);
            }

            double spaceBetweenShedInnerPostAndFirstCarportPost = shedInnerCornerPostXPosition - POST_OFFSET_X_LARGE;
            if (spaceBetweenShedInnerPostAndFirstCarportPost > MAX_LENGTH_BLOCKING)
            {
                double middlePostUnderTopPlatesX = POST_OFFSET_X_LARGE + (spaceBetweenShedInnerPostAndFirstCarportPost / 2);
                svgService.addRectangle(middlePostUnderTopPlatesX, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE);

                if (isFullWidth)
                {
                    svgService.addRectangle(middlePostUnderTopPlatesX, carportWidth - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE);
                }
            }

        }
        else
        {
            if (carport.getLength() <= MAX_LENGTH_CARPORT_FOR_POST_SPACING)
            {
                svgService.addRectangle(POST_OFFSET_X_SMALL, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER LEFT
                svgService.addRectangle(carportLength - POST_OFFSET_X_SMALL, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE); // UPPER RIGHT
                svgService.addRectangle(POST_OFFSET_X_SMALL, carportWidth - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE); // LOWER LEFT
                svgService.addRectangle(carportLength - POST_OFFSET_X_SMALL, carportWidth - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE); // LOWER RIGHT
            }
            if (carport.getLength() > MAX_LENGTH_CARPORT_FOR_POST_SPACING)
            {
                double posts = (double) calculatorService.calculatePosts(carport) / 2;
                double spaceBetween = (carportLength - POST_OFFSET_EDGE_BUFFER) / (posts - 1); // Calculate spacing: posts - 1 gives number of gaps

                for (int i = 0; i < posts; i++)
                {
                    double x = POST_OFFSET_X_LARGE + (i * spaceBetween);
                    svgService.addRectangle(x, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE);
                    svgService.addRectangle(x, carportWidth - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE);
                }
            }
        }
    }


    private void addPerforatedStrips()
    {
        int joists = calculatorService.calculateCeilingJoist(carport)
                .values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        double startX = (carport.getLength() / (joists - 1)); //joist-1 is amount of gaps needed for equal spacing
        double endJoist = Math.min(10, joists - 2);

        double endX = endJoist * startX;

        svgService.addLine(startX, TOP_PLATE_OFFSET, endX, carport.getWidth() - TOP_PLATE_OFFSET, DASHED_LINE); //uses TOP-PLATE_OFFSET due to needing to be connected to the ceilingjoist atop that;
        svgService.addLine(startX, carport.getWidth() - TOP_PLATE_OFFSET, endX, TOP_PLATE_OFFSET, DASHED_LINE);
    }

    @Override
    public String toString()
    {
        return svgService.toString();
    }

}