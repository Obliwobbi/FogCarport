package app.services;

import app.entities.Carport;

public class CarportTopViewSvg
{
    private Carport carport;
    private SvgService svgService;
    private final CalculatorService calculatorService;

    private final String STYLE = "stroke-width:1px; stroke: black; fill: white;";
    private final String DASHED_LINE = "stroke-width:1px; stroke: black; stroke-dasharray:5 5;";
    private final String ARROW = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";


    double TOP_PLATE_WIDTH = 4.5;
    double POST_WIDTH = 10;
    double TOP_PLATE_OFFSET = 50;
    double POST_OFFSET_Y_TOP = TOP_PLATE_OFFSET-2.5;
    double POST_OFFSET_Y_BOTTOM = TOP_PLATE_OFFSET + 2.5;
    double POST_OFFSET_X_SMALL = 40;
    double POST_OFFSET_X_LARGE = 100;
    double POST_OFFSET_EDGE_BUFFER = 200;
    int MAX_LENGTH_CARPORT_FOR_POST_SPACING = 390;

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

        //TODO if shed, need to align plates with shed width if over certain size
        if (carport.isWithShed() && carport.getShedWidth() < carport.getWidth() / 2)
        {
            double firstTopPlate = (width - carport.getShedWidth()) / 2;
            double secondTopPlate = (width + firstTopPlate);

            svgService.addRectangle(0, 25, TOP_PLATE_WIDTH, length, STYLE);
            svgService.addRectangle(0, secondTopPlate, TOP_PLATE_WIDTH, length, STYLE);
        }

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
        double width = carport.getWidth();
        double length = carport.getLength();
        double posts = (double) calculatorService.calculatePosts(carport) / 2;

        // Calculate spacing: posts - 1 gives number of gaps
        double spaceBetween = (length - POST_OFFSET_EDGE_BUFFER) / (posts - 1);


        if (carport.getLength() <= MAX_LENGTH_CARPORT_FOR_POST_SPACING)
        {
            svgService.addRectangle(POST_OFFSET_X_SMALL, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE);
            svgService.addRectangle(POST_OFFSET_X_SMALL, width - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE);
            svgService.addRectangle(length - POST_OFFSET_X_SMALL, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE);
            svgService.addRectangle(length - POST_OFFSET_X_SMALL, width - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE);
        }
        if (carport.getLength() > MAX_LENGTH_CARPORT_FOR_POST_SPACING)
            for (int i = 0; i < posts; i++)
            {
                double x = POST_OFFSET_X_LARGE + (i * spaceBetween);
                svgService.addRectangle(x, POST_OFFSET_Y_TOP, POST_WIDTH, POST_WIDTH, STYLE);
                svgService.addRectangle(x, width - POST_OFFSET_Y_BOTTOM, POST_WIDTH, POST_WIDTH, STYLE);
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