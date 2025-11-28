package app.services;

public class CarportTopViewSvg
{
    private double width;
    private double height;
    private SvgService svgService;

    private final String STYLE = "stroke-width:5px; stroke: black; fill: white;";
    private final String DASHED_LINE = "stroke-width:5px; stroke: black; stroke-dasharray:5 5;";
    private final String ARROW = "marker-start: url(#beginArrow); marker-end: url(#endArrow);";


    public CarportTopViewSvg(double width, double height)
    {
        this.width = width;
        this.height = height;
        this.svgService = new SvgServiceImpl(0, 0, "0 0 855 690", "100%", "auto");
        addTopPlate();
        addCeilingJoist();
        addPosts();

    }

    private void addTopPlate()
    {
        svgService.addRectangle(0, 4, 780, 600, STYLE);

    }

    private void addCeilingJoist()
    {
//        for(int i = 0; i < 775.5; i + )
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