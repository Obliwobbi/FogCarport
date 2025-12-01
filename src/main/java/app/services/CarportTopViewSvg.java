package app.services;

import app.entities.Carport;

public class CarportTopViewSvg
{
    private double width;
    private double height;
    private Carport carport;
    private SvgService svgService;
    private final CalculatorService calculatorService;

    private final String STYLE = "stroke-width:5px; stroke: black; fill: white;";
    private final String DASHED_LINE = "stroke-width:5px; stroke: black; stroke-dasharray:5 5;";
    private final String ARROW = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";



    public CarportTopViewSvg(Carport carport, CalculatorService calculatorService, SvgService svgService )
    {
        this.carport = carport;
        this.calculatorService = calculatorService;
        this.svgService = svgService;

        addFasciaBoard();
        addTopPlate();
        addCeilingJoist();
        addPosts();

    }

    private void addFasciaBoard()
    {
        svgService.addRectangle(0,0, carport.getWidth(), carport.getLength(), STYLE);
    }

    private void addTopPlate()
    {
//        svgService.addRectangle(0, 4, 780, 600, STYLE);

    }

    private void addCeilingJoist()
    {
        calculatorService.calculateCeilingJoist(carport);
//            for(int i = 0; i < width; i + )
    }

    private void addPosts()
    {

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