package app.services;

import app.entities.Carport;

public class CarportTopViewSvg
{
    private double width;
    private double height;
    private Carport carport;
    private SvgService svgService;
    private final CalculatorService calculatorService;

    private final String STYLE = "stroke-width:1px; stroke: black; fill: white;";
    private final String DASHED_LINE = "stroke-width:1px; stroke: black; stroke-dasharray:5 5;";
    private final String ARROW = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";


    double TOP_PLATE_WIDTH = 4.5;
    double POST_WIDTH = 10;


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
        double CARPORT_WIDTH = carport.getWidth();
        double CARPORT_LENGTH = carport.getLength();

        if (carport.isWithShed() && carport.getShedWidth() < carport.getWidth() / 2)
        {
            int firstTopPlate = (int) (CARPORT_WIDTH - carport.getShedWidth()) / 2;
            int secondTopPlate = (int) (CARPORT_WIDTH + firstTopPlate);

            svgService.addRectangle(0, 25, TOP_PLATE_WIDTH, CARPORT_LENGTH, STYLE);
            svgService.addRectangle(0, secondTopPlate, TOP_PLATE_WIDTH, CARPORT_LENGTH, STYLE);
        }

        svgService.addRectangle(0, 15, TOP_PLATE_WIDTH, CARPORT_LENGTH, STYLE);
        svgService.addRectangle(0, (int) CARPORT_WIDTH - 15, TOP_PLATE_WIDTH, CARPORT_LENGTH, STYLE);
    }

    private void addCeilingJoist()
    {
        int joists = calculatorService.calculateCeilingJoist(carport);
        double spaceBetween = (carport.getLength() / (joists - 1)); //joist-1 is amount of gaps needed for equal spacing
        for (double i = 0; i < joists; i++)
        {
            double x = i * spaceBetween;
            svgService.addRectangle(x, 0, carport.getWidth(), TOP_PLATE_WIDTH, STYLE);
        }
    }

    private void addPosts()
    {
        double CARPORT_WIDTH = carport.getWidth();
        double CARPORT_LENGTH = carport.getLength();
        double posts = (double) calculatorService.calculatePosts(carport) / 2;

        // Calculate spacing: posts - 1 gives number of gaps
        double spaceBetween = (CARPORT_LENGTH - 200) / (posts - 1);


        if (carport.getLength() <= 390)
        {
            svgService.addRectangle(40, 12.5, POST_WIDTH, POST_WIDTH, STYLE);
            svgService.addRectangle(40, CARPORT_WIDTH - 17.5, POST_WIDTH, POST_WIDTH, STYLE);
            svgService.addRectangle(carport.getLength() - 40, 12.5, POST_WIDTH, POST_WIDTH, STYLE);
            svgService.addRectangle(carport.getLength() - 40, CARPORT_WIDTH - 17.5, POST_WIDTH, POST_WIDTH, STYLE);
        }
        if (carport.getLength() > 390)
            for (int i = 0; i < posts; i++)
            {
                double x = 100 + (i * spaceBetween); // Start at 100cm offset
                svgService.addRectangle(x, 12.5, POST_WIDTH, POST_WIDTH, STYLE);
                svgService.addRectangle(x, CARPORT_WIDTH - 17.5, POST_WIDTH, POST_WIDTH, STYLE);
            }

    }

    private void addPerforatedStrips()
    {
        int joists = calculatorService.calculateCeilingJoist(carport);
        double startX = (carport.getLength() / (joists - 1)); //joist-1 is amount of gaps needed for equal spacing
        double endJoist = Math.min(10, joists - 2);

        double endX = endJoist * startX;

        svgService.addLine(startX, 15, endX, carport.getWidth() - 15, DASHED_LINE);
        svgService.addLine(startX, carport.getWidth() - 15, endX, 15, DASHED_LINE);
    }

    @Override
    public String toString()
    {
        return svgService.toString();
    }
}


//        carportSvg.addRectangle(0, 0, 600, 780, style);
//        carportSvg.addLine(50,50,500,700,dashedLine);
//        carportSvg.addArrow(20,20,600,700,style + arrow);